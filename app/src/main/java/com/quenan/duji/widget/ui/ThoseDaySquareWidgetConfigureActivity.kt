package com.quenan.duji.widget.ui

import androidx.compose.runtime.Composable
import com.quenan.duji.data.day.DayData
import com.quenan.duji.data.day.DayRepository
import com.quenan.duji.data.day.computeStatus
import com.quenan.duji.widget.WidgetSelection
import com.quenan.duji.widget.WidgetSelectionType

class ThoseDaySquareWidgetConfigureActivity : BaseWidgetConfigureActivity<DayData>() {
    override val screenTitle: String = "选择日子组件数据"

    override suspend fun loadEntries(): List<DayData> {
        return DayRepository(applicationContext).getAllDays()
    }

    override fun toSelection(entry: DayData): WidgetSelection {
        return WidgetSelection(type = WidgetSelectionType.THOSE_DAY_SQUARE, targetId = entry.id)
    }

    @Composable
    override fun EntryCard(entry: DayData, onClick: () -> Unit) {
        val status = entry.computeStatus()
        DefaultEntryCard(
            title = "${entry.emoji} ${entry.name}",
            subtitle = status.dateText,
            trailing = status.statusText,
            onClick = onClick,
        )
    }
}
