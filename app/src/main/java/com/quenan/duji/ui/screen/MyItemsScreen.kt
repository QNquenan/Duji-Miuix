package com.quenan.duji.ui.screen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quenan.duji.data.item.ItemData
import com.quenan.duji.data.item.ItemStats
import com.quenan.duji.ui.component.rememberNoticeAction
import java.util.Calendar
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NumberPicker
import top.yukonga.miuix.kmp.basic.NumberPickerColors
import top.yukonga.miuix.kmp.basic.NumberPickerDefaults
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.All
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.extended.ListView
import top.yukonga.miuix.kmp.icon.extended.Pin
import top.yukonga.miuix.kmp.icon.extended.Sort
import top.yukonga.miuix.kmp.icon.extended.Years
import top.yukonga.miuix.kmp.menu.OverlayIconCascadingDropdownMenu
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun MyItemsScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val scrollBehavior = MiuixScrollBehavior()
    var fabVisible by remember { mutableStateOf(true) }
    var scrollDistance by remember { mutableFloatStateOf(0f) }
    val fabBottomOffset by animateDpAsState(
        targetValue = if (fabVisible) 0.dp else 120.dp,
        animationSpec = tween(durationMillis = 300),
        label = "my-items-fab-offset",
    )
    val fabScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                scrollDistance += delta
                if (scrollDistance < -50f) {
                    if (fabVisible) fabVisible = false
                    scrollDistance = 0f
                } else if (scrollDistance > 50f) {
                    if (!fabVisible) fabVisible = true
                    scrollDistance = 0f
                }
                return Offset.Zero
            }
        }
    }
    var showBottomSheet by remember { mutableStateOf(false) }
    var itemName by remember { mutableStateOf("") }
    var itemPrice by remember { mutableStateOf("") }
    var itemDate by remember { mutableStateOf("") }
    var itemNote by remember { mutableStateOf("") }
    var isPinned by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<ItemData?>(null) }
    var showDetailBottomSheet by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ItemData?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val viewModel: MyItemsViewModel = viewModel()
    val items by viewModel.items.collectAsStateWithLifecycle()
    val currentSort by viewModel.currentSort.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val showNotice = rememberNoticeAction()
    var showDateDialog by remember { mutableStateOf(false) }
    var showIconDialog by remember { mutableStateOf(false) }
    var showCustomIconDialog by remember { mutableStateOf(false) }
    var selectedIcon by remember { mutableStateOf("📦") }
    var customIconText by remember { mutableStateOf("") }
    val calendar = remember { Calendar.getInstance() }
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH) + 1
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    var selectedYear by remember { mutableIntStateOf(currentYear) }
    var selectedMonth by remember { mutableIntStateOf(currentMonth) }
    var selectedDay by remember { mutableIntStateOf(currentDay) }
    var itemViewMode by remember { mutableStateOf(ItemViewMode.List) }
    var searchQuery by remember { mutableStateOf("") }
    var searchExpanded by remember { mutableStateOf(false) }
    val filteredItems = remember(items, searchQuery) {
        val keyword = searchQuery.trim()
        if (keyword.isEmpty()) {
            items
        } else {
            items.filter { item ->
                item.name.contains(keyword, ignoreCase = true) ||
                    item.note.contains(keyword, ignoreCase = true) ||
                    item.icon.contains(keyword)
            }
        }
    }
    fun populateForm(item: ItemData) {
        itemName = item.name
        itemPrice = item.price.toString()
        itemDate = item.date
        itemNote = item.note
        isPinned = item.isPinned
        selectedIcon = item.icon
        customIconText = ""
        val parts = item.date.split("-")
        if (parts.size == 3) {
            selectedYear = parts[0].toIntOrNull() ?: currentYear
            selectedMonth = parts[1].toIntOrNull() ?: currentMonth
            selectedDay = parts[2].toIntOrNull() ?: currentDay
        }
    }

    fun resetAddForm() {
        itemName = ""
        itemPrice = ""
        itemDate = ""
        itemNote = ""
        isPinned = false
        selectedIcon = "📦"
        customIconText = ""
        selectedYear = currentYear
        selectedMonth = currentMonth
        selectedDay = currentDay
    }

    val sortEntries = remember(currentSort) {
        listOf(
            DropdownEntry(
                items = listOf(
                    DropdownItem(
                        text = "创建时间",
                        selected = currentSort.field == ItemSortField.CREATED_AT,
                        onClick = {
                            viewModel.updateSort(
                                ItemSortField.CREATED_AT,
                                currentSort.direction,
                            )
                        },
                    ),
                    DropdownItem(
                        text = "购买日期",
                        selected = currentSort.field == ItemSortField.PURCHASE_DATE,
                        onClick = {
                            viewModel.updateSort(
                                ItemSortField.PURCHASE_DATE,
                                currentSort.direction,
                            )
                        },
                    ),
                )
            ),
            DropdownEntry(
                items = listOf(
                    DropdownItem(
                        text = buildSortDirectionMenuTitle(currentSort.direction),
                        children = listOf(
                            DropdownItem(
                                text = sortDirectionLabel(SortDirection.ASC),
                                selected = currentSort.direction == SortDirection.ASC,
                                onClick = {
                                    viewModel.updateSort(
                                        currentSort.field,
                                        SortDirection.ASC,
                                    )
                                },
                            ),
                            DropdownItem(
                                text = sortDirectionLabel(SortDirection.DESC),
                                selected = currentSort.direction == SortDirection.DESC,
                                onClick = {
                                    viewModel.updateSort(
                                        currentSort.field,
                                        SortDirection.DESC,
                                    )
                                },
                            ),
                        ),
                    ),
                )
            ),
        )
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = "我的物品",
                    scrollBehavior = scrollBehavior,
                    actions = {
                        IconButton(onClick = {
                            itemViewMode = if (itemViewMode == ItemViewMode.List) {
                                ItemViewMode.Grid
                            } else {
                                ItemViewMode.List
                            }
                        }) {
                            Icon(
                                imageVector = if (itemViewMode == ItemViewMode.Grid) MiuixIcons.All else MiuixIcons.ListView,
                                contentDescription = "切换视图",
                                tint = MiuixTheme.colorScheme.onBackground,
                            )
                        }
                        OverlayIconCascadingDropdownMenu(entries = sortEntries) {
                            Icon(
                                imageVector = MiuixIcons.Sort,
                                contentDescription = "排序",
                                tint = MiuixTheme.colorScheme.onBackground,
                            )
                        }
                    }
                )
                SearchBar(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    insideMargin = DpSize(0.dp, 0.dp),
                    inputField = {
                        InputField(
                            query = searchQuery,
                            onQueryChange = {
                                searchQuery = it
                                searchExpanded = it.isNotBlank()
                            },
                            onSearch = { searchExpanded = false },
                            expanded = searchExpanded,
                            onExpandedChange = { searchExpanded = it },
                            label = "搜索物品"
                        )
                    },
                    expanded = searchExpanded,
                    onExpandedChange = { searchExpanded = it }
                ) {}
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            if (itemViewMode == ItemViewMode.List) {
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
                    item {
                        StatsCard(stats = stats)
                    }
                    items(filteredItems, key = { it.id }) { item ->
                        ItemListCard(
                            icon = item.icon,
                            name = item.name,
                            date = item.date,
                            avgPrice = "¥${item.price / maxOf(1, daysSince(item.date))}/天",
                            totalPrice = "¥${item.price}",
                            isPinned = item.isPinned,
                            onClick = {
                                selectedItem = item
                                showDetailBottomSheet = true
                            }
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
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        StatsCard(stats = stats)
                    }
                    items(filteredItems.size, key = { filteredItems[it].id }) { index ->
                        val item = filteredItems[index]
                        ItemGridCard(
                            icon = item.icon,
                            name = item.name,
                            date = item.date,
                            avgPrice = "¥${item.price / maxOf(1, daysSince(item.date))}/天",
                            totalPrice = "¥${item.price}",
                            isPinned = item.isPinned,
                            onClick = {
                                selectedItem = item
                                showDetailBottomSheet = true
                            }
                        )
                    }
                }
            }

            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(contentPadding)
                    .padding(end = 16.dp, bottom = 16.dp)
                    .offset(y = fabBottomOffset),
                onClick = {
                    resetAddForm()
                    editingItem = null
                    showBottomSheet = true
                },
            ) {
                Icon(
                    imageVector = MiuixIcons.Add,
                    contentDescription = "添加",
                    tint = Color.White,
                )
            }

            selectedItem?.let { detailItem ->
                WindowBottomSheet(
                    show = showDetailBottomSheet,
                    title = "物品详情",
                    backgroundColor = MiuixTheme.colorScheme.surface,
                    startAction = {
                        val dismiss = LocalDismissState.current
                        IconButton(onClick = {
                            showDetailBottomSheet = false
                            dismiss?.invoke()
                        }) {
                            Icon(
                                imageVector = MiuixIcons.Close,
                                contentDescription = "关闭",
                                tint = MiuixTheme.colorScheme.onBackground,
                            )
                        }
                    },
                    endAction = {
                        val dismiss = LocalDismissState.current
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = {
                                showDeleteConfirmDialog = true
                            }) {
                                Icon(
                                    imageVector = MiuixIcons.Delete,
                                    contentDescription = "删除",
                                    tint = Color(0xFFFF3B30),
                                )
                            }
                            IconButton(onClick = {
                                populateForm(detailItem)
                                editingItem = detailItem
                                showDetailBottomSheet = false
                                dismiss?.invoke()
                                showBottomSheet = true
                            }) {
                                Icon(
                                    imageVector = MiuixIcons.Edit,
                                    contentDescription = "修改",
                                )
                            }
                        }
                    },
                    onDismissRequest = { showDetailBottomSheet = false },
                    onDismissFinished = {
                        selectedItem = null
                    },
                ) {
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
                                .align(Alignment.CenterHorizontally)
                                .clip(RoundedCornerShape(16.dp))
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

                        SmallTitle(text = "详细信息",insideMargin = PaddingValues(16.dp, 2.dp), modifier = Modifier.fillMaxWidth())
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
                                DetailRow(label = "名称", value = detailItem.name)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 1.dp)
                                DetailRow(label = "购买日期", value = detailItem.date)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 1.dp)
                                DetailRow(label = "总价格", value = "¥${detailItem.price}")
                                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 1.dp)
                                DetailRow(label = "日均价格", value = "¥${detailItem.price / maxOf(1, daysSince(detailItem.date))}/天")
                            }
                        }
                        if (detailItem.note.isNotBlank()) {
                            SmallTitle(text = "备注", insideMargin = PaddingValues(16.dp, 2.dp), modifier = Modifier.fillMaxWidth())
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

                        // 底部间距
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            if (showDeleteConfirmDialog && selectedItem != null) {
                WindowDialog(
                    title = "删除物品",
                    summary = "确认删除 ${selectedItem?.name} 吗？",
                    show = true,
//                    insideMargin = DpSize(16.dp, 16.dp),
                    onDismissRequest = { showDeleteConfirmDialog = false },
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            TextButton(
                                text = "取消",
                                onClick = { showDeleteConfirmDialog = false },
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(
                                text = "删除",
                                onClick = {
                                    selectedItem?.let { item ->
                                        viewModel.deleteItem(item)
                                        showNotice("删除成功")
                                    }
                                    showDeleteConfirmDialog = false
                                    showDetailBottomSheet = false
                                    selectedItem = null
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.textButtonColors(
                                    textColor = Color(0xFFFF3B30),
                                ),
                            )
                        }
                    }
                }
            }

            // 添加/修改物品底部抽屉
            WindowBottomSheet(
                show = showBottomSheet,
                title = if (editingItem == null) "添加物品" else "修改物品",
                backgroundColor = MiuixTheme.colorScheme.surface,
                startAction = {
                    val dismiss = LocalDismissState.current
                    IconButton(onClick = {
                        editingItem = null
                        dismiss?.invoke()
                    }) {
                        Icon(
                            imageVector = MiuixIcons.Close,
                            contentDescription = "关闭",
                            tint = MiuixTheme.colorScheme.onBackground,
                        )
                    }
                },
                endAction = {
                    val dismiss = LocalDismissState.current
                    IconButton(onClick = {
                        when {
                            itemName.isBlank() -> showNotice("名称不能为空")
                            itemPrice.isBlank() -> showNotice("价格不能为空")
                            itemDate.isBlank() -> showNotice("日期不能为空")
                            else -> {
                                if (editingItem == null) {
                                    viewModel.addItem(
                                        icon = selectedIcon,
                                        name = itemName,
                                        date = itemDate,
                                        price = itemPrice,
                                        note = itemNote,
                                        isPinned = isPinned,
                                    )
                                    showNotice("添加成功")
                                } else {
                                    viewModel.updateItem(
                                        id = editingItem!!.id,
                                        icon = selectedIcon,
                                        name = itemName,
                                        date = itemDate,
                                        price = itemPrice,
                                        note = itemNote,
                                        isPinned = isPinned,
                                    )
                                    showNotice("修改成功")
                                }
                                editingItem = null
                                dismiss?.invoke()
                            }
                        }
                    }) {
                        Icon(
                            imageVector = MiuixIcons.Ok,
                            contentDescription = "确认",
                            tint = MiuixTheme.colorScheme.onBackground,
                        )
                    }
                },
                onDismissRequest = {
                    showIconDialog = false
                    showCustomIconDialog = false
                    showDateDialog = false
                    showBottomSheet = false
                    editingItem = null
                },
            ) {
                // BottomSheet 内容区域 - 可滚动
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // 居中正方形图标占位
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.CenterHorizontally)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                color = MiuixTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { showIconDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedIcon,
                            fontSize = 32.sp,
                        )
                    }

                    SmallTitle(text = "信息",insideMargin = PaddingValues(16.dp, 2.dp), modifier = Modifier.fillMaxWidth())
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        insideMargin = PaddingValues(0.dp),
                        colors = CardDefaults.defaultColors(
                            color = MiuixTheme.colorScheme.surfaceContainer,
                        ),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            TextField(
                                value = itemName,
                                onValueChange = { itemName = it },
                                label = "名称",
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                maxLines = 1,
                            )

                            TextField(
                                value = itemPrice,
                                onValueChange = {
                                    if (it.all { char -> char.isDigit() }) {
                                        itemPrice = it
                                    }
                                },
                                label = "价格",
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            )

                            Surface(
                                onClick = { showDateDialog = true },
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = MiuixTheme.colorScheme.secondaryContainer,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            start = 16.dp,
                                            end = 16.dp,
                                            top = if (itemDate.isEmpty()) 17.dp else 11.dp,
                                            bottom = if (itemDate.isEmpty()) 17.dp else 11.dp,
                                        ),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    if (itemDate.isNotEmpty()) {
                                        Text(
                                            text = "购买日期",
                                            color = MiuixTheme.colorScheme.onSecondaryContainer,
                                            fontSize = 10.sp,
                                        )
                                    }
                                    Text(
                                        text = if (itemDate.isEmpty()) "购买日期" else itemDate,
                                        color = if (itemDate.isEmpty()) MiuixTheme.colorScheme.onSecondaryContainer else MiuixTheme.colorScheme.onBackground,
                                        style = MiuixTheme.textStyles.main,
                                    )
                                }
                            }

                            TextField(
                                value = itemNote,
                                onValueChange = { itemNote = it },
                                label = "备注",
                                singleLine = false,
                                minLines = 3,
                                maxLines = 5,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            )
                        }
                    }

                    SmallTitle(text = "功能",insideMargin = PaddingValues(16.dp, 2.dp), modifier = Modifier.fillMaxWidth())
                    Card(
                        modifier = Modifier.fillMaxWidth(), insideMargin = PaddingValues(0.dp),
                        colors = CardDefaults.defaultColors(
                            color = MiuixTheme.colorScheme.surfaceContainer,
                        ),
                    ) {
                        SwitchPreference(
                            title = "置顶",
                            checked = isPinned,
                            onCheckedChange = { isPinned = it }, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        )
                    }

                    // 底部间距
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // 图标选择对话框
            WindowDialog(
                title = "选择图标",
                show = showIconDialog,
                onDismissRequest = { showIconDialog = false },
            ) {
                val commonIcons = listOf(
                    "📱", "⌚️", "🖥️", "🖨️", "🖱️",
                    "🧭", "🎮", "📺", "🎧", "💻",
                    "⌨️", "📷", "🎛️", "🖊️", "📦",
                    "🔑", "💡", "🎒", "👟", "👕",
                    "🕶️", "🧢", "💍", "🎸"
                )
                var tempSelectedIcon by remember { mutableStateOf(selectedIcon) }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // 5 列网格展示常用图标
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        commonIcons.chunked(5).forEach { rowIcons ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                rowIcons.forEach { icon ->
                                    val isSelected = tempSelectedIcon == icon
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                color = if (isSelected) {
                                                    MiuixTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                } else {
                                                    MiuixTheme.colorScheme.secondaryContainer
                                                },
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { tempSelectedIcon = icon },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = icon,
                                            fontSize = 24.sp,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            text = "取消",
                            onClick = { showIconDialog = false },
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            text = "自定义",
                            onClick = {
                                showIconDialog = false
                                showCustomIconDialog = true
                            },
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            text = "确定",
                            onClick = {
                                selectedIcon = tempSelectedIcon
                                showIconDialog = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.textButtonColorsPrimary(),
                        )
                    }
                }
            }

            // 自定义图标输入对话框
            WindowDialog(
                title = "自定义图标",
                show = showCustomIconDialog,
                onDismissRequest = { showCustomIconDialog = false },
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    TextField(
                        value = customIconText,
                        onValueChange = { customIconText = it },
                        label = "输入表情或文字",
                        useLabelAsPlaceholder = true,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            text = "取消",
                            onClick = { showCustomIconDialog = false },
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            text = "确定",
                            onClick = {
                                selectedIcon = customIconText
                                customIconText = ""
                                showCustomIconDialog = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.textButtonColorsPrimary(),
                        )
                    }
                }
            }

            // 购买日期选择对话框
            WindowDialog(
                title = "选择日期",
                show = showDateDialog,
                onDismissRequest = { showDateDialog = false },
            ) {
                val dismiss = LocalDismissState.current
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Top,
                    ) {
                        // 年
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "年",
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.onBackgroundVariant,
                            )
                            NumberPicker(
                                value = selectedYear,
                                onValueChange = { selectedYear = it },
                                range = 1900..2100,
                                wrapAround = true,
                                modifier = Modifier.width(100.dp),
                                label = { it.toString() },
                                colors = NumberPickerDefaults.colors(
                                    selectedTextColor = MiuixTheme.colorScheme.primary,
                                ),
                            )
                        }

                        // 月
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "月",
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.onBackgroundVariant,
                            )
                            NumberPicker(
                                value = selectedMonth,
                                onValueChange = { selectedMonth = it },
                                range = 1..12,
                                wrapAround = true,
                                modifier = Modifier.width(80.dp),
                                label = { it.toString().padStart(2, '0') },
                                colors = NumberPickerDefaults.colors(
                                    selectedTextColor = MiuixTheme.colorScheme.primary,
                                ),
                            )
                        }

                        // 日
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "日",
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.onBackgroundVariant,
                            )
                            NumberPicker(
                                value = selectedDay,
                                onValueChange = { selectedDay = it },
                                range = 1..31,
                                wrapAround = true,
                                modifier = Modifier.width(80.dp),
                                label = { it.toString().padStart(2, '0') },
                                colors = NumberPickerDefaults.colors(
                                    selectedTextColor = MiuixTheme.colorScheme.primary,
                                ),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            text = "取消",
                            onClick = {
                                selectedYear = currentYear
                                selectedMonth = currentMonth
                                selectedDay = currentDay
                                showDateDialog = false
                            },
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            text = "确定",
                            onClick = {
                                itemDate = "$selectedYear-${selectedMonth.toString().padStart(2, '0')}-${selectedDay.toString().padStart(2, '0')}"
                                showDateDialog = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.textButtonColorsPrimary(),
                        )
                    }
                }
            }
        }
    }
}

