package com.quenan.duji.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val COLOR_MODE_SYSTEM = 0
const val COLOR_MODE_LIGHT = 1
const val COLOR_MODE_DARK = 2

data class SettingsData(
    val colorModeIndex: Int = COLOR_MODE_SYSTEM,
    val predictiveBackEnabled: Boolean = true,
)

class SettingsRepository(context: Context) {
    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("settings.preferences_pb") }
    )

    fun observeSettings(): Flow<SettingsData> = dataStore.data.map { preferences ->
        SettingsData(
            colorModeIndex = preferences[COLOR_MODE_KEY] ?: COLOR_MODE_SYSTEM,
            predictiveBackEnabled = preferences[PREDICTIVE_BACK_KEY] ?: true,
        )
    }

    suspend fun updateColorMode(index: Int) {
        dataStore.edit { preferences ->
            preferences[COLOR_MODE_KEY] = index
        }
    }

    suspend fun updatePredictiveBackEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PREDICTIVE_BACK_KEY] = enabled
        }
    }

    private companion object {
        val COLOR_MODE_KEY = intPreferencesKey("color_mode")
        val PREDICTIVE_BACK_KEY = booleanPreferencesKey("predictive_back_enabled")
    }
}
