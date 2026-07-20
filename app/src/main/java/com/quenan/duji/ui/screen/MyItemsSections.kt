package com.quenan.duji.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quenan.duji.data.item.ItemData
import com.quenan.duji.data.item.ItemStats
import com.quenan.duji.data.item.buildAvgPriceText
import com.quenan.duji.data.item.buildTotalPriceText
import com.quenan.duji.ui.component.EmptyStateCard
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun MyItemsSearchBar(
    searchQuery: String,
    searchExpanded: Boolean,
    onQueryChange: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MiuixTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        SearchBar(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = DpSize(0.dp, 0.dp),
            inputField = {
                InputField(
                    query = searchQuery,
                    onQueryChange = onQueryChange,
                    onSearch = { onExpandedChange(false) },
                    expanded = searchExpanded,
                    onExpandedChange = onExpandedChange,
                    label = "搜索物品"
                )
            },
            expanded = searchExpanded,
            onExpandedChange = onExpandedChange
        ) {}
    }
}

@Composable
internal fun MyItemsContent(
    filteredItems: List<ItemCardUiModel>,
    normalizedSearchQuery: String,
    itemViewMode: ItemViewMode,
    stats: ItemStats,
    innerPadding: PaddingValues,
    contentPadding: PaddingValues,
    fabScrollConnection: NestedScrollConnection,
    scrollBehavior: ScrollBehavior,
    onItemClick: (ItemData) -> Unit,
) {
    if (filteredItems.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            EmptyStateCard(
                title = if (normalizedSearchQuery.isEmpty()) "我的物品" else "没有找到物品",
                summary = if (normalizedSearchQuery.isEmpty()) {
                    "还没有物品，点击右下角 + 添加"
                } else {
                    "没有匹配“${normalizedSearchQuery}”的物品"
                },
            )
        }
    } else if (itemViewMode == ItemViewMode.List) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(fabScrollConnection)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 12.dp,
                bottom = contentPadding.calculateBottomPadding() + 12.dp,
                start = 12.dp,
                end = 12.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(contentType = "stats") {
                StatsCard(stats = stats)
            }
            items(
                items = filteredItems,
                key = { it.item.id },
                contentType = { "item-list" }
            ) { model ->
                ItemListCard(
                    icon = model.iconText,
                    name = model.title,
                    date = model.dateText,
                    avgPrice = model.avgPriceText,
                    totalPrice = model.totalPriceText,
                    isPinned = model.isPinned,
                    onClick = { onItemClick(model.item) }
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(fabScrollConnection)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 12.dp,
                bottom = contentPadding.calculateBottomPadding() + 12.dp,
                start = 12.dp,
                end = 12.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(
                span = { GridItemSpan(maxLineSpan) },
                contentType = "stats",
            ) {
                StatsCard(stats = stats)
            }
            items(
                count = filteredItems.size,
                key = { filteredItems[it].item.id },
                contentType = { "item-grid" }
            ) { index ->
                val model = filteredItems[index]
                ItemGridCard(
                    icon = model.iconText,
                    name = model.title,
                    date = model.dateText,
                    avgPrice = model.avgPriceText,
                    totalPrice = model.totalPriceText,
                    isPinned = model.isPinned,
                    onClick = { onItemClick(model.item) }
                )
            }
        }
    }
}

@Composable
internal fun MyItemsDetailContent(detailItem: ItemData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MiuixTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = detailItem.icon,
                fontSize = 32.sp,
            )
        }

        SmallTitle(
            text = "详细信息",
            insideMargin = PaddingValues(top = 16.dp, bottom = 2.dp, start = 16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = PaddingValues(16.dp),
            colors = CardDefaults.defaultColors(
                color = MiuixTheme.colorScheme.surfaceContainer,
            ),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                MyItemsDetailRow(label = "名称", value = detailItem.name)
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 1.dp)
                MyItemsDetailRow(label = "购买日期", value = detailItem.date)
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 1.dp)
                MyItemsDetailRow(label = "总价格", value = buildTotalPriceText(detailItem.price))
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 1.dp)
                MyItemsDetailRow(label = "日均价格", value = buildAvgPriceText(detailItem.price, detailItem.date))
            }
        }
        if (detailItem.note.isNotBlank()) {
            SmallTitle(
                text = "备注",
                insideMargin = PaddingValues(top = 16.dp, bottom = 2.dp, start = 16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                insideMargin = PaddingValues(16.dp),
                colors = CardDefaults.defaultColors(
                    color = MiuixTheme.colorScheme.surfaceContainer,
                ),
            ) {
                Text(
                    text = detailItem.note,
                    fontSize = 16.sp,
                    color = MiuixTheme.colorScheme.onSurface,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MyItemsDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = MiuixTheme.colorScheme.onSurface,
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}
