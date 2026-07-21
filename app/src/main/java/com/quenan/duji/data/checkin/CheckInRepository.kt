package com.quenan.duji.data.checkin

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class CheckInRepository(context: Context) {
    private val dataStore = getDataStore(context.applicationContext)

    fun observeItems(): Flow<List<CheckInItem>> = dataStore.data.map { preferences ->
        decodeItems(preferences[ITEMS_KEY].orEmpty())
    }

    fun observeRecords(): Flow<List<CheckInRecord>> = dataStore.data.map { preferences ->
        decodeRecords(preferences[RECORDS_KEY].orEmpty())
    }

    suspend fun addItem(item: CheckInItem) {
        dataStore.edit { preferences ->
            val items = decodeItems(preferences[ITEMS_KEY].orEmpty()).toMutableList()
            val nextId = (items.maxOfOrNull(CheckInItem::id) ?: 0L) + 1L
            items += item.copy(id = nextId, createdAt = System.currentTimeMillis())
            preferences[ITEMS_KEY] = encodeItems(items)
        }
    }

    suspend fun checkIn(itemId: Long, date: String): Boolean {
        var added = false
        dataStore.edit { preferences ->
            val records = decodeRecords(preferences[RECORDS_KEY].orEmpty()).toMutableList()
            if (records.none { it.itemId == itemId && it.date == date }) {
                records += CheckInRecord(itemId = itemId, date = date)
                preferences[RECORDS_KEY] = encodeRecords(records)
                added = true
            }
        }
        return added
    }

    suspend fun cancelCheckIn(itemId: Long, date: String): Boolean {
        var removed = false
        dataStore.edit { preferences ->
            val records = decodeRecords(preferences[RECORDS_KEY].orEmpty())
            val remainingRecords = records.filterNot { record ->
                record.itemId == itemId && record.date == date
            }
            if (remainingRecords.size != records.size) {
                preferences[RECORDS_KEY] = encodeRecords(remainingRecords)
                removed = true
            }
        }
        return removed
    }

    suspend fun updateItem(item: CheckInItem) {
        dataStore.edit { preferences ->
            val updatedItems = decodeItems(preferences[ITEMS_KEY].orEmpty()).map { current ->
                if (current.id == item.id) item else current
            }
            preferences[ITEMS_KEY] = encodeItems(updatedItems)
        }
    }

    suspend fun deleteItem(itemId: Long) {
        dataStore.edit { preferences ->
            val remainingItems = decodeItems(preferences[ITEMS_KEY].orEmpty())
                .filterNot { item -> item.id == itemId }
            val remainingRecords = decodeRecords(preferences[RECORDS_KEY].orEmpty())
                .filterNot { record -> record.itemId == itemId }
            preferences[ITEMS_KEY] = encodeItems(remainingItems)
            preferences[RECORDS_KEY] = encodeRecords(remainingRecords)
        }
    }

    suspend fun getAllItems(): List<CheckInItem> = dataStore.data.map { preferences ->
        decodeItems(preferences[ITEMS_KEY].orEmpty())
    }.first()

    suspend fun getAllRecords(): List<CheckInRecord> = dataStore.data.map { preferences ->
        decodeRecords(preferences[RECORDS_KEY].orEmpty())
    }.first()

    suspend fun importData(importedItems: List<CheckInItem>, importedRecords: List<CheckInRecord>) {
        dataStore.edit { preferences ->
            val currentItems = decodeItems(preferences[ITEMS_KEY].orEmpty()).toMutableList()
            val nextIdStart = (currentItems.maxOfOrNull(CheckInItem::id) ?: 0L) + 1L
            val idMap = mutableMapOf<Long, Long>()
            importedItems.forEachIndexed { index, item ->
                val newId = nextIdStart + index
                idMap[item.id] = newId
                currentItems += item.copy(id = newId)
            }

            val currentRecords = decodeRecords(preferences[RECORDS_KEY].orEmpty()).toMutableList()
            importedRecords.forEach { record ->
                val mappedItemId = idMap[record.itemId] ?: return@forEach
                if (currentRecords.none { it.itemId == mappedItemId && it.date == record.date }) {
                    currentRecords += record.copy(itemId = mappedItemId)
                }
            }
            preferences[ITEMS_KEY] = encodeItems(currentItems)
            preferences[RECORDS_KEY] = encodeRecords(currentRecords)
        }
    }

    private fun decodeItems(raw: String): List<CheckInItem> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList(array.length()) {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(
                        CheckInItem(
                            id = item.optLong("id"),
                            emoji = item.optString("emoji", "🏋️"),
                            name = item.optString("name"),
                            colorArgb = item.optLong("colorArgb", DEFAULT_COLOR_ARGB),
                            createdAt = item.optLong("createdAt"),
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun decodeRecords(raw: String): List<CheckInRecord> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList(array.length()) {
                for (index in 0 until array.length()) {
                    val record = array.getJSONObject(index)
                    add(
                        CheckInRecord(
                            itemId = record.optLong("itemId"),
                            date = record.optString("date"),
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun encodeItems(items: List<CheckInItem>): String = JSONArray().apply {
        items.forEach { item ->
            put(
                JSONObject().apply {
                    put("id", item.id)
                    put("emoji", item.emoji)
                    put("name", item.name)
                    put("colorArgb", item.colorArgb)
                    put("createdAt", item.createdAt)
                }
            )
        }
    }.toString()

    private fun encodeRecords(records: List<CheckInRecord>): String = JSONArray().apply {
        records.forEach { record ->
            put(
                JSONObject().apply {
                    put("itemId", record.itemId)
                    put("date", record.date)
                }
            )
        }
    }.toString()

    private companion object {
        val ITEMS_KEY = stringPreferencesKey("check_in_items_json")
        val RECORDS_KEY = stringPreferencesKey("check_in_records_json")
        const val DEFAULT_COLOR_ARGB = 0xFF5EBD7DL

        @Volatile
        private var dataStoreInstance: DataStore<Preferences>? = null

        fun getDataStore(context: Context): DataStore<Preferences> {
            return dataStoreInstance ?: synchronized(this) {
                dataStoreInstance ?: PreferenceDataStoreFactory.create(
                    produceFile = { context.preferencesDataStoreFile("check_in.preferences_pb") },
                ).also { dataStoreInstance = it }
            }
        }
    }
}
