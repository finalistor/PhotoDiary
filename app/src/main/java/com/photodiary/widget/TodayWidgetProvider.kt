package com.photodiary.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import com.photodiary.MainActivity
import com.photodiary.R
import com.photodiary.data.local.AppDatabase
import com.photodiary.data.local.entity.EntryWithPhotos
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TodayWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, TodayWidgetProvider::class.java)
            )
            if (ids.isNotEmpty()) {
                val provider = TodayWidgetProvider()
                provider.onUpdate(context, manager, ids)
            }
        }
    }

    private fun updateWidget(context: Context, manager: AppWidgetManager, id: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_today)

        val todayStart = java.time.LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val todayEnd = todayStart + 24 * 60 * 60 * 1000

        val db = getDatabase(context)
        val todayEntries = runBlocking {
            db.diaryEntryDao().getAllEntriesWithPhotos().firstOrNull()?.filter {
                it.entry.createdAt in todayStart until todayEnd
            } ?: emptyList()
        }

        val dateText = todayStart.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy年M月d日"))
        }

        views.setTextViewText(R.id.widget_date, dateText)

        if (todayEntries.isNotEmpty()) {
            val first = todayEntries.first()
            val title = first.entry.title.ifEmpty { first.entry.content.take(30) }
            views.setTextViewText(
                R.id.widget_title,
                if (todayEntries.size > 1) "共${todayEntries.size}篇日记" else title
            )
            views.setViewVisibility(R.id.widget_title, android.view.View.VISIBLE)

            val photoBitmap = loadThumbnail(context, first)
            if (photoBitmap != null) {
                views.setImageViewBitmap(R.id.widget_photo, photoBitmap)
                views.setViewVisibility(R.id.widget_photo, android.view.View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.widget_photo, android.view.View.GONE)
            }
        } else {
            views.setTextViewText(R.id.widget_title, "今天还没有日记")
            views.setViewVisibility(R.id.widget_title, android.view.View.VISIBLE)
            views.setViewVisibility(R.id.widget_photo, android.view.View.GONE)
        }

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_today_root, pendingIntent)

        manager.updateAppWidget(id, views)
    }

    private fun loadThumbnail(context: Context, entry: EntryWithPhotos): Bitmap? {
        val firstPhoto = entry.photos.firstOrNull() ?: return null
        val file = File(context.filesDir, "photos/${firstPhoto.fileName}")
        if (!file.exists()) return null

        val options = BitmapFactory.Options().apply {
            inSampleSize = 4
        }
        return try {
            BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (_: Exception) {
            null
        }
    }

    private var cachedDb: AppDatabase? = null

    private fun getDatabase(context: Context): AppDatabase {
        return cachedDb ?: androidx.room.Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "photo_diary.db"
        )
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
            .build()
            .also { cachedDb = it }
    }
}
