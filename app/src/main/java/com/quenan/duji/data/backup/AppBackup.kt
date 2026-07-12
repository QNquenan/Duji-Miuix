package com.quenan.duji.data.backup

import com.quenan.duji.data.day.DayData
import com.quenan.duji.data.day.DayType
import com.quenan.duji.data.day.RepeatCycle
import com.quenan.duji.data.item.ItemData
import org.json.JSONArray
import org.json.JSONObject

data class AppBackup(
    val version: Int,
    val items: List<ItemData>,
    val days: List<DayData>,
)

fun AppBackup.toJsonString(): String {
    return JSONObject().apply {
        put("version", version)
        put("items", JSONArray().apply {
            items.forEach { item ->
                put(
                    JSONObject().apply {
                        put("id", item.id)
                        put("icon", item.icon)
                        put("name", item.name)
                        put("date", item.date)
                        put("price", item.price)
                        put("note", item.note)
                        put("isPinned", item.isPinned)
                        put("createdAt", item.createdAt)
                    }
                )
            }
        })
        put("days", JSONArray().apply {
            days.forEach { day ->
                put(
                    JSONObject().apply {
                        put("id", day.id)
                        put("emoji", day.emoji)
                        put("emojiName", day.emojiName)
                        put("name", day.name)
                        put("type", day.type.name)
                        put("repeatCycle", day.repeatCycle.name)
                        put("targetDate", day.targetDate)
                        put("note", day.note)
                        put("weekDays", JSONArray(day.weekDays))
                        put("monthDays", JSONArray(day.monthDays))
                        put("isLunar", day.isLunar)
                        put("isPinned", day.isPinned)
                        put("createdAt", day.createdAt)
                    }
                )
            }
        })
    }.toString()
}

fun parseAppBackup(raw: String): AppBackup {
    val json = JSONObject(raw)
    val itemsArray = json.optJSONArray("items") ?: JSONArray()
    val daysArray = json.optJSONArray("days") ?: JSONArray()

    val items = buildList(itemsArray.length()) {
        for (index in 0 until itemsArray.length()) {
            val obj = itemsArray.getJSONObject(index)
            add(
                ItemData(
                    id = obj.optLong("id"),
                    icon = obj.optString("icon", "📦"),
                    name = obj.optString("name"),
                    date = obj.optString("date"),
                    price = obj.optInt("price"),
                    note = obj.optString("note"),
                    isPinned = obj.optBoolean("isPinned"),
                    createdAt = obj.optLong("createdAt"),
                )
            )
        }
    }

    val days = buildList(daysArray.length()) {
        for (index in 0 until daysArray.length()) {
            val obj = daysArray.getJSONObject(index)
            val weekDaysArray = obj.optJSONArray("weekDays") ?: JSONArray()
            val monthDaysArray = obj.optJSONArray("monthDays") ?: JSONArray()
            add(
                DayData(
                    id = obj.optLong("id"),
                    emoji = obj.optString("emoji", "📅"),
                    emojiName = obj.optString("emojiName", "日历"),
                    name = obj.optString("name"),
                    type = DayType.valueOf(obj.optString("type", DayType.DAYS.name)),
                    repeatCycle = RepeatCycle.valueOf(obj.optString("repeatCycle", RepeatCycle.NONE.name)),
                    targetDate = obj.optString("targetDate"),
                    note = obj.optString("note"),
                    weekDays = buildList(weekDaysArray.length()) {
                        for (weekIndex in 0 until weekDaysArray.length()) add(weekDaysArray.optInt(weekIndex))
                    },
                    monthDays = buildList(monthDaysArray.length()) {
                        for (monthIndex in 0 until monthDaysArray.length()) add(monthDaysArray.optInt(monthIndex))
                    },
                    isLunar = obj.optBoolean("isLunar"),
                    isPinned = obj.optBoolean("isPinned"),
                    createdAt = obj.optLong("createdAt"),
                )
            )
        }
    }

    return AppBackup(
        version = json.optInt("version", 1),
        items = items,
        days = days,
    )
}
