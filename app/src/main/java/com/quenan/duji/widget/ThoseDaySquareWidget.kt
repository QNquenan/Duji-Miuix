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
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.quenan.duji.R

class ThoseDaySquareWidget : BaseDuJiWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val glanceId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val model = WidgetDataProvider(context).loadSquareDay(glanceId, context)
        provideContent {
            if (model == null) {
                emptyCard("那些日子", "点击进入应用重新选择日子")()
            } else {
                DaySquareWidgetContent(model)
            }
        }
    }
}

@Composable
private fun DaySquareWidgetContent(model: DayWidgetModel) {
    val context = LocalContext.current
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(ImageProviders.surfaceBackground)
            .cornerRadius(32.dp)
            .clickable(WidgetIntentFactory.detailAction(context, WidgetTargetType.DAY, model.id)),
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(ImageProviders.accentBlue)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = model.titlePrefix + model.titleSuffix,
                    style = TextStyle(
                        color = ColorProvider(R.color.widget_on_blue),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    )
                )
            }
            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = model.numberText,
                        style = TextStyle(
                            color = ColorProvider(R.color.widget_text_primary),
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    )
                    if (model.unitText.isNotBlank()) {
                        Text(
                            text = model.unitText,
                            style = TextStyle(
                                color = ColorProvider(R.color.widget_text_primary),
                                fontSize = 14.sp,
                            ),
                            modifier = GlanceModifier.padding(start = 4.dp),
                        )
                    }
                }
                Text(
                    text = model.dateText,
                    style = TextStyle(
                        color = ColorProvider(R.color.widget_text_secondary),
                        fontSize = 12.sp,
                    ),
                    modifier = GlanceModifier.padding(top = 8.dp),
                )
            }
        }
    }
}
