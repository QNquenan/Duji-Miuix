package com.quenan.duji.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.quenan.duji.R

class MyItemWideWidget : BaseDuJiWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val model = WidgetDataProvider(context).loadWideSummary()
        provideContent {
            WideSummaryWidgetContent(model)
        }
    }
}

@Composable
private fun WideSummaryWidgetContent(model: ItemWideSummaryWidgetModel) {
    val context = LocalContext.current
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(R.color.widget_black_surface))
            .clickable(WidgetIntentFactory.myItemsPageAction(context))
            .padding(20.dp),
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Text(
                text = model.title,
                style = TextStyle(
                    color = ColorProvider(R.color.widget_on_dark),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            )
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(top = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WideStatColumn(label = "物品价值", value = model.totalValueText)
                WideStatColumn(label = "物品数量", value = model.itemCountText)
                WideStatColumn(label = "总日均价格", value = model.totalDailyPriceText)
            }
        }
    }
}

@Composable
private fun WideStatColumn(label: String, value: String) {
    Column(
        modifier = GlanceModifier.padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = value,
            style = TextStyle(
                color = ColorProvider(R.color.widget_on_dark),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        )
        Text(
            text = label,
            style = TextStyle(
                color = ColorProvider(R.color.widget_on_dark_secondary),
                fontSize = 12.sp,
            ),
            modifier = GlanceModifier.padding(top = 6.dp),
        )
    }
}
