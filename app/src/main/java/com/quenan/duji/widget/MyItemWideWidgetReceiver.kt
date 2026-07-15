package com.quenan.duji.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.quenan.duji.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class MyItemWideWidgetReceiver : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
        ) {
            val views = RemoteViews(context.packageName, R.layout.my_item_wide_widget_layout)
            runCatching {
                val model = runBlocking(Dispatchers.IO) {
                    WidgetDataProvider(context).loadWideSummary()
                }
                views.setTextViewText(R.id.widget_wide_title, model.title)
                views.setTextViewText(R.id.widget_wide_total_value, model.totalValueText)
                views.setTextViewText(R.id.widget_wide_item_count, model.itemCountText)
                views.setTextViewText(R.id.widget_wide_daily_price, model.totalDailyPriceText)
            }.onFailure {
                views.setTextViewText(R.id.widget_wide_title, "我的物品")
                views.setTextViewText(R.id.widget_wide_total_value, "¥0")
                views.setTextViewText(R.id.widget_wide_item_count, "0")
                views.setTextViewText(R.id.widget_wide_daily_price, "¥0")
            }
            WidgetRemoteViews.setClick(
                context = context,
                views = views,
                viewId = R.id.widget_wide_root,
                intent = WidgetIntentFactory.myItemsIntent(context),
                requestCode = appWidgetId,
            )
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
