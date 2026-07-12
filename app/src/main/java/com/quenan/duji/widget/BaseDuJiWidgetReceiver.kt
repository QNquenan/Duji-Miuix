package com.quenan.duji.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

abstract class BaseDuJiWidgetReceiver : GlanceAppWidgetReceiver() {
    private companion object {
        const val TAG = "DuJiWidget"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        Log.i(TAG, "receiver onUpdate: receiver=${javaClass.simpleName}, ids=${appWidgetIds.contentToString()}")
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                val glanceManager = GlanceAppWidgetManager(appContext)
                appWidgetIds.forEach { appWidgetId ->
                    val glanceId = glanceManager.getGlanceIdBy(appWidgetId)
                    Log.i(TAG, "receiver direct update: receiver=${javaClass.simpleName}, appWidgetId=$appWidgetId, glanceId=$glanceId")
                    glanceAppWidget.update(appContext, glanceId)
                }
                Log.i(TAG, "receiver direct update completed: receiver=${javaClass.simpleName}")
            } catch (error: Exception) {
                Log.e(TAG, "receiver direct update failed: receiver=${javaClass.simpleName}", error)
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: android.content.Intent) {
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            Log.i(TAG, "receiver onReceive APPWIDGET_UPDATE: receiver=${javaClass.simpleName}, ids=${intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)?.contentToString()}")
        }
        super.onReceive(context, intent)
    }
}
