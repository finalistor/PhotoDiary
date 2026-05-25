package com.photodiary.domain.repository

import android.net.Uri
import com.photodiary.domain.model.DiaryEntry
import kotlinx.coroutines.flow.Flow

interface DiaryRepository {
    fun getAllEntries(): Flow<List<DiaryEntry>>
    fun getEntryWithPhotos(entryId: Long): Flow<DiaryEntry?>
    suspend fun createEntry(
        title: String,
        content: String,
        photoFileNames: List<String>,
        tags: List<String> = emptyList(),
        createdAt: Long = System.currentTimeMillis()
    ): Long
    suspend fun updateEntry(entry: DiaryEntry): Unit
    suspend fun updateEntryWithPhotos(
        entryId: Long,
        title: String,
        content: String,
        createdAt: Long,
        photoFileNames: List<String>,
        tags: List<String> = emptyList()
    )
    fun searchEntries(query: String): Flow<List<DiaryEntry>>
    fun getEntriesByTag(tag: String): Flow<List<DiaryEntry>>
    suspend fun deleteEntry(entryId: Long): Unit
    fun resolvePhotoPath(fileName: String): String
    suspend fun savePhoto(uri: Uri): String
}
