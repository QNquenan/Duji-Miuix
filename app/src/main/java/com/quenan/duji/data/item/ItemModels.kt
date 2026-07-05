package com.quenan.duji.data.item

import java.util.Calendar

data class ItemData(
    val id: Long,
    val icon: String,
    val name: String,
    val date: String,
    val price: Int,
    val note: String,
    val isPinned: Boolean,
    val createdAt: Long,
)

data class ItemStats(
    val totalValue: Int = 0,
    val itemCount: Int = 0,
    val totalDailyPrice: Int = 0,
)

fun List<ItemData>.toStats(): ItemStats = ItemStats(
    totalValue = sumOf { it.price },
    itemCount = size,
    totalDailyPrice = sumOf { item ->
        item.price / maxOf(1, daysSince(item.date))
    },
)

fun parseItemDateToMillis(dateString: String): Long? {
    return runCatching {
        val parts = dateString.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts[2].toInt()
        Calendar.getInstance().apply {
            set(year, month - 1, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }.getOrNull()
}

private fun daysSince(dateString: String): Int {
    return parseItemDateToMillis(dateString)?.let { purchaseMillis ->
        val diff = Calendar.getInstance().timeInMillis - purchaseMillis
        maxOf(1, (diff / (1000 * 60 * 60 * 24)).toInt())
    } ?: 1
}
