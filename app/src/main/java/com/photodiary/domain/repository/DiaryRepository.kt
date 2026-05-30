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
        createdAt: Long = System.currentTimeMillis(),
        entryDateMillis: Long = createdAt
    ): Long
    suspend fun updateEntry(entry: DiaryEntry)
    suspend fun updateEntryWithPhotos(
        entryId: Long,
        title: String,
        content: String,
        createdAt: Long,
        photoFileNames: List<String>,
        tags: List<String> = emptyList(),
        entryDateMillis: Long = createdAt
    )
    fun searchEntries(query: String): Flow<List<DiaryEntry>>
    fun getEntriesByTag(tag: String): Flow<List<DiaryEntry>>
    suspend fun deleteEntry(entryId: Long)
    fun resolvePhotoPath(fileName: String): String
    suspend fun savePhoto(uri: Uri): String
    suspend fun entryExistsForDate(date: Long, excludeId: Long = 0): Boolean
    suspend fun getEntryByDate(date: Long): DiaryEntry?
}

class DateConflictException(message: String) : Exception(message)
