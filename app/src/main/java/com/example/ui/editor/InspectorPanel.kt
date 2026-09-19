package com.example.ui.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ElementType
import com.example.data.model.PdfElement

@Composable
fun InspectorPanel(
    selectedElement: PdfElement?,
    isOpen: Boolean,
    onClose: () -> Unit,
    onUpdateElement: ((PdfElement) -> PdfElement) -> Unit,
    onBringForward: () -> Unit,
    onSendBackward: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally { it },
        exit = slideOutHorizontally { it },
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .width(280.dp)
                .fillMaxHeight()
                .shadow(16.dp),
            color = Color(0xFF1E293B), // Dark slate
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Properties Inspector",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close Inspector",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedElement == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No element selected.\nClick on any text, shape, image or form field on the canvas to inspect and edit its settings.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 18.sp
                        )
                    }
                    return@Column
                }

                // Section 1: Object Transform & Placement
                SectionTitle("TRANSFORM & POSITION")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PropertyField(
                        label = "X (pt)",
                        value = selectedElement.x.toInt().toString(),
                        onValueChange = { str ->
                            str.toFloatOrNull()?.let { v -> onUpdateElement { it.copy(x = v) } }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    PropertyField(
                        label = "Y (pt)",
                        value = selectedElement.y.toInt().toString(),
                        onValueChange = { str ->
                            str.toFloatOrNull()?.let { v -> onUpdateElement { it.copy(y = v) } }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PropertyField(
                        label = "Width (pt)",
                        value = selectedElement.width.toInt().toString(),
                        onValueChange = { str ->
                            str.toFloatOrNull()?.let { v -> onUpdateElement { it.copy(width = v) } }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    PropertyField(
                        label = "Height (pt)",
                        value = selectedElement.height.toInt().toString(),
                        onValueChange = { str ->
                            str.toFloatOrNull()?.let { v -> onUpdateElement { it.copy(height = v) } }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Rotation slider
                Text(
                    text = "Rotation: ${selectedElement.rotation.toInt()}°",
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1)
                )
                Slider(
                    value = selectedElement.rotation,
                    onValueChange = { r -> onUpdateElement { it.copy(rotation = r) } },
                    valueRange = -180f..180f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF3B82F6),
                        activeTrackColor = Color(0xFF3B82F6)
                    )
                )

                // Opacity slider
                Text(
                    text = "Opacity: ${(selectedElement.opacity * 100).toInt()}%",
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1)
                )
                Slider(
                    value = selectedElement.opacity,
                    onValueChange = { o -> onUpdateElement { it.copy(opacity = o) } },
                    valueRange = 0.1f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF3B82F6),
                        activeTrackColor = Color(0xFF3B82F6)
                    )
                )

                // Layer arrangement
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = false,
                        onClick = onBringForward,
                        label = { Text("Bring Forward", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFF334155),
                            labelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = false,
                        onClick = onSendBackward,
                        label = { Text("Send Backward", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFF334155),
                            labelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Section 2: Text Styling & Settings (if text)
                if (selectedElement.type == ElementType.TEXT) {
                    Spacer(modifier = Modifier.height(14.dp))
                    SectionTitle("TEXT SETTINGS & STYLING")

                    // Font Family
                    Text("Font Family", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(4.dp))
                    val fonts = listOf("SansSerif", "Serif", "Monospace", "Cursive")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        fonts.forEach { f ->
                            val isSel = selectedElement.fontFamily.equals(f, ignoreCase = true)
                            FilterChip(
                                selected = isSel,
                                onClick = { onUpdateElement { it.copy(fontFamily = f) } },
                                label = { Text(f, fontSize = 9.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF3B82F6),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF334155),
                                    labelColor = Color(0xFFCBD5E1)
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Font Size Slider
                    Text("Font Size: ${selectedElement.fontSize.toInt()} pt", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                    Slider(
                        value = selectedElement.fontSize,
                        onValueChange = { s -> onUpdateElement { it.copy(fontSize = s) } },
                        valueRange = 8f..64f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF3B82F6), activeTrackColor = Color(0xFF3B82F6))
                    )

                    // Line Spacing Multiplier
                    Text("Line Spacing: ${"%.2f".format(selectedElement.lineSpacingMultiplier)}x", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                    Slider(
                        value = selectedElement.lineSpacingMultiplier,
                        onValueChange = { m -> onUpdateElement { it.copy(lineSpacingMultiplier = m) } },
                        valueRange = 1.0f..2.5f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF3B82F6), activeTrackColor = Color(0xFF3B82F6))
                    )

                    // Letter Spacing
                    Text("Letter Spacing: ${selectedElement.letterSpacingSp.toInt()}", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                    Slider(
                        value = selectedElement.letterSpacingSp,
                        onValueChange = { ls -> onUpdateElement { it.copy(letterSpacingSp = ls) } },
                        valueRange = -2f..8f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF3B82F6), activeTrackColor = Color(0xFF3B82F6))
                    )

                    // Text Transformation (UPPERCASE, lowercase, Capitalize)
                    Text("Text Case", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(4.dp))
                    val cases = listOf("none" to "Normal", "uppercase" to "UPPER", "lowercase" to "lower", "capitalize" to "Title")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        cases.forEach { (key, label) ->
                            val isSel = selectedElement.textTransform == key
                            FilterChip(
                                selected = isSel,
                                onClick = { onUpdateElement { it.copy(textTransform = key) } },
                                label = { Text(label, fontSize = 9.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF3B82F6),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF334155),
                                    labelColor = Color(0xFFCBD5E1)
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Text Color & Background Highlight Color
                    Text("Text Color", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(4.dp))
                    ColorPaletteRow(
                        selectedColor = selectedElement.textColor,
                        onSelectColor = { c -> onUpdateElement { it.copy(textColor = c) } }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Text Highlight Color", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(4.dp))
                    val highlightColors = listOf(
                        0x00000000L, // None
                        0x66FEF08A, // Soft yellow
                        0x66BBF7D0, // Soft green
                        0x66BAE6FD, // Soft cyan
                        0x66FECDD3, // Soft pink
                        0x66FED7AA  // Soft orange
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        highlightColors.forEach { c ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(if (c == 0L) Color(0xFF334155) else Color(c), CircleShape)
                                    .border(
                                        width = if (selectedElement.textBgColor == c) 2.dp else 1.dp,
                                        color = if (selectedElement.textBgColor == c) Color.White else Color(0xFF64748B),
                                        shape = CircleShape
                                    )
                                    .clickable { onUpdateElement { it.copy(textBgColor = c) } }
                            )
                        }
                    }
                }

                // Section 3: Shapes & Borders
                if (selectedElement.type in listOf(ElementType.RECTANGLE, ElementType.CIRCLE, ElementType.LINE)) {
                    Spacer(modifier = Modifier.height(14.dp))
                    SectionTitle("SHAPE & BORDER")

                    Text("Fill Color", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(4.dp))
                    ColorPaletteRow(
                        selectedColor = selectedElement.fillColor,
                        onSelectColor = { c -> onUpdateElement { it.copy(fillColor = c) } }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Border Stroke Color", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(4.dp))
                    ColorPaletteRow(
                        selectedColor = selectedElement.strokeColor,
                        onSelectColor = { c -> onUpdateElement { it.copy(strokeColor = c) } }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Stroke Width: ${selectedElement.strokeWidth.toInt()} pt", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                    Slider(
                        value = selectedElement.strokeWidth,
                        onValueChange = { sw -> onUpdateElement { it.copy(strokeWidth = sw) } },
                        valueRange = 0f..12f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF3B82F6), activeTrackColor = Color(0xFF3B82F6))
                    )

                    if (selectedElement.type == ElementType.RECTANGLE) {
                        Text("Corner Radius: ${selectedElement.cornerRadius.toInt()} pt", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                        Slider(
                            value = selectedElement.cornerRadius,
                            onValueChange = { cr -> onUpdateElement { it.copy(cornerRadius = cr) } },
                            valueRange = 0f..32f,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF3B82F6), activeTrackColor = Color(0xFF3B82F6))
                        )
                    }
                }

                // Section 4: Form Field Configuration
                if (selectedElement.type in listOf(
                        ElementType.FORM_TEXT,
                        ElementType.FORM_CHECKBOX,
                        ElementType.FORM_RADIO,
                        ElementType.FORM_DROPDOWN,
                        ElementType.FORM_SIGNATURE,
                        ElementType.FORM_DATE
                    )
                ) {
                    Spacer(modifier = Modifier.height(14.dp))
                    SectionTitle("FORM FIELD CONFIG")

                    PropertyField(
                        label = "Field Label",
                        value = selectedElement.fieldLabel,
                        onValueChange = { v -> onUpdateElement { it.copy(fieldLabel = v) } },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (selectedElement.type == ElementType.FORM_TEXT) {
                        PropertyField(
                            label = "Placeholder",
                            value = selectedElement.placeholder,
                            onValueChange = { v -> onUpdateElement { it.copy(placeholder = v) } },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Required Field", fontSize = 11.sp, color = Color.White)
                            Switch(
                                checked = selectedElement.isRequired,
                                onCheckedChange = { req -> onUpdateElement { it.copy(isRequired = req) } },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF3B82F6))
                            )
                        }
                    }

                    if (selectedElement.type == ElementType.FORM_DROPDOWN) {
                        Spacer(modifier = Modifier.height(6.dp))
                        PropertyField(
                            label = "Options (comma-separated)",
                            value = selectedElement.options.joinToString(", "),
                            onValueChange = { str ->
                                val list = str.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                onUpdateElement { it.copy(options = list) }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF60A5FA),
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun PropertyField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 10.sp) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color(0xFFCBD5E1),
            focusedBorderColor = Color(0xFF3B82F6),
            unfocusedBorderColor = Color(0xFF475569),
            focusedLabelColor = Color(0xFF60A5FA),
            unfocusedLabelColor = Color(0xFF94A3B8),
            focusedContainerColor = Color(0xFF0F172A),
            unfocusedContainerColor = Color(0xFF0F172A)
        ),
        modifier = modifier
    )
}

@Composable
private fun ColorPaletteRow(
    selectedColor: Long,
    onSelectColor: (Long) -> Unit
) {
    val colors = listOf(
        0xFF0F172A, // Dark slate
        0xFF1E293B, // Charcoal
        0xFF2563EB, // Primary blue
        0xFFDC2626, // Red
        0xFF16A34A, // Green
        0xFFD97706, // Amber
        0xFF7C3AED, // Purple
        0xFF0D9488, // Teal
        0xFFFFFFFF  // White
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        colors.forEach { c ->
            val isSel = selectedColor == c
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(c), CircleShape)
                    .border(
                        width = if (isSel) 2.5.dp else 1.dp,
                        color = if (isSel) Color(0xFF3B82F6) else Color(0xFF64748B),
                        shape = CircleShape
                    )
                    .clickable { onSelectColor(c) }
            )
        }
    }
}
