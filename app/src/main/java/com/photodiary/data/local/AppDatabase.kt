package com.photodiary.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.photodiary.data.local.dao.DiaryEntryDao
import com.photodiary.data.local.dao.PhotoDao
import com.photodiary.data.local.entity.DiaryEntryEntity
import com.photodiary.data.local.entity.PhotoEntity

@Database(
    entities = [DiaryEntryEntity::class, PhotoEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(TagsConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diaryEntryDao(): DiaryEntryDao
    abstract fun photoDao(): PhotoDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE diary_entries ADD COLUMN tags TEXT NOT NULL DEFAULT '[]'"
                )
                db.execSQL(
                    "ALTER TABLE diary_entries ADD COLUMN mood TEXT"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE diary_entries ADD COLUMN moods TEXT NOT NULL DEFAULT '[]'"
                )
                db.execSQL(
                    "UPDATE diary_entries SET moods = '[\"' || mood || '\"]' WHERE mood IS NOT NULL"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE diary_entries_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        content TEXT NOT NULL,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL,
                        tags TEXT NOT NULL DEFAULT '[]'
                    )
                """)
                db.execSQL(
                    "INSERT INTO diary_entries_new (id, title, content, created_at, updated_at, tags) SELECT id, title, content, created_at, updated_at, tags FROM diary_entries"
                )
                db.execSQL("DROP TABLE diary_entries")
                db.execSQL("ALTER TABLE diary_entries_new RENAME TO diary_entries")
            }
        }
    }
}