private enum class ItemViewMode(val label: String) {
    List("列表"),
    Grid("块状"),
}

private fun daysSince(dateString: String): Int {
    return try {
        val parts = dateString.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts[2].toInt()
        val purchase = Calendar.getInstance().apply { set(year, month - 1, day) }
        val now = Calendar.getInstance()
        val diff = now.timeInMillis - purchase.timeInMillis
        maxOf(1, (diff / (1000 * 60 * 60 * 24)).toInt())
    } catch (_: Exception) {
        1
    }
}

private fun buildSortDirectionMenuTitle(direction: SortDirection): String {
    return "正倒序（${sortDirectionLabel(direction)}）"
}

private fun sortDirectionLabel(direction: SortDirection): String {
    return when (direction) {
        SortDirection.ASC -> "升序"
        SortDirection.DESC -> "倒序"
    }
}

@Composable
private fun ItemListCard(
    icon: String,
    name: String,
    date: String,
    avgPrice: String,
    totalPrice: String,
    isPinned: Boolean = false,
    onClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.defaultColors(
            color = MiuixTheme.colorScheme.surfaceContainer
        ),
        insideMargin = PaddingValues(16.dp),
        onClick = onClick,
        showIndication = true,
        pressFeedbackType = PressFeedbackType.Tilt
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 左侧图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 24.sp,
                )
            }

            // 中间信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                    if (isPinned) {
                        Icon(
                            imageVector = MiuixIcons.Pin,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFFF9500)
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = MiuixIcons.Years,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MiuixTheme.colorScheme.onSurfaceSecondary
                    )
                    Text(
                        text = date,
                        fontSize = 13.sp,
                        color = MiuixTheme.colorScheme.onSurfaceSecondary,
                    )
                }
            }

            // 右侧价格
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = avgPrice,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onSurface,
                )
                Text(
                    text = totalPrice,
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceSecondary,
                )
            }
        }
    }
}

