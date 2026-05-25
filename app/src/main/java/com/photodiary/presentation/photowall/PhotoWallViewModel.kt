package com.photodiary.presentation.photowall

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.photodiary.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

@Immutable
data class WallPhoto(
    val year: Int,
    val month: Int,
    val filePath: String,
    val entryId: Long
)

@Immutable
data class PhotoWallUiState(
    val selectedYear: Int = 0,
    val allPhotos: Map<Int, Map<Int, List<WallPhoto>>> = emptyMap(),
    val isLoading: Boolean = true
) {
    val availableYears: List<Int>
        get() = allPhotos.keys.sortedDescending()

    val monthPhotos: Map<Int, List<WallPhoto>>
        get() = allPhotos[selectedYear] ?: emptyMap()
}

class PhotoWallViewModel(
    private val repository: DiaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhotoWallUiState())
    val uiState: StateFlow<PhotoWallUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllEntries().collect { entries ->
                val zone = ZoneId.systemDefault()
                val currentYear = java.time.LocalDate.now().year

                val grouped: Map<Int, Map<Int, List<WallPhoto>>> = entries
                    .filter { it.photos.isNotEmpty() }
                    .flatMap { entry ->
                        val dt = Instant.ofEpochMilli(entry.createdAt).atZone(zone)
                        entry.photos.map { photo ->
                            WallPhoto(
                                year = dt.year,
                                month = dt.monthValue,
                                filePath = photo.filePath,
                                entryId = entry.id
                            )
                        }
                    }
                    .groupBy { it.year }
                    .mapValues { (_, photos) ->
                        photos.groupBy { it.month }
                            .toSortedMap()
                    }
                    .toSortedMap(reverseOrder())

                val prevState = _uiState.value
                val selectedYear = if (prevState.selectedYear != 0 && grouped.containsKey(prevState.selectedYear)) {
                    prevState.selectedYear
                } else {
                    grouped.keys.firstOrNull() ?: currentYear
                }

                _uiState.value = PhotoWallUiState(
                    selectedYear = selectedYear,
                    allPhotos = grouped,
                    isLoading = false
                )
            }
        }
    }

    fun selectYear(year: Int) {
        if (year != _uiState.value.selectedYear) {
            _uiState.value = _uiState.value.copy(selectedYear = year)
        }
    }

    fun goToPreviousYear() {
        val state = _uiState.value
        val years = state.availableYears
        val idx = years.indexOf(state.selectedYear)
        if (idx < years.size - 1) {
            selectYear(years[idx + 1])
        }
    }

    fun goToNextYear() {
        val state = _uiState.value
        val years = state.availableYears
        val idx = years.indexOf(state.selectedYear)
        if (idx > 0) {
            selectYear(years[idx - 1])
        }
    }

    class Factory(
        private val repository: DiaryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PhotoWallViewModel(repository) as T
        }
    }
}
