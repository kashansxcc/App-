package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

object PdfJsonSerializer {

    fun serialize(doc: PdfDocumentData): String {
        val root = JSONObject()
        root.put("id", doc.id)
        root.put("title", doc.title)
        root.put("pageSize", doc.pageSize)
        root.put("isLandscape", doc.isLandscape)
        root.put("headerText", doc.headerText)
        root.put("headerAlign", doc.headerAlign)
        root.put("headerExcludeFirst", doc.headerExcludeFirst)
        root.put("footerText", doc.footerText)
        root.put("footerAlign", doc.footerAlign)
        root.put("footerExcludeFirst", doc.footerExcludeFirst)
        root.put("watermarkEnabled", doc.watermarkEnabled)
        root.put("watermarkText", doc.watermarkText)
        root.put("watermarkOpacity", doc.watermarkOpacity.toDouble())
        root.put("watermarkRotation", doc.watermarkRotation.toDouble())
        root.put("watermarkColor", doc.watermarkColor)
        root.put("lastModified", doc.lastModified)

        val pagesArray = JSONArray()
        doc.pages.forEach { page ->
            val pageObj = JSONObject()
            pageObj.put("id", page.id)
            pageObj.put("pageIndex", page.pageIndex)

            val elemArray = JSONArray()
            page.elements.forEach { elem ->
                val elemObj = JSONObject()
                elemObj.put("id", elem.id)
                elemObj.put("type", elem.type.name)
                elemObj.put("x", elem.x.toDouble())
                elemObj.put("y", elem.y.toDouble())
                elemObj.put("width", elem.width.toDouble())
                elemObj.put("height", elem.height.toDouble())
                elemObj.put("rotation", elem.rotation.toDouble())
                elemObj.put("opacity", elem.opacity.toDouble())
                elemObj.put("zIndex", elem.zIndex)

                elemObj.put("text", elem.text)
                elemObj.put("fontFamily", elem.fontFamily)
                elemObj.put("fontSize", elem.fontSize.toDouble())
                elemObj.put("textColor", elem.textColor)
                elemObj.put("textBgColor", elem.textBgColor)
                elemObj.put("isBold", elem.isBold)
                elemObj.put("isItalic", elem.isItalic)
                elemObj.put("isUnderline", elem.isUnderline)
                elemObj.put("isStrikethrough", elem.isStrikethrough)
                elemObj.put("textAlign", elem.textAlign)
                elemObj.put("lineSpacingMultiplier", elem.lineSpacingMultiplier.toDouble())
                elemObj.put("letterSpacingSp", elem.letterSpacingSp.toDouble())
                elemObj.put("textTransform", elem.textTransform)

                elemObj.put("imageUri", elem.imageUri ?: "")
                elemObj.put("fillColor", elem.fillColor)
                elemObj.put("strokeColor", elem.strokeColor)
                elemObj.put("strokeWidth", elem.strokeWidth.toDouble())
                elemObj.put("cornerRadius", elem.cornerRadius.toDouble())

                elemObj.put("fieldName", elem.fieldName)
                elemObj.put("fieldLabel", elem.fieldLabel)
                elemObj.put("fieldValue", elem.fieldValue)
                elemObj.put("placeholder", elem.placeholder)
                elemObj.put("isRequired", elem.isRequired)
                elemObj.put("isChecked", elem.isChecked)

                val optArray = JSONArray()
                elem.options.forEach { optArray.put(it) }
                elemObj.put("options", optArray)

                elemObj.put("signaturePoints", elem.signaturePoints ?: "")
                elemArray.put(elemObj)
            }
            pageObj.put("elements", elemArray)
            pagesArray.put(pageObj)
        }
        root.put("pages", pagesArray)
        return root.toString()
    }

