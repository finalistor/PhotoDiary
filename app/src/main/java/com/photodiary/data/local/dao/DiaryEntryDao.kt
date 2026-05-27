package com.photodiary.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.photodiary.data.local.entity.DiaryEntryEntity
import com.photodiary.data.local.entity.EntryWithPhotos
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryEntryDao {

    @Transaction
    @Query("SELECT * FROM diary_entries ORDER BY created_at DESC")
    fun getAllEntriesWithPhotos(): Flow<List<EntryWithPhotos>>

    @Transaction
    @Query("SELECT * FROM diary_entries WHERE id = :entryId")
    fun getEntryWithPhotos(entryId: Long): Flow<EntryWithPhotos?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DiaryEntryEntity): Long

    @Update
    suspend fun updateEntry(entry: DiaryEntryEntity)

    @Transaction
    @Query("SELECT * FROM diary_entries WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY created_at DESC")
    fun searchEntries(query: String): Flow<List<EntryWithPhotos>>

    @Transaction
    @Query("SELECT * FROM diary_entries WHERE tags LIKE '%\"' || :tag || '\"%' ORDER BY created_at DESC")
    fun getEntriesByTag(tag: String): Flow<List<EntryWithPhotos>>

    @Query("DELETE FROM diary_entries WHERE id = :entryId")
    suspend fun deleteEntry(entryId: Long)

    @Transaction
    @Query("SELECT * FROM diary_entries WHERE entry_date = :date")
    suspend fun getEntriesWithPhotosByDate(date: Long): List<EntryWithPhotos>

    @Query("SELECT EXISTS(SELECT 1 FROM diary_entries WHERE entry_date = :date AND id != :excludeId)")
    suspend fun entryExistsForDate(date: Long, excludeId: Long = 0): Boolean
}
