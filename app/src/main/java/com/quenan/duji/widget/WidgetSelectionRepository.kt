package com.quenan.duji.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class WidgetSelectionRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dataStore: DataStore<Preferences> = getDataStore(appContext)

    suspend fun saveSelection(appWidgetId: Int, selection: WidgetSelection) {
        dataStore.edit { preferences ->
            preferences[typeKey(appWidgetId)] = selection.type.ordinal
            preferences[targetIdKey(appWidgetId)] = selection.targetId
        }
    }

    suspend fun getSelection(appWidgetId: Int): WidgetSelection? {
        return dataStore.data.map { preferences ->
            val typeOrdinal = preferences[typeKey(appWidgetId)] ?: return@map null
            val targetId = preferences[targetIdKey(appWidgetId)] ?: return@map null
            val type = WidgetSelectionType.entries.getOrNull(typeOrdinal) ?: return@map null
            WidgetSelection(type = type, targetId = targetId)
        }.first()
    }

    suspend fun clearSelection(appWidgetId: Int) {
        dataStore.edit { preferences ->
            preferences.remove(typeKey(appWidgetId))
            preferences.remove(targetIdKey(appWidgetId))
        }
    }

    private fun typeKey(appWidgetId: Int) = intPreferencesKey("widget_selection_type_$appWidgetId")

    private fun targetIdKey(appWidgetId: Int) = longPreferencesKey("widget_selection_target_id_$appWidgetId")

    private companion object {
        @Volatile
        private var dataStoreInstance: DataStore<Preferences>? = null

        fun getDataStore(context: Context): DataStore<Preferences> {
            return dataStoreInstance ?: synchronized(this) {
                dataStoreInstance ?: PreferenceDataStoreFactory.create(
                    produceFile = { context.preferencesDataStoreFile("widget_selection.preferences_pb") }
                ).also { dataStoreInstance = it }
            }
        }
    }
}

enum class WidgetSelectionType {
    MY_ITEM_WIDE,
    MY_ITEM_SQUARE,
    THOSE_DAY_SQUARE,
}

data class WidgetSelection(
    val type: WidgetSelectionType,
    val targetId: Long,
)
