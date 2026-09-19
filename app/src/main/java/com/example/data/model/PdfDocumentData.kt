package com.example.data.model

import java.util.UUID

data class PdfPage(
    val id: String = UUID.randomUUID().toString(),
    val pageIndex: Int = 0,
    val elements: List<PdfElement> = emptyList()
)

data class PdfDocumentData(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Untitled Document",
    val pageSize: String = "A4", // "A4" (595x842 pt), "Letter" (612x792 pt)
    val isLandscape: Boolean = false,
    val pages: List<PdfPage> = listOf(PdfPage(pageIndex = 0)),
    
    // Headers & Footers
    val headerText: String = "",
    val headerAlign: String = "center", // "left", "center", "right"
    val headerExcludeFirst: Boolean = false,
    val footerText: String = "Page {page} of {total}",
    val footerAlign: String = "center", // "left", "center", "right"
    val footerExcludeFirst: Boolean = false,

    // Watermark
    val watermarkEnabled: Boolean = false,
    val watermarkText: String = "DRAFT",
    val watermarkOpacity: Float = 0.18f,
    val watermarkRotation: Float = -45f,
    val watermarkColor: Long = 0xFFDC2626, // Crimson red watermark

    val lastModified: Long = System.currentTimeMillis()
) {
    // Width and Height in points
    val pageWidth: Float
        get() = when (pageSize) {
            "Letter" -> if (isLandscape) 792f else 612f
            else -> if (isLandscape) 842f else 595f // A4 standard
        }

    val pageHeight: Float
        get() = when (pageSize) {
            "Letter" -> if (isLandscape) 612f else 792f
            else -> if (isLandscape) 595f else 842f // A4 standard
        }
}
