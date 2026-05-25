package com.photodiary.di

import android.content.Context
import androidx.room.Room
import com.photodiary.data.local.AppDatabase
import com.photodiary.data.local.UserPreferences
import com.photodiary.data.repository.DiaryRepositoryImpl
import com.photodiary.domain.repository.DiaryRepository

class AppContainer(private val context: Context) {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "photo_diary.db"
        ).addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4)
            .build()
    }

    val repository: DiaryRepository by lazy {
        DiaryRepositoryImpl(
            diaryEntryDao = database.diaryEntryDao(),
            photoDao = database.photoDao(),
            context = context.applicationContext
        )
    }

    val userPreferences: UserPreferences by lazy {
        UserPreferences(context.applicationContext)
    }
}
