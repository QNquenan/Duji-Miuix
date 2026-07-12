package com.quenan.duji.widget.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quenan.duji.data.day.DayData
import com.quenan.duji.data.day.DayRepository
import com.quenan.duji.data.item.ItemData
import com.quenan.duji.data.item.ItemRepository
import com.quenan.duji.ui.screen.MyItemsDetailContent
import com.quenan.duji.ui.screen.ThoseDaysDetailContent
import com.quenan.duji.ui.theme.DuJiTheme
import com.quenan.duji.widget.WidgetTargetType
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar

class WidgetEntryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val targetType = intent.getStringExtra(EXTRA_TARGET_TYPE)?.let(WidgetTargetType::valueOf)
        val targetId = intent.getLongExtra(EXTRA_TARGET_ID, -1L)
        setContent {
            DuJiTheme {
                var item by remember { mutableStateOf<ItemData?>(null) }
                var day by remember { mutableStateOf<DayData?>(null) }
                var notFound by remember { mutableStateOf(false) }
                androidx.compose.runtime.LaunchedEffect(targetType, targetId) {
                    when (targetType) {
                        WidgetTargetType.ITEM -> {
                            item = ItemRepository(applicationContext).getAllItems().firstOrNull { it.id == targetId }
                            notFound = item == null
                        }
                        WidgetTargetType.DAY -> {
                            day = DayRepository(applicationContext).getAllDays().firstOrNull { it.id == targetId }
                            notFound = day == null
                        }
                        null -> notFound = true
                    }
                }
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = when (targetType) {
                                WidgetTargetType.ITEM -> "我的物品详情"
                                WidgetTargetType.DAY -> "那些日子详情"
                                null -> "详情"
                            },
                        )
                    }
                ) { innerPadding ->
                    when {
                        item != null -> MyItemsDetailContent(detailItem = item!!)
                        day != null -> ThoseDaysDetailContent(detailDay = day!!)
                        notFound -> Text(
                            text = "对应数据项不存在，可能已被删除。",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .padding(20.dp)
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_TARGET_TYPE = "extra_target_type"
        const val EXTRA_TARGET_ID = "extra_target_id"
    }
}
