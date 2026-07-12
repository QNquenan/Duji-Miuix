package com.quenan.duji.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.quenan.duji.R

class MyItemWideWidget : BaseDuJiWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val glanceId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val model = WidgetDataProvider(context).loadWideItem(glanceId, context)
        provideContent {
            if (model == null) {
                emptyCard("我的物品", "点击进入应用重新选择物品")()
            } else {
                WideItemWidgetContent(model)
            }
        }
    }
}

@Composable
private fun WideItemWidgetContent(model: ItemWidgetModel) {
    val context = LocalContext.current
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProviders.surfaceBackground)
            .clickable(WidgetIntentFactory.detailAction(context, WidgetTargetType.ITEM, model.id))
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = GlanceModifier
                    .size(48.dp)
                    .background(ImageProviders.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = model.icon, style = TextStyle(fontSize = 24.sp))
            }
            Column(
                modifier = GlanceModifier.padding(start = 12.dp, end = 12.dp),
            ) {
                Text(
                    text = model.name,
                    style = TextStyle(
                        color = ColorProvider(R.color.widget_text_primary),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                )
                Text(
                    text = model.date,
                    style = TextStyle(
                        color = ColorProvider(R.color.widget_text_secondary),
                        fontSize = 12.sp,
                    )
                )
                Text(
                    text = model.avgPriceText,
                    style = TextStyle(
                        color = ColorProvider(R.color.widget_text_primary),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    )
                )
                Text(
                    text = model.totalPriceText,
                    style = TextStyle(
                        color = ColorProvider(R.color.widget_text_secondary),
                        fontSize = 12.sp,
                    )
                )
            }
        }
    }
}
