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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PdfDocumentData

@Composable
fun PageThumbnailsDrawer(
    document: PdfDocumentData,
    currentPageIndex: Int,
    isOpen: Boolean,
    onClose: () -> Unit,
    onSelectPage: (Int) -> Unit,
    onAddPage: () -> Unit,
    onDuplicatePage: () -> Unit,
    onDeletePage: () -> Unit,
    onMovePage: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally { -it },
        exit = slideOutHorizontally { -it },
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .width(240.dp)
                .fillMaxHeight()
                .shadow(16.dp),
            color = Color(0xFF1E293B), // Dark slate panel
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(12.dp)
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
                            Icons.Default.Layers,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Pages (${document.pages.size})",
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
                            contentDescription = "Close Pages",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Add Page Button
                FilledTonalButton(
                    onClick = onAddPage,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF2563EB),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add New Page", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Page Thumbnails List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(document.pages) { index, page ->
                        val isSelected = index == currentPageIndex

                        ElevatedCard(
                            onClick = { onSelectPage(index) },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF3B82F6) else Color(0xFF334155),
                                    shape = RoundedCornerShape(6.dp)
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .background(if (isSelected) Color(0xFF1E3A8A).copy(alpha = 0.3f) else Color(0xFF0F172A))
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Page ${index + 1}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFF93C5FD) else Color.White
                                    )
                                    Text(
                                        text = "${page.elements.size} items",
                                        fontSize = 10.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Mini desktop page representation
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .background(Color.White, RoundedCornerShape(2.dp))
                                        .border(0.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(2.dp))
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(50.dp)
                                                .height(4.dp)
                                                .background(Color(0xFFCBD5E1), RoundedCornerShape(2.dp))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .width(70.dp)
                                                .height(3.dp)
                                                .background(Color(0xFFE2E8F0), RoundedCornerShape(2.dp))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .width(60.dp)
                                                .height(3.dp)
                                                .background(Color(0xFFE2E8F0), RoundedCornerShape(2.dp))
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { onMovePage(index, (index - 1).coerceAtLeast(0)) },
                                            enabled = index > 0,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ArrowUpward,
                                                contentDescription = "Move Up",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { onMovePage(index, (index + 1).coerceAtMost(document.pages.lastIndex)) },
                                            enabled = index < document.pages.lastIndex,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ArrowDownward,
                                                contentDescription = "Move Down",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = onDuplicatePage,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ContentCopy,
                                                contentDescription = "Duplicate",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = onDeletePage,
                                            enabled = document.pages.size > 1,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = if (document.pages.size > 1) Color(0xFFFCA5A5) else Color(0xFF64748B),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
