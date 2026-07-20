package com.quenan.duji.ui.screen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quenan.duji.data.item.ItemData
import com.quenan.duji.data.item.ItemRepository
import com.quenan.duji.data.item.ItemStats
import com.quenan.duji.data.item.buildAvgPriceText
import com.quenan.duji.data.item.buildTotalPriceText
import com.quenan.duji.data.item.parseItemDateToMillis
import com.quenan.duji.data.item.toStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MyItemsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ItemRepository(application)
    private val sortOption = MutableStateFlow(ItemSortOption.default())

    private val sortedItems = combine(repository.observeItems(), sortOption) { itemList, option ->
        itemList.sortedWith(itemComparator(option))
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val items = sortedItems

    val itemCardModels = sortedItems
        .map { itemList -> itemList.toItemCardModels() }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currentSort = sortOption
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ItemSortOption.default())

    val stats = sortedItems
        .map(List<ItemData>::toStats)
        .flowOn(Dispatchers.Default)
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
                    createdAt = 0L,
                )
            )
        }
    }

    fun updateItem(
        id: Long,
        icon: String,
        name: String,
        date: String,
        price: String,
        note: String,
        isPinned: Boolean,
    ) {
        val parsedPrice = price.toIntOrNull() ?: return
        viewModelScope.launch {
            repository.updateItem(
                ItemData(
                    id = id,
                    icon = icon,
                    name = name.trim(),
                    date = date,
                    price = parsedPrice,
                    note = note,
                    isPinned = isPinned,
                    createdAt = items.value.firstOrNull { it.id == id }?.createdAt ?: 0L,
                )
            )
        }
    }

    fun deleteItem(item: ItemData) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun updateSort(field: ItemSortField, direction: SortDirection) {
        sortOption.update { ItemSortOption(field = field, direction = direction) }
    }
}

enum class ItemSortField {
    CREATED_AT,
    PURCHASE_DATE,
}

enum class SortDirection {
    ASC,
    DESC,
}

data class ItemSortOption(
    val field: ItemSortField,
    val direction: SortDirection,
) {
    companion object {
        fun default() = ItemSortOption(
            field = ItemSortField.CREATED_AT,
            direction = SortDirection.DESC,
        )
    }
}

private fun List<ItemData>.toItemCardModels(): List<ItemCardUiModel> {
    return map { item ->
        ItemCardUiModel(
            item = item,
            title = item.name,
            iconText = item.icon,
            dateText = item.date,
            avgPriceText = buildAvgPriceText(item.price, item.date),
            totalPriceText = buildTotalPriceText(item.price),
            isPinned = item.isPinned,
        )
    }
}

private fun itemComparator(option: ItemSortOption): Comparator<ItemData> {
    val fieldComparator = when (option.field) {
        ItemSortField.CREATED_AT -> compareBy<ItemData> { it.createdAt }
        ItemSortField.PURCHASE_DATE -> compareBy<ItemData> {
            parseItemDateToMillis(it.date) ?: Long.MIN_VALUE
        }
    }
    val directionalComparator = when (option.direction) {
        SortDirection.ASC -> fieldComparator
        SortDirection.DESC -> fieldComparator.reversed()
    }
    return compareByDescending<ItemData> { it.isPinned }
        .then(directionalComparator)
        .thenByDescending(ItemData::id)
}
