package com.quenan.duji.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import com.quenan.duji.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class ThoseDaySquareWidgetReceiver : AppWidgetProvider() {
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
            val model = runCatching {
                runBlocking(Dispatchers.IO) {
                    WidgetDataProvider(context).loadSquareDay(appWidgetId, context)
                }
            }.getOrNull()

            if (model == null) {
                val views = RemoteViews(
                    context.packageName,
                    R.layout.those_day_square_widget_empty_layout,
                )
                views.setTextViewText(R.id.widget_day_empty_title, "那些日子")
                views.setTextViewText(R.id.widget_day_empty_summary, "点击进入应用重新选择日子")
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } else {
                val views = RemoteViews(context.packageName, R.layout.those_day_square_widget_layout)
                views.setTextViewText(R.id.widget_day_title, model.titlePrefix + model.titleSuffix)
                views.setTextViewText(R.id.widget_day_number, model.numberText)
                views.setTextViewText(R.id.widget_day_unit, model.unitText)
                views.setTextViewText(R.id.widget_day_date, model.dateText)
                views.setViewVisibility(
                    R.id.widget_day_unit,
                    if (model.unitText.isBlank()) View.GONE else View.VISIBLE,
                )
                WidgetRemoteViews.setClick(
                    context = context,
                    views = views,
                    viewId = R.id.widget_day_root,
                    intent = WidgetIntentFactory.detailIntent(context, WidgetTargetType.DAY, model.id),
                    requestCode = appWidgetId,
                )
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
