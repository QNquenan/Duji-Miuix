package com.quenan.duji.widget.ui

import androidx.compose.runtime.Composable
import com.quenan.duji.data.item.ItemData
import com.quenan.duji.data.item.ItemRepository
import com.quenan.duji.data.item.buildAvgPriceText
import com.quenan.duji.data.item.buildTotalPriceText
import com.quenan.duji.widget.WidgetSelection
import com.quenan.duji.widget.WidgetSelectionType

class MyItemWideWidgetConfigureActivity : BaseWidgetConfigureActivity<ItemData>() {
    override val screenTitle: String = "选择长方形物品组件数据"

    override suspend fun loadEntries(): List<ItemData> {
        return ItemRepository(applicationContext).getAllItems()
    }

    override fun toSelection(entry: ItemData): WidgetSelection {
        return WidgetSelection(type = WidgetSelectionType.MY_ITEM_WIDE, targetId = entry.id)
    }

    @Composable
    override fun EntryCard(entry: ItemData, onClick: () -> Unit) {
        DefaultEntryCard(
            title = "${entry.icon} ${entry.name}",
            subtitle = entry.date,
            trailing = buildAvgPriceText(entry.price, entry.date) + " · " + buildTotalPriceText(entry.price),
            onClick = onClick,
        )
    }
}
