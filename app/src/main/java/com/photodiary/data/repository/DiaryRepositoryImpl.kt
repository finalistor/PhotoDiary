package com.photodiary.data.repository

import android.content.Context
import android.net.Uri
import com.photodiary.data.local.dao.DiaryEntryDao
import com.photodiary.data.local.dao.PhotoDao
import com.photodiary.data.local.entity.DiaryEntryEntity
import com.photodiary.data.local.entity.EntryWithPhotos
import com.photodiary.data.local.entity.PhotoEntity
import com.photodiary.domain.model.DiaryEntry
import com.photodiary.domain.model.Photo
import com.photodiary.domain.repository.DateConflictException
import com.photodiary.domain.repository.DiaryRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

class DiaryRepositoryImpl(
    private val diaryEntryDao: DiaryEntryDao,
    private val photoDao: PhotoDao,
    private val context: Context
) : DiaryRepository {

    private val photosDir: File by lazy {
        File(context.filesDir, "photos").also { it.mkdirs() }
    }

    @OptIn(FlowPreview::class)
    override fun getAllEntries(): Flow<List<DiaryEntry>> {
        return diaryEntryDao.getAllEntriesWithPhotos()
            .map { list -> list.map { it.toDomainModel() } }
            .debounce(50)
    }

    @OptIn(FlowPreview::class)
    override fun searchEntries(query: String): Flow<List<DiaryEntry>> {
        return diaryEntryDao.searchEntries(query)
            .map { list -> list.map { it.toDomainModel() } }
            .debounce(50)
    }

    override fun getEntriesByTag(tag: String): Flow<List<DiaryEntry>> {
        return diaryEntryDao.getEntriesByTag(tag).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun getEntryWithPhotos(entryId: Long): Flow<DiaryEntry?> {
        return diaryEntryDao.getEntryWithPhotos(entryId).map { it?.toDomainModel() }
    }

    override suspend fun createEntry(
        title: String,
        content: String,
        photoFileNames: List<String>,
        tags: List<String>,
        createdAt: Long,
        entryDateMillis: Long
    ): Long {
        val entryDate = normalizeToLocalMidnight(entryDateMillis)
        if (diaryEntryDao.entryExistsForDate(entryDate)) {
            throw DateConflictException("该日期已有日记")
        }
        val now = System.currentTimeMillis()
        val entryId = diaryEntryDao.insertEntry(
            DiaryEntryEntity(
                title = title,
                content = content,
                createdAt = createdAt,
                updatedAt = now,
                entryDate = entryDate,
                tags = tags
            )
        )
        if (photoFileNames.isNotEmpty()) {
            photoDao.insertPhotos(
                photoFileNames.mapIndexed { index, fileName ->
                    PhotoEntity(
                        entryId = entryId,
                        fileName = fileName,
                        sortOrder = index,
                        createdAt = now
                    )
                }
            )
        }
        return entryId
    }

    override suspend fun updateEntry(entry: DiaryEntry) {
        val entryDate = if (entry.entryDate > 0) normalizeToLocalMidnight(entry.entryDate)
                        else normalizeToLocalMidnight(entry.createdAt)
        diaryEntryDao.updateEntry(
            DiaryEntryEntity(
                id = entry.id,
                title = entry.title,
                content = entry.content,
                createdAt = entry.createdAt,
                updatedAt = System.currentTimeMillis(),
                entryDate = entryDate,
                tags = entry.tags
            )
        )
    }

    override suspend fun updateEntryWithPhotos(
        entryId: Long,
        title: String,
        content: String,
        createdAt: Long,
        photoFileNames: List<String>,
        tags: List<String>,
        entryDateMillis: Long
    ) {
        val entryDate = normalizeToLocalMidnight(entryDateMillis)
        if (diaryEntryDao.entryExistsForDate(entryDate, excludeId = entryId)) {
            throw DateConflictException("该日期已有日记")
        }
        val oldPhotos = photoDao.getPhotosForEntry(entryId)
        val oldFileNames = oldPhotos.map { it.fileName }.toSet()
        val newFileNames = photoFileNames.toSet()

        oldPhotos.forEach { photo ->
            if (photo.fileName !in newFileNames) {
                val file = File(photosDir, photo.fileName)
                if (file.exists()) file.delete()
                photoDao.deletePhoto(photo.id)
            }
        }

        diaryEntryDao.updateEntry(
            DiaryEntryEntity(
                id = entryId,
                title = title,
                content = content,
                createdAt = createdAt,
                updatedAt = System.currentTimeMillis(),
                entryDate = entryDate,
                tags = tags
            )
        )

        // Only insert photos that are NEW (not in old list)
        val newOnly = photoFileNames.filter { it !in oldFileNames }
        if (newOnly.isNotEmpty()) {
            val now = System.currentTimeMillis()
            photoDao.insertPhotos(
                newOnly.mapIndexed { index, fileName ->
                    PhotoEntity(
                        entryId = entryId,
                        fileName = fileName,
                        sortOrder = index,
                        createdAt = now
                    )
                }
            )
        }
    }

    override suspend fun deleteEntry(entryId: Long) {
        diaryEntryDao.deleteEntry(entryId)
        val photos = photoDao.getPhotosForEntry(entryId)
        photos.forEach { photo ->
            val file = File(photosDir, photo.fileName)
            if (file.exists()) file.delete()
        }
    }

    override fun resolvePhotoPath(fileName: String): String {
        return File(photosDir, fileName).absolutePath
    }

    override suspend fun savePhoto(uri: Uri): String {
        val fileName = "${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg"
        val destFile = File(photosDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return fileName
    }

    private fun EntryWithPhotos.toDomainModel(): DiaryEntry {
        val mappedPhotos = photos.map { p ->
            Photo(
                id = p.id,
                entryId = p.entryId,
                fileName = p.fileName,
                filePath = resolvePhotoPath(p.fileName),
                sortOrder = p.sortOrder,
                createdAt = p.createdAt
            )
        }
        return DiaryEntry(
            id = entry.id,
            title = entry.title,
            content = entry.content,
            createdAt = entry.createdAt,
            updatedAt = entry.updatedAt,
            entryDate = entry.entryDate,
            photos = mappedPhotos,
            tags = entry.tags
        )
    }

    override suspend fun entryExistsForDate(date: Long, excludeId: Long): Boolean {
        return diaryEntryDao.entryExistsForDate(date, excludeId)
    }

    override suspend fun getEntryByDate(date: Long): DiaryEntry? {
        val entryDate = normalizeToLocalMidnight(date)
        return diaryEntryDao.getEntriesWithPhotosByDate(entryDate)
            .firstOrNull()?.toDomainModel()
    }

    private fun normalizeToLocalMidnight(epochMillis: Long): Long {
        val zdt = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault())
        return zdt.toLocalDate().atStartOfDay(zdt.zone).toInstant().toEpochMilli()
    }
}
