# CLAUDE.md — PhotoDiary

## Quick Context

Android photo diary app. Kotlin + Jetpack Compose + Room + Coil + Material3 + Sentry.
Min SDK 29, target 34. Manual DI (no Hilt). MVVM + Clean Architecture.

## Release

- Signing: `app/release.jks` (RSA 2048, 25yr), config via `keystore.properties` (gitignored)
- `app/build.gradle.kts`: `signingConfigs { release }` + `buildTypes { release { isMinifyEnabled=true, isShrinkResources=true } }`
- ProGuard: `app/proguard-rules.pro` — keep rules for Room, Coil, Compose, Sentry, DataStore, ViewModels
- Sentry: auto-init disabled via manifest metadata; manual init in `PhotoDiaryApplication.onCreate()` (release only), DSN in `sentry.properties` (gitignored)
- Build: `./gradlew assembleRelease` → `app/build/outputs/apk/release/app-release.apk`
- Mapping: `app/build/outputs/mapping/release/mapping.txt` (save for crash deobfuscation)
- Privacy: `PRIVACY_POLICY.md`, About dialog in TimelineScreen (Info icon in top bar)

## Project Layout

```
app/src/main/java/com/photodiary/
├── MainActivity.kt                    # Single Activity, sets PhotoDiaryTheme + NavHost
├── PhotoDiaryApplication.kt           # Holds AppContainer singleton
├── data/local/entity/                 # Room entities (DiaryEntryEntity, PhotoEntity, EntryWithPhotos)
├── data/local/dao/                    # Room DAOs (DiaryEntryDao, PhotoDao)
├── data/local/AppDatabase.kt          # Room DB (v5, exportSchema=false)
├── data/local/TagsConverter.kt        # TypeConverter: List<String> ↔ JSON
├── data/local/UserPreferences.kt      # DataStore<Preferences> — theme mode + custom tags
├── data/repository/                   # DiaryRepositoryImpl (Room + file I/O)
├── domain/model/                      # DiaryEntry, Photo, CalendarDay, TagDefinitions
├── domain/repository/                 # DiaryRepository interface
├── di/AppContainer.kt                 # Manual DI: creates DB + repository + userPreferences
├── navigation/                        # Screen routes (sealed class) + AppNavGraph
├── presentation/timeline/             # Main screen: calendar + search + recent entries + pull-to-refresh + photo-wall btn
├── presentation/calendar/             # Full-screen calendar grid
├── presentation/editentry/            # Create/edit entry form + tags picker
├── presentation/entrydetail/          # Entry detail + photo grid + tags + share
├── presentation/photowall/            # Year-grouped photo grid (replaces calendar btn)
├── presentation/photoviewer/          # Full-screen HorizontalPager
├── presentation/tagmanagement/        # Tag management: list all tags + add/delete
├── presentation/tagfilter/            # Entries filtered by a tag
├── presentation/components/           # DayCell, CustomInputDialog, PhotoGrid, PhotoThumbnail, RecentEntryCard
├── widget/                            # TodayWidgetProvider (AppWidget)
├── util/                              # ShareImageGenerator (Canvas bitmap rendering)
└── ui/theme/                          # Color, Type, Theme, ThemeMode (Terracotta warm palette)
```

## Architecture Rules

### DI Pattern
```kotlin
// PhotoDiaryApplication.kt
val repository = (application as PhotoDiaryApplication).container.repository
val userPreferences = app.container.userPreferences

// Every Screen gets repository via parameter, creates ViewModel with Factory:
viewModel(factory = TimelineViewModel.Factory(repository))

// ViewModel Factory pattern:
class Factory(private val repository: DiaryRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ...
}

// UserPreferences (DataStore) passed to NavGraph → TimelineScreen for theme toggle
// ThemeMode collected in MainActivity via collectAsState() → PhotoDiaryTheme(themeMode)
```

### State Management
- ViewModels expose `StateFlow<XxxUiState>` (immutable data class)
- Screens collect via `val uiState by viewModel.uiState.collectAsState()`
- One-shot events (navigation, toasts) use `Channel<Event>` → `LaunchedEffect { viewModel.events.collect {...} }`
- Room DAOs return `Flow<>` — UI auto-refreshes on DB changes

