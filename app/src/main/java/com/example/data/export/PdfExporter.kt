package com.example.data.export

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.ElementType
import com.example.data.model.PdfDocumentData
import com.example.data.model.PdfElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PdfExporter {

    suspend fun exportToPdf(context: Context, doc: PdfDocumentData): File = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()

        doc.pages.forEachIndexed { index, page ->
            val pageNumber = index + 1
            val pageInfo = PdfDocument.PageInfo.Builder(
                doc.pageWidth.toInt(),
                doc.pageHeight.toInt(),
                pageNumber
            ).create()

            val pdfPage = pdfDocument.startPage(pageInfo)
            val canvas = pdfPage.canvas

            // 1. Draw page background
            canvas.drawColor(Color.WHITE)

            // 2. Draw Header
            if (doc.headerText.isNotBlank() && (!doc.headerExcludeFirst || pageNumber > 1)) {
                drawHeader(canvas, doc, doc.pageWidth)
            }

            // 3. Draw Watermark if enabled
            if (doc.watermarkEnabled && doc.watermarkText.isNotBlank()) {
                drawWatermark(canvas, doc)
            }

            // 4. Draw Elements in zIndex order
            val sortedElements = page.elements.sortedBy { it.zIndex }
            sortedElements.forEach { element ->
                drawElement(context, canvas, element)
            }

            // 5. Draw Footer
            if (doc.footerText.isNotBlank() && (!doc.footerExcludeFirst || pageNumber > 1)) {
                val formattedFooter = doc.footerText
                    .replace("{page}", pageNumber.toString())
                    .replace("{total}", doc.pages.size.toString())
                drawFooter(canvas, formattedFooter, doc.footerAlign, doc.pageWidth, doc.pageHeight)
            }

            pdfDocument.finishPage(pdfPage)
        }

        val sanitizedTitle = doc.title.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val exportFile = File(context.cacheDir, "$sanitizedTitle.pdf")
        val outputStream = FileOutputStream(exportFile)
        pdfDocument.writeTo(outputStream)
        outputStream.flush()
        outputStream.close()
        pdfDocument.close()

        exportFile
    }

    fun sharePdf(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, file.name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Export & Share PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private fun drawHeader(canvas: Canvas, doc: PdfDocumentData, pageWidth: Float) {
        val paint = Paint().apply {
            color = Color.rgb(100, 116, 139) // Slate 500
            textSize = 9f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = when (doc.headerAlign) {
                "left" -> Paint.Align.LEFT
                "right" -> Paint.Align.RIGHT
                else -> Paint.Align.CENTER
            }
        }

        val x = when (doc.headerAlign) {
            "left" -> 40f
            "right" -> pageWidth - 40f
            else -> pageWidth / 2f
        }
        canvas.drawText(doc.headerText, x, 25f, paint)

        // Subtle divider
        val linePaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 0.5f
        }
        canvas.drawLine(40f, 30f, pageWidth - 40f, 30f, linePaint)
    }

    private fun drawFooter(canvas: Canvas, footerText: String, align: String, pageWidth: Float, pageHeight: Float) {
        val paint = Paint().apply {
            color = Color.rgb(148, 163, 184) // Slate 400
            textSize = 9f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = when (align) {
                "left" -> Paint.Align.LEFT
                "right" -> Paint.Align.RIGHT
                else -> Paint.Align.CENTER
            }
        }

        val x = when (align) {
            "left" -> 40f
            "right" -> pageWidth - 40f
            else -> pageWidth / 2f
        }

        // Subtle divider
        val linePaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 0.5f
        }
        canvas.drawLine(40f, pageHeight - 30f, pageWidth - 40f, pageHeight - 30f, linePaint)
        canvas.drawText(footerText, x, pageHeight - 16f, paint)
    }

    private fun drawWatermark(canvas: Canvas, doc: PdfDocumentData) {
        val paint = Paint().apply {
            color = doc.watermarkColor.toInt()
            alpha = (doc.watermarkOpacity * 255).toInt().coerceIn(10, 255)
            textSize = 54f
            isAntiAlias = true
            isFakeBoldText = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        canvas.save()
        canvas.translate(doc.pageWidth / 2f, doc.pageHeight / 2f)
        canvas.rotate(doc.watermarkRotation)
        canvas.drawText(doc.watermarkText, 0f, 15f, paint)
        canvas.restore()
    }

    private fun drawElement(context: Context, canvas: Canvas, element: PdfElement) {
        canvas.save()
        val centerX = element.x + element.width / 2f
        val centerY = element.y + element.height / 2f
        canvas.rotate(element.rotation, centerX, centerY)

        val alphaInt = (element.opacity * 255).toInt().coerceIn(0, 255)

        when (element.type) {
            ElementType.TEXT -> {
                // Background highlight if set
                if (element.textBgColor != 0L) {
                    val bgPaint = Paint().apply {
                        color = element.textBgColor.toInt()
                        alpha = alphaInt
                        style = Paint.Style.FILL
                    }
                    canvas.drawRect(element.x, element.y, element.x + element.width, element.y + element.height, bgPaint)
                }

                val typefaceStyle = when {
                    element.isBold && element.isItalic -> Typeface.BOLD_ITALIC
                    element.isBold -> Typeface.BOLD
                    element.isItalic -> Typeface.ITALIC
                    else -> Typeface.NORMAL
                }

                val baseTypeface = when (element.fontFamily.lowercase()) {
                    "serif" -> Typeface.SERIF
                    "monospace" -> Typeface.MONOSPACE
                    "cursive" -> Typeface.create("cursive", typefaceStyle)
                    else -> Typeface.SANS_SERIF
                }

                val paint = Paint().apply {
                    color = element.textColor.toInt()
                    alpha = alphaInt
                    textSize = element.fontSize
                    typeface = Typeface.create(baseTypeface, typefaceStyle)
                    isAntiAlias = true
                    isUnderlineText = element.isUnderline
                    isStrikeThruText = element.isStrikethrough
                    letterSpacing = element.letterSpacingSp / 20f
                    textAlign = when (element.textAlign) {
                        "center" -> Paint.Align.CENTER
                        "right" -> Paint.Align.RIGHT
                        else -> Paint.Align.LEFT
                    }
                }

                val processedText = when (element.textTransform) {
                    "uppercase" -> element.text.uppercase()
                    "lowercase" -> element.text.lowercase()
                    "capitalize" -> element.text.split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                    else -> element.text
                }

                val lines = processedText.split("\n")
                val lineHeight = element.fontSize * element.lineSpacingMultiplier
                var currentY = element.y + element.fontSize

                val textX = when (element.textAlign) {
                    "center" -> element.x + element.width / 2f
                    "right" -> element.x + element.width
                    else -> element.x
                }

                lines.forEach { line ->
                    canvas.drawText(line, textX, currentY, paint)
                    currentY += lineHeight
                }
            }

            ElementType.RECTANGLE -> {
                val fillPaint = Paint().apply {
                    color = element.fillColor.toInt()
                    alpha = alphaInt
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                val rectF = RectF(element.x, element.y, element.x + element.width, element.y + element.height)
                canvas.drawRoundRect(rectF, element.cornerRadius, element.cornerRadius, fillPaint)

                if (element.strokeWidth > 0) {
                    val strokePaint = Paint().apply {
                        color = element.strokeColor.toInt()
                        alpha = alphaInt
                        style = Paint.Style.STROKE
                        strokeWidth = element.strokeWidth
                        isAntiAlias = true
                    }
                    canvas.drawRoundRect(rectF, element.cornerRadius, element.cornerRadius, strokePaint)
                }
            }

            ElementType.CIRCLE -> {
                val fillPaint = Paint().apply {
                    color = element.fillColor.toInt()
                    alpha = alphaInt
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                val rectF = RectF(element.x, element.y, element.x + element.width, element.y + element.height)
                canvas.drawOval(rectF, fillPaint)

                if (element.strokeWidth > 0) {
                    val strokePaint = Paint().apply {
                        color = element.strokeColor.toInt()
                        alpha = alphaInt
                        style = Paint.Style.STROKE
                        strokeWidth = element.strokeWidth
                        isAntiAlias = true
                    }
                    canvas.drawOval(rectF, strokePaint)
                }
            }

            ElementType.LINE -> {
                val linePaint = Paint().apply {
                    color = element.strokeColor.toInt()
                    alpha = alphaInt
                    strokeWidth = element.strokeWidth.coerceAtLeast(1f)
                    isAntiAlias = true
                }
                val midY = element.y + element.height / 2f
                canvas.drawLine(element.x, midY, element.x + element.width, midY, linePaint)
            }

            ElementType.IMAGE -> {
                if (!element.imageUri.isNullOrBlank()) {
                    try {
                        val uri = Uri.parse(element.imageUri)
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()
                        if (bitmap != null) {
                            val destRect = RectF(element.x, element.y, element.x + element.width, element.y + element.height)
                            val imgPaint = Paint().apply {
                                alpha = alphaInt
                                isFilterBitmap = true
                            }
                            canvas.drawBitmap(bitmap, null, destRect, imgPaint)
                        }
                    } catch (e: Exception) {
                        // Fallback placeholder box
                        val p = Paint().apply {
                            color = Color.LTGRAY
                            style = Paint.Style.FILL
                        }
                        canvas.drawRect(element.x, element.y, element.x + element.width, element.y + element.height, p)
                    }
                } else {
                    // Placeholder card
                    val p = Paint().apply {
                        color = Color.LTGRAY
                        style = Paint.Style.STROKE
                        strokeWidth = 2f
                    }
                    canvas.drawRect(element.x, element.y, element.x + element.width, element.y + element.height, p)
                }
            }

            ElementType.FORM_TEXT -> {
                // Label
                val labelPaint = Paint().apply {
                    color = Color.rgb(71, 85, 105) // Slate 600
                    textSize = 10f
                    isAntiAlias = true
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
                val reqSuffix = if (element.isRequired) " *" else ""
                canvas.drawText("${element.fieldLabel}$reqSuffix", element.x, element.y - 4f, labelPaint)

                // Input box
                val boxPaint = Paint().apply {
                    color = Color.rgb(248, 250, 252)
                    style = Paint.Style.FILL
                }
                val borderPaint = Paint().apply {
                    color = Color.rgb(203, 213, 225)
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                }
                val rectF = RectF(element.x, element.y, element.x + element.width, element.y + element.height)
                canvas.drawRoundRect(rectF, 4f, 4f, boxPaint)
                canvas.drawRoundRect(rectF, 4f, 4f, borderPaint)

                // Text value or placeholder
                val valPaint = Paint().apply {
                    color = if (element.fieldValue.isNotBlank()) Color.rgb(15, 23, 42) else Color.rgb(148, 163, 184)
                    textSize = 12f
                    isAntiAlias = true
                }
                val display = if (element.fieldValue.isNotBlank()) element.fieldValue else element.placeholder
                canvas.drawText(display, element.x + 8f, element.y + element.height / 2f + 4f, valPaint)
            }

            ElementType.FORM_CHECKBOX -> {
                // Checkbox square
                val size = 18f
                val checkY = element.y + (element.height - size) / 2f
                val boxRect = RectF(element.x, checkY, element.x + size, checkY + size)

                val boxPaint = Paint().apply {
                    color = if (element.isChecked) Color.rgb(37, 99, 235) else Color.WHITE
                    style = Paint.Style.FILL
                }
                val borderPaint = Paint().apply {
                    color = if (element.isChecked) Color.rgb(37, 99, 235) else Color.rgb(148, 163, 184)
                    style = Paint.Style.STROKE
                    strokeWidth = 1.5f
                }
                canvas.drawRoundRect(boxRect, 3f, 3f, boxPaint)
                canvas.drawRoundRect(boxRect, 3f, 3f, borderPaint)

                if (element.isChecked) {
                    val markPaint = Paint().apply {
                        color = Color.WHITE
                        style = Paint.Style.STROKE
                        strokeWidth = 2.5f
                        isAntiAlias = true
                        strokeCap = Paint.Cap.ROUND
                    }
                    val path = Path().apply {
                        moveTo(element.x + 4f, checkY + 9f)
                        lineTo(element.x + 7.5f, checkY + 13f)
                        lineTo(element.x + 14f, checkY + 5f)
                    }
                    canvas.drawPath(path, markPaint)
                }

                // Label
                val labelPaint = Paint().apply {
                    color = Color.rgb(30, 41, 59)
                    textSize = 11.5f
                    isAntiAlias = true
                }
                canvas.drawText(element.fieldLabel, element.x + size + 10f, element.y + element.height / 2f + 4f, labelPaint)
            }

            ElementType.FORM_RADIO -> {
                val radius = 9f
                val radioY = element.y + element.height / 2f
                val radioX = element.x + radius

                val outerPaint = Paint().apply {
                    color = if (element.isChecked) Color.rgb(37, 99, 235) else Color.rgb(148, 163, 184)
                    style = Paint.Style.STROKE
                    strokeWidth = 1.5f
                    isAntiAlias = true
                }
                canvas.drawCircle(radioX, radioY, radius, outerPaint)

                if (element.isChecked) {
                    val innerPaint = Paint().apply {
                        color = Color.rgb(37, 99, 235)
                        style = Paint.Style.FILL
                        isAntiAlias = true
                    }
                    canvas.drawCircle(radioX, radioY, radius - 4f, innerPaint)
                }

                val labelPaint = Paint().apply {
                    color = Color.rgb(30, 41, 59)
                    textSize = 11.5f
                    isAntiAlias = true
                }
                canvas.drawText(element.fieldLabel, element.x + radius * 2 + 10f, radioY + 4f, labelPaint)
            }

            ElementType.FORM_DROPDOWN -> {
                val labelPaint = Paint().apply {
                    color = Color.rgb(71, 85, 105)
                    textSize = 10f
                    isAntiAlias = true
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
                canvas.drawText(element.fieldLabel, element.x, element.y - 4f, labelPaint)

                val rectF = RectF(element.x, element.y, element.x + element.width, element.y + element.height)
                val boxPaint = Paint().apply {
                    color = Color.rgb(248, 250, 252)
                    style = Paint.Style.FILL
                }
                val borderPaint = Paint().apply {
                    color = Color.rgb(203, 213, 225)
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                }
                canvas.drawRoundRect(rectF, 4f, 4f, boxPaint)
                canvas.drawRoundRect(rectF, 4f, 4f, borderPaint)

                val valPaint = Paint().apply {
                    color = Color.rgb(15, 23, 42)
                    textSize = 12f
                    isAntiAlias = true
                }
                val display = if (element.fieldValue.isNotBlank()) element.fieldValue else element.options.firstOrNull() ?: "Select..."
                canvas.drawText(display, element.x + 8f, element.y + element.height / 2f + 4f, valPaint)

                // Down arrow
                val arrowPaint = Paint().apply {
                    color = Color.rgb(100, 116, 139)
                    style = Paint.Style.STROKE
                    strokeWidth = 1.5f
                    isAntiAlias = true
                }
                val arrowX = element.x + element.width - 16f
                val arrowY = element.y + element.height / 2f
                val arrowPath = Path().apply {
                    moveTo(arrowX - 4f, arrowY - 2f)
                    lineTo(arrowX, arrowY + 3f)
                    lineTo(arrowX + 4f, arrowY - 2f)
                }
                canvas.drawPath(arrowPath, arrowPaint)
            }

            ElementType.FORM_SIGNATURE -> {
                val labelPaint = Paint().apply {
                    color = Color.rgb(71, 85, 105)
                    textSize = 10f
                    isAntiAlias = true
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
                canvas.drawText(element.fieldLabel, element.x, element.y - 4f, labelPaint)

                val rectF = RectF(element.x, element.y, element.x + element.width, element.y + element.height)
                val boxPaint = Paint().apply {
                    color = Color.rgb(250, 250, 250)
                    style = Paint.Style.FILL
                }
                val dashPaint = Paint().apply {
                    color = Color.rgb(203, 213, 225)
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                }
                canvas.drawRoundRect(rectF, 4f, 4f, boxPaint)
                canvas.drawRoundRect(rectF, 4f, 4f, dashPaint)

                // Draw baseline
                val linePaint = Paint().apply {
                    color = Color.rgb(203, 213, 225)
                    strokeWidth = 1f
                }
                canvas.drawLine(element.x + 10f, element.y + element.height - 15f, element.x + element.width - 10f, element.y + element.height - 15f, linePaint)

                // Draw signature strokes if present
                if (!element.signaturePoints.isNullOrBlank()) {
                    drawSignatureStrokes(canvas, element.x, element.y, element.signaturePoints)
                } else {
                    val signHint = Paint().apply {
                        color = Color.rgb(148, 163, 184)
                        textSize = 11f
                        isAntiAlias = true
                        typeface = Typeface.create("cursive", Typeface.ITALIC)
                    }
                    canvas.drawText("Sign Here ✍", element.x + 16f, element.y + element.height / 2f + 4f, signHint)
                }
            }

            ElementType.FORM_DATE -> {
                val labelPaint = Paint().apply {
                    color = Color.rgb(71, 85, 105)
                    textSize = 10f
                    isAntiAlias = true
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
                canvas.drawText(element.fieldLabel, element.x, element.y - 4f, labelPaint)

                val rectF = RectF(element.x, element.y, element.x + element.width, element.y + element.height)
                val boxPaint = Paint().apply {
                    color = Color.rgb(248, 250, 252)
                    style = Paint.Style.FILL
                }
                val borderPaint = Paint().apply {
                    color = Color.rgb(203, 213, 225)
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                }
                canvas.drawRoundRect(rectF, 4f, 4f, boxPaint)
                canvas.drawRoundRect(rectF, 4f, 4f, borderPaint)

                val valPaint = Paint().apply {
                    color = Color.rgb(15, 23, 42)
                    textSize = 12f
                    isAntiAlias = true
                }
                val display = if (element.fieldValue.isNotBlank()) element.fieldValue else "YYYY-MM-DD"
                canvas.drawText(display, element.x + 8f, element.y + element.height / 2f + 4f, valPaint)
            }
        }

        canvas.restore()
    }

    private fun drawSignatureStrokes(canvas: Canvas, originX: Float, originY: Float, pointsStr: String) {
        val paint = Paint().apply {
            color = Color.rgb(30, 58, 138) // Deep signature ink blue
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }

        val strokes = pointsStr.split(";")
        for (stroke in strokes) {
            val coords = stroke.split(",").mapNotNull { it.toFloatOrNull() }
            if (coords.size >= 4) {
                val path = Path()
                path.moveTo(originX + coords[0], originY + coords[1])
                for (i in 2 until coords.size step 2) {
                    path.lineTo(originX + coords[i], originY + coords[i + 1])
                }
                canvas.drawPath(path, paint)
            }
        }
    }
}
