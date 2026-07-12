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
import com.quenan.duji.data.day.DayData
import com.quenan.duji.data.day.computeStatus
import com.quenan.duji.data.day.targetDateFormatted
import com.quenan.duji.data.day.typeLabel
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
internal fun ThoseDaysSearchBar(
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
                    label = "搜索日子"
                )
            },
            expanded = searchExpanded,
            onExpandedChange = onExpandedChange
        ) {}
    }
}

@Composable
internal fun ThoseDaysContent(
    filteredDays: List<DayCardUiModel>,
    normalizedSearchQuery: String,
    currentViewMode: ThoseDaysViewMode,
    innerPadding: PaddingValues,
    contentPadding: PaddingValues,
    fabScrollConnection: NestedScrollConnection,
    scrollBehavior: ScrollBehavior,
    onDayClick: (DayData) -> Unit,
) {
    if (filteredDays.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            EmptyStateCard(
                title = if (normalizedSearchQuery.isEmpty()) "那些日子" else "没有找到日子",
                summary = if (normalizedSearchQuery.isEmpty()) {
                    "还没有日子，点击右下角 + 添加"
                } else {
                    "没有匹配“${normalizedSearchQuery}”的日子"
                },
            )
        }
    } else if (currentViewMode == ThoseDaysViewMode.List) {
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
            items(
                items = filteredDays,
                key = { it.day.id },
                contentType = { "day-list" }
            ) { model ->
                DayListItem(
                    day = model.day,
                    status = model.status,
                    onClick = { onDayClick(model.day) }
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
            items(
                count = filteredDays.size,
                key = { filteredDays[it].day.id },
                contentType = { "day-grid" }
            ) { index ->
                val model = filteredDays[index]
                DayGridItem(
                    day = model.day,
                    status = model.status,
                    onClick = { onDayClick(model.day) }
                )
            }
        }
    }
}

@Composable
internal fun ThoseDaysDetailContent(detailDay: DayData) {
    val status = detailDay.computeStatus()
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
                .background(MiuixTheme.colorScheme.secondaryContainer, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = detailDay.emoji, fontSize = 32.sp)
        }
        SmallTitle(
            text = "详细信息",
            insideMargin = PaddingValues(top = 16.dp, bottom = 2.dp, start = 16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = PaddingValues(16.dp),
            colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                ThoseDaysDetailRow(label = "名称", value = detailDay.name)
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 1.dp)
                ThoseDaysDetailRow(label = "类型", value = detailDay.typeLabel())
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 1.dp)
                ThoseDaysDetailRow(label = "数字", value = status.statusText)
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 1.dp)
                ThoseDaysDetailRow(label = "日期", value = detailDay.targetDateFormatted())
            }
        }
        if (detailDay.note.isNotBlank()) {
            SmallTitle(
                text = "备注",
                insideMargin = PaddingValues(top = 16.dp, bottom = 2.dp, start = 16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                insideMargin = PaddingValues(16.dp),
                colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
            ) {
                Text(text = detailDay.note, fontSize = 16.sp, color = MiuixTheme.colorScheme.onBackground)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ThoseDaysDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = MiuixTheme.colorScheme.onBackground,
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = MiuixTheme.colorScheme.onBackground,
        )
    }
}
