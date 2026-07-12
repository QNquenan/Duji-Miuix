package com.quenan.duji.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.quenan.duji.R

abstract class BaseDuJiWidget : androidx.glance.appwidget.GlanceAppWidget() {
    protected fun emptyCard(
        title: String,
        summary: String,
    ): @Composable () -> Unit = {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ImageProviders.surfaceBackground)
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    style = TextStyle(
                        color = ColorProvider(R.color.widget_text_primary),
                        fontWeight = FontWeight.Bold,
                    )
                )
                Text(
                    text = summary,
                    style = TextStyle(color = ColorProvider(R.color.widget_text_secondary)),
                )
            }
        }
    }
}

object ImageProviders {
    val surfaceBackground = ColorProvider(R.color.widget_surface)
    val secondaryContainer = ColorProvider(R.color.widget_secondary_container)
    val accentBlue = ColorProvider(R.color.widget_blue)
}
