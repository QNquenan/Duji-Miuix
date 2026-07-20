package com.quenan.duji.ui.screen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quenan.duji.data.checkin.CheckInItem
import com.quenan.duji.data.checkin.CheckInRecord
import com.quenan.duji.data.checkin.CheckInRepository
import java.time.LocalDate
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
        val today = LocalDate.now().toString()
        items.map { item ->
            val itemRecords = allRecords.filter { it.itemId == item.id }
            CheckInCardUiModel(
                item = item,
                records = itemRecords,
                checkedToday = itemRecords.any { it.date == today },
            )
        }
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
        viewModelScope.launch {
            onResult(repository.checkIn(itemId, LocalDate.now().toString()))
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
