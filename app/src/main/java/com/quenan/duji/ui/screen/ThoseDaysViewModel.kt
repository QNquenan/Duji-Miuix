package com.quenan.duji.ui.screen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quenan.duji.data.day.DayData
import com.quenan.duji.data.day.DayRepository
import com.quenan.duji.data.day.DayType
import com.quenan.duji.data.day.RepeatCycle
import com.quenan.duji.data.day.parseDayDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ThoseDaysViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DayRepository(application)
    private val viewMode = MutableStateFlow(ThoseDaysViewMode.List)

    val days = repository.observeDays()
        .map { dayList ->
            dayList.sortedWith(
                compareByDescending<DayData> { it.isPinned }
                    .thenByDescending { parseDayDate(it.targetDate) }
                    .thenByDescending { it.createdAt }
                    .thenByDescending { it.id }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currentViewMode = viewMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThoseDaysViewMode.List)

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
}

enum class ThoseDaysViewMode {
    List,
    Grid,
}
