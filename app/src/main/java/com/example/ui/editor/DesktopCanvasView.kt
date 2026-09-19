package com.example.ui.editor

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ElementType
import com.example.data.model.PdfDocumentData
import com.example.data.model.PdfElement
import com.example.data.model.PdfPage
import kotlin.math.roundToInt

@Composable
fun DesktopCanvasView(
    document: PdfDocumentData,
    currentPageIndex: Int,
    selectedElementId: String?,
    zoomScale: Float,
    isFormFillMode: Boolean,
    onSelectElement: (String?) -> Unit,
    onUpdateElement: (String, (PdfElement) -> PdfElement) -> Unit,
    onDeleteElement: () -> Unit,
    onDuplicateElement: () -> Unit,
    onBringForward: () -> Unit,
    onEditText: () -> Unit,
    onOpenSignaturePad: () -> Unit,
    onZoomChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val activePage = document.pages.getOrNull(currentPageIndex) ?: PdfPage()
    val pageWidthPt = document.pageWidth
    val pageHeightPt = document.pageHeight

    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Desktop dark slate canvas desk background
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    onZoomChange(zoomScale * zoom)
                    panOffsetX += pan.x
                    panOffsetY += pan.y
                }
            }
            .pointerInput(Unit) {
                detectTapGestures {
                    onSelectElement(null)
                }
            }
    ) {
        val containerWidthPx = constraints.maxWidth.toFloat()
        val containerHeightPx = constraints.maxHeight.toFloat()

        // Calculate page display width and height in dp based on zoom
        // Standard A4 is 595 x 842 pt
        val baseFitWidthScale = (containerWidthPx / (pageWidthPt * density.density) * 0.92f).coerceAtLeast(0.4f)
        val effectiveScale = if (zoomScale == 1.0f) baseFitWidthScale else baseFitWidthScale * zoomScale

        val pageDisplayWidthDp = (pageWidthPt * effectiveScale).dp
        val pageDisplayHeightDp = (pageHeightPt * effectiveScale).dp

        // Desktop Workspace Canvas
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Document Page Sheet with Desktop shadow and margins
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(panOffsetX.roundToInt(), panOffsetY.roundToInt())
                    }
                    .size(pageDisplayWidthDp, pageDisplayHeightDp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(2.dp),
                        ambientColor = Color.Black.copy(alpha = 0.5f),
                        spotColor = Color.Black.copy(alpha = 0.7f)
                    )
                    .background(Color.White, RoundedCornerShape(2.dp))
                    .border(0.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(2.dp))
                    .testTag("desktop_pdf_sheet")
            ) {
                // 1. Watermark Layer (rendered behind text)
                if (document.watermarkEnabled && document.watermarkText.isNotBlank()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = document.watermarkText,
                            fontSize = (48 * effectiveScale).sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            color = Color(document.watermarkColor).copy(alpha = document.watermarkOpacity),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.rotate(document.watermarkRotation)
                        )
                    }
                }

                // 2. Desktop Header
                val pageNumber = currentPageIndex + 1
                if (document.headerText.isNotBlank() && (!document.headerExcludeFirst || pageNumber > 1)) {
                    val headerAlign = when (document.headerAlign) {
                        "left" -> TextAlign.Left
                        "right" -> TextAlign.Right
                        else -> TextAlign.Center
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = (36 * effectiveScale).dp, vertical = (16 * effectiveScale).dp)
                    ) {
                        Text(
                            text = document.headerText,
                            fontSize = (9 * effectiveScale).sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B),
                            textAlign = headerAlign,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 3. Document Elements
                val sortedElements = activePage.elements.sortedBy { it.zIndex }
                sortedElements.forEach { element ->
                    val isSelected = element.id == selectedElementId && !isFormFillMode

                    ElementView(
                        element = element,
                        effectiveScale = effectiveScale,
                        isSelected = isSelected,
                        isFormFillMode = isFormFillMode,
                        onSelect = { onSelectElement(element.id) },
                        onUpdate = { transform -> onUpdateElement(element.id, transform) },
                        onOpenSignaturePad = onOpenSignaturePad,
                        onEditText = onEditText
                    )
                }

                // 4. Desktop Footer with dynamic page token
                if (document.footerText.isNotBlank() && (!document.footerExcludeFirst || pageNumber > 1)) {
                    val footerText = document.footerText
                        .replace("{page}", pageNumber.toString())
                        .replace("{total}", document.pages.size.toString())
                    val footerAlign = when (document.footerAlign) {
                        "left" -> TextAlign.Left
                        "right" -> TextAlign.Right
                        else -> TextAlign.Center
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = (36 * effectiveScale).dp, vertical = (16 * effectiveScale).dp)
                    ) {
                        Text(
                            text = footerText,
                            fontSize = (9 * effectiveScale).sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF94A3B8),
                            textAlign = footerAlign,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Selected Element Floating Quick Action Toolbar (Desktop style)
                if (selectedElementId != null && !isFormFillMode) {
                    val selected = activePage.elements.find { it.id == selectedElementId }
                    if (selected != null) {
                        val toolbarX = (selected.x * effectiveScale).dp
                        val toolbarY = ((selected.y - 48f).coerceAtLeast(4f) * effectiveScale).dp

                        Surface(
                            modifier = Modifier
                                .offset(x = toolbarX, y = toolbarY)
                                .shadow(8.dp, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF1E293B),
                            tonalElevation = 6.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (selected.type == ElementType.TEXT) {
                                    IconButton(
                                        onClick = onEditText,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit Text",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = onDuplicateElement,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Duplicate",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = onBringForward,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Layers,
                                        contentDescription = "Bring Forward",
                                        tint = Color(0xFF93C5FD),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = onDeleteElement,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFFCA5A5),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Desktop Viewport Zoom & Mode Controls
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .shadow(12.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF1E293B).copy(alpha = 0.92f),
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { onZoomChange((zoomScale - 0.15f).coerceAtLeast(0.5f)) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "${(zoomScale * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .clickable { onZoomChange(1.0f); panOffsetX = 0f; panOffsetY = 0f }
                        .padding(horizontal = 4.dp)
                )

                IconButton(
                    onClick = { onZoomChange((zoomScale + 0.15f).coerceAtMost(2.5f)) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                FilledTonalIconButton(
                    onClick = {
                        onZoomChange(1.0f)
                        panOffsetX = 0f
                        panOffsetY = 0f
                    },
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color(0xFF334155),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        Icons.Default.FitScreen,
                        contentDescription = "Reset Zoom",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ElementView(
    element: PdfElement,
    effectiveScale: Float,
    isSelected: Boolean,
    isFormFillMode: Boolean,
    onSelect: () -> Unit,
    onUpdate: ((PdfElement) -> PdfElement) -> Unit,
    onOpenSignaturePad: () -> Unit,
    onEditText: () -> Unit
) {
    val elemWidthDp = (element.width * effectiveScale).dp
    val elemHeightDp = (element.height * effectiveScale).dp
    val elemXDp = (element.x * effectiveScale).dp
    val elemYDp = (element.y * effectiveScale).dp

    Box(
        modifier = Modifier
            .offset(x = elemXDp, y = elemYDp)
            .size(elemWidthDp, elemHeightDp)
            .rotate(element.rotation)
            .pointerInput(isFormFillMode) {
                if (!isFormFillMode) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val deltaX = dragAmount.x / effectiveScale
                        val deltaY = dragAmount.y / effectiveScale
                        onUpdate {
                            it.copy(
                                x = (it.x + deltaX).coerceAtLeast(0f),
                                y = (it.y + deltaY).coerceAtLeast(0f)
                            )
                        }
                    }
                }
            }
            .clickable {
                if (!isFormFillMode) {
                    onSelect()
                }
            }
    ) {
        // Element Content rendering
        when (element.type) {
            ElementType.TEXT -> {
                val fontFamily = when (element.fontFamily.lowercase()) {
                    "serif" -> FontFamily.Serif
                    "monospace" -> FontFamily.Monospace
                    "cursive" -> FontFamily.Cursive
                    else -> FontFamily.SansSerif
                }
                val fontWeight = if (element.isBold) FontWeight.Bold else FontWeight.Normal
                val fontStyle = if (element.isItalic) FontStyle.Italic else FontStyle.Normal
                val textDeco = when {
                    element.isUnderline && element.isStrikethrough -> TextDecoration.combine(listOf(TextDecoration.Underline, TextDecoration.LineThrough))
                    element.isUnderline -> TextDecoration.Underline
                    element.isStrikethrough -> TextDecoration.LineThrough
                    else -> TextDecoration.None
                }
                val textAlign = when (element.textAlign) {
                    "center" -> TextAlign.Center
                    "right" -> TextAlign.Right
                    "justify" -> TextAlign.Justify
                    else -> TextAlign.Left
                }

                val processedText = when (element.textTransform) {
                    "uppercase" -> element.text.uppercase()
                    "lowercase" -> element.text.lowercase()
                    "capitalize" -> element.text.split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                    else -> element.text
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (element.textBgColor != 0L) Color(element.textBgColor) else Color.Transparent)
                ) {
                    Text(
                        text = processedText,
                        fontSize = (element.fontSize * effectiveScale).sp,
                        fontWeight = fontWeight,
                        fontStyle = fontStyle,
                        fontFamily = fontFamily,
                        textDecoration = textDeco,
                        textAlign = textAlign,
                        letterSpacing = (element.letterSpacingSp * effectiveScale).sp,
                        lineHeight = ((element.fontSize * element.lineSpacingMultiplier) * effectiveScale).sp,
                        color = Color(element.textColor).copy(alpha = element.opacity),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            ElementType.RECTANGLE -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color(element.fillColor).copy(alpha = element.opacity),
                            RoundedCornerShape((element.cornerRadius * effectiveScale).dp)
                        )
                        .border(
                            (element.strokeWidth * effectiveScale).dp,
                            Color(element.strokeColor).copy(alpha = element.opacity),
                            RoundedCornerShape((element.cornerRadius * effectiveScale).dp)
                        )
                )
            }

            ElementType.CIRCLE -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color(element.fillColor).copy(alpha = element.opacity),
                            CircleShape
                        )
                        .border(
                            (element.strokeWidth * effectiveScale).dp,
                            Color(element.strokeColor).copy(alpha = element.opacity),
                            CircleShape
                        )
                )
            }

            ElementType.LINE -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((element.strokeWidth * effectiveScale).coerceAtLeast(1f).dp)
                        .align(Alignment.Center)
                        .background(Color(element.strokeColor).copy(alpha = element.opacity))
                )
            }

            ElementType.IMAGE -> {
                if (!element.imageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = Uri.parse(element.imageUri),
                        contentDescription = "Document image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape((element.cornerRadius * effectiveScale).dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Image placeholder", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                }
            }

            ElementType.FORM_TEXT -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "${element.fieldLabel}${if (element.isRequired) " *" else ""}",
                        fontSize = (9 * effectiveScale).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(4.dp))
                            .border(1.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                            .clickable(enabled = isFormFillMode) {
                                onEditText()
                            }
                            .padding(horizontal = (6 * effectiveScale).dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = if (element.fieldValue.isNotBlank()) element.fieldValue else element.placeholder,
                            fontSize = (11 * effectiveScale).sp,
                            color = if (element.fieldValue.isNotBlank()) Color(0xFF0F172A) else Color(0xFF94A3B8)
                        )
                    }
                }
            }

            ElementType.FORM_CHECKBOX -> {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(enabled = isFormFillMode) {
                            onUpdate { it.copy(isChecked = !it.isChecked) }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = element.isChecked,
                        onCheckedChange = { checked ->
                            onUpdate { it.copy(isChecked = checked) }
                        },
                        enabled = isFormFillMode
                    )
                    Text(
                        text = element.fieldLabel,
                        fontSize = (11 * effectiveScale).sp,
                        color = Color(0xFF1E293B)
                    )
                }
            }

            ElementType.FORM_RADIO -> {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(enabled = isFormFillMode) {
                            onUpdate { it.copy(isChecked = !it.isChecked) }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = element.isChecked,
                        onClick = {
                            onUpdate { it.copy(isChecked = !it.isChecked) }
                        },
                        enabled = isFormFillMode
                    )
                    Text(
                        text = element.fieldLabel,
                        fontSize = (11 * effectiveScale).sp,
                        color = Color(0xFF1E293B)
                    )
                }
            }

            ElementType.FORM_DROPDOWN -> {
                var expanded by remember { mutableStateOf(false) }

                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = element.fieldLabel,
                        fontSize = (9 * effectiveScale).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                            .clickable(enabled = isFormFillMode) {
                                expanded = true
                            }
                            .padding(horizontal = (6 * effectiveScale).dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (element.fieldValue.isNotBlank()) element.fieldValue else element.options.firstOrNull() ?: "Select",
                                fontSize = (11 * effectiveScale).sp,
                                color = Color(0xFF0F172A)
                            )
                            Text("▾", fontSize = 12.sp, color = Color(0xFF64748B))
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            element.options.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        onUpdate { it.copy(fieldValue = opt) }
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            ElementType.FORM_SIGNATURE -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = element.fieldLabel,
                        fontSize = (9 * effectiveScale).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color(0xFFFAFAFA), RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                            .clickable(enabled = isFormFillMode) {
                                onOpenSignaturePad()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!element.signaturePoints.isNullOrBlank()) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawSignatureCanvas(
                                    element.signaturePoints,
                                    size.width,
                                    size.height,
                                    effectiveScale
                                )
                            }
                        } else {
                            Text(
                                text = "✍ Tap to Sign",
                                fontSize = (11 * effectiveScale).sp,
                                fontStyle = FontStyle.Italic,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            ElementType.FORM_DATE -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = element.fieldLabel,
                        fontSize = (9 * effectiveScale).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                            .clickable(enabled = isFormFillMode) {
                                onEditText()
                            }
                            .padding(horizontal = (6 * effectiveScale).dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = if (element.fieldValue.isNotBlank()) element.fieldValue else "YYYY-MM-DD",
                            fontSize = (11 * effectiveScale).sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }
        }

        // Desktop Selection Handles & Bounding Box
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.5.dp, Color(0xFF2563EB), RoundedCornerShape(2.dp))
            ) {
                // Bottom-right corner resize handle
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(16.dp)
                        .offset(x = 6.dp, y = 6.dp)
                        .background(Color(0xFF2563EB), CircleShape)
                        .border(1.5.dp, Color.White, CircleShape)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val dw = dragAmount.x / effectiveScale
                                val dh = dragAmount.y / effectiveScale
                                onUpdate {
                                    it.copy(
                                        width = (it.width + dw).coerceAtLeast(30f),
                                        height = (it.height + dh).coerceAtLeast(20f)
                                    )
                                }
                            }
                        }
                )

                // Top-right corner handle
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(10.dp)
                        .offset(x = 4.dp, y = (-4).dp)
                        .background(Color(0xFF2563EB), RoundedCornerShape(2.dp))
                )

                // Top-left corner handle
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(10.dp)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .background(Color(0xFF2563EB), RoundedCornerShape(2.dp))
                )

                // Bottom-left corner handle
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(10.dp)
                        .offset(x = (-4).dp, y = 4.dp)
                        .background(Color(0xFF2563EB), RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSignatureCanvas(
    pointsStr: String,
    w: Float,
    h: Float,
    scale: Float
) {
    val strokes = pointsStr.split(";")
    for (stroke in strokes) {
        val coords = stroke.split(",").mapNotNull { it.toFloatOrNull() }
        if (coords.size >= 4) {
            val path = Path()
            path.moveTo(coords[0] * scale, coords[1] * scale)
            for (i in 2 until coords.size step 2) {
                path.lineTo(coords[i] * scale, coords[i + 1] * scale)
            }
            drawPath(
                path = path,
                color = Color(0xFF1E3A8A),
                style = Stroke(width = 2.5f * scale)
            )
        }
    }
}
