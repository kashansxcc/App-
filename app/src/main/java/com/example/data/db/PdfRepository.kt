package com.example.data.db

import com.example.data.model.ElementType
import com.example.data.model.PdfDocumentData
import com.example.data.model.PdfElement
import com.example.data.model.PdfJsonSerializer
import com.example.data.model.PdfPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PdfRepository(private val pdfDao: PdfDao) {

    val allDocuments: Flow<List<PdfDocumentEntity>> = pdfDao.getAllDocuments()

    suspend fun saveDocument(doc: PdfDocumentData) = withContext(Dispatchers.IO) {
        val json = PdfJsonSerializer.serialize(doc)
        val entity = PdfDocumentEntity(
            id = doc.id,
            title = doc.title,
            pageCount = doc.pages.size,
            lastModified = System.currentTimeMillis(),
            jsonContent = json
        )
        pdfDao.insertDocument(entity)
    }

    suspend fun getDocument(id: String): PdfDocumentData? = withContext(Dispatchers.IO) {
        val entity = pdfDao.getDocumentById(id) ?: return@withContext null
        try {
            PdfJsonSerializer.deserialize(entity.jsonContent)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteDocument(id: String) = withContext(Dispatchers.IO) {
        pdfDao.deleteDocumentById(id)
    }

    fun createSampleDocument(): PdfDocumentData {
        val page1Elements = listOf(
            PdfElement(
                id = "elem_header_bar",
                type = ElementType.RECTANGLE,
                x = 40f,
                y = 45f,
                width = 515f,
                height = 5f,
                fillColor = 0xFF2563EB, // Brand Blue
                strokeWidth = 0f
            ),
            PdfElement(
                id = "elem_title",
                type = ElementType.TEXT,
                x = 40f,
                y = 65f,
                width = 515f,
                height = 45f,
                text = "PROJECT SPECIFICATION & CONTRACT",
                fontSize = 22f,
                isBold = true,
                fontFamily = "Serif",
                textColor = 0xFF0F172A,
                textAlign = "left"
            ),
            PdfElement(
                id = "elem_subtitle",
                type = ElementType.TEXT,
                x = 40f,
                y = 115f,
                width = 515f,
                height = 30f,
                text = "Professional Document Design • Desktop View Enabled",
                fontSize = 12f,
                isItalic = true,
                textColor = 0xFF64748B,
                textAlign = "left"
            ),
            PdfElement(
                id = "elem_body_p1",
                type = ElementType.TEXT,
                x = 40f,
                y = 155f,
                width = 515f,
                height = 95f,
                text = "This document is opened in high-fidelity Desktop View mode to preserve exact page layout, margins, and object coordinates without mobile squishing. You can freely edit paragraphs, upload images, adjust fonts & colors, and configure watermarks and interactive forms.",
                fontSize = 13f,
                textColor = 0xFF334155,
                lineSpacingMultiplier = 1.35f,
                textAlign = "justify"
            ),
            PdfElement(
                id = "elem_bullet_box",
                type = ElementType.RECTANGLE,
                x = 40f,
                y = 260f,
                width = 515f,
                height = 100f,
                fillColor = 0xFFF8FAFC,
                strokeColor = 0xFFCBD5E1,
                strokeWidth = 1f,
                cornerRadius = 6f
            ),
            PdfElement(
                id = "elem_bullets_text",
                type = ElementType.TEXT,
                x = 55f,
                y = 272f,
                width = 485f,
                height = 80f,
                text = "• Desktop Viewport: Pan, zoom, and fit-to-width with zero distortion\n• Typography: Custom fonts, colors, bold/italic, alignment & line spacing\n• Object Manipulation: Resize handles, drag & drop, rotate, opacity\n• Interactive Forms: Text fields, checkboxes, dropdowns, and digital signature",
                fontSize = 11.5f,
                textColor = 0xFF1E293B,
                lineSpacingMultiplier = 1.3f
            ),
            PdfElement(
                id = "elem_form_section_header",
                type = ElementType.TEXT,
                x = 40f,
                y = 380f,
                width = 300f,
                height = 25f,
                text = "INTERACTIVE FORM FIELDS",
                fontSize = 14f,
                isBold = true,
                textColor = 0xFF1E3A8A
            ),
            PdfElement(
                id = "elem_form_input1",
                type = ElementType.FORM_TEXT,
                x = 40f,
                y = 415f,
                width = 245f,
                height = 42f,
                fieldLabel = "Full Name",
                placeholder = "Jane Doe",
                fieldValue = "Alex Johnson",
                isRequired = true
            ),
            PdfElement(
                id = "elem_form_input2",
                type = ElementType.FORM_TEXT,
                x = 305f,
                y = 415f,
                width = 250f,
                height = 42f,
                fieldLabel = "Email Address",
                placeholder = "client@example.com",
                fieldValue = "alex.j@enterprise.com",
                isRequired = true
            ),
            PdfElement(
                id = "elem_form_check1",
                type = ElementType.FORM_CHECKBOX,
                x = 40f,
                y = 475f,
                width = 245f,
                height = 36f,
                fieldLabel = "I agree to the terms and specifications",
                isChecked = true
            ),
            PdfElement(
                id = "elem_form_dropdown",
                type = ElementType.FORM_DROPDOWN,
                x = 305f,
                y = 475f,
                width = 250f,
                height = 42f,
                fieldLabel = "Project Priority",
                fieldValue = "High Priority",
                options = listOf("Standard", "High Priority", "Urgent - Critical")
            ),
            PdfElement(
                id = "elem_form_sign",
                type = ElementType.FORM_SIGNATURE,
                x = 40f,
                y = 535f,
                width = 245f,
                height = 75f,
                fieldLabel = "Authorized Signature"
            ),
            PdfElement(
                id = "elem_form_date",
                type = ElementType.FORM_DATE,
                x = 305f,
                y = 535f,
                width = 250f,
                height = 42f,
                fieldLabel = "Date Signed",
                fieldValue = "2026-09-19"
            )
        )

        return PdfDocumentData(
            id = "default_contract_doc",
            title = "Project Specification & Agreement",
            pageSize = "A4",
            isLandscape = false,
            pages = listOf(
                PdfPage(
                    id = "page_1",
                    pageIndex = 0,
                    elements = page1Elements
                )
            ),
            headerText = "ENTERPRISE DOCUMENT SUITE • STRICT CONFIDENTIAL",
            headerAlign = "center",
            headerExcludeFirst = false,
            footerText = "Page {page} of {total} • Generated via Desktop PDF Editor",
            footerAlign = "center",
            footerExcludeFirst = false,
            watermarkEnabled = true,
            watermarkText = "OFFICIAL DRAFT",
            watermarkOpacity = 0.12f,
            watermarkRotation = -40f,
            watermarkColor = 0xFFDC2626
        )
    }
}
