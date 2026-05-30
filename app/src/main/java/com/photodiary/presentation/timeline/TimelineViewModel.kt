package com.photodiary.presentation.timeline

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.photodiary.domain.model.CalendarDay
import com.photodiary.domain.model.DiaryEntry
import com.photodiary.domain.model.buildCalendarDays
import com.photodiary.domain.repository.DiaryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

@Immutable
data class TimelineUiState(
    val entries: List<DiaryEntry> = emptyList(),
    val calendarDays: List<CalendarDay> = emptyList(),
    val currentMonth: YearMonth = YearMonth.now(),
    val isLoading: Boolean = true,
    val isSearching: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<DiaryEntry> = emptyList(),
    val isRefreshing: Boolean = false
)

class TimelineViewModel(private val repository: DiaryRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            repository.getAllEntries().collect { entries ->
                _uiState.value = _uiState.value.copy(
                    entries = entries,
                    isLoading = false,
                    calendarDays = buildCalendarDays(entries, _uiState.value.currentMonth, LocalDate.now())
                )
            }
        }
    }

    fun goToPreviousMonth() {
        val prevMonth = _uiState.value.currentMonth.minusMonths(1)
        _uiState.value = _uiState.value.copy(
            currentMonth = prevMonth,
            calendarDays = buildCalendarDays(_uiState.value.entries, prevMonth, LocalDate.now())
        )
    }

    fun goToNextMonth() {
        val nextMonth = _uiState.value.currentMonth.plusMonths(1)
        _uiState.value = _uiState.value.copy(
            currentMonth = nextMonth,
            calendarDays = buildCalendarDays(_uiState.value.entries, nextMonth, LocalDate.now())
        )
    }

    fun toggleSearch() {
        val entering = !_uiState.value.isSearching
        _uiState.value = _uiState.value.copy(
            isSearching = entering,
            searchQuery = "",
            searchResults = emptyList()
        )
        if (!entering) searchJob?.cancel()
    }

    fun onSearchQueryChanged(query: String) {
        if (query == _uiState.value.searchQuery) return
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }
        searchJob = viewModelScope.launch {
            repository.searchEntries(query).collect { results ->
                _uiState.value = _uiState.value.copy(searchResults = results)
            }
        }
    }

    suspend fun refresh() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        delay(500L)
        _uiState.value = _uiState.value.copy(isRefreshing = false)
    }

    class Factory(private val repository: DiaryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TimelineViewModel(repository) as T
        }
    }
}
