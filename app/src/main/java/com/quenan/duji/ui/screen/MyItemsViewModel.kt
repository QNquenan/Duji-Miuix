package com.quenan.duji.ui.screen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quenan.duji.data.item.ItemData
import com.quenan.duji.data.item.ItemRepository
import com.quenan.duji.data.item.ItemStats
import com.quenan.duji.data.item.toStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MyItemsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ItemRepository(application)

    val items = repository.observeItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val stats = items
        .map(List<ItemData>::toStats)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ItemStats())

    fun addItem(
        icon: String,
        name: String,
        date: String,
        price: String,
        note: String,
        isPinned: Boolean,
    ) {
        val parsedPrice = price.toIntOrNull() ?: return
        viewModelScope.launch {
            repository.addItem(
                ItemData(
                    id = 0,
                    icon = icon,
                    name = name.trim(),
                    date = date,
                    price = parsedPrice,
                    note = note,
                    isPinned = isPinned,
                )
            )
        }
    }

    fun deleteItem(item: ItemData) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }
}
