package com.photodiary.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Photo(
    val id: Long = 0,
    val entryId: Long,
    val fileName: String,
    val filePath: String,
    val sortOrder: Int,
    val createdAt: Long
)
