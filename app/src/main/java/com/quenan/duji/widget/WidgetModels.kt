package com.quenan.duji.widget

import com.quenan.duji.data.day.DayData
import com.quenan.duji.data.day.computeStatus
import com.quenan.duji.data.item.ItemData
import com.quenan.duji.data.item.ItemStats
import com.quenan.duji.data.item.buildAvgPriceText
import com.quenan.duji.data.item.buildTotalPriceText

data class ItemWidgetModel(
    val id: Long,
    val icon: String,
    val name: String,
    val date: String,
    val avgPriceText: String,
    val totalPriceText: String,
    val isPinned: Boolean,
)

data class ItemWideSummaryWidgetModel(
    val title: String,
    val totalValueText: String,
    val itemCountText: String,
    val totalDailyPriceText: String,
)

data class DayWidgetModel(
    val id: Long,
    val titlePrefix: String,
    val titleSuffix: String,
    val numberText: String,
    val unitText: String,
    val dateText: String,
    val fullStatusText: String,
)

fun ItemData.toWideWidgetModel(): ItemWidgetModel = ItemWidgetModel(
    id = id,
    icon = icon,
    name = name,
    date = date,
    avgPriceText = buildAvgPriceText(price, date),
    totalPriceText = buildTotalPriceText(price),
    isPinned = isPinned,
)

fun ItemData.toSquareWidgetModel(): ItemWidgetModel = toWideWidgetModel()

fun ItemStats.toWideSummaryWidgetModel(): ItemWideSummaryWidgetModel = ItemWideSummaryWidgetModel(
    title = "我的物品",
    totalValueText = "¥$totalValue",
    itemCountText = itemCount.toString(),
    totalDailyPriceText = "¥$totalDailyPrice",
)

fun DayData.toWidgetModel(): DayWidgetModel {
    val status = computeStatus()
    val rawStatus = status.statusText.replace(" ", "")
    val suffix = when {
        rawStatus.startsWith("已经") -> "已经"
        rawStatus.startsWith("还有") -> "还有"
        rawStatus.startsWith("就是今天") -> ""
        else -> rawStatus
    }
    val number = when {
        rawStatus.startsWith("已经") -> rawStatus.removePrefix("已经").removeSuffix("天").removeSuffix("周年")
        rawStatus.startsWith("还有") -> rawStatus.removePrefix("还有").removeSuffix("天").removeSuffix("周年")
        rawStatus.startsWith("就是今天") -> "就是今天"
        else -> status.diff.toString()
    }.ifBlank { "0" }
    val unit = when {
        rawStatus.startsWith("就是今天") -> ""
        rawStatus.contains("周年") -> "周年"
        else -> "天"
    }
    return DayWidgetModel(
        id = id,
        titlePrefix = name,
        titleSuffix = suffix,
        numberText = number,
        unitText = unit,
        dateText = status.dateText,
        fullStatusText = status.statusText,
    )
}
