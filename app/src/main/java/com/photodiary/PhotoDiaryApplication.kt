package com.photodiary

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.photodiary.di.AppContainer
import io.sentry.Sentry
import io.sentry.android.core.SentryAndroid

class PhotoDiaryApplication : Application(), ImageLoaderFactory {

    companion object {
        private const val DISK_CACHE_MAX_BYTES = 128L * 1024 * 1024
    }
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        if (!com.photodiary.BuildConfig.DEBUG) {
            try {
                SentryAndroid.init(this) { options ->
                    options.isEnableUserInteractionTracing = true
                }
            } catch (_: Exception) {
                // Sentry DSN not configured yet — silently skip
            }
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.15)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("coil"))
                    .maxSizeBytes(DISK_CACHE_MAX_BYTES)
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
