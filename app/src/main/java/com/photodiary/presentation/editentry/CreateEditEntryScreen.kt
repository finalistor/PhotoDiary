package com.photodiary.presentation.editentry

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.photodiary.data.local.UserPreferences
import com.photodiary.domain.model.TagDefinition
import com.photodiary.domain.model.allTagDefs
import com.photodiary.domain.repository.DiaryRepository
import com.photodiary.presentation.components.CustomInputDialog
import com.photodiary.presentation.components.PhotoGrid
import com.photodiary.widget.TodayWidgetProvider
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateEditEntryScreen(
    entryId: Long?,
    initialDate: Long? = null,
    repository: DiaryRepository,
    userPreferences: UserPreferences? = null,
    onNavigateBack: () -> Unit,
    onNavigateToEntry: (Long) -> Unit = {},
    onNavigateToEditEntry: (Long) -> Unit = {},
    viewModel: CreateEditEntryViewModel = viewModel(
        factory = CreateEditEntryViewModel.Factory(repository, entryId, initialDate, userPreferences)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDiscardDialog by remember { mutableStateOf(false) }
    var pendingDiscard by remember { mutableStateOf(false) }
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCustomTagDialog by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var dateConflictEvent by remember { mutableStateOf<CreateEditEntryEvent.DateConflictRedirect?>(null) }

    val tagDefs = remember(uiState.customTagNames, uiState.tags) { allTagDefs(uiState.customTagNames) }

    val pickMultipleMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris: List<Uri> ->
        uris.forEach { uri -> viewModel.addPhoto(uri) }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraUri != null) {
            viewModel.addPhoto(cameraUri!!)
        }
    }

    val requestCameraPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val file = File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            Toast.makeText(context, "需要相机权限才能拍照", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CreateEditEntryEvent.NavigateBack -> {
                    if (event.saved) TodayWidgetProvider.updateAllWidgets(context)
                    onNavigateBack()
                }
                is CreateEditEntryEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is CreateEditEntryEvent.DateConflictRedirect -> {
                    if (event.hasUnsavedChanges) {
                        dateConflictEvent = event
                    } else if (event.navigateToEdit) {
                        onNavigateToEditEntry(event.targetEntryId)
                    } else {
                        onNavigateToEntry(event.targetEntryId)
                    }
                }
                is CreateEditEntryEvent.NavigateToEntry -> {
                    TodayWidgetProvider.updateAllWidgets(context)
                    onNavigateToEntry(event.entryId)
                }
                is CreateEditEntryEvent.NavigateToEditEntry -> {
                    TodayWidgetProvider.updateAllWidgets(context)
                    onNavigateToEditEntry(event.entryId)
                }
            }
        }
    }

    LaunchedEffect(pendingDiscard) {
        if (pendingDiscard) {
            viewModel.discard()
        }
    }

    dateConflictEvent?.let { event ->
        AlertDialog(
            onDismissRequest = { dateConflictEvent = null },
            title = { Text("日期已有日记") },
            text = {
                Column {
                    Text("该日期已有日记「${event.targetEntryTitle}」")
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = {
                            dateConflictEvent = null
                            viewModel.discardAndNavigateToEditEntry(event.targetEntryId)
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("跳转并修改", color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    dateConflictEvent = null
                    viewModel.saveAndNavigateToEntry(event.targetEntryId)
                }) { Text("保存并查看") }
            },
            dismissButton = {
                TextButton(onClick = {
                    dateConflictEvent = null
                    viewModel.discardAndNavigateToEntry(event.targetEntryId)
                }) { Text("放弃并查看") }
            }
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("放弃编辑？") },
            text = { Text("当前修改尚未保存，确定要离开吗？") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    pendingDiscard = true
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("取消") }
            }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.selectedEntryDate,
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val date = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                    return !date.isAfter(LocalDate.now())
                }
                override fun isSelectableYear(year: Int): Boolean = true
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.updateSelectedEntryDate(it) }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showCustomTagDialog) {
        CustomInputDialog(
            title = "添加自定义标签",
            placeholder = "输入标签名",
            currentItems = uiState.tags,
            onAdd = { viewModel.toggleTag(it) },
            onDismiss = { showCustomTagDialog = false }
        )
    }

    if (showPhotoSourceDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoSourceDialog = false },
            title = { Text("添加照片") },
            text = { Text("请选择照片来源") },
            confirmButton = {
                TextButton(onClick = {
                    showPhotoSourceDialog = false
                    pickMultipleMedia.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) { Text("从相册选取") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPhotoSourceDialog = false
                    val hasCameraPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasCameraPermission) {
                        val file = File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
                        val uri = FileProvider.getUriForFile(
                            context, "${context.packageName}.fileprovider", file
                        )
                        cameraUri = uri
                        takePictureLauncher.launch(uri)
                    } else {
                        requestCameraPermission.launch(Manifest.permission.CAMERA)
                    }
                }) { Text("拍照") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isEditing) "编辑日记" else "新建日记",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    TextButton(onClick = { showDiscardDialog = true }) {
                        Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.save() },
                        enabled = !uiState.isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(end = 8.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        }
                        Text(
                            "保存",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("标题") },
                placeholder = { Text("给这一天取个名字...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                textStyle = MaterialTheme.typography.titleMedium
            )

            if (uiState.selectedEntryDate > 0) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = "选择日期",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = ZonedDateTime.ofInstant(
                            Instant.ofEpochMilli(uiState.selectedEntryDate),
                            ZoneId.systemDefault()
                        ).format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = "标签",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tagDefs.forEach { def ->
                    TagChip(
                        name = def.name,
                        color = def.color,
                        selected = def.name in uiState.tags,
                        onClick = { viewModel.toggleTag(def.name) }
                    )
                }
                FilterChip(
                    selected = false,
                    onClick = { showCustomTagDialog = true },
                    label = { Text("+") },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = uiState.content,
                onValueChange = { viewModel.updateContent(it) },
                label = { Text("写点什么...") },
                placeholder = { Text("今天发生了什么有趣的事？") },
                modifier = Modifier.fillMaxWidth().height(180.dp),
                maxLines = 12,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "照片",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            PhotoGrid(
                photoPaths = uiState.photoPaths,
                showAddButton = true,
                showDeleteButtons = true,
                onAddClick = { showPhotoSourceDialog = true },
                onPhotoClick = { },
                onPhotoDelete = { index -> viewModel.removePhoto(index) }
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun TagChip(
    name: String,
    color: androidx.compose.ui.graphics.Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(name) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color,
            selectedLabelColor = androidx.compose.ui.graphics.Color.White
        )
    )
}

