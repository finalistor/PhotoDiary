package com.photodiary.presentation.calendar

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.photodiary.domain.model.CalendarDay
import com.photodiary.domain.model.DiaryEntry
import com.photodiary.domain.model.buildCalendarDays
import com.photodiary.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

@Immutable
data class CalendarUiState(
    val entries: List<DiaryEntry> = emptyList(),
    val calendarDays: List<CalendarDay> = emptyList(),
    val currentMonth: YearMonth = YearMonth.now(),
    val isLoading: Boolean = true
)

class CalendarViewModel(
    private val repository: DiaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllEntries().collect { entries ->
                _uiState.value = CalendarUiState(
                    entries = entries,
                    calendarDays = buildCalendarDays(entries, YearMonth.now(), LocalDate.now()),
                    currentMonth = YearMonth.now(),
                    isLoading = false
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

    class Factory(private val repository: DiaryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CalendarViewModel(repository) as T
        }
    }
}
