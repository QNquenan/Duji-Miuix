package com.quenan.duji.data.day

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.quenan.duji.data.reminder.DayReminderScheduler
import com.quenan.duji.widget.refreshWidgetsAsync
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class DayRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dataStore: DataStore<Preferences> = getDataStore(appContext)

    fun observeDays(): Flow<List<DayData>> = dataStore.data.map { preferences ->
        decodeDays(preferences[DAYS_KEY].orEmpty())
    }

    suspend fun addDay(day: DayData) {
        var savedDay: DayData? = null
        dataStore.edit { preferences ->
            val days = decodeDays(preferences[DAYS_KEY].orEmpty()).toMutableList()
            val nextId = (days.maxOfOrNull(DayData::id) ?: 0L) + 1L
            savedDay = day.copy(id = nextId, createdAt = System.currentTimeMillis())
            days.add(requireNotNull(savedDay))
            preferences[DAYS_KEY] = encodeDays(days)
        }
        DayReminderScheduler.schedule(appContext, requireNotNull(savedDay))
        appContext.refreshWidgetsAsync()
    }

    suspend fun updateDay(day: DayData) {
        dataStore.edit { preferences ->
            val updated = decodeDays(preferences[DAYS_KEY].orEmpty()).map { current ->
                if (current.id == day.id) day else current
            }
            preferences[DAYS_KEY] = encodeDays(updated)
        }
        DayReminderScheduler.schedule(appContext, day)
        appContext.refreshWidgetsAsync()
    }

    suspend fun deleteDay(day: DayData) {
        dataStore.edit { preferences ->
            val filtered = decodeDays(preferences[DAYS_KEY].orEmpty())
                .filterNot { current -> current.id == day.id }
            preferences[DAYS_KEY] = encodeDays(filtered)
        }
        DayReminderScheduler.cancel(appContext, day.id)
        appContext.refreshWidgetsAsync()
    }

    suspend fun getAllDays(): List<DayData> = dataStore.data.map { preferences ->
        decodeDays(preferences[DAYS_KEY].orEmpty())
    }.first()

    suspend fun importDays(importedDays: List<DayData>) {
        dataStore.edit { preferences ->
            val currentDays = decodeDays(preferences[DAYS_KEY].orEmpty())
            val nextIdStart = (currentDays.maxOfOrNull(DayData::id) ?: 0L) + 1L
            val mergedDays = currentDays.toMutableList()
            importedDays.forEachIndexed { index, day ->
                mergedDays.add(day.copy(id = nextIdStart + index))
            }
            preferences[DAYS_KEY] = encodeDays(mergedDays)
        }
        DayReminderScheduler.rescheduleAll(appContext)
        appContext.refreshWidgetsAsync()
    }

    private fun decodeDays(raw: String): List<DayData> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList(array.length()) {
                for (index in 0 until array.length()) {
                    val obj = array.getJSONObject(index)
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
                            weekDays = obj.optJSONArray("weekDays")?.toIntList().orEmpty(),
                            monthDays = obj.optJSONArray("monthDays")?.toIntList().orEmpty(),
                            isLunar = obj.optBoolean("isLunar"),
                            isPinned = obj.optBoolean("isPinned"),
                            createdAt = obj.optLong("createdAt").takeIf { it > 0L } ?: obj.optLong("id"),
                            reminderEnabled = obj.optBoolean("reminderEnabled"),
                            reminderDaysBefore = obj.optInt("reminderDaysBefore", DEFAULT_REMINDER_DAYS_BEFORE).coerceAtLeast(0),
                            remindOnDay = obj.optBoolean(
                                "remindOnDay",
                                obj.optInt("reminderDaysBefore", DEFAULT_REMINDER_DAYS_BEFORE) == 0,
                            ),
                            reminderHour = obj.optInt("reminderHour", DEFAULT_REMINDER_HOUR).coerceIn(0, 23),
                            reminderMinute = obj.optInt("reminderMinute", DEFAULT_REMINDER_MINUTE).coerceIn(0, 59),
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun encodeDays(days: List<DayData>): String {
        val array = JSONArray()
        days.forEach { day ->
            array.put(
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
                    put("reminderEnabled", day.reminderEnabled)
                    put("reminderDaysBefore", day.reminderDaysBefore)
                    put("remindOnDay", day.remindOnDay)
                    put("reminderHour", day.reminderHour)
                    put("reminderMinute", day.reminderMinute)
                }
            )
        }
        return array.toString()
    }

    private fun JSONArray.toIntList(): List<Int> = buildList(length()) {
        for (i in 0 until length()) add(optInt(i))
    }

    private companion object {
        val DAYS_KEY = stringPreferencesKey("days_json")

        @Volatile
        private var dataStoreInstance: DataStore<Preferences>? = null

        fun getDataStore(context: Context): DataStore<Preferences> {
            return dataStoreInstance ?: synchronized(this) {
                dataStoreInstance ?: PreferenceDataStoreFactory.create(
                    produceFile = { context.preferencesDataStoreFile("days.preferences_pb") }
                ).also { dataStoreInstance = it }
            }
        }
    }
}
