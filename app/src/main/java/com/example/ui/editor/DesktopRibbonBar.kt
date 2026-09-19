package com.example.ui.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DynamicForm
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatLineSpacing
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.ViewSidebar
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ElementType
import com.example.data.model.PdfElement

@Composable
fun DesktopRibbonBar(
    uiState: EditorUiState,
    selectedElement: PdfElement?,
    onSelectTab: (RibbonTab) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onTogglePageDrawer: () -> Unit,
    onToggleInspector: () -> Unit,
    onToggleFormFillMode: () -> Unit,
    onExportPdf: () -> Unit,
    onInsertTextBox: () -> Unit,
    onInsertHeading: () -> Unit,
    onInsertImage: (Uri) -> Unit,
    onInsertShape: (ElementType) -> Unit,
    onInsertStamp: (String) -> Unit,
    onInsertFormField: (ElementType) -> Unit,
    onUpdateSelectedElement: ((PdfElement) -> PdfElement) -> Unit,
    onAddPage: () -> Unit,
    onDuplicatePage: () -> Unit,
    onDeletePage: () -> Unit,
    onOpenWatermarkDialog: () -> Unit,
    onOpenTextEditDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onInsertImage(uri)
            }
        }
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF1E293B), // Premium dark slate ribbon header
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Row 1: Document Title, Status, Quick Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onTogglePageDrawer,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (uiState.showPageDrawer) Color(0xFF3B82F6) else Color(0xFF334155),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Default.ViewSidebar,
                            contentDescription = "Page Thumbnails",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = uiState.document.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF22C55E), CircleShape)
                            )
                            Text(
                                text = uiState.autoSaveStatus,
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                // Center/Right: Undo, Redo, Form Fill Toggle, Export
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onUndo,
                        enabled = uiState.canUndo,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Undo,
                            contentDescription = "Undo",
                            tint = if (uiState.canUndo) Color.White else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onRedo,
                        enabled = uiState.canRedo,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Redo,
                            contentDescription = "Redo",
                            tint = if (uiState.canRedo) Color.White else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Form Mode Pill (Design vs Fill Mode)
                    FilterChip(
                        selected = uiState.isFormFillMode,
                        onClick = onToggleFormFillMode,
                        label = {
                            Text(
                                text = if (uiState.isFormFillMode) "✍ Fill Form" else "📐 Design",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF3B82F6),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF334155),
                            labelColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.height(30.dp)
                    )

                    // Export Button
                    Button(
                        onClick = onExportPdf,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Export PDF",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = onToggleInspector,
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (uiState.showInspector) Color(0xFF3B82F6) else Color(0xFF334155),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Inspector Settings",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Row 2: Desktop Ribbon Tabs (Insert, Format & Style, Pages, Watermark, Forms)
            ScrollableTabRow(
                selectedTabIndex = uiState.activeRibbonTab.ordinal,
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White,
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[uiState.activeRibbonTab.ordinal]),
                        color = Color(0xFF3B82F6),
                        height = 2.dp
                    )
                },
                modifier = Modifier.height(34.dp)
            ) {
                RibbonTab.values().forEach { tab ->
                    val tabTitle = when (tab) {
                        RibbonTab.FILE -> "File"
                        RibbonTab.INSERT -> "Insert"
                        RibbonTab.FORMAT -> "Format & Style"
                        RibbonTab.PAGES -> "Pages"
                        RibbonTab.WATERMARK -> "Watermark & Headers"
                        RibbonTab.FORMS -> "Forms"
                    }
                    Tab(
                        selected = uiState.activeRibbonTab == tab,
                        onClick = { onSelectTab(tab) },
                        text = {
                            Text(
                                text = tabTitle,
                                fontSize = 11.sp,
                                fontWeight = if (uiState.activeRibbonTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Row 3: Ribbon Action Tool Bar Content
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1E293B)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    when (uiState.activeRibbonTab) {
                        RibbonTab.FILE -> {
                            RibbonButton(
                                icon = Icons.Default.Description,
                                label = "Page: ${uiState.document.pageSize}",
                                onClick = {}
                            )
                            RibbonButton(
                                icon = Icons.Default.Download,
                                label = "Export PDF",
                                onClick = onExportPdf
                            )
                        }

                        RibbonTab.INSERT -> {
                            RibbonButton(
                                icon = Icons.Default.TextFields,
                                label = "Text Box",
                                onClick = onInsertTextBox
                            )
                            RibbonButton(
                                icon = Icons.Default.Title,
                                label = "Heading",
                                onClick = onInsertHeading
                            )
                            RibbonButton(
                                icon = Icons.Default.AddPhotoAlternate,
                                label = "Upload Image",
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                            RibbonButton(
                                icon = Icons.Default.CheckBox,
                                label = "Rectangle",
                                onClick = { onInsertShape(ElementType.RECTANGLE) }
                            )
                            RibbonButton(
                                icon = Icons.Default.RadioButtonChecked,
                                label = "Circle",
                                onClick = { onInsertShape(ElementType.CIRCLE) }
                            )
                            RibbonButton(
                                icon = Icons.Default.HorizontalRule,
                                label = "Divider Line",
                                onClick = { onInsertShape(ElementType.LINE) }
                            )
                            RibbonButton(
                                icon = Icons.Default.AutoAwesome,
                                label = "Stamp: APPROVED",
                                onClick = { onInsertStamp("APPROVED") }
                            )
                            RibbonButton(
                                icon = Icons.Default.AutoAwesome,
                                label = "Stamp: CONFIDENTIAL",
                                onClick = { onInsertStamp("CONFIDENTIAL") }
                            )
                        }

                        RibbonTab.FORMAT -> {
                            FormatRibbonContent(
                                selectedElement = selectedElement,
                                onUpdateSelectedElement = onUpdateSelectedElement,
                                onOpenTextEditDialog = onOpenTextEditDialog
                            )
                        }

                        RibbonTab.PAGES -> {
                            RibbonButton(
                                icon = Icons.Default.Add,
                                label = "Add Page",
                                onClick = onAddPage
                            )
                            RibbonButton(
                                icon = Icons.Default.ContentCopy,
                                label = "Duplicate Page",
                                onClick = onDuplicatePage
                            )
                            RibbonButton(
                                icon = Icons.Default.Delete,
                                label = "Delete Page",
                                onClick = onDeletePage,
                                tint = Color(0xFFFCA5A5)
                            )
                        }

                        RibbonTab.WATERMARK -> {
                            RibbonButton(
                                icon = Icons.Default.BrandingWatermark,
                                label = "Configure Watermark",
                                onClick = onOpenWatermarkDialog
                            )
                            RibbonButton(
                                icon = Icons.Default.Title,
                                label = "Header & Footer",
                                onClick = onOpenWatermarkDialog
                            )
                        }

                        RibbonTab.FORMS -> {
                            RibbonButton(
                                icon = Icons.Default.TextFields,
                                label = "+ Text Field",
                                onClick = { onInsertFormField(ElementType.FORM_TEXT) }
                            )
                            RibbonButton(
                                icon = Icons.Default.CheckBox,
                                label = "+ Checkbox",
                                onClick = { onInsertFormField(ElementType.FORM_CHECKBOX) }
                            )
                            RibbonButton(
                                icon = Icons.Default.RadioButtonChecked,
                                label = "+ Radio",
                                onClick = { onInsertFormField(ElementType.FORM_RADIO) }
                            )
                            RibbonButton(
                                icon = Icons.Default.DynamicForm,
                                label = "+ Dropdown",
                                onClick = { onInsertFormField(ElementType.FORM_DROPDOWN) }
                            )
                            RibbonButton(
                                icon = Icons.Default.Edit,
                                label = "+ Signature",
                                onClick = { onInsertFormField(ElementType.FORM_SIGNATURE) }
                            )
                            RibbonButton(
                                icon = Icons.Default.LinearScale,
                                label = "+ Date Picker",
                                onClick = { onInsertFormField(ElementType.FORM_DATE) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormatRibbonContent(
    selectedElement: PdfElement?,
    onUpdateSelectedElement: ((PdfElement) -> PdfElement) -> Unit,
    onOpenTextEditDialog: () -> Unit
) {
    if (selectedElement == null) {
        Text(
            text = "Select any text or object on the canvas to format its typography, fonts & colors.",
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(vertical = 4.dp)
        )
        return
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (selectedElement.type == ElementType.TEXT) {
            // Edit text button
            FilledTonalButton(
                onClick = onOpenTextEditDialog,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White
                ),
                modifier = Modifier.height(30.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Edit Content", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            // Bold
            RibbonToggleIcon(
                icon = Icons.Default.FormatBold,
                active = selectedElement.isBold,
                onClick = { onUpdateSelectedElement { it.copy(isBold = !it.isBold) } }
            )

            // Italic
            RibbonToggleIcon(
                icon = Icons.Default.FormatItalic,
                active = selectedElement.isItalic,
                onClick = { onUpdateSelectedElement { it.copy(isItalic = !it.isItalic) } }
            )

            // Underline
            RibbonToggleIcon(
                icon = Icons.Default.FormatUnderlined,
                active = selectedElement.isUnderline,
                onClick = { onUpdateSelectedElement { it.copy(isUnderline = !it.isUnderline) } }
            )

            // Strikethrough
            RibbonToggleIcon(
                icon = Icons.Default.FormatStrikethrough,
                active = selectedElement.isStrikethrough,
                onClick = { onUpdateSelectedElement { it.copy(isStrikethrough = !it.isStrikethrough) } }
            )

            // Align Left
            RibbonToggleIcon(
                icon = Icons.Default.FormatAlignLeft,
                active = selectedElement.textAlign == "left",
                onClick = { onUpdateSelectedElement { it.copy(textAlign = "left") } }
            )

            // Align Center
            RibbonToggleIcon(
                icon = Icons.Default.FormatAlignCenter,
                active = selectedElement.textAlign == "center",
                onClick = { onUpdateSelectedElement { it.copy(textAlign = "center") } }
            )

            // Align Right
            RibbonToggleIcon(
                icon = Icons.Default.FormatAlignRight,
                active = selectedElement.textAlign == "right",
                onClick = { onUpdateSelectedElement { it.copy(textAlign = "right") } }
            )

            // Font size controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFF334155), RoundedCornerShape(6.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                IconButton(
                    onClick = {
                        onUpdateSelectedElement { it.copy(fontSize = (it.fontSize - 2f).coerceAtLeast(8f)) }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Text("-", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Text(
                    text = "${selectedElement.fontSize.toInt()}pt",
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                IconButton(
                    onClick = {
                        onUpdateSelectedElement { it.copy(fontSize = (it.fontSize + 2f).coerceAtMost(72f)) }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Text("+", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Quick Color Palette for Text
            val textColors = listOf(
                0xFF0F172A, // Black / Slate
                0xFF2563EB, // Blue
                0xFFDC2626, // Red
                0xFF16A34A, // Green
                0xFF7C3AED, // Purple
                0xFFD97706  // Amber
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                textColors.forEach { c ->
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(Color(c), CircleShape)
                            .border(
                                width = if (selectedElement.textColor == c) 2.dp else 1.dp,
                                color = if (selectedElement.textColor == c) Color.White else Color(0xFF64748B),
                                shape = CircleShape
                            )
                            .clickable {
                                onUpdateSelectedElement { it.copy(textColor = c) }
                            }
                    )
                }
            }
        } else {
            // Shape or Image or Form element
            Text(
                text = "Object: ${selectedElement.type.name} (W:${selectedElement.width.toInt()}pt, H:${selectedElement.height.toInt()}pt)",
                fontSize = 11.sp,
                color = Color(0xFFE2E8F0)
            )
            // Color options for shapes
            if (selectedElement.type in listOf(ElementType.RECTANGLE, ElementType.CIRCLE)) {
                val shapeColors = listOf(0xFFE2E8F0, 0xFFBFDBFE, 0xFFBBF7D0, 0xFFFED7AA, 0xFFDDD6FE)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    shapeColors.forEach { c ->
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .background(Color(c), CircleShape)
                                .clickable {
                                    onUpdateSelectedElement { it.copy(fillColor = c) }
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RibbonButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = Color.White
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFF334155),
        modifier = Modifier.height(30.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(14.dp))
            Text(label, fontSize = 11.sp, color = tint, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun RibbonToggleIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = if (active) Color(0xFF2563EB) else Color(0xFF334155),
        modifier = Modifier.size(30.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (active) Color.White else Color(0xFFCBD5E1),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}