    fun deserialize(jsonStr: String): PdfDocumentData {
        val root = JSONObject(jsonStr)
        val id = root.optString("id", "")
        val title = root.optString("title", "Untitled Document")
        val pageSize = root.optString("pageSize", "A4")
        val isLandscape = root.optBoolean("isLandscape", false)
        val headerText = root.optString("headerText", "")
        val headerAlign = root.optString("headerAlign", "center")
        val headerExcludeFirst = root.optBoolean("headerExcludeFirst", false)
        val footerText = root.optString("footerText", "Page {page} of {total}")
        val footerAlign = root.optString("footerAlign", "center")
        val footerExcludeFirst = root.optBoolean("footerExcludeFirst", false)
        val watermarkEnabled = root.optBoolean("watermarkEnabled", false)
        val watermarkText = root.optString("watermarkText", "DRAFT")
        val watermarkOpacity = root.optDouble("watermarkOpacity", 0.18).toFloat()
        val watermarkRotation = root.optDouble("watermarkRotation", -45.0).toFloat()
        val watermarkColor = root.optLong("watermarkColor", 0xFFDC2626)
        val lastModified = root.optLong("lastModified", System.currentTimeMillis())

        val pages = mutableListOf<PdfPage>()
        val pagesArray = root.optJSONArray("pages")
        if (pagesArray != null) {
            for (i in 0 until pagesArray.length()) {
                val pageObj = pagesArray.getJSONObject(i)
                val pageId = pageObj.optString("id", "")
                val pageIndex = pageObj.optInt("pageIndex", i)

                val elements = mutableListOf<PdfElement>()
                val elemArray = pageObj.optJSONArray("elements")
                if (elemArray != null) {
                    for (j in 0 until elemArray.length()) {
                        val elemObj = elemArray.getJSONObject(j)
                        val typeStr = elemObj.optString("type", ElementType.TEXT.name)
                        val type = try {
                            ElementType.valueOf(typeStr)
                        } catch (e: Exception) {
                            ElementType.TEXT
                        }

                        val optList = mutableListOf<String>()
                        val opts = elemObj.optJSONArray("options")
                        if (opts != null) {
                            for (k in 0 until opts.length()) {
                                optList.add(opts.getString(k))
                            }
                        }

                        val elem = PdfElement(
                            id = elemObj.optString("id", ""),
                            type = type,
                            x = elemObj.optDouble("x", 50.0).toFloat(),
                            y = elemObj.optDouble("y", 100.0).toFloat(),
                            width = elemObj.optDouble("width", 200.0).toFloat(),
                            height = elemObj.optDouble("height", 60.0).toFloat(),
                            rotation = elemObj.optDouble("rotation", 0.0).toFloat(),
                            opacity = elemObj.optDouble("opacity", 1.0).toFloat(),
                            zIndex = elemObj.optInt("zIndex", j),
                            text = elemObj.optString("text", ""),
                            fontFamily = elemObj.optString("fontFamily", "SansSerif"),
                            fontSize = elemObj.optDouble("fontSize", 16.0).toFloat(),
                            textColor = elemObj.optLong("textColor", 0xFF1E293B),
                            textBgColor = elemObj.optLong("textBgColor", 0x00000000),
                            isBold = elemObj.optBoolean("isBold", false),
                            isItalic = elemObj.optBoolean("isItalic", false),
                            isUnderline = elemObj.optBoolean("isUnderline", false),
                            isStrikethrough = elemObj.optBoolean("isStrikethrough", false),
                            textAlign = elemObj.optString("textAlign", "left"),
                            lineSpacingMultiplier = elemObj.optDouble("lineSpacingMultiplier", 1.2).toFloat(),
                            letterSpacingSp = elemObj.optDouble("letterSpacingSp", 0.0).toFloat(),
                            textTransform = elemObj.optString("textTransform", "none"),
                            imageUri = elemObj.optString("imageUri").takeIf { it.isNotBlank() },
                            fillColor = elemObj.optLong("fillColor", 0xFFE2E8F0),
                            strokeColor = elemObj.optLong("strokeColor", 0xFF475569),
                            strokeWidth = elemObj.optDouble("strokeWidth", 1.5).toFloat(),
                            cornerRadius = elemObj.optDouble("cornerRadius", 4.0).toFloat(),
                            fieldName = elemObj.optString("fieldName", "Field_1"),
                            fieldLabel = elemObj.optString("fieldLabel", "Text Field"),
                            fieldValue = elemObj.optString("fieldValue", ""),
                            placeholder = elemObj.optString("placeholder", "Enter text..."),
                            isRequired = elemObj.optBoolean("isRequired", false),
                            isChecked = elemObj.optBoolean("isChecked", false),
                            options = if (optList.isNotEmpty()) optList else listOf("Option 1", "Option 2"),
                            signaturePoints = elemObj.optString("signaturePoints").takeIf { it.isNotBlank() }
                        )
                        elements.add(elem)
                    }
                }
                pages.add(PdfPage(id = pageId, pageIndex = pageIndex, elements = elements))
            }
        }

        if (pages.isEmpty()) {
            pages.add(PdfPage(pageIndex = 0))
        }

        return PdfDocumentData(
            id = id,
            title = title,
            pageSize = pageSize,
            isLandscape = isLandscape,
            pages = pages,
            headerText = headerText,
            headerAlign = headerAlign,
            headerExcludeFirst = headerExcludeFirst,
            footerText = footerText,
            footerAlign = footerAlign,
            footerExcludeFirst = footerExcludeFirst,
            watermarkEnabled = watermarkEnabled,
            watermarkText = watermarkText,
            watermarkOpacity = watermarkOpacity,
            watermarkRotation = watermarkRotation,
            watermarkColor = watermarkColor,
            lastModified = lastModified
        )
    }
}
