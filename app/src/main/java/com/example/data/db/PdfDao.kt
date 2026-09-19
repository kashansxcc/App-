package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PdfDao {
    @Query("SELECT * FROM pdf_documents ORDER BY lastModified DESC")
    fun getAllDocuments(): Flow<List<PdfDocumentEntity>>

    @Query("SELECT * FROM pdf_documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: String): PdfDocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: PdfDocumentEntity)

    @Query("DELETE FROM pdf_documents WHERE id = :id")
    suspend fun deleteDocumentById(id: String)
}
