package com.quenan.duji.data.item

data class ItemData(
    val id: Long,
    val icon: String,
    val name: String,
    val date: String,
    val price: Int,
    val note: String,
    val isPinned: Boolean,
)

data class ItemStats(
    val totalValue: Int = 0,
    val itemCount: Int = 0,
    val totalDailyPrice: Int = 0,
)

fun ItemEntity.toItemData(): ItemData = ItemData(
    id = id,
    icon = icon,
    name = name,
    date = date,
    price = price,
    note = note,
    isPinned = isPinned,
)

fun ItemData.toEntity(): ItemEntity = ItemEntity(
    id = id,
    icon = icon,
    name = name,
    date = date,
    price = price,
    note = note,
    isPinned = isPinned,
)

fun List<ItemData>.toStats(): ItemStats = ItemStats(
    totalValue = sumOf { it.price },
    itemCount = size,
    totalDailyPrice = sumOf { item ->
        item.price / maxOf(1, daysSince(item.date))
    },
)

private fun daysSince(dateString: String): Int {
    return try {
        val parts = dateString.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts[2].toInt()
        val purchase = java.util.Calendar.getInstance().apply { set(year, month - 1, day) }
        val now = java.util.Calendar.getInstance()
        val diff = now.timeInMillis - purchase.timeInMillis
        maxOf(1, (diff / (1000 * 60 * 60 * 24)).toInt())
    } catch (_: Exception) {
        1
    }
}
