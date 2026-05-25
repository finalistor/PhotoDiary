package com.photodiary.presentation.tagmanagement

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.photodiary.data.local.UserPreferences
import com.photodiary.domain.model.allTagDefs
import com.photodiary.domain.model.presetTagDefs
import com.photodiary.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Immutable
data class TagItem(
    val name: String,
    val color: Color,
    val entryCount: Int,
    val isPreset: Boolean
)

@Immutable
data class TagManagementUiState(
    val tagItems: List<TagItem> = emptyList(),
    val isLoading: Boolean = true
)

class TagManagementViewModel(
    private val repository: DiaryRepository,
    private val userPreferences: UserPreferences?
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagManagementUiState())
    val uiState: StateFlow<TagManagementUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getAllEntries(),
                userPreferences?.customTagsFlow ?: MutableStateFlow(emptyList())
            ) { entries, customTagNames ->
                val tagCounts = entries
                    .flatMap { it.tags }
                    .groupingBy { it }
                    .eachCount()
                val customTagSet = customTagNames.toSet()
                val tagDefs = allTagDefs(customTagNames)
                val items = tagDefs.map { def ->
                    TagItem(
                        name = def.name,
                        color = def.color,
                        entryCount = tagCounts[def.name] ?: 0,
                        isPreset = def.name !in customTagSet
                    )
                }
                TagManagementUiState(tagItems = items, isLoading = false)
            }.collect { _uiState.value = it }
        }
    }

    fun addTag(name: String) {
        userPreferences?.let { up ->
            viewModelScope.launch { up.addCustomTag(name) }
        }
    }

    fun deleteTag(item: TagItem) {
        viewModelScope.launch {
            val entries = repository.getAllEntries().first()
            entries.filter { item.name in it.tags }.forEach { entry ->
                repository.updateEntry(entry.copy(tags = entry.tags - item.name))
            }
            if (!item.isPreset) {
                userPreferences?.deleteCustomTag(item.name)
            }
        }
    }

    class Factory(
        private val repository: DiaryRepository,
        private val userPreferences: UserPreferences?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TagManagementViewModel(repository, userPreferences) as T
        }
    }
}
