package com.quenan.duji.ui.screen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quenan.duji.data.day.DayData
import com.quenan.duji.data.day.DayRepository
import com.quenan.duji.data.day.DayType
import com.quenan.duji.data.day.RepeatCycle
import com.quenan.duji.data.day.parseDayDate
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ThoseDaysViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DayRepository(application)
    private val viewMode = MutableStateFlow(ThoseDaysViewMode.List)
    private val sortOption = MutableStateFlow(DaySortOption.default())

    val days = combine(repository.observeDays(), sortOption) { dayList, option ->
        dayList.sortedWith(dayComparator(option))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currentViewMode = viewMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThoseDaysViewMode.List)

    val currentSort = sortOption
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DaySortOption.default())

    fun addDay(
        emoji: String,
        emojiName: String,
        name: String,
        type: DayType,
        repeatCycle: RepeatCycle,
        targetDate: String,
        note: String,
        weekDays: List<Int>,
        monthDays: List<Int>,
        isLunar: Boolean,
        isPinned: Boolean,
    ) {
        viewModelScope.launch {
            repository.addDay(
                DayData(
                    id = 0L,
                    emoji = emoji,
                    emojiName = emojiName,
                    name = name.trim(),
                    type = type,
                    repeatCycle = repeatCycle,
                    targetDate = targetDate,
                    note = note,
                    weekDays = weekDays,
                    monthDays = monthDays,
                    isLunar = isLunar,
                    isPinned = isPinned,
                    createdAt = 0L,
                )
            )
        }
    }

    fun updateDay(day: DayData) {
        viewModelScope.launch {
            repository.updateDay(day)
        }
    }

    fun deleteDay(day: DayData) {
        viewModelScope.launch {
            repository.deleteDay(day)
        }
    }

    fun updateViewMode(mode: ThoseDaysViewMode) {
        viewMode.update { mode }
    }

    fun updateSort(field: DaySortField, direction: SortDirection) {
        sortOption.update { DaySortOption(field = field, direction = direction) }
    }
}

enum class ThoseDaysViewMode {
    List,
    Grid,
}

enum class DaySortField {
    CREATED_AT,
    EVENT_DATE,
}

data class DaySortOption(
    val field: DaySortField,
    val direction: SortDirection,
) {
    companion object {
        fun default() = DaySortOption(
            field = DaySortField.CREATED_AT,
            direction = SortDirection.DESC,
        )
    }
}

private fun dayComparator(option: DaySortOption): Comparator<DayData> {
    val fieldComparator = when (option.field) {
        DaySortField.CREATED_AT -> compareBy<DayData> { it.createdAt }
        DaySortField.EVENT_DATE -> compareBy { parseDayDate(it.targetDate) ?: LocalDate.MIN }
    }
    val directionalComparator = when (option.direction) {
        SortDirection.ASC -> fieldComparator
        SortDirection.DESC -> fieldComparator.reversed()
    }
    return compareByDescending<DayData> { it.isPinned }
        .then(directionalComparator)
        .thenByDescending(DayData::id)
}
