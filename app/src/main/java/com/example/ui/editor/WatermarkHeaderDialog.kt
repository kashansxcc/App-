package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PdfDocumentData

@Composable
fun WatermarkHeaderDialog(
    document: PdfDocumentData,
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSave: (headerText: String, headerAlign: String, headerExcludeFirst: Boolean,
            footerText: String, footerAlign: String, footerExcludeFirst: Boolean,
            watermarkEnabled: Boolean, watermarkText: String, watermarkOpacity: Float,
            watermarkRotation: Float, watermarkColor: Long) -> Unit
) {
    if (!isOpen) return

    var headerText by remember(document) { mutableStateOf(document.headerText) }
    var headerAlign by remember(document) { mutableStateOf(document.headerAlign) }
    var headerExcludeFirst by remember(document) { mutableStateOf(document.headerExcludeFirst) }

    var footerText by remember(document) { mutableStateOf(document.footerText) }
    var footerAlign by remember(document) { mutableStateOf(document.footerAlign) }
    var footerExcludeFirst by remember(document) { mutableStateOf(document.footerExcludeFirst) }

    var watermarkEnabled by remember(document) { mutableStateOf(document.watermarkEnabled) }
    var watermarkText by remember(document) { mutableStateOf(document.watermarkText) }
    var watermarkOpacity by remember(document) { mutableFloatStateOf(document.watermarkOpacity) }
    var watermarkRotation by remember(document) { mutableFloatStateOf(document.watermarkRotation) }
    var watermarkColor by remember(document) { mutableStateOf(document.watermarkColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        headerText, headerAlign, headerExcludeFirst,
                        footerText, footerAlign, footerExcludeFirst,
                        watermarkEnabled, watermarkText, watermarkOpacity,
                        watermarkRotation, watermarkColor
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Apply to Document", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        },
        containerColor = Color(0xFF1E293B),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.BrandingWatermark, contentDescription = null, tint = Color(0xFF60A5FA))
                Text(
                    text = "Headers, Footers & Watermark",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: Header
                Text(
                    text = "DOCUMENT HEADER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF60A5FA),
                    letterSpacing = 1.sp
                )
                OutlinedTextField(
                    value = headerText,
                    onValueChange = { headerText = it },
                    label = { Text("Header Text") },
                    singleLine = true,
                    placeholder = { Text("e.g. COMPANY CONFIDENTIAL") },
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Alignment", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("left" to Icons.Default.FormatAlignLeft, "center" to Icons.Default.FormatAlignCenter, "right" to Icons.Default.FormatAlignRight).forEach { (alignKey, icon) ->
                            val isSel = headerAlign == alignKey
                            Surface(
                                onClick = { headerAlign = alignKey },
                                shape = RoundedCornerShape(4.dp),
                                color = if (isSel) Color(0xFF3B82F6) else Color(0xFF334155),
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Exclude on first page (Cover)", fontSize = 11.sp, color = Color.White)
                    Switch(
                        checked = headerExcludeFirst,
                        onCheckedChange = { headerExcludeFirst = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF3B82F6))
                    )
                }

                // Section 2: Footer
                Text(
                    text = "DOCUMENT FOOTER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF60A5FA),
                    letterSpacing = 1.sp
                )
                OutlinedTextField(
                    value = footerText,
                    onValueChange = { footerText = it },
                    label = { Text("Footer Text ({page}, {total})") },
                    singleLine = true,
                    placeholder = { Text("Page {page} of {total}") },
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Exclude on first page", fontSize = 11.sp, color = Color.White)
                    Switch(
                        checked = footerExcludeFirst,
                        onCheckedChange = { footerExcludeFirst = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF3B82F6))
                    )
                }

                // Section 3: Watermark
                Text(
                    text = "WATERMARK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF60A5FA),
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Watermark", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = watermarkEnabled,
                        onCheckedChange = { watermarkEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF3B82F6))
                    )
                }

                if (watermarkEnabled) {
                    OutlinedTextField(
                        value = watermarkText,
                        onValueChange = { watermarkText = it },
                        label = { Text("Watermark Text") },
                        singleLine = true,
                        colors = dialogTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick presets: DRAFT, CONFIDENTIAL, SAMPLE, COPY
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("DRAFT", "CONFIDENTIAL", "SAMPLE", "COPY").forEach { preset ->
                            FilterChip(
                                selected = watermarkText == preset,
                                onClick = { watermarkText = preset },
                                label = { Text(preset, fontSize = 9.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF3B82F6),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF334155),
                                    labelColor = Color(0xFFCBD5E1)
                                ),
                                modifier = Modifier.height(26.dp)
                            )
                        }
                    }

                    Text("Opacity: ${(watermarkOpacity * 100).toInt()}%", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                    Slider(
                        value = watermarkOpacity,
                        onValueChange = { watermarkOpacity = it },
                        valueRange = 0.05f..0.50f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF3B82F6), activeTrackColor = Color(0xFF3B82F6))
                    )

                    Text("Rotation: ${watermarkRotation.toInt()}°", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                    Slider(
                        value = watermarkRotation,
                        onValueChange = { watermarkRotation = it },
                        valueRange = -90f..90f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF3B82F6), activeTrackColor = Color(0xFF3B82F6))
                    )

                    Text("Color", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                    val watermarkColors = listOf(0xFFDC2626, 0xFF2563EB, 0xFF475569, 0xFF16A34A, 0xFFD97706)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        watermarkColors.forEach { c ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color(c), CircleShape)
                                    .border(
                                        width = if (watermarkColor == c) 2.dp else 1.dp,
                                        color = if (watermarkColor == c) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { watermarkColor = c }
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun dialogTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color(0xFFCBD5E1),
    focusedBorderColor = Color(0xFF3B82F6),
    unfocusedBorderColor = Color(0xFF475569),
    focusedLabelColor = Color(0xFF60A5FA),
    unfocusedLabelColor = Color(0xFF94A3B8),
    focusedContainerColor = Color(0xFF0F172A),
    unfocusedContainerColor = Color(0xFF0F172A)
)
