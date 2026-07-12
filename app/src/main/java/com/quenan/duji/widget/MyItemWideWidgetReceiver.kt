package com.quenan.duji.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class MyItemWideWidgetReceiver : BaseDuJiWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MyItemWideWidget()
}
