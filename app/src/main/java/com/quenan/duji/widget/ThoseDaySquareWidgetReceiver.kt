package com.quenan.duji.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget

class ThoseDaySquareWidgetReceiver : BaseDuJiWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ThoseDaySquareWidget()
}
