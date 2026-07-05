package com.quenan.duji.data.item

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class ItemRepository(context: Context) {
    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("items.preferences_pb") }
    )

    fun observeItems(): Flow<List<ItemData>> = dataStore.data.map { preferences ->
        decodeItems(preferences[ITEMS_KEY].orEmpty())
    }

    suspend fun addItem(item: ItemData) {
        dataStore.edit { preferences ->
            val items = decodeItems(preferences[ITEMS_KEY].orEmpty()).toMutableList()
            val nextId = (items.maxOfOrNull(ItemData::id) ?: 0L) + 1L
            items.add(item.copy(id = nextId, createdAt = System.currentTimeMillis()))
            preferences[ITEMS_KEY] = encodeItems(items)
        }
    }

    suspend fun updateItem(item: ItemData) {
        dataStore.edit { preferences ->
            val updatedItems = decodeItems(preferences[ITEMS_KEY].orEmpty()).map { current ->
                if (current.id == item.id) item else current
            }
            preferences[ITEMS_KEY] = encodeItems(updatedItems)
        }
    }

    suspend fun deleteItem(item: ItemData) {
        dataStore.edit { preferences ->
            val filteredItems = decodeItems(preferences[ITEMS_KEY].orEmpty())
                .filterNot { current -> current.id == item.id }
            preferences[ITEMS_KEY] = encodeItems(filteredItems)
        }
    }

    private fun decodeItems(raw: String): List<ItemData> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList(array.length()) {
                for (index in 0 until array.length()) {
                    val obj = array.getJSONObject(index)
                    add(
                        ItemData(
                            id = obj.optLong("id"),
                            icon = obj.optString("icon", "📦"),
                            name = obj.optString("name"),
                            date = obj.optString("date"),
                            price = obj.optInt("price"),
                            note = obj.optString("note"),
                            isPinned = obj.optBoolean("isPinned"),
                            createdAt = obj.optLong("createdAt").takeIf { it > 0L }
                                ?: estimateCreatedAt(obj.optString("date"), obj.optLong("id")),
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun encodeItems(items: List<ItemData>): String {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
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
        return array.toString()
    }

    private fun estimateCreatedAt(date: String, id: Long): Long {
        val baseTime = parseItemDateToMillis(date) ?: 0L
        return if (baseTime > 0L) baseTime + id else id
    }

    private companion object {
        val ITEMS_KEY = stringPreferencesKey("items_json")
    }
}
