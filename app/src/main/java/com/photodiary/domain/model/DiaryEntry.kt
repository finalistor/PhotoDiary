package com.photodiary.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class DiaryEntry(
    val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val entryDate: Long = createdAt,
    val photos: List<Photo> = emptyList(),
    val tags: List<String> = emptyList()
)