### Navigation
- 8 routes in `Screen` sealed class: Timeline, EntryDetail, CreateEditEntry, PhotoViewer, PhotoWall, Calendar, TagManagement, TagFilter
- Typed args: `entryId: Long` (required), `photoIndex: Int` (required), optional `entryId` for create/edit (default -1L), `tag: String` for TagFilter (URL-encoded)
- `CreateEditEntry.createRoute(entryId)` — null = new, non-null = edit
- `Calendar.createRoute(pickerMode)` — pickerMode=true from FAB (edit on occupied), false=normal (detail on occupied)
- `TagFilter.createRoute(tag)` — URL-encodes tag name for Chinese/special chars

### Image Storage
- Photos saved to `context.filesDir/photos/` (app-internal, privacy-preserving)
- Naming: `{timestamp}_{uuid8}.jpg`
- `resolvePhotoPath(fileName)` returns full absolute path
- Photo Picker (PickMultipleVisualMedia) for gallery, FileProvider + TakePicture contract for camera

## Repository API

```kotlin
fun getAllEntries(): Flow<List<DiaryEntry>>
fun getEntryWithPhotos(entryId: Long): Flow<DiaryEntry?>
fun searchEntries(query: String): Flow<List<DiaryEntry>>            // LIKE on title & content
fun getEntriesByTag(tag: String): Flow<List<DiaryEntry>>            // exact tag match via JSON LIKE
suspend fun createEntry(title, content, photoFileNames, tags, createdAt): Long  // throws DateConflictException if date taken
suspend fun updateEntry(entry: DiaryEntry)                          // title/content/tags only, no conflict check
suspend fun updateEntryWithPhotos(id, title, content, createdAt, photoFileNames, tags)  // throws DateConflictException
suspend fun deleteEntry(entryId: Long)
suspend fun entryExistsForDate(date, excludeId): Boolean            // check if a date already has an entry
fun resolvePhotoPath(fileName: String): String
suspend fun savePhoto(uri: Uri): String
```

## Key Design Decisions

