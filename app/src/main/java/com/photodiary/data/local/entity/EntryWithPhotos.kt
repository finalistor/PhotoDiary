package com.photodiary.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class EntryWithPhotos(
    @Embedded val entry: DiaryEntryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "entry_id"
    )
    val photos: List<PhotoEntity>
)
