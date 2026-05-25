# PhotoDiary ProGuard Rules

# --- Room ---
-keep class com.photodiary.data.local.entity.** { *; }
-keep class com.photodiary.data.local.dao.** { *; }
-keep class com.photodiary.data.local.AppDatabase { *; }
-keep class com.photodiary.data.local.TagsConverter { *; }
-keep class com.photodiary.data.local.AppDatabaseKt { *; }

# --- DataStore ---
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

# --- Coil ---
-keep class coil.** { *; }
-dontwarn coil.**

# --- Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# --- Compose ---
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# --- ViewModels ---
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# --- Application ---
-keep class com.photodiary.PhotoDiaryApplication { *; }
-keep class com.photodiary.di.AppContainer { *; }
-keep class com.photodiary.di.AppContainerKt { *; }

# --- AppWidget ---
-keep class com.photodiary.widget.** { *; }

# --- Sentry ---
-keep class io.sentry.** { *; }
-dontwarn io.sentry.**
