package com.quenan.duji.ui.screen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.quenan.duji.data.item.DuJiDatabase
import com.quenan.duji.data.item.ItemData
import com.quenan.duji.data.item.ItemEntity
import com.quenan.duji.data.item.ItemRepository
import com.quenan.duji.data.item.ItemStats
import com.quenan.duji.data.item.toEntity
import com.quenan.duji.data.item.toItemData
import com.quenan.duji.data.item.toStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MyItemsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = Room.databaseBuilder(
        application,
        DuJiDatabase::class.java,
        "duji.db",
    ).build()

    private val repository = ItemRepository(database.itemDao())

    val items = repository.observeItems()
        .map { entities -> entities.map(ItemEntity::toItemData) }
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
                ItemEntity(
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
            repository.deleteItem(item.toEntity())
        }
    }
}
