package com.quenan.duji.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object WidgetUpdateDispatcher {
    private const val TAG = "DuJiWidget"
    private val updateScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    suspend fun refreshAll(context: Context) {
        MyItemWideWidget().updateAll(context)
        MyItemSquareWidget().updateAll(context)
        ThoseDaySquareWidget().updateAll(context)
    }

    fun appWidgetIds(context: Context, receiverClass: Class<*>): IntArray {
        return AppWidgetManager.getInstance(context).getAppWidgetIds(
            ComponentName(context, receiverClass)
        )
    }

    fun updateConfiguredWidget(context: Context, appWidgetId: Int, selection: WidgetSelection) {
        val appContext = context.applicationContext
        Log.i(TAG, "schedule update: appWidgetId=$appWidgetId, selection=$selection")
        updateScope.launch {
            repeat(4) { attempt ->
                try {
                    Log.i(TAG, "update attempt=${attempt + 1}/4: appWidgetId=$appWidgetId")
                    val targetGlanceId = GlanceAppWidgetManager(appContext).getGlanceIdBy(appWidgetId)
                    Log.i(TAG, "glance id resolved: appWidgetId=$appWidgetId, glanceId=$targetGlanceId")
                    when (selection.type) {
                        WidgetSelectionType.MY_ITEM_SQUARE -> MyItemSquareWidget().update(appContext, targetGlanceId)
                        WidgetSelectionType.THOSE_DAY_SQUARE -> ThoseDaySquareWidget().update(appContext, targetGlanceId)
                        WidgetSelectionType.MY_ITEM_WIDE -> MyItemWideWidget().update(appContext, targetGlanceId)
                    }
                    Log.i(TAG, "single widget update completed: appWidgetId=$appWidgetId")
                    return@launch
                } catch (error: Exception) {
                    Log.w(TAG, "single widget update failed: appWidgetId=$appWidgetId, attempt=${attempt + 1}/4", error)
                    if (attempt < 3) delay(150L * (attempt + 1))
                }
            }

            Log.w(TAG, "falling back to updateAll: appWidgetId=$appWidgetId, type=${selection.type}")
            when (selection.type) {
                WidgetSelectionType.MY_ITEM_SQUARE -> MyItemSquareWidget().updateAll(appContext)
                WidgetSelectionType.THOSE_DAY_SQUARE -> ThoseDaySquareWidget().updateAll(appContext)
                WidgetSelectionType.MY_ITEM_WIDE -> MyItemWideWidget().updateAll(appContext)
            }
            Log.i(TAG, "updateAll fallback completed: appWidgetId=$appWidgetId")
        }
    }

    suspend fun hasAnyWidget(context: Context): Boolean {
        val manager = GlanceAppWidgetManager(context)
        return manager.getGlanceIds(MyItemWideWidget::class.java).isNotEmpty() ||
            manager.getGlanceIds(MyItemSquareWidget::class.java).isNotEmpty() ||
            manager.getGlanceIds(ThoseDaySquareWidget::class.java).isNotEmpty()
    }

}
