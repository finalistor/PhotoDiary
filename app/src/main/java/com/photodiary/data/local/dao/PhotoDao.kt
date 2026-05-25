package com.photodiary.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.photodiary.data.local.entity.PhotoEntity

@Dao
interface PhotoDao {

    @Insert
    suspend fun insertPhoto(photo: PhotoEntity): Long

    @Insert
    suspend fun insertPhotos(photos: List<PhotoEntity>)

    @Query("DELETE FROM photos WHERE id = :photoId")
    suspend fun deletePhoto(photoId: Long)

    @Query("SELECT * FROM photos WHERE entry_id = :entryId ORDER BY sort_order")
    suspend fun getPhotosForEntry(entryId: Long): List<PhotoEntity>

    @Query("DELETE FROM photos WHERE entry_id = :entryId")
    suspend fun deletePhotosForEntry(entryId: Long)
}
