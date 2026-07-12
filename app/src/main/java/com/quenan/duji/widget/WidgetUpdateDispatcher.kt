package com.quenan.duji.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll

object WidgetUpdateDispatcher {
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

    fun notifyAppWidgetChanged(context: Context, receiverClass: Class<*>, appWidgetId: Int) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, receiverClass)
        if (appWidgetManager.getAppWidgetIds(componentName).contains(appWidgetId)) {
            appWidgetManager.updateAppWidgetOptions(appWidgetId, appWidgetManager.getAppWidgetOptions(appWidgetId))
        }
    }

    suspend fun updateConfiguredWidget(context: Context, appWidgetId: Int, selection: WidgetSelection) {
        val manager = GlanceAppWidgetManager(context)
        val targetGlanceId = manager.getGlanceIdBy(appWidgetId)

        when (selection.type) {
            WidgetSelectionType.MY_ITEM_SQUARE -> MyItemSquareWidget().update(context, targetGlanceId)
            WidgetSelectionType.THOSE_DAY_SQUARE -> ThoseDaySquareWidget().update(context, targetGlanceId)
            WidgetSelectionType.MY_ITEM_WIDE -> MyItemWideWidget().update(context, targetGlanceId)
        }
    }

    suspend fun hasAnyWidget(context: Context): Boolean {
        val manager = GlanceAppWidgetManager(context)
        return manager.getGlanceIds(MyItemWideWidget::class.java).isNotEmpty() ||
            manager.getGlanceIds(MyItemSquareWidget::class.java).isNotEmpty() ||
            manager.getGlanceIds(ThoseDaySquareWidget::class.java).isNotEmpty()
    }

}
