package com.quenan.duji.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class ThoseDaySquareWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ThoseDaySquareWidget()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.i("DuJiWidget", "receiver onUpdate: ids=${appWidgetIds.contentToString()}")
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            Log.i("DuJiWidget", "receiver onReceive APPWIDGET_UPDATE: ids=${intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)?.contentToString()}")
        }
        super.onReceive(context, intent)
    }
}
