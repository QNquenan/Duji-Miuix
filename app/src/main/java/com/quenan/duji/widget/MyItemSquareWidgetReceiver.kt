package com.quenan.duji.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.quenan.duji.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class MyItemSquareWidgetReceiver : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val repository = WidgetSelectionRepository(context)
        appWidgetIds.forEach(repository::clearSelection)
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
        ) {
            val views = RemoteViews(context.packageName, R.layout.my_item_square_widget_layout)
            val model = runCatching {
                runBlocking(Dispatchers.IO) {
                    WidgetDataProvider(context).loadSquareItem(appWidgetId, context)
                }
            }.getOrNull()

            if (model == null) {
                views.setTextViewText(R.id.widget_item_icon, "📦")
                views.setTextViewText(R.id.widget_item_name, "我的物品")
                views.setTextViewText(R.id.widget_item_date, "请点击配置")
                views.setTextViewText(R.id.widget_item_average_price, "")
                views.setTextViewText(R.id.widget_item_total_price, "")
            } else {
                views.setTextViewText(R.id.widget_item_icon, model.icon)
                views.setTextViewText(R.id.widget_item_name, model.name)
                views.setTextViewText(R.id.widget_item_date, model.date)
                views.setTextViewText(R.id.widget_item_average_price, model.avgPriceText)
                views.setTextViewText(R.id.widget_item_total_price, model.totalPriceText)
                WidgetRemoteViews.setClick(
                    context = context,
                    views = views,
                    viewId = R.id.widget_item_root,
                    intent = WidgetIntentFactory.detailIntent(context, WidgetTargetType.ITEM, model.id),
                    requestCode = appWidgetId,
                )
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