@Composable
private fun ItemGridCard(
    icon: String,
    name: String,
    date: String,
    avgPrice: String,
    totalPrice: String,
    isPinned: Boolean = false,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.defaultColors(
            color = MiuixTheme.colorScheme.surfaceContainer
        ),
        insideMargin = PaddingValues(14.dp),
        onClick = onClick,
        showIndication = true,
        pressFeedbackType = PressFeedbackType.Tilt
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 22.sp,
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        modifier = Modifier.weight(1f, fill = false),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MiuixTheme.colorScheme.onSurface,
                        maxLines = 2,
                    )
                    if (isPinned) {
                        Spacer(modifier = Modifier.padding(start = 4.dp))
                        Icon(
                            imageVector = MiuixIcons.Pin,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFFF9500)
                        )
                    }
                }
                Text(
                    text = date,
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurfaceSecondary,
                    maxLines = 1,
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = avgPrice,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MiuixTheme.colorScheme.onSurface,
                )
                Text(
                    text = totalPrice,
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurfaceSecondary,
                )
            }
        }
    }
}

// ... 以下 StatsCard / StatItem 保持原样 ...
@Composable
private fun StatsCard(stats: ItemStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.defaultColors(
            color = Color(0xFF1C1C1E)
        ),
        insideMargin = PaddingValues(20.dp),
        onClick = { },
        showIndication = true,
        pressFeedbackType = PressFeedbackType.Tilt
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            Text(
                text = "我的物品",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "物品价值",
                    value = "¥${stats.totalValue}",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "物品数量",
                    value = stats.itemCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "总日均价格",
                    value = "¥${stats.totalDailyPrice}",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MiuixTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF8E8E93),
            fontWeight = FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
