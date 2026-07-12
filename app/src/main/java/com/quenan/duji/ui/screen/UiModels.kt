package com.quenan.duji.ui.screen

import com.quenan.duji.data.day.DayData
import com.quenan.duji.data.day.DayStatus
import com.quenan.duji.data.item.ItemData

data class ItemCardUiModel(
    val item: ItemData,
    val avgPriceText: String,
    val totalPriceText: String,
)

data class DayCardUiModel(
    val day: DayData,
    val status: DayStatus,
)
