package com.photodiary.domain.model

import androidx.compose.runtime.Immutable
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

@Immutable
data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val firstEntryId: Long?,
    val thumbnailPaths: List<String> = emptyList(),
    val entryIds: List<Long> = emptyList(),
    val entryTitles: List<String> = emptyList()
)

fun buildCalendarDays(
    entries: List<DiaryEntry>,
    month: YearMonth,
    today: LocalDate
): List<CalendarDay> {
    val zone = ZoneId.systemDefault()

    val entriesWithDate = entries.map { entry ->
        Instant.ofEpochMilli(entry.createdAt).atZone(zone).toLocalDate() to entry
    }
    val entriesByDate: Map<LocalDate, List<DiaryEntry>> = entriesWithDate
        .filter { (date, _) ->
            val entryMonth = YearMonth.from(date)
            entryMonth == month ||
                entryMonth == month.minusMonths(1) ||
                entryMonth == month.plusMonths(1)
        }
        .groupBy({ it.first }, { it.second })

    val firstDayOfMonth = month.atDay(1)
    val startOffset = firstDayOfMonth.dayOfWeek.value % 7
    val startDate = firstDayOfMonth.minusDays(startOffset.toLong())

    return (0 until 42).map { i ->
        val date = startDate.plusDays(i.toLong())
        val dayEntries = entriesByDate[date].orEmpty()
        CalendarDay(
            date = date,
            isCurrentMonth = YearMonth.from(date) == month,
            isToday = date == today,
            firstEntryId = dayEntries.firstOrNull()?.id,
            thumbnailPaths = dayEntries.flatMap { it.photos }.take(4).map { it.filePath },
            entryIds = dayEntries.map { it.id },
            entryTitles = dayEntries.map { it.title }
        )
    }
}
