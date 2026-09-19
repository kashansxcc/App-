package com.example.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PdfElement

@Composable
fun TextEditDialog(
    element: PdfElement?,
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSaveText: (String) -> Unit
) {
    if (!isOpen || element == null) return

    var currentText by remember(element) {
        mutableStateOf(
            if (element.type == com.example.data.model.ElementType.FORM_TEXT) element.fieldValue else element.text
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onSaveText(currentText)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Update Text", fontWeight = FontWeight.Bold)
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
                Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF60A5FA))
                Text(
                    text = if (element.type == com.example.data.model.ElementType.FORM_TEXT) "Edit Form Value" else "Edit Text & Paragraph",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Formatting helpers: Bullet list, Numbered list
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = false,
                        onClick = {
                            val lines = currentText.split("\n")
                            currentText = lines.joinToString("\n") { line ->
                                if (line.startsWith("• ")) line else "• $line"
                            }
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FormatListBulleted, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Bullet List", fontSize = 10.sp)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFF334155),
                            labelColor = Color.White
                        ),
                        modifier = Modifier.height(28.dp)
                    )

                    FilterChip(
                        selected = false,
                        onClick = {
                            val lines = currentText.split("\n")
                            currentText = lines.mapIndexed { idx, line ->
                                val clean = line.replace(Regex("^\\d+\\.\\s*"), "").replace(Regex("^•\\s*"), "")
                                "${idx + 1}. $clean"
                            }.joinToString("\n")
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FormatListNumbered, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Numbered List", fontSize = 10.sp)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFF334155),
                            labelColor = Color.White
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                }

                OutlinedTextField(
                    value = currentText,
                    onValueChange = { currentText = it },
                    label = { Text("Paragraph Content") },
                    minLines = 4,
                    maxLines = 10,
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
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}
