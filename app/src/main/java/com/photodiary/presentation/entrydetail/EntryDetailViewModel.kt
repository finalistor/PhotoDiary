package com.photodiary.presentation.entrydetail

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.photodiary.domain.model.DiaryEntry
import com.photodiary.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Immutable
data class EntryDetailUiState(
    val entry: DiaryEntry? = null,
    val isLoading: Boolean = true
)

class EntryDetailViewModel(
    private val repository: DiaryRepository,
    private val entryId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(EntryDetailUiState())
    val uiState: StateFlow<EntryDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getEntryWithPhotos(entryId).collect { entry ->
                _uiState.value = EntryDetailUiState(entry = entry, isLoading = false)
            }
        }
    }

    fun deleteEntry(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteEntry(entryId)
            onDeleted()
        }
    }

    class Factory(
        private val repository: DiaryRepository,
        private val entryId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EntryDetailViewModel(repository, entryId) as T
        }
    }
}
