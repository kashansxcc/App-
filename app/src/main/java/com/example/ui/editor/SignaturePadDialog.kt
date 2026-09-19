package com.example.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SignaturePadDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSaveSignature: (String) -> Unit
) {
    if (!isOpen) return

    val strokes = remember { mutableStateListOf<List<Offset>>() }
    val currentStroke = remember { mutableStateListOf<Offset>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    // Serialize strokes to string format "x1,y1,x2,y2;x1,y1..."
                    val sb = StringBuilder()
                    strokes.forEachIndexed { i, stroke ->
                        if (stroke.size >= 2) {
                            if (i > 0) sb.append(";")
                            sb.append(stroke.joinToString(",") { "${it.x.toInt()},${it.y.toInt()}" })
                        }
                    }
                    onSaveSignature(sb.toString())
                },
                enabled = strokes.isNotEmpty() || currentStroke.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Insert Signature", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        strokes.clear()
                        currentStroke.clear()
                    }
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        },
        containerColor = Color(0xFF1E293B),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Draw, contentDescription = null, tint = Color(0xFF60A5FA))
                Text(
                    text = "Sign with Finger or Stylus",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Draw your legal signature inside the box below:",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.5.dp, Color(0xFF3B82F6), RoundedCornerShape(8.dp))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentStroke.clear()
                                    currentStroke.add(offset)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    currentStroke.add(change.position)
                                },
                                onDragEnd = {
                                    if (currentStroke.isNotEmpty()) {
                                        strokes.add(currentStroke.toList())
                                        currentStroke.clear()
                                    }
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Draw signature baseline
                        drawLine(
                            color = Color(0xFFE2E8F0),
                            start = Offset(20f, size.height - 40f),
                            end = Offset(size.width - 20f, size.height - 40f),
                            strokeWidth = 2f
                        )

                        // Draw finished strokes
                        val paintColor = Color(0xFF1E3A8A) // Dark blue ink
                        val strokeStyle = Stroke(
                            width = 3.5f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )

                        strokes.forEach { stroke ->
                            if (stroke.size >= 2) {
                                val path = Path().apply {
                                    moveTo(stroke.first().x, stroke.first().y)
                                    for (i in 1 until stroke.size) {
                                        lineTo(stroke[i].x, stroke[i].y)
                                    }
                                }
                                drawPath(path, paintColor, style = strokeStyle)
                            }
                        }

                        // Draw current active stroke
                        if (currentStroke.size >= 2) {
                            val path = Path().apply {
                                moveTo(currentStroke.first().x, currentStroke.first().y)
                                for (i in 1 until currentStroke.size) {
                                    lineTo(currentStroke[i].x, currentStroke[i].y)
                                }
                            }
                            drawPath(path, paintColor, style = strokeStyle)
                        }
                    }

                    if (strokes.isEmpty() && currentStroke.isEmpty()) {
                        Text(
                            text = "✍ Sign on this line",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 24.dp, bottom = 44.dp)
                        )
                    }
                }
            }
        }
    )
}
