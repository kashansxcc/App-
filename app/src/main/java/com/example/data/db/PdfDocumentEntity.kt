package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pdf_documents")
data class PdfDocumentEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val pageCount: Int,
    val lastModified: Long,
    val jsonContent: String
)
