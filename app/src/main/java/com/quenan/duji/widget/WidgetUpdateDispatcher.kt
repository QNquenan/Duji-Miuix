package com.quenan.duji.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
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
        Log.i(TAG, "schedule receiver update: appWidgetId=$appWidgetId, selection=$selection")
        updateScope.launch {
            delay(400L)
            requestSystemUpdate(appContext, appWidgetId, selection.type)
        }
    }

    private fun requestSystemUpdate(context: Context, appWidgetId: Int, type: WidgetSelectionType) {
        val receiverClass = when (type) {
            WidgetSelectionType.MY_ITEM_SQUARE -> MyItemSquareWidgetReceiver::class.java
            WidgetSelectionType.THOSE_DAY_SQUARE -> ThoseDaySquareWidgetReceiver::class.java
            WidgetSelectionType.MY_ITEM_WIDE -> MyItemWideWidgetReceiver::class.java
        }
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
            component = ComponentName(context, receiverClass)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
        }
        Log.i(TAG, "request system widget update: appWidgetId=$appWidgetId, receiver=${receiverClass.simpleName}")
        context.sendBroadcast(intent)
    }

    suspend fun hasAnyWidget(context: Context): Boolean {
        val manager = GlanceAppWidgetManager(context)
        return manager.getGlanceIds(MyItemWideWidget::class.java).isNotEmpty() ||
            manager.getGlanceIds(MyItemSquareWidget::class.java).isNotEmpty() ||
            manager.getGlanceIds(ThoseDaySquareWidget::class.java).isNotEmpty()
    }

}
