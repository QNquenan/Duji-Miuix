package com.quenan.duji.ui.screen

import androidx.compose.runtime.Immutable
import com.quenan.duji.data.day.DayData
import com.quenan.duji.data.day.DayStatus
import com.quenan.duji.data.item.ItemData

@Immutable
data class ItemCardUiModel(
    val item: ItemData,
    val title: String,
    val iconText: String,
    val dateText: String,
    val avgPriceText: String,
    val totalPriceText: String,
    val isPinned: Boolean,
)

@Immutable
data class DayCardUiModel(
    val day: DayData,
    val title: String,
    val iconText: String,
    val dateText: String,
    val status: DayStatus,
    val isPinned: Boolean,
)
