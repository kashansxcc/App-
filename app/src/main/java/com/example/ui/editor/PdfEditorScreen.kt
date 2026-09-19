package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ElementType

@Composable
fun PdfEditorScreen(
    viewModel: PdfEditorViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0F172A), // Desktop dark slate canvas environment
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Desktop Ribbon Toolbar (Tabs, Actions, Auto-save status, Undo/Redo)
            DesktopRibbonBar(
                uiState = uiState,
                selectedElement = viewModel.selectedElement,
                onSelectTab = { viewModel.selectTab(it) },
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onTogglePageDrawer = { viewModel.togglePageDrawer() },
                onToggleInspector = { viewModel.toggleInspector() },
                onToggleFormFillMode = { viewModel.toggleFormFillMode() },
                onExportPdf = { viewModel.exportPdf() },
                onInsertTextBox = { viewModel.insertTextBox() },
                onInsertHeading = { viewModel.insertHeading() },
                onInsertImage = { uri -> viewModel.insertImage(uri) },
                onInsertShape = { shapeType -> viewModel.insertShape(shapeType) },
                onInsertStamp = { preset -> viewModel.insertStamp(preset) },
                onInsertFormField = { formType -> viewModel.insertFormField(formType) },
                onUpdateSelectedElement = { transform -> viewModel.updateSelectedElement(transform) },
                onAddPage = { viewModel.addPage() },
                onDuplicatePage = { viewModel.duplicateCurrentPage() },
                onDeletePage = { viewModel.deleteCurrentPage() },
                onOpenWatermarkDialog = { viewModel.setWatermarkDialog(true) },
                onOpenTextEditDialog = { viewModel.setTextEditDialog(true) }
            )

            // 2. Main Desktop Stage with Left/Right Drawers and Center Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Center Desktop Canvas Viewport (Pannable & Zoomable WYSIWYG)
                DesktopCanvasView(
                    document = uiState.document,
                    currentPageIndex = uiState.currentPageIndex,
                    selectedElementId = uiState.selectedElementId,
                    zoomScale = uiState.zoomScale,
                    isFormFillMode = uiState.isFormFillMode,
                    onSelectElement = { id -> viewModel.selectElement(id) },
                    onUpdateElement = { id, transform -> viewModel.updateElementById(id, transform) },
                    onDeleteElement = { viewModel.deleteSelectedElement() },
                    onDuplicateElement = { viewModel.duplicateSelectedElement() },
                    onBringForward = { viewModel.bringForward() },
                    onEditText = { viewModel.setTextEditDialog(true) },
                    onOpenSignaturePad = { viewModel.setSignaturePadDialog(true) },
                    onZoomChange = { scale -> viewModel.setZoomScale(scale) },
                    modifier = Modifier.fillMaxSize()
                )

                // Left Slide-over: Page Thumbnails Drawer
                PageThumbnailsDrawer(
                    document = uiState.document,
                    currentPageIndex = uiState.currentPageIndex,
                    isOpen = uiState.showPageDrawer,
                    onClose = { viewModel.togglePageDrawer() },
                    onSelectPage = { idx -> viewModel.setCurrentPage(idx) },
                    onAddPage = { viewModel.addPage() },
                    onDuplicatePage = { viewModel.duplicateCurrentPage() },
                    onDeletePage = { viewModel.deleteCurrentPage() },
                    onMovePage = { from, to -> viewModel.movePage(from, to) },
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                // Right Slide-over: Element Properties Inspector
                InspectorPanel(
                    selectedElement = viewModel.selectedElement,
                    isOpen = uiState.showInspector,
                    onClose = { viewModel.toggleInspector() },
                    onUpdateElement = { transform -> viewModel.updateSelectedElement(transform) },
                    onBringForward = { viewModel.bringForward() },
                    onSendBackward = { viewModel.sendBackward() },
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }

        // Dialogs
        WatermarkHeaderDialog(
            document = uiState.document,
            isOpen = uiState.showWatermarkDialog,
            onDismiss = { viewModel.setWatermarkDialog(false) },
            onSave = { hText, hAlign, hEx, fText, fAlign, fEx, wEn, wText, wOp, wRot, wCol ->
                viewModel.updateHeader(hText, hAlign, hEx)
                viewModel.updateFooter(fText, fAlign, fEx)
                viewModel.updateWatermark(wEn, wText, wOp, wRot, wCol)
            }
        )

        SignaturePadDialog(
            isOpen = uiState.showSignaturePadDialog,
            onDismiss = { viewModel.setSignaturePadDialog(false) },
            onSaveSignature = { points -> viewModel.saveDrawnSignature(points) }
        )

        TextEditDialog(
            element = viewModel.selectedElement,
            isOpen = uiState.showTextEditDialog,
            onDismiss = { viewModel.setTextEditDialog(false) },
            onSaveText = { newText ->
                viewModel.updateSelectedElement {
                    if (it.type == ElementType.FORM_TEXT) it.copy(fieldValue = newText)
                    else it.copy(text = newText)
                }
            }
        )

        ExportCompleteDialog(
            file = uiState.exportedFile,
            isOpen = uiState.showExportDialog,
            pageCount = uiState.document.pages.size,
            onDismiss = { viewModel.setExportDialog(false) },
            onShare = { viewModel.shareExportedPdf() }
        )
    }
}
