package com.quenan.duji.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val COLOR_MODE_SYSTEM = 0
const val COLOR_MODE_LIGHT = 1
const val COLOR_MODE_DARK = 2

data class SettingsData(
    val colorModeIndex: Int = COLOR_MODE_SYSTEM,
    val predictiveBackEnabled: Boolean = true,
    val autoCheckUpdates: Boolean = true,
    val notificationPermissionRequested: Boolean = false,
)

class SettingsRepository(context: Context) {
    private val dataStore: DataStore<Preferences> = getDataStore(context.applicationContext)

    fun observeSettings(): Flow<SettingsData> = dataStore.data.map { preferences ->
        SettingsData(
            colorModeIndex = preferences[COLOR_MODE_KEY] ?: COLOR_MODE_SYSTEM,
            predictiveBackEnabled = preferences[PREDICTIVE_BACK_KEY] ?: true,
            autoCheckUpdates = preferences[AUTO_CHECK_UPDATES_KEY] ?: true,
            notificationPermissionRequested = preferences[NOTIFICATION_PERMISSION_REQUESTED_KEY] ?: false,
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

    suspend fun updateAutoCheckUpdates(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_CHECK_UPDATES_KEY] = enabled
        }
    }

    suspend fun markNotificationPermissionRequested() {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_PERMISSION_REQUESTED_KEY] = true
        }
    }

    suspend fun consumeAutoUpdateCheckForToday(today: String): Boolean {
        var shouldCheck = false
        dataStore.edit { preferences ->
            if (preferences[LAST_AUTO_UPDATE_CHECK_DATE_KEY] != today) {
                preferences[LAST_AUTO_UPDATE_CHECK_DATE_KEY] = today
                shouldCheck = true
            }
        }
        return shouldCheck
    }

    private companion object {
        val COLOR_MODE_KEY = intPreferencesKey("color_mode")
        val PREDICTIVE_BACK_KEY = booleanPreferencesKey("predictive_back_enabled")
        val AUTO_CHECK_UPDATES_KEY = booleanPreferencesKey("auto_check_updates")
        val NOTIFICATION_PERMISSION_REQUESTED_KEY = booleanPreferencesKey("notification_permission_requested")
        val LAST_AUTO_UPDATE_CHECK_DATE_KEY = stringPreferencesKey("last_auto_update_check_date")

        @Volatile
        private var dataStoreInstance: DataStore<Preferences>? = null

        fun getDataStore(context: Context): DataStore<Preferences> {
            return dataStoreInstance ?: synchronized(this) {
                dataStoreInstance ?: PreferenceDataStoreFactory.create(
                    produceFile = { context.preferencesDataStoreFile("settings.preferences_pb") }
                ).also { dataStoreInstance = it }
            }
        }
    }
}