1. **No Hilt** — manual DI is simpler for this scope, avoids annotation processing overhead
2. **Photo edit = incremental update** — `updateEntryWithPhotos` compares old vs new photo file names, only deletes removed photos (disk+DB) and only inserts new ones. Editor `save()` prefers `updateEntry()` (text-only) when photo list unchanged.
3. **Discard cleanup** — "取消" → AlertDialog → "确定" sets `showDiscardDialog = false` + `pendingDiscard = true` → `LaunchedEffect` waits for dialog animation to finish → `viewModel.discard()` deletes only newly-added files (tracked via `initialPhotoFileNames: Set<String>`)
4. **Calendar shared logic** — `CalendarDay` + `buildCalendarDays()` in `domain/model/CalendarDay.kt`, used by both TimelineViewModel and CalendarViewModel
5. **DayCell `compact` param** — single composable for both inline calendar (`compact=true`) and full-screen (`compact=false`)
6. **Theme** — warm Terracotta/Brown/Gold palette, light+dark adaptive, custom Typography scale. Manual toggle (SYSTEM/LIGHT/DARK) persisted in DataStore, cycled from Timeline top bar.
7. **Search** — Room SQL `LIKE` on title & content, reactive Flow. ViewModel manages `isSearching`/`searchQuery`/`searchResults` state, cancels previous search Job on new input. Timeline top bar switches to search field.
8. **Pull-to-refresh** — Material3 `PullToRefreshContainer` + `rememberPullToRefreshState` on Timeline LazyColumn. `ViewModel.refresh()` is a `suspend` function, called from `LaunchedEffect` when pull triggers.
9. **Date formatting** — All timestamps use `java.time` (`Instant.ofEpochMilli` → `ZonedDateTime` → `DateTimeFormatter`). No `SimpleDateFormat` remaining.
10. **Calendar day click** — Past/today dates always clickable. Normal mode: has entry → navigate to detail, no entry → create with date pre-filled. Picker mode (from FAB): has entry → navigate to edit, no entry → create. Calendar route supports `pickerMode` boolean arg.
11. **Date picker in editor** — `DatePickerDialog` on the create/edit screen, selectable dates limited to today and earlier. `createEntry()` accepts `createdAt: Long` param (defaults to `System.currentTimeMillis()`).
12. **Calendar thumbnails** — `CalendarDay.thumbnailPaths: List<String>` holds up to 4 photo paths. `buildCalendarDays()` uses `flatMap { it.photos }.take(4)` across all entries on a date. `DayCell` renders 1 photo full-size, 2-4 photos as a 2×2 grid.
13. **One entry per day** — `diary_entries.entry_date` (local midnight epoch) has a UNIQUE constraint. Each calendar day can have at most one diary entry. FAB opens calendar picker (pickerMode): has entry → edit, no entry → create. Date picker conflict: shows dialog (保存并查看 / 放弃并查看 / 跳转并修改) if unsaved changes, or auto-redirects to existing entry. New entry init: if today already has entry, auto-redirects (FAB→edit, calendar click→view).
14. **entryDate field** — Added in DB v5 migration. Stored as local midnight epoch millis alongside `createdAt` (actual timestamp). UNIQUE index enforces one-entry-per-day at DB level. Repository normalizes dates via `normalizeToLocalMidnight()` using `java.time`. `CalendarDay.buildCalendarDays` groups by `entryDate`. Widget queries by `entry_date` equality.
15. **Tags** — `DiaryEntryEntity.tags` stored as JSON array via `TagsConverter` (TypeConverter), default `[]`. Multi-select with preset options (旅行/美食/日常/工作) plus custom input via "+" button → AlertDialog. Tag definitions with colors in `domain/model/TagDefinitions.kt` (preset colors + custom palette via index). Custom tags persisted in DataStore (`UserPreferences.customTagsFlow`) at save time, not on toggle (prevents orphan tags); `toggleTag()` immediately updates `customTagNames` in UiState for instant visual feedback. `UserPreferences` supports add + delete for custom tags. DB v1→v2→v3→v4: v2 added tags column + legacy mood column, v3 converted mood→moods JSON array, v4 drops moods column (table rebuild migration). Editor: colored tag FilterChips. Detail + Timeline card: colored tag chips below content. Tag management page (see #21).
16. **Share** — `EntryDetailScreen` top bar has share button. Generates a composite image (1080px wide, white background) via `ShareImageGenerator`: first photo (scaled to fit, 720px tall), title, content (max 10 lines), "PhotoDiary" watermark. Bitmap saved to `context.cacheDir`, shared via `FileProvider` URI with `Intent.ACTION_SEND` (image/jpeg). Uses `Dispatchers.IO` for bitmap generation and file I/O.
17. **Photo wall** — `TimelineScreen` top bar `Wallpaper` icon → `PhotoWallScreen`. Year selector in top bar (chevrons). Shows all 12 months for selected year: `stickyHeader` per month ("M月" + photo count), empty months show "无照片". Photos in 3-column grid rows (1:1 aspect `SubcomposeAsyncImage`). Click photo → navigate to entry detail.
18. **Performance** — All domain/UiState data classes annotated with `@Immutable` for Compose stability. Coil ImageLoader: 15% memory cache + 128MB disk cache at `cacheDir/coil`, global crossfade. Tag color lookup uses `Map<String, Color>` (O(1)) pre-built via `buildTagColorMap` and passed to cards. `allTagDefs` wrapped in `remember(uiState.customTagNames, uiState.tags)`. `dayOfWeekLabels` memoized via `remember`. DataStore flows use `distinctUntilChanged()`. Room Flows debounced (50ms) to batch rapid DB emissions. `updateEntryWithPhotos` now uses incremental photo diff — only deletes removed photos, only inserts new ones. Editor save uses `updateEntry()` (text-only) when photos unchanged to avoid unnecessary file I/O. PhotoWall `selectYear` has no-op guard. Search query no-op guard (`if query == current return`). LazyColumn keys use composite string keys not `hashCode()`.
19. **Animations** — Navigation: `NavHost` global `enterTransition`/`exitTransition` = slide + fade (300ms tween). Calendar: `AnimatedContent` keyed on `currentMonth` with direction-aware horizontal slide (forward=right, backward=left, determined by `targetState > initialState`) 1/4-distance + fade in both Timeline inline and full CalendarScreen. FAB: `AnimatedVisibility` with slide-in/out vertical + fade when entering/exiting search mode. List items: `animateItemPlacement()` on Timeline `RecentEntryCard` and PhotoWall grid rows for smooth insert/delete/reorder transitions.
20. **AppWidget** — `TodayWidgetProvider`: 4x2 home-screen widget showing today's date, entry count/title, and first photo thumbnail. Uses RemoteViews layout with rounded card background. Queries Room DB directly (separate DB instance with same migrations). Auto-refreshes every 30 min via `updatePeriodMillis`. Immediate refresh triggered after save (CreateEditEntryScreen) and delete (EntryDetailScreen) via `TodayWidgetProvider.updateAllWidgets(context)`. Tap opens MainActivity.
21. **Tag Management** — `TimelineScreen` top bar `Bookmark` icon → `TagManagementScreen`. Lists all tags (preset + custom) with color circle, name, and entry count badge. Both preset and custom tags show delete button → `AlertDialog` confirms deletion with scope description (preset: removed from all entries but stays in picker; custom: removed from all entries + permanently deleted from DataStore). `FAB` "+" → `CustomInputDialog` for adding new custom tags (persisted to DataStore). Click tag → `TagFilterScreen` showing all entries with that tag via `getEntriesByTag()` DAO query (`LIKE '%"tag"%'` for exact match in JSON array). Custom tag persistence delayed to `save()` time to prevent orphan tags; `toggleTag()` immediately updates `customTagNames` in UiState for instant visual feedback. `RecentEntryCard` extracted to `presentation/components/` for reuse across Timeline and TagFilter.

## Common Pitfalls

- **forward reference**: Kotlin doesn't hoist composable launchers — declare `takePictureLauncher` BEFORE `requestCameraPermission`
- **ExperimentalFoundationApi**: `HorizontalPager` + `rememberPagerState` need `@OptIn` annotation
- **`@Composable` functions calling `rememberLauncherForActivityResult`**: these are fine inside Composable, but order matters for captured variables
- **Room Flow is infinite**: don't `stateIn()` in ViewModel init unless you handle cancellation; prefer collecting in `viewModelScope.launch`
- **`Flow.collect` in init blocks**: make sure to wrap in `viewModelScope.launch`, not `runBlocking`
- **search Flow lifecycle**: cancel the previous `searchJob` before starting a new one when query changes; empty query → skip Flow and clear results directly
- **PullToRefresh + nestedScroll**: wrap the scrollable content in a `Box` with `nestedScroll(state.nestedScrollConnection)`, place `PullToRefreshContainer` inside the same Box (e.g., `Modifier.align(Alignment.TopCenter)`)
- **DataStore in Composable**: collect via `collectAsState(initial = ...)` at the top level (MainActivity) and pass down; don't collect inside `SideEffect` or event handlers
- **SQLite JSON functions are not portable**: avoid `json_array()`, `json_object()`, etc. in Room migrations — some Android builds (e.g., MuMu emulator) ship with SQLite builds lacking JSON extension. Use string concatenation to build JSON arrays manually
- **DataStore `data` emits full snapshot on ANY key change**: when collecting multiple Flows derived from the same DataStore, add `.distinctUntilChanged()` to each Flow to prevent unrelated changes (e.g., theme toggle) from triggering recomposition in screens collecting tags/moods

## Gradle

```bash
./gradlew assembleDebug          # build APK
./gradlew installDebug           # install to connected device
./gradlew clean assembleDebug    # clean rebuild
```

Gradle 8.5, AGP 8.2.2, Kotlin 1.9.22, KSP for Room compiler.

Added deps beyond original scaffold:
- `androidx.datastore:datastore-preferences:1.0.0` — persisted theme mode
- `io.sentry:sentry-android:7.8.0` — crash reporting (release only)

## Permissions

- `CAMERA` — runtime permission, `required="false"` for camera-less devices
- `INTERNET` — added in manifest, used only by Sentry for crash report upload (release builds only)

## Backlog

Low-priority improvements, roughly in recommended order:

| # | Feature | Notes | Est. |
|---|---------|-------|------|
（全部完成）
