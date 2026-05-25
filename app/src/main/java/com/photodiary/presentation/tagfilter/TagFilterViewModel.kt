package com.photodiary.presentation.tagfilter

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.photodiary.data.local.UserPreferences
import com.photodiary.domain.model.DiaryEntry
import com.photodiary.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Immutable
data class TagFilterUiState(
    val tag: String = "",
    val entries: List<DiaryEntry> = emptyList(),
    val isLoading: Boolean = true,
    val customTagNames: List<String> = emptyList()
)

class TagFilterViewModel(
    private val repository: DiaryRepository,
    private val tag: String,
    private val userPreferences: UserPreferences?
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagFilterUiState(tag = tag))
    val uiState: StateFlow<TagFilterUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getEntriesByTag(tag).collect { entries ->
                _uiState.value = _uiState.value.copy(
                    entries = entries,
                    isLoading = false
                )
            }
        }
        if (userPreferences != null) {
            viewModelScope.launch {
                userPreferences.customTagsFlow.collect { customNames ->
                    _uiState.value = _uiState.value.copy(customTagNames = customNames)
                }
            }
        }
    }

    class Factory(
        private val repository: DiaryRepository,
        private val tag: String,
        private val userPreferences: UserPreferences?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TagFilterViewModel(repository, tag, userPreferences) as T
        }
    }
}
