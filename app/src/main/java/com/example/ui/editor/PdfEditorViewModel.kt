package com.example.ui.editor

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.PdfDatabase
import com.example.data.db.PdfRepository
import com.example.data.export.PdfExporter
import com.example.data.model.ElementType
import com.example.data.model.PdfDocumentData
import com.example.data.model.PdfElement
import com.example.data.model.PdfPage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class RibbonTab {
    FILE,
    INSERT,
    FORMAT,
    PAGES,
    WATERMARK,
    FORMS
}

data class EditorUiState(
    val document: PdfDocumentData = PdfDocumentData(),
    val currentPageIndex: Int = 0,
    val selectedElementId: String? = null,
    val isDesktopView: Boolean = true,
    val isFormFillMode: Boolean = false,
    val activeRibbonTab: RibbonTab = RibbonTab.INSERT,
    val zoomScale: Float = 1.0f,
    val showPageDrawer: Boolean = false,
    val showInspector: Boolean = false,
    val showTextEditDialog: Boolean = false,
    val showSignaturePadDialog: Boolean = false,
    val showWatermarkDialog: Boolean = false,
    val showExportDialog: Boolean = false,
    val autoSaveStatus: String = "Saved",
    val exportedFile: File? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
)

class PdfEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PdfRepository
    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val undoStack = mutableListOf<PdfDocumentData>()
    private val redoStack = mutableListOf<PdfDocumentData>()
    private var autoSaveJob: Job? = null

    init {
        val db = PdfDatabase.getDatabase(application)
        repository = PdfRepository(db.pdfDao())
        loadInitialDocument()
    }

    private fun loadInitialDocument() {
        viewModelScope.launch {
            // Load default document or create initial desktop specimen
            val initial = repository.createSampleDocument()
            pushToUndo(initial)
            _uiState.update { it.copy(document = initial, autoSaveStatus = "Auto-save active") }
            triggerAutoSave()
        }
    }

    private fun pushToUndo(doc: PdfDocumentData) {
        if (undoStack.size > 30) {
            undoStack.removeAt(0)
        }
        undoStack.add(doc)
        redoStack.clear()
        updateUndoRedoAvailability()
    }

    private fun updateUndoRedoAvailability() {
        _uiState.update {
            it.copy(
                canUndo = undoStack.size > 1,
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun undo() {
        if (undoStack.size > 1) {
            val current = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(current)
            val previous = undoStack.last()
            _uiState.update { it.copy(document = previous, selectedElementId = null) }
            updateUndoRedoAvailability()
            triggerAutoSave()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(next)
            _uiState.update { it.copy(document = next, selectedElementId = null) }
            updateUndoRedoAvailability()
            triggerAutoSave()
        }
    }

    private fun triggerAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            _uiState.update { it.copy(autoSaveStatus = "Saving...") }
            delay(800) // Debounce auto-save
            val currentDoc = _uiState.value.document
            repository.saveDocument(currentDoc)
            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            _uiState.update { it.copy(autoSaveStatus = "Auto-saved $timeStr") }
        }
    }

    fun selectTab(tab: RibbonTab) {
        _uiState.update { it.copy(activeRibbonTab = tab) }
    }

    fun setDesktopView(enabled: Boolean) {
        _uiState.update { it.copy(isDesktopView = enabled) }
    }

    fun toggleFormFillMode() {
        _uiState.update {
            val newMode = !it.isFormFillMode
            it.copy(
                isFormFillMode = newMode,
                selectedElementId = if (newMode) null else it.selectedElementId
            )
        }
    }

    fun setZoomScale(scale: Float) {
        val clamped = scale.coerceIn(0.4f, 3.0f)
        _uiState.update { it.copy(zoomScale = clamped) }
    }

    fun zoomIn() {
        setZoomScale(_uiState.value.zoomScale + 0.15f)
    }

    fun zoomOut() {
        setZoomScale(_uiState.value.zoomScale - 0.15f)
    }

    fun resetZoom() {
        setZoomScale(1.0f)
    }

    fun setCurrentPage(index: Int) {
        val validIndex = index.coerceIn(0, (_uiState.value.document.pages.size - 1).coerceAtLeast(0))
        _uiState.update {
            it.copy(
                currentPageIndex = validIndex,
                selectedElementId = null
            )
        }
    }

    fun selectElement(id: String?) {
        _uiState.update {
            it.copy(
                selectedElementId = id,
                // Automatically activate format tab if text or inspector if needed
                activeRibbonTab = if (id != null && it.activeRibbonTab == RibbonTab.FILE) RibbonTab.FORMAT else it.activeRibbonTab
            )
        }
    }

    val selectedElement: PdfElement?
        get() {
            val state = _uiState.value
            val page = state.document.pages.getOrNull(state.currentPageIndex) ?: return null
            return page.elements.find { it.id == state.selectedElementId }
        }

    fun updateSelectedElement(transform: (PdfElement) -> PdfElement) {
        val id = _uiState.value.selectedElementId ?: return
        updateElementById(id, transform)
    }

    fun updateElementById(id: String, transform: (PdfElement) -> PdfElement) {
        val currentDoc = _uiState.value.document
        val pageIdx = _uiState.value.currentPageIndex
        val pages = currentDoc.pages.toMutableList()
        val targetPage = pages.getOrNull(pageIdx) ?: return

        val updatedElements = targetPage.elements.map { elem ->
            if (elem.id == id) transform(elem) else elem
        }

        val updatedPage = targetPage.copy(elements = updatedElements)
        pages[pageIdx] = updatedPage
        val newDoc = currentDoc.copy(pages = pages, lastModified = System.currentTimeMillis())

        pushToUndo(newDoc)
        _uiState.update { it.copy(document = newDoc) }
        triggerAutoSave()
    }

    fun addElementToCurrentPage(element: PdfElement) {
        val currentDoc = _uiState.value.document
        val pageIdx = _uiState.value.currentPageIndex
        val pages = currentDoc.pages.toMutableList()
        val targetPage = pages.getOrNull(pageIdx) ?: return

        val nextZ = (targetPage.elements.maxOfOrNull { it.zIndex } ?: 0) + 1
        val newElem = element.copy(zIndex = nextZ)
        val updatedElements = targetPage.elements + newElem

        pages[pageIdx] = targetPage.copy(elements = updatedElements)
        val newDoc = currentDoc.copy(pages = pages, lastModified = System.currentTimeMillis())

        pushToUndo(newDoc)
        _uiState.update {
            it.copy(
                document = newDoc,
                selectedElementId = newElem.id,
                activeRibbonTab = if (newElem.type == ElementType.TEXT) RibbonTab.FORMAT else it.activeRibbonTab
            )
        }
        triggerAutoSave()
    }

    fun deleteSelectedElement() {
        val id = _uiState.value.selectedElementId ?: return
        val currentDoc = _uiState.value.document
        val pageIdx = _uiState.value.currentPageIndex
        val pages = currentDoc.pages.toMutableList()
        val targetPage = pages.getOrNull(pageIdx) ?: return

        val updatedElements = targetPage.elements.filterNot { it.id == id }
        pages[pageIdx] = targetPage.copy(elements = updatedElements)
        val newDoc = currentDoc.copy(pages = pages, lastModified = System.currentTimeMillis())

        pushToUndo(newDoc)
        _uiState.update { it.copy(document = newDoc, selectedElementId = null) }
        triggerAutoSave()
    }

    fun duplicateSelectedElement() {
        val selected = selectedElement ?: return
        val duplicated = selected.copy(
            id = UUID.randomUUID().toString(),
            x = selected.x + 20f,
            y = selected.y + 20f,
            zIndex = selected.zIndex + 1
        )
        addElementToCurrentPage(duplicated)
    }

    fun bringForward() {
        updateSelectedElement { it.copy(zIndex = it.zIndex + 1) }
    }

    fun sendBackward() {
        updateSelectedElement { it.copy(zIndex = (it.zIndex - 1).coerceAtLeast(0)) }
    }

    // Quick insertion shortcuts
    fun insertTextBox() {
        addElementToCurrentPage(
            PdfElement(
                type = ElementType.TEXT,
                x = 60f,
                y = 120f,
                width = 240f,
                height = 50f,
                text = "New text box. Tap to edit.",
                fontSize = 14f,
                textColor = 0xFF1E293B
            )
        )
    }

    fun insertHeading() {
        addElementToCurrentPage(
            PdfElement(
                type = ElementType.TEXT,
                x = 60f,
                y = 100f,
                width = 380f,
                height = 45f,
                text = "DOCUMENT SECTION HEADING",
                fontSize = 20f,
                isBold = true,
                textColor = 0xFF0F172A
            )
        )
    }

    fun insertImage(uri: Uri) {
        addElementToCurrentPage(
            PdfElement(
                type = ElementType.IMAGE,
                imageUri = uri.toString(),
                x = 80f,
                y = 160f,
                width = 180f,
                height = 140f
            )
        )
    }

    fun insertShape(type: ElementType) {
        val w = if (type == ElementType.LINE) 300f else 140f
        val h = if (type == ElementType.LINE) 4f else 90f
        addElementToCurrentPage(
            PdfElement(
                type = type,
                x = 80f,
                y = 180f,
                width = w,
                height = h,
                fillColor = 0xFFE2E8F0,
                strokeColor = 0xFF3B82F6,
                strokeWidth = 2f
            )
        )
    }

    fun insertStamp(preset: String) {
        val (text, color) = when (preset) {
            "APPROVED" -> "APPROVED" to 0xFF16A34AL // Green
            "CONFIDENTIAL" -> "CONFIDENTIAL" to 0xFFDC2626L // Red
            "PAID" -> "PAID" to 0xFF2563EBL // Blue
            else -> "OFFICIAL" to 0xFFD97706L // Amber
        }

        addElementToCurrentPage(
            PdfElement(
                type = ElementType.RECTANGLE,
                x = 100f,
                y = 150f,
                width = 150f,
                height = 50f,
                fillColor = 0x15000000L or (color and 0x00FFFFFFL),
                strokeColor = color,
                strokeWidth = 2.5f,
                cornerRadius = 6f
            )
        )
        addElementToCurrentPage(
            PdfElement(
                type = ElementType.TEXT,
                x = 105f,
                y = 160f,
                width = 140f,
                height = 30f,
                text = text,
                fontSize = 16f,
                isBold = true,
                textColor = color,
                textAlign = "center"
            )
        )
    }

    fun insertFormField(type: ElementType) {
        when (type) {
            ElementType.FORM_TEXT -> {
                addElementToCurrentPage(
                    PdfElement(
                        type = ElementType.FORM_TEXT,
                        x = 60f,
                        y = 180f,
                        width = 220f,
                        height = 40f,
                        fieldLabel = "Text Field",
                        placeholder = "Type response here..."
                    )
                )
            }
            ElementType.FORM_CHECKBOX -> {
                addElementToCurrentPage(
                    PdfElement(
                        type = ElementType.FORM_CHECKBOX,
                        x = 60f,
                        y = 180f,
                        width = 200f,
                        height = 34f,
                        fieldLabel = "I agree / Confirm",
                        isChecked = false
                    )
                )
            }
            ElementType.FORM_RADIO -> {
                addElementToCurrentPage(
                    PdfElement(
                        type = ElementType.FORM_RADIO,
                        x = 60f,
                        y = 180f,
                        width = 180f,
                        height = 30f,
                        fieldLabel = "Select Choice",
                        isChecked = false
                    )
                )
            }
            ElementType.FORM_DROPDOWN -> {
                addElementToCurrentPage(
                    PdfElement(
                        type = ElementType.FORM_DROPDOWN,
                        x = 60f,
                        y = 180f,
                        width = 220f,
                        height = 40f,
                        fieldLabel = "Dropdown Choice",
                        fieldValue = "Option 1",
                        options = listOf("Option 1", "Option 2", "Option 3")
                    )
                )
            }
            ElementType.FORM_SIGNATURE -> {
                addElementToCurrentPage(
                    PdfElement(
                        type = ElementType.FORM_SIGNATURE,
                        x = 60f,
                        y = 180f,
                        width = 240f,
                        height = 75f,
                        fieldLabel = "Signature"
                    )
                )
            }
            ElementType.FORM_DATE -> {
                addElementToCurrentPage(
                    PdfElement(
                        type = ElementType.FORM_DATE,
                        x = 60f,
                        y = 180f,
                        width = 180f,
                        height = 40f,
                        fieldLabel = "Date",
                        fieldValue = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    )
                )
            }
            else -> {}
        }
    }

    // Page operations
    fun addPage() {
        val currentDoc = _uiState.value.document
        val newPage = PdfPage(
            id = UUID.randomUUID().toString(),
            pageIndex = currentDoc.pages.size,
            elements = emptyList()
        )
        val newDoc = currentDoc.copy(
            pages = currentDoc.pages + newPage,
            lastModified = System.currentTimeMillis()
        )
        pushToUndo(newDoc)
        _uiState.update {
            it.copy(
                document = newDoc,
                currentPageIndex = newDoc.pages.lastIndex,
                selectedElementId = null
            )
        }
        triggerAutoSave()
    }

    fun duplicateCurrentPage() {
        val currentDoc = _uiState.value.document
        val currentIdx = _uiState.value.currentPageIndex
        val sourcePage = currentDoc.pages.getOrNull(currentIdx) ?: return

        val clonedElements = sourcePage.elements.map {
            it.copy(id = UUID.randomUUID().toString())
        }
        val newPage = PdfPage(
            id = UUID.randomUUID().toString(),
            pageIndex = currentIdx + 1,
            elements = clonedElements
        )
        val newPages = currentDoc.pages.toMutableList().apply {
            add(currentIdx + 1, newPage)
        }
        val reindexed = newPages.mapIndexed { idx, p -> p.copy(pageIndex = idx) }
        val newDoc = currentDoc.copy(pages = reindexed, lastModified = System.currentTimeMillis())

        pushToUndo(newDoc)
        _uiState.update {
            it.copy(
                document = newDoc,
                currentPageIndex = currentIdx + 1,
                selectedElementId = null
            )
        }
        triggerAutoSave()
    }

    fun deleteCurrentPage() {
        val currentDoc = _uiState.value.document
        if (currentDoc.pages.size <= 1) return // Keep at least one page

        val currentIdx = _uiState.value.currentPageIndex
        val newPages = currentDoc.pages.toMutableList().apply {
            removeAt(currentIdx)
        }
        val reindexed = newPages.mapIndexed { idx, p -> p.copy(pageIndex = idx) }
        val newDoc = currentDoc.copy(pages = reindexed, lastModified = System.currentTimeMillis())

        val newPageIdx = currentIdx.coerceAtMost(newDoc.pages.lastIndex)
        pushToUndo(newDoc)
        _uiState.update {
            it.copy(
                document = newDoc,
                currentPageIndex = newPageIdx,
                selectedElementId = null
            )
        }
        triggerAutoSave()
    }

    fun movePage(from: Int, to: Int) {
        val currentDoc = _uiState.value.document
        if (from !in currentDoc.pages.indices || to !in currentDoc.pages.indices || from == to) return

        val newPages = currentDoc.pages.toMutableList()
        val moved = newPages.removeAt(from)
        newPages.add(to, moved)
        val reindexed = newPages.mapIndexed { idx, p -> p.copy(pageIndex = idx) }
        val newDoc = currentDoc.copy(pages = reindexed, lastModified = System.currentTimeMillis())

        pushToUndo(newDoc)
        _uiState.update {
            it.copy(
                document = newDoc,
                currentPageIndex = to
            )
        }
        triggerAutoSave()
    }

    fun updateDocumentSettings(transform: (PdfDocumentData) -> PdfDocumentData) {
        val currentDoc = _uiState.value.document
        val newDoc = transform(currentDoc).copy(lastModified = System.currentTimeMillis())
        pushToUndo(newDoc)
        _uiState.update { it.copy(document = newDoc) }
        triggerAutoSave()
    }

    fun updateHeader(text: String, align: String, excludeFirst: Boolean) {
        updateDocumentSettings {
            it.copy(
                headerText = text,
                headerAlign = align,
                headerExcludeFirst = excludeFirst
            )
        }
    }

    fun updateFooter(text: String, align: String, excludeFirst: Boolean) {
        updateDocumentSettings {
            it.copy(
                footerText = text,
                footerAlign = align,
                footerExcludeFirst = excludeFirst
            )
        }
    }

    fun updateWatermark(enabled: Boolean, text: String, opacity: Float, rotation: Float, color: Long) {
        updateDocumentSettings {
            it.copy(
                watermarkEnabled = enabled,
                watermarkText = text,
                watermarkOpacity = opacity,
                watermarkRotation = rotation,
                watermarkColor = color
            )
        }
    }

    fun exportPdf() {
        viewModelScope.launch {
            _uiState.update { it.copy(autoSaveStatus = "Exporting PDF...") }
            try {
                val file = PdfExporter.exportToPdf(getApplication(), _uiState.value.document)
                _uiState.update {
                    it.copy(
                        exportedFile = file,
                        showExportDialog = true,
                        autoSaveStatus = "Export complete"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(autoSaveStatus = "Export failed: ${e.message}") }
            }
        }
    }

    fun shareExportedPdf() {
        val file = _uiState.value.exportedFile ?: return
        PdfExporter.sharePdf(getApplication(), file)
    }

    fun togglePageDrawer() {
        _uiState.update { it.copy(showPageDrawer = !it.showPageDrawer) }
    }

    fun toggleInspector() {
        _uiState.update { it.copy(showInspector = !it.showInspector) }
    }

    fun setWatermarkDialog(visible: Boolean) {
        _uiState.update { it.copy(showWatermarkDialog = visible) }
    }

    fun setExportDialog(visible: Boolean) {
        _uiState.update { it.copy(showExportDialog = visible) }
    }

    fun setTextEditDialog(visible: Boolean) {
        _uiState.update { it.copy(showTextEditDialog = visible) }
    }

    fun setSignaturePadDialog(visible: Boolean) {
        _uiState.update { it.copy(showSignaturePadDialog = visible) }
    }

    fun saveDrawnSignature(points: String) {
        val selected = selectedElement
        if (selected != null && selected.type == ElementType.FORM_SIGNATURE) {
            updateSelectedElement { it.copy(signaturePoints = points) }
        } else {
            // Add new signature element
            addElementToCurrentPage(
                PdfElement(
                    type = ElementType.FORM_SIGNATURE,
                    x = 60f,
                    y = 200f,
                    width = 240f,
                    height = 80f,
                    fieldLabel = "Digital Signature",
                    signaturePoints = points
                )
            )
        }
        setSignaturePadDialog(false)
    }

    fun createNewDocument(title: String, pageSize: String = "A4", isLandscape: Boolean = false) {
        val newDoc = PdfDocumentData(
            id = UUID.randomUUID().toString(),
            title = title,
            pageSize = pageSize,
            isLandscape = isLandscape,
            pages = listOf(PdfPage(pageIndex = 0))
        )
        pushToUndo(newDoc)
        _uiState.update {
            it.copy(
                document = newDoc,
                currentPageIndex = 0,
                selectedElementId = null
            )
        }
        triggerAutoSave()
    }
}
