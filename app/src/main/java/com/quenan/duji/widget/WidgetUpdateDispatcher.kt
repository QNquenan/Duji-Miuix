package com.quenan.duji.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object WidgetUpdateDispatcher {
    private const val TAG = "DuJiWidget"
    private val updateScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val updateMutex = Mutex()

    fun requestRefresh(context: Context) {
        val appContext = context.applicationContext
        updateScope.launch {
            refreshAll(appContext)
        }
    }

    suspend fun refreshAll(context: Context) {
        updateMutex.withLock {
            val appContext = context.applicationContext
            updateInstances(
                appContext,
                MyItemWideWidgetReceiver::class.java,
            ) { widgetId, manager ->
                MyItemWideWidgetReceiver.updateAppWidget(appContext, manager, widgetId)
            }
            updateInstances(
                appContext,
                MyItemSquareWidgetReceiver::class.java,
            ) { widgetId, manager ->
                MyItemSquareWidgetReceiver.updateAppWidget(appContext, manager, widgetId)
            }
            updateInstances(
                appContext,
                ThoseDaySquareWidgetReceiver::class.java,
            ) { widgetId, manager ->
                ThoseDaySquareWidgetReceiver.updateAppWidget(appContext, manager, widgetId)
            }
        }
    }

    fun updateConfiguredWidget(context: Context, appWidgetId: Int, selection: WidgetSelection) {
        val appContext = context.applicationContext
        runBlocking(Dispatchers.IO) {
            updateMutex.withLock {
                val manager = AppWidgetManager.getInstance(appContext)
                Log.i(TAG, "configured widget update: appWidgetId=$appWidgetId, selection=$selection")
                when (selection.type) {
                    WidgetSelectionType.MY_ITEM_SQUARE -> {
                        MyItemSquareWidgetReceiver.updateAppWidget(appContext, manager, appWidgetId)
                    }
                    WidgetSelectionType.THOSE_DAY_SQUARE -> {
                        ThoseDaySquareWidgetReceiver.updateAppWidget(appContext, manager, appWidgetId)
                    }
                    WidgetSelectionType.MY_ITEM_WIDE -> {
                        MyItemWideWidgetReceiver.updateAppWidget(appContext, manager, appWidgetId)
                    }
                }
            }
        }
    }

    fun appWidgetIds(context: Context, receiverClass: Class<*>): IntArray {
        return AppWidgetManager.getInstance(context).getAppWidgetIds(
            ComponentName(context, receiverClass),
        )
    }

    suspend fun hasAnyWidget(context: Context): Boolean {
        return appWidgetIds(context, MyItemWideWidgetReceiver::class.java).isNotEmpty() ||
            appWidgetIds(context, MyItemSquareWidgetReceiver::class.java).isNotEmpty() ||
            appWidgetIds(context, ThoseDaySquareWidgetReceiver::class.java).isNotEmpty()
    }

    private fun updateInstances(
        context: Context,
        receiverClass: Class<*>,
        update: (Int, AppWidgetManager) -> Unit,
    ) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, receiverClass))
        Log.i(TAG, "direct refresh: receiver=${receiverClass.simpleName}, ids=${ids.contentToString()}")
        ids.forEach { widgetId -> update(widgetId, manager) }
    }
}
