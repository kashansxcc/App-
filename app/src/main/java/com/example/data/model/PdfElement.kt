package com.example.data.model

import java.util.UUID

enum class ElementType {
    TEXT,
    IMAGE,
    RECTANGLE,
    CIRCLE,
    LINE,
    FORM_TEXT,
    FORM_CHECKBOX,
    FORM_RADIO,
    FORM_DROPDOWN,
    FORM_SIGNATURE,
    FORM_DATE
}

data class PdfElement(
    val id: String = UUID.randomUUID().toString(),
    val type: ElementType = ElementType.TEXT,
    // Dimensions in standard PDF points (e.g., A4 is 595 x 842 points)
    val x: Float = 50f,
    val y: Float = 100f,
    val width: Float = 200f,
    val height: Float = 60f,
    val rotation: Float = 0f,
    val opacity: Float = 1.0f,
    val zIndex: Int = 0,

    // Text properties
    val text: String = "Double tap to edit text",
    val fontFamily: String = "SansSerif", // "SansSerif", "Serif", "Monospace", "Cursive"
    val fontSize: Float = 16f,
    val textColor: Long = 0xFF1E293B, // Charcoal / dark slate
    val textBgColor: Long = 0x00000000, // Transparent
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val isStrikethrough: Boolean = false,
    val textAlign: String = "left", // "left", "center", "right", "justify"
    val lineSpacingMultiplier: Float = 1.2f,
    val letterSpacingSp: Float = 0f,
    val textTransform: String = "none", // "none", "uppercase", "lowercase", "capitalize"

    // Image & Shape properties
    val imageUri: String? = null,
    val fillColor: Long = 0xFFE2E8F0, // Soft gray
    val strokeColor: Long = 0xFF475569, // Slate border
    val strokeWidth: Float = 1.5f,
    val cornerRadius: Float = 4f,

    // Form builder properties
    val fieldName: String = "Field_1",
    val fieldLabel: String = "Text Field",
    val fieldValue: String = "",
    val placeholder: String = "Enter text...",
    val isRequired: Boolean = false,
    val isChecked: Boolean = false,
    val options: List<String> = listOf("Option 1", "Option 2", "Option 3"),
    val signaturePoints: String? = null // Comma-separated points for drawn signature
)
