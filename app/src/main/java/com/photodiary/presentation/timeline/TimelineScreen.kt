package com.photodiary.presentation.timeline

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.photodiary.data.local.UserPreferences
import com.photodiary.domain.model.CalendarDay
import com.photodiary.domain.model.DiaryEntry
import com.photodiary.domain.model.buildTagColorMap
import com.photodiary.domain.model.tagColor
import com.photodiary.domain.repository.DiaryRepository
import com.photodiary.presentation.components.DayCell
import com.photodiary.presentation.components.RecentEntryCard
import com.photodiary.presentation.theme.ThemePickerSheet
import com.photodiary.ui.theme.ThemeMode
import com.photodiary.ui.theme.ThemePreset
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TimelineScreen(
    repository: DiaryRepository,
    userPreferences: UserPreferences,
    onNavigateToCreateWithDate: (Long) -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToPhotoWall: () -> Unit,
    onNavigateToTagManagement: () -> Unit = {},
    onNavigateToCalendarPicker: () -> Unit = {},
    viewModel: TimelineViewModel = viewModel(factory = TimelineViewModel.Factory(repository))
) {
    val uiState by viewModel.uiState.collectAsState()
    val themeMode by userPreferences.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)
    val themePreset by userPreferences.themePresetFlow.collectAsState(initial = ThemePreset.TERRACOTTA)
    val customTagNames by userPreferences.customTagsFlow.collectAsState(initial = emptyList())
    val tagColorMap = remember(customTagNames) { buildTagColorMap(customTagNames) }
    val coroutineScope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullToRefreshState()
    var showAboutDialog by remember { mutableStateOf(false) }
    var showThemePicker by remember { mutableStateOf(false) }

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(Unit) {
            viewModel.refresh()
            pullToRefreshState.endRefresh()
        }
    }

    Scaffold(
        topBar = {
            AnimatedContent(
                targetState = uiState.isSearching,
                transitionSpec = {
                    if (targetState) {
                        (slideInVertically(spring(stiffness = 300f)) { -it } + fadeIn(spring(stiffness = 300f)))
                            .togetherWith(slideOutVertically(spring(stiffness = 300f)) { it } + fadeOut(spring(stiffness = 300f)))
                    } else {
                        (slideInVertically(spring(stiffness = 300f)) { it } + fadeIn(spring(stiffness = 300f)))
                            .togetherWith(slideOutVertically(spring(stiffness = 300f)) { -it } + fadeOut(spring(stiffness = 300f)))
                    }
                },
                label = "search_bar"
            ) { searching ->
            if (searching) {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { viewModel.toggleSearch() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "取消搜索",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    title = {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = { Text("搜索日记...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    },
                    actions = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "清除",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            "相册日记",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleSearch() }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "搜索",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = {
                            coroutineScope.launch {
                                userPreferences.setThemeMode(
                                    when (themeMode) {
                                        ThemeMode.SYSTEM -> ThemeMode.LIGHT
                                        ThemeMode.LIGHT -> ThemeMode.DARK
                                        ThemeMode.DARK -> ThemeMode.SYSTEM
                                    }
                                )
                            }
                        }) {
                            Icon(
                                when (themeMode) {
                                    ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                    ThemeMode.LIGHT -> Icons.Default.LightMode
                                    ThemeMode.DARK -> Icons.Default.DarkMode
                                },
                                contentDescription = "切换主题",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { showThemePicker = true }) {
                            Icon(
                                Icons.Default.Palette,
                                contentDescription = "主题配色",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = onNavigateToPhotoWall) {
                            Icon(
                                Icons.Default.Wallpaper,
                                contentDescription = "照片墙",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = onNavigateToTagManagement) {
                            Icon(
                                Icons.Default.Bookmark,
                                contentDescription = "标签管理",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { showAboutDialog = true }) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "关于",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
            }  // AnimatedContent
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !uiState.isSearching,
                enter = slideInVertically(spring(stiffness = 200f)) { it } + fadeIn(spring(stiffness = 200f)),
                exit = slideOutVertically(spring(stiffness = 200f)) { it } + fadeOut(spring(stiffness = 200f))
            ) {
                FloatingActionButton(
                    onClick = onNavigateToCalendarPicker,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "新建日记",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (uiState.isSearching) {
                if (uiState.searchQuery.isBlank()) {
                    Text(
                        text = "输入关键词搜索日记",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (uiState.searchResults.isEmpty()) {
                    Text(
                        text = "没有找到相关日记",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    var listVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { listVisible = true }

                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        itemsIndexed(uiState.searchResults, key = { _, entry -> entry.id }) { _, entry ->
                            AnimatedVisibility(
                                visible = listVisible,
                                enter = fadeIn(spring(stiffness = 150f, dampingRatio = 0.6f)) +
                                    slideInVertically(spring(stiffness = 150f, dampingRatio = 0.6f)) { it / 6 },
                                modifier = Modifier.animateItemPlacement()
                            ) {
                                RecentEntryCard(
                                    entry = entry,
                                    onClick = { onNavigateToDetail(entry.id) },
                                    tagColorMap = tagColorMap,
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(pullToRefreshState.nestedScrollConnection)
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        item {
                            CalendarSection(
                                calendarDays = uiState.calendarDays,
                                currentMonth = uiState.currentMonth,
                                onPreviousMonth = { viewModel.goToPreviousMonth() },
                                onNextMonth = { viewModel.goToNextMonth() },
                                onDayClick = { day ->
                                    val entryId = day.firstEntryId
                                    if (entryId != null) {
                                        onNavigateToDetail(entryId)
                                    } else {
                                        val epoch = day.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                        onNavigateToCreateWithDate(epoch)
                                    }
                                }
                            )
                        }

                        item {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }

                        item {
                            Text(
                                text = "最近日记",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        if (uiState.entries.isEmpty()) {
                            item {
                                EmptyState()
                            }
                        } else {
                            itemsIndexed(uiState.entries.take(15), key = { _, entry -> entry.id }) { _, entry ->
                                RecentEntryCard(
                                    entry = entry,
                                    onClick = { onNavigateToDetail(entry.id) },
                                    tagColorMap = tagColorMap,
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                        .animateItemPlacement()
                                )
                            }
                        }
                    }
                    PullToRefreshContainer(
                        state = pullToRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text("关于 相册日记", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "版本 v1.3.1",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "相册日记是一款纯本地日记应用，帮助你记录生活中的美好瞬间。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "隐私声明：本应用不收集、不存储、不上传任何个人信息。所有日记内容和照片均保存在您的设备本地。开发者无法访问您的数据。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "崩溃报告：正式版本使用 Sentry 收集匿名崩溃日志，仅用于改进应用稳定性。崩溃日志不包含您的日记内容或个人信息。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("确定")
                }
            }
        )
    }

    if (showThemePicker) {
        ThemePickerSheet(
            currentPreset = themePreset,
            onPresetSelected = { preset ->
                coroutineScope.launch { userPreferences.setThemePreset(preset) }
                showThemePicker = false
            },
            onDismiss = { showThemePicker = false }
        )
    }
}

@Composable
private fun CalendarSection(
    calendarDays: List<CalendarDay>,
    currentMonth: java.time.YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (CalendarDay) -> Unit
) {
    val dayOfWeekLabels = remember {
        listOf(
            java.time.DayOfWeek.SUNDAY,
            java.time.DayOfWeek.MONDAY,
            java.time.DayOfWeek.TUESDAY,
            java.time.DayOfWeek.WEDNESDAY,
            java.time.DayOfWeek.THURSDAY,
            java.time.DayOfWeek.FRIDAY,
            java.time.DayOfWeek.SATURDAY
        ).map { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "上个月",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${currentMonth.year}年${currentMonth.monthValue}月",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onNextMonth, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "下个月",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            dayOfWeekLabels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        AnimatedContent(
            targetState = currentMonth,
            transitionSpec = {
                val forward = targetState > initialState
                val direction = if (forward) 1 else -1
                (slideInHorizontally(spring(stiffness = 200f, dampingRatio = 0.6f)) { direction * it / 4 } +
                    fadeIn(spring(stiffness = 200f)))
                    .togetherWith(slideOutHorizontally(spring(stiffness = 200f)) { -direction * it / 4 } +
                        fadeOut(spring(stiffness = 200f)))
                    .using(SizeTransform(clip = false))
            }
        ) {
            Column {
                for (row in 0 until 6) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until 7) {
                            val index = row * 7 + col
                            val day = calendarDays.getOrNull(index)
                            if (day != null) {
                                DayCell(
                                    day = day,
                                    compact = true,
                                    onClick = { onDayClick(it) },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "还没有日记\n点击右下角的 + 开始记录吧",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
