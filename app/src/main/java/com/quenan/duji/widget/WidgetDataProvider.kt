package com.quenan.duji.widget

import android.content.Context
import android.util.Log
import com.quenan.duji.data.day.DayRepository
import com.quenan.duji.data.item.ItemRepository
import com.quenan.duji.data.item.toStats

class WidgetDataProvider(context: Context) {
    private val itemRepository = ItemRepository(context)
    private val dayRepository = DayRepository(context)

    suspend fun loadWideSummary(): ItemWideSummaryWidgetModel {
        return itemRepository.getAllItems().toStats().toWideSummaryWidgetModel()
    }

    suspend fun loadSquareItem(appWidgetId: Int, context: Context): ItemWidgetModel? {
        val selection = WidgetSelectionRepository(context).getSelection(appWidgetId) ?: return null
        if (selection.type != WidgetSelectionType.MY_ITEM_SQUARE) return null
        return itemRepository.getAllItems()
            .firstOrNull { it.id == selection.targetId }
            ?.toSquareWidgetModel()
    }

    suspend fun loadSquareDay(appWidgetId: Int, context: Context): DayWidgetModel? {
        val selection = WidgetSelectionRepository(context).getSelection(appWidgetId)
        Log.i("DuJiWidget", "loadSquareDay: appWidgetId=$appWidgetId, selection=$selection")
        if (selection == null || selection.type != WidgetSelectionType.THOSE_DAY_SQUARE) return null
        val days = dayRepository.getAllDays()
        val day = days.firstOrNull { it.id == selection.targetId }
        Log.i("DuJiWidget", "loadSquareDay target: targetId=${selection.targetId}, dayFound=${day != null}, totalDays=${days.size}")
        return day?.toWidgetModel()
    }
}
