package com.quenan.duji.data.backup

import com.quenan.duji.data.day.DayData
import com.quenan.duji.data.day.DayType
import com.quenan.duji.data.day.RepeatCycle
import com.quenan.duji.data.item.ItemData
import org.json.JSONArray
import org.json.JSONObject

const val APP_BACKUP_VERSION = 1

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
    val json = runCatching { JSONObject(raw) }
        .getOrElse { throw IllegalArgumentException("备份文件不是合法 JSON") }

    val version = if (json.has("version")) json.optInt("version", -1) else -1
    require(version > 0) { "备份文件缺少 version 字段" }
    require(version <= APP_BACKUP_VERSION) { "备份文件版本过高，当前应用暂不支持导入" }
    require(json.has("items")) { "备份文件缺少 items 字段" }
    require(json.has("days")) { "备份文件缺少 days 字段" }

    val itemsArray = json.optJSONArray("items")
        ?: throw IllegalArgumentException("备份文件中的 items 不是数组")
    val daysArray = json.optJSONArray("days")
        ?: throw IllegalArgumentException("备份文件中的 days 不是数组")

    val items = buildList(itemsArray.length()) {
        for (index in 0 until itemsArray.length()) {
            val obj = itemsArray.optJSONObject(index)
                ?: throw IllegalArgumentException("items[$index] 不是对象")
            val name = obj.optString("name").trim()
            require(name.isNotEmpty()) { "items[$index].name 不能为空" }
            val date = obj.optString("date").trim()
            require(date.isNotEmpty()) { "items[$index].date 不能为空" }
            add(
                ItemData(
                    id = obj.optLong("id"),
                    icon = obj.optString("icon", "📦"),
                    name = name,
                    date = date,
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
            val obj = daysArray.optJSONObject(index)
                ?: throw IllegalArgumentException("days[$index] 不是对象")
            val weekDaysArray = obj.optJSONArray("weekDays") ?: JSONArray()
            val monthDaysArray = obj.optJSONArray("monthDays") ?: JSONArray()
            val name = obj.optString("name").trim()
            require(name.isNotEmpty()) { "days[$index].name 不能为空" }
            val targetDate = obj.optString("targetDate").trim()
            require(targetDate.isNotEmpty()) { "days[$index].targetDate 不能为空" }
            val typeName = obj.optString("type", DayType.DAYS.name)
            val repeatCycleName = obj.optString("repeatCycle", RepeatCycle.NONE.name)
            val dayType = runCatching { DayType.valueOf(typeName) }
                .getOrElse { throw IllegalArgumentException("days[$index].type 无效：$typeName") }
            val repeatCycle = runCatching { RepeatCycle.valueOf(repeatCycleName) }
                .getOrElse { throw IllegalArgumentException("days[$index].repeatCycle 无效：$repeatCycleName") }
            add(
                DayData(
                    id = obj.optLong("id"),
                    emoji = obj.optString("emoji", "📅"),
                    emojiName = obj.optString("emojiName", "日历"),
                    name = name,
                    type = dayType,
                    repeatCycle = repeatCycle,
                    targetDate = targetDate,
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
        version = version,
        items = items,
        days = days,
    )
}
