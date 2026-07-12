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
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.quenan.duji.R

class MyItemSquareWidget : BaseDuJiWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val glanceId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val model = WidgetDataProvider(context).loadSquareItem(glanceId, context)
        provideContent {
            if (model == null) {
                emptyCard("我的物品", "点击进入应用重新选择物品")()
            } else {
                SquareItemWidgetContent(model)
            }
        }
    }
}

@Composable
private fun SquareItemWidgetContent(model: ItemWidgetModel) {
    val context = LocalContext.current
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(ImageProviders.surfaceBackground)
            .cornerRadius(32.dp)
            .clickable(WidgetIntentFactory.detailAction(context, WidgetTargetType.ITEM, model.id))
            .padding(14.dp),
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Box(
                modifier = GlanceModifier
                    .size(44.dp)
                    .background(ImageProviders.secondaryContainer)
                    .cornerRadius(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = model.icon, style = TextStyle(fontSize = 22.sp))
            }
            Text(
                text = model.name,
                style = TextStyle(
                    color = ColorProvider(R.color.widget_text_primary),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = GlanceModifier.padding(top = 14.dp),
            )
            Text(
                text = model.date,
                style = TextStyle(
                    color = ColorProvider(R.color.widget_text_secondary),
                    fontSize = 11.sp,
                )
            )
            Text(
                text = model.avgPriceText,
                style = TextStyle(
                    color = ColorProvider(R.color.widget_text_primary),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
                modifier = GlanceModifier.padding(top = 18.dp),
            )
            Text(
                text = model.totalPriceText,
                style = TextStyle(
                    color = ColorProvider(R.color.widget_text_secondary),
                    fontSize = 11.sp,
                )
            )
        }
    }
}
