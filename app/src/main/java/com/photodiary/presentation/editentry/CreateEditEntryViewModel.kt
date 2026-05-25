package com.photodiary.presentation.editentry

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.photodiary.data.local.UserPreferences
import com.photodiary.domain.model.presetTagDefs
import com.photodiary.domain.repository.DiaryRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Immutable
data class CreateEditEntryUiState(
    val title: String = "",
    val content: String = "",
    val photoFileNames: List<String> = emptyList(),
    val photoPaths: List<String> = emptyList(),
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val originalCreatedAt: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList(),
    val customTagNames: List<String> = emptyList(),
    val error: String? = null
)

sealed class CreateEditEntryEvent {
    data class NavigateBack(val saved: Boolean) : CreateEditEntryEvent()
    data class ShowError(val message: String) : CreateEditEntryEvent()
}

class CreateEditEntryViewModel(
    private val repository: DiaryRepository,
    private val entryId: Long?,
    private val initialDate: Long?,
    private val userPreferences: UserPreferences?
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CreateEditEntryUiState(originalCreatedAt = initialDate ?: System.currentTimeMillis())
    )
    val uiState: StateFlow<CreateEditEntryUiState> = _uiState.asStateFlow()

    private val _events = Channel<CreateEditEntryEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var initialPhotoFileNames: Set<String> = emptySet()

    init {
        viewModelScope.launch {
            if (userPreferences != null) {
                val customTags = userPreferences.customTagsFlow.first()
                _uiState.value = _uiState.value.copy(customTagNames = customTags)
            }
        }
        if (entryId != null) {
            viewModelScope.launch {
                val entry = repository.getEntryWithPhotos(entryId).first { it != null }
                entry?.let {
                    val names = it.photos.map { p -> p.fileName }
                    val paths = it.photos.map { p -> p.filePath }
                    initialPhotoFileNames = names.toSet()
                    _uiState.value = _uiState.value.copy(
                        title = it.title,
                        content = it.content,
                        photoFileNames = names,
                        photoPaths = paths,
                        isEditing = true,
                        originalCreatedAt = it.createdAt,
                        tags = it.tags
                    )
                }
            }
        }
    }

    fun discard() {
        viewModelScope.launch {
            _uiState.value.photoFileNames.forEach { fileName ->
                if (fileName !in initialPhotoFileNames) {
                    val file = java.io.File(repository.resolvePhotoPath(fileName))
                    if (file.exists()) file.delete()
                }
            }
            _events.send(CreateEditEntryEvent.NavigateBack(saved = false))
        }
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateContent(content: String) {
        _uiState.value = _uiState.value.copy(content = content)
    }

    fun addPhoto(uri: Uri) {
        viewModelScope.launch {
            try {
                val fileName = repository.savePhoto(uri)
                val path = repository.resolvePhotoPath(fileName)
                _uiState.value = _uiState.value.copy(
                    photoFileNames = _uiState.value.photoFileNames + fileName,
                    photoPaths = _uiState.value.photoPaths + path
                )
            } catch (e: Exception) {
                _events.send(CreateEditEntryEvent.ShowError("添加照片失败: ${e.message}"))
            }
        }
    }

    fun removePhoto(index: Int) {
        val names = _uiState.value.photoFileNames.toMutableList()
        val paths = _uiState.value.photoPaths.toMutableList()
        if (index in names.indices) {
            names.removeAt(index)
            paths.removeAt(index)
        }
        _uiState.value = _uiState.value.copy(
            photoFileNames = names,
            photoPaths = paths
        )
    }

    fun toggleTag(tag: String) {
        val state = _uiState.value
        val newTags = toggleInList(state.tags, tag)
        val wasAdded = tag in newTags && tag !in state.tags
        val presetNames = presetTagDefs.map { it.name }.toSet()
        _uiState.value = state.copy(tags = newTags)
        if (wasAdded && tag !in presetNames && tag !in state.customTagNames) {
            _uiState.value = _uiState.value.copy(
                customTagNames = _uiState.value.customTagNames + tag
            )
        }
    }

    private fun toggleInList(list: List<String>, item: String): List<String> {
        return if (item in list) list - item else list + item
    }

    fun updateCreatedAt(date: Long) {
        _uiState.value = _uiState.value.copy(originalCreatedAt = date)
    }

    fun save() {
        val state = _uiState.value
        if (state.title.isBlank() && state.content.isBlank()) {
            viewModelScope.launch {
                _events.send(CreateEditEntryEvent.ShowError("请输入标题或内容"))
            }
            return
        }

        _uiState.value = state.copy(isSaving = true)
        viewModelScope.launch {
            try {
                val up = userPreferences
                if (up != null) {
                    val presetTagNames = presetTagDefs.map { it.name }.toSet()
                    state.tags.filter { it !in presetTagNames }
                        .forEach { up.addCustomTag(it) }
                }
                if (state.isEditing && entryId != null) {
                    // Only use updateEntryWithPhotos if photos actually changed
                    val photosChanged = state.photoFileNames.toSet() != initialPhotoFileNames
                    if (photosChanged) {
                        repository.updateEntryWithPhotos(
                            entryId = entryId,
                            title = state.title,
                            content = state.content,
                            createdAt = state.originalCreatedAt,
                            photoFileNames = state.photoFileNames,
                            tags = state.tags
                        )
                    } else {
                        repository.updateEntry(
                            com.photodiary.domain.model.DiaryEntry(
                                id = entryId,
                                title = state.title,
                                content = state.content,
                                createdAt = state.originalCreatedAt,
                                updatedAt = System.currentTimeMillis(),
                                tags = state.tags
                            )
                        )
                    }
                } else {
                    repository.createEntry(
                        state.title, state.content, state.photoFileNames,
                        tags = state.tags, createdAt = state.originalCreatedAt
                    )
                }
                _events.send(CreateEditEntryEvent.NavigateBack(saved = true))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false)
                _events.send(CreateEditEntryEvent.ShowError("保存失败: ${e.message}"))
            }
        }
    }

    class Factory(
        private val repository: DiaryRepository,
        private val entryId: Long?,
        private val initialDate: Long?,
        private val userPreferences: UserPreferences? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CreateEditEntryViewModel(repository, entryId, initialDate, userPreferences) as T
        }
    }
}
