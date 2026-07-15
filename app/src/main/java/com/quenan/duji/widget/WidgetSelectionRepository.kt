package com.quenan.duji.widget

import android.content.Context

class WidgetSelectionRepository(context: Context) {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveSelection(appWidgetId: Int, selection: WidgetSelection): Boolean {
        return preferences.edit()
            .putString(selectionKey(appWidgetId), selection.type.name)
            .putLong(targetIdKey(appWidgetId), selection.targetId)
            .commit()
    }

    fun getSelection(appWidgetId: Int): WidgetSelection? {
        val typeName = preferences.getString(selectionKey(appWidgetId), null) ?: return null
        val targetId = if (!preferences.contains(targetIdKey(appWidgetId))) return null
        else preferences.getLong(targetIdKey(appWidgetId), 0L)
        val type = runCatching { WidgetSelectionType.valueOf(typeName) }.getOrNull() ?: return null
        return WidgetSelection(type = type, targetId = targetId)
    }

    fun clearSelection(appWidgetId: Int): Boolean {
        return preferences.edit()
            .remove(selectionKey(appWidgetId))
            .remove(targetIdKey(appWidgetId))
            .commit()
    }

    private fun selectionKey(appWidgetId: Int) = "widget_selection_type_$appWidgetId"

    private fun targetIdKey(appWidgetId: Int) = "widget_selection_target_id_$appWidgetId"

    private companion object {
        const val PREFS_NAME = "widget_data"
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
