package com.quenan.duji.ui.screen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quenan.duji.data.checkin.CheckInItem
import com.quenan.duji.data.checkin.CheckInRecord
import com.quenan.duji.data.checkin.CheckInRepository
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CheckInViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CheckInRepository(application)
    private val records = repository.observeRecords()
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), replay = 1)

    val cardModels = combine(repository.observeItems(), records) { items, allRecords ->
        buildCheckInCardModels(
            items = items,
            allRecords = allRecords,
            today = LocalDate.now(),
        )
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addItem(emoji: String, name: String, colorArgb: Long) {
        viewModelScope.launch {
            repository.addItem(
                CheckInItem(
                    id = 0L,
                    emoji = emoji,
                    name = name.trim(),
                    colorArgb = colorArgb,
                    createdAt = 0L,
                )
            )
        }
    }

    fun checkIn(itemId: Long, onResult: (Boolean) -> Unit) {
        checkIn(itemId, LocalDate.now(), onResult)
    }

    fun checkIn(itemId: Long, date: LocalDate, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            onResult(repository.checkIn(itemId, date.toString()))
        }
    }

    fun cancelCheckIn(itemId: Long, date: LocalDate, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            onResult(repository.cancelCheckIn(itemId, date.toString()))
        }
    }

    fun updateItem(item: CheckInItem) {
        viewModelScope.launch {
            repository.updateItem(item)
        }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            repository.deleteItem(itemId)
        }
    }

    suspend fun getAllItems(): List<CheckInItem> = repository.getAllItems()

    suspend fun getAllRecords(): List<CheckInRecord> = repository.getAllRecords()

    suspend fun importData(items: List<CheckInItem>, records: List<CheckInRecord>) {
        repository.importData(items, records)
    }
}

internal fun buildCheckInCardModels(
    items: List<CheckInItem>,
    allRecords: List<CheckInRecord>,
    today: LocalDate,
): List<CheckInCardUiModel> {
    val recordsByItemId = allRecords.groupBy(CheckInRecord::itemId)
    val currentMonth = YearMonth.from(today)
    val todayText = today.toString()

    return items.map { item ->
        val itemRecords = recordsByItemId[item.id].orEmpty()
        val recordDates = itemRecords.asSequence()
            .mapNotNull { record -> runCatching { LocalDate.parse(record.date) }.getOrNull() }
            .toSet()
        val currentMonthCompletedDays = recordDates.asSequence()
            .filter { date -> YearMonth.from(date) == currentMonth }
            .map(LocalDate::getDayOfMonth)
            .toSet()

        CheckInCardUiModel(
            item = item,
            records = itemRecords,
            recordDates = recordDates,
            currentMonthDayCount = currentMonth.lengthOfMonth(),
            currentMonthCompletedDays = currentMonthCompletedDays,
            checkedToday = itemRecords.any { it.date == todayText },
        )
    }
}
