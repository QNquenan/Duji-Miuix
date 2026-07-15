package com.quenan.duji.ui.screen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.quenan.duji.data.day.DayData
import com.quenan.duji.data.day.DayType
import com.quenan.duji.data.day.RepeatCycle
import com.quenan.duji.data.day.computeStatus
import com.quenan.duji.data.day.parseDayDate
import com.quenan.duji.data.day.repeatLabel
import com.quenan.duji.data.day.targetDateFormatted
import com.quenan.duji.data.day.typeLabel
import com.quenan.duji.ui.component.EmptyStateCard
import com.quenan.duji.ui.component.rememberNoticeAction
import com.quenan.duji.ui.util.LunarDate
import com.quenan.duji.ui.util.lunarMonthDayCount
import com.quenan.duji.ui.util.lunarMonthList
import com.quenan.duji.ui.util.lunarToSolar
import com.quenan.duji.ui.util.solarToLunar
import java.time.LocalDate
import java.util.Calendar
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NumberPicker
import top.yukonga.miuix.kmp.basic.NumberPickerDefaults
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.TabRowWithContour
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
import top.yukonga.miuix.kmp.icon.extended.ListView
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.icon.extended.Sort
import top.yukonga.miuix.kmp.menu.OverlayIconCascadingDropdownMenu
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun ThoseDaysScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    openDayId: Long? = null,
    onOpenDayConsumed: () -> Unit = {},
) {
    val scrollBehavior = MiuixScrollBehavior()
    var fabVisible by remember { mutableStateOf(true) }
    var scrollDistance by remember { mutableFloatStateOf(0f) }
    val fabBottomOffset by animateDpAsState(
        targetValue = if (fabVisible) 0.dp else 120.dp,
        animationSpec = tween(durationMillis = 300),
        label = "those-days-fab-offset",
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

    val typeOptions = remember { listOf("倒/正数日", "纪念日", "生日") }
    val repeatOptions = remember { listOf("不重复", "每周", "每月", "每年") }
    val viewModel: ThoseDaysViewModel = viewModel()
    val dayCardModels by viewModel.dayCardModels.collectAsStateWithLifecycle()
    val currentViewMode by viewModel.currentViewMode.collectAsStateWithLifecycle()
    val currentSort by viewModel.currentSort.collectAsStateWithLifecycle()
    val showNotice = rememberNoticeAction()
    val emojiOptions = remember {
        listOf(
            EmojiOption("爱心", "❤️"), EmojiOption("双心", "💕"), EmojiOption("戒指", "💍"),
            EmojiOption("牵手", "🤝"), EmojiOption("玫瑰", "🌹"), EmojiOption("蛋糕", "🎂"),
            EmojiOption("庆祝", "🎉"), EmojiOption("彩花", "🎊"), EmojiOption("蝴蝶结", "🎀"),
            EmojiOption("气球", "🎈"), EmojiOption("灯笼", "🏮"), EmojiOption("红包", "🧧"),
            EmojiOption("礼物", "🎁"), EmojiOption("星星", "⭐"), EmojiOption("闪光", "✨"),
            EmojiOption("火焰", "🔥"), EmojiOption("满分", "💯"), EmojiOption("毕业帽", "🎓"),
            EmojiOption("飞机", "✈️"), EmojiOption("地图", "🗺️"), EmojiOption("家", "🏠"),
            EmojiOption("太阳", "☀️"), EmojiOption("月亮", "🌙"), EmojiOption("樱花", "🌸"),
        )
    }
    val emojiNameMap = remember(emojiOptions) { emojiOptions.associate { it.emoji to it.name } }

    var showBottomSheet by remember { mutableStateOf(false) }
    var showDetailBottomSheet by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var selectedDayItem by remember { mutableStateOf<DayData?>(null) }
    var editingDay by remember { mutableStateOf<DayData?>(null) }
    var showDateDialog by remember { mutableStateOf(false) }
    var showEmojiDialog by remember { mutableStateOf(false) }
    var showCustomEmojiDialog by remember { mutableStateOf(false) }
    var showWeekDaysDialog by remember { mutableStateOf(false) }
    var showMonthDaysDialog by remember { mutableStateOf(false) }
    var dayName by remember { mutableStateOf("") }
    var dayType by remember { mutableStateOf(typeOptions[0]) }
    var repeatCycle by remember { mutableStateOf(repeatOptions[0]) }
    var selectedDate by remember { mutableStateOf("") }
    var dayNote by remember { mutableStateOf("") }
    var isPinned by remember { mutableStateOf(false) }
    var selectedEmoji by remember { mutableStateOf<String?>(null) }
    var customEmojiText by remember { mutableStateOf("") }
    var selectedWeekDays by remember { mutableStateOf(setOf<Int>()) }
    var selectedMonthDays by remember { mutableStateOf(setOf<Int>()) }
    var hasPickedDate by remember { mutableStateOf(false) }

    val calendar = remember { Calendar.getInstance() }
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH) + 1
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    var selectedYear by remember { mutableIntStateOf(currentYear) }
    var selectedMonth by remember { mutableIntStateOf(currentMonth) }
    var selectedDay by remember { mutableIntStateOf(currentDay) }
    var isLunarMode by remember { mutableStateOf(false) }
    var lunarYear by remember { mutableIntStateOf(currentYear.coerceIn(1901, 2100)) }
    var lunarMonth by remember { mutableIntStateOf(currentMonth) }
    var lunarDay by remember { mutableIntStateOf(currentDay.coerceAtMost(30)) }
    var searchQuery by remember { mutableStateOf("") }
    var searchExpanded by remember { mutableStateOf(false) }
    val normalizedSearchQuery = remember(searchQuery) { searchQuery.trim() }
    val filteredDays = remember(dayCardModels, normalizedSearchQuery) {
        if (normalizedSearchQuery.isEmpty()) {
            dayCardModels
        } else {
            dayCardModels.filter { model ->
                model.day.name.contains(normalizedSearchQuery, ignoreCase = true) ||
                    model.day.note.contains(normalizedSearchQuery, ignoreCase = true) ||
                    model.day.emoji.contains(normalizedSearchQuery)
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(openDayId, dayCardModels) {
        val targetId = openDayId ?: return@LaunchedEffect
        val matchedDay = dayCardModels.firstOrNull { it.day.id == targetId }?.day ?: return@LaunchedEffect
        selectedDayItem = matchedDay
        showDetailBottomSheet = true
        onOpenDayConsumed()
    }

    fun resetAddForm() {
        editingDay = null
        dayName = ""
        dayType = typeOptions[0]
        repeatCycle = repeatOptions[0]
        selectedDate = ""
        dayNote = ""
        isPinned = false
        selectedEmoji = null
        customEmojiText = ""
        selectedWeekDays = emptySet()
        selectedMonthDays = emptySet()
        hasPickedDate = false
        isLunarMode = false
        selectedYear = currentYear
        selectedMonth = currentMonth
        selectedDay = currentDay
        lunarYear = currentYear.coerceIn(1901, 2100)
        lunarMonth = currentMonth
        lunarDay = currentDay.coerceAtMost(30)
    }

    fun populateForm(day: DayData) {
        editingDay = day
        selectedEmoji = day.emoji
        dayName = day.name
        dayType = day.typeLabel()
        repeatCycle = day.repeatLabel()
        dayNote = day.note
        isPinned = day.isPinned
        selectedWeekDays = day.weekDays.toSet()
        selectedMonthDays = day.monthDays.toSet()
        isLunarMode = day.isLunar
        parseDayDate(day.targetDate)?.let { date ->
            selectedYear = date.year
            selectedMonth = date.monthValue
            selectedDay = date.dayOfMonth
            hasPickedDate = true
            val lunar = solarToLunar(date.year, date.monthValue, date.dayOfMonth)
            if (lunar != null) {
                lunarYear = lunar.year
                lunarMonth = lunar.month
                lunarDay = lunar.day
            }
        }
        selectedDate = when {
            dayType == "生日" && isLunarMode -> {
                val lunar = LunarDate(lunarYear, lunarMonth, lunarDay)
                "每年 农历${lunar.formatted}"
            }
            dayType == "生日" -> "每年 ${selectedMonth.toString().padStart(2, '0')}月${selectedDay.toString().padStart(2, '0')}日"
            repeatCycle == "每周" -> {
                if (selectedWeekDays.isEmpty()) "请选择"
                else selectedWeekDays.sorted().joinToString(" ") { listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")[it] }
            }
            repeatCycle == "每月" -> {
                if (selectedMonthDays.isEmpty()) "请选择"
                else "每月 ${selectedMonthDays.sorted().joinToString(" ") { "$it 日" }}"
            }
            isLunarMode && hasPickedDate -> {
                val lunar = LunarDate(lunarYear, lunarMonth, lunarDay)
                "农历 ${lunarYear}年${lunar.formatted}"
            }
            hasPickedDate -> "$selectedYear-${selectedMonth.toString().padStart(2, '0')}-${selectedDay.toString().padStart(2, '0')}"
            else -> ""
        }
    }

    fun syncDateLabel() {
        selectedDate = when {
            dayType == "生日" && isLunarMode -> {
                val lunar = LunarDate(lunarYear, lunarMonth, lunarDay)
                "每年 农历${lunar.formatted}"
            }
            dayType == "生日" -> "每年 ${selectedMonth.toString().padStart(2, '0')}月${selectedDay.toString().padStart(2, '0')}日"
            repeatCycle == "每周" -> {
                if (selectedWeekDays.isEmpty()) "请选择"
                else selectedWeekDays.sorted().joinToString(" ") { listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")[it] }
            }
            repeatCycle == "每月" -> {
                if (selectedMonthDays.isEmpty()) "请选择"
                else "每月 ${selectedMonthDays.sorted().joinToString(" ") { "$it 日" }}"
            }
            isLunarMode && hasPickedDate -> {
                val lunar = LunarDate(lunarYear, lunarMonth, lunarDay)
                "农历 ${lunarYear}年${lunar.formatted}"
            }
            hasPickedDate -> "$selectedYear-${selectedMonth.toString().padStart(2, '0')}-${selectedDay.toString().padStart(2, '0')}"
            else -> ""
        }
    }

    fun syncLunarFromSolar() {
        val lunar = solarToLunar(selectedYear, selectedMonth, selectedDay) ?: return
        lunarYear = lunar.year
        lunarMonth = lunar.month
        lunarDay = lunar.day
    }

    fun applyTypeChange(newType: String) {
        dayType = newType
        if (newType == "生日") {
            repeatCycle = repeatOptions[3]
        }
        syncDateLabel()
    }

    fun applyRepeatCycleChange(newRepeatCycle: String) {
        repeatCycle = newRepeatCycle
        if (newRepeatCycle != "每周") {
            selectedWeekDays = emptySet()
        }
        if (newRepeatCycle != "每月") {
            selectedMonthDays = emptySet()
        }
        syncDateLabel()
    }

    fun safeLunarToSolar(year: Int, month: Int, day: Int): LocalDate? {
        lunarToSolar(year, month, day)?.let { return it }
        for (candidateDay in day - 1 downTo 1) {
            lunarToSolar(year, month, candidateDay)?.let { return it }
        }
        return null
    }

    val sortEntries = remember(currentSort) {
        listOf(
            DropdownEntry(
                items = listOf(
                    DropdownItem(
                        text = "创建时间",
                        selected = currentSort.field == DaySortField.CREATED_AT,
                        onClick = {
                            viewModel.updateSort(
                                DaySortField.CREATED_AT,
                                currentSort.direction,
                            )
                        },
                    ),
                    DropdownItem(
                        text = "事件日期",
                        selected = currentSort.field == DaySortField.EVENT_DATE,
                        onClick = {
                            viewModel.updateSort(
                                DaySortField.EVENT_DATE,
                                currentSort.direction,
                            )
                        },
                    ),
                )
            ),
            DropdownEntry(
                items = listOf(
                    DropdownItem(
                        text = buildDaySortDirectionMenuTitle(currentSort.direction),
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
                    title = "那些日子",
                    scrollBehavior = scrollBehavior,
                    actions = {
                        IconButton(onClick = {
                            viewModel.updateViewMode(
                                if (currentViewMode == ThoseDaysViewMode.List) ThoseDaysViewMode.Grid else ThoseDaysViewMode.List
                            )
                        }) {
                            Icon(
                                imageVector = if (currentViewMode == ThoseDaysViewMode.Grid) MiuixIcons.All else MiuixIcons.ListView,
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
                ThoseDaysSearchBar(
                    searchQuery = searchQuery,
                    searchExpanded = searchExpanded,
                    onQueryChange = {
                        searchQuery = it
                        searchExpanded = it.isNotBlank()
                    },
                    onExpandedChange = { searchExpanded = it },
                )
            }
        },
    ) { innerPadding ->
        Box(modifier = modifier.fillMaxSize()) {
            ThoseDaysContent(
                filteredDays = filteredDays,
                normalizedSearchQuery = normalizedSearchQuery,
                currentViewMode = currentViewMode,
                innerPadding = innerPadding,
                contentPadding = contentPadding,
                fabScrollConnection = fabScrollConnection,
                scrollBehavior = scrollBehavior,
                onDayClick = { day ->
                    selectedDayItem = day
                    showDetailBottomSheet = true
                },
            )

            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(contentPadding)
                    .padding(end = 16.dp, bottom = 16.dp)
                    .offset(y = fabBottomOffset),
                onClick = {
                    resetAddForm()
                    showBottomSheet = true
                },
            ) {
                Icon(imageVector = MiuixIcons.Add, contentDescription = "添加", tint = Color.White)
            }

            selectedDayItem?.let { detailDay ->
                WindowBottomSheet(
                    show = showDetailBottomSheet,
                    title = "日子详情",
                    backgroundColor = MiuixTheme.colorScheme.surface,
                    insideMargin = DpSize(10.dp, 12.dp),
                    startAction = {
                        val dismiss = LocalDismissState.current
                        IconButton(onClick = {
                            showDetailBottomSheet = false
                            dismiss?.invoke()
                        }) {
                            Icon(imageVector = MiuixIcons.Close, contentDescription = "关闭", tint = MiuixTheme.colorScheme.onBackground)
                        }
                    },
                    endAction = {
                        val dismiss = LocalDismissState.current
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { showDeleteConfirmDialog = true }) {
                                Icon(imageVector = MiuixIcons.Delete, contentDescription = "删除", tint = Color(0xFFFF3B30))
                            }
                            IconButton(onClick = {
                                populateForm(detailDay)
                                showDetailBottomSheet = false
                                dismiss?.invoke()
                                showBottomSheet = true
                            }) {
                                Icon(imageVector = MiuixIcons.Edit, contentDescription = "修改")
                            }
                        }
                    },
                    onDismissRequest = { showDetailBottomSheet = false },
                    onDismissFinished = { selectedDayItem = null },
                ) {
                    ThoseDaysDetailContent(detailDay = detailDay)
                }
            }

            if (showDeleteConfirmDialog && selectedDayItem != null) {
                WindowDialog(
                    title = "删除日子",
                    summary = "确认删除 ${selectedDayItem?.name} 吗？",
                    show = true,
                    onDismissRequest = { showDeleteConfirmDialog = false },
                ) {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(text = "取消", onClick = { showDeleteConfirmDialog = false }, modifier = Modifier.weight(1f))
                            TextButton(
                                text = "删除",
                                onClick = {
                                    selectedDayItem?.let { day ->
                                        viewModel.deleteDay(day)
                                        showNotice("删除成功")
                                    }
                                    showDeleteConfirmDialog = false
                                    showDetailBottomSheet = false
                                    selectedDayItem = null
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.textButtonColors(textColor = Color(0xFFFF3B30)),
                            )
                        }
                    }
                }
            }

            WindowBottomSheet(
                show = showBottomSheet,
                title = if (editingDay == null) "添加日子" else "修改日子",
                backgroundColor = MiuixTheme.colorScheme.surface,
                insideMargin = DpSize(10.dp, 12.dp),
                startAction = {
                    val dismiss = LocalDismissState.current
                    IconButton(onClick = {
                        showDateDialog = false
                        showEmojiDialog = false
                        showCustomEmojiDialog = false
                        showWeekDaysDialog = false
                        showMonthDaysDialog = false
                        editingDay = null
                        dismiss?.invoke()
                    }) {
                        Icon(imageVector = MiuixIcons.Close, contentDescription = "关闭", tint = MiuixTheme.colorScheme.onBackground)
                    }
                },
                endAction = {
                    val dismiss = LocalDismissState.current
                    IconButton(onClick = {
                        val shouldPickDate = repeatCycle != "每周" && repeatCycle != "每月"
                        when {
                            dayName.isBlank() -> showNotice("日子名称不能为空")
                            repeatCycle == "每周" && selectedWeekDays.isEmpty() -> showNotice("每周日期不能为空")
                            repeatCycle == "每月" && selectedMonthDays.isEmpty() -> showNotice("每月日期不能为空")
                            shouldPickDate && !hasPickedDate -> showNotice("日期不能为空")
                            else -> {
                                val type = when (dayType) {
                                    "纪念日" -> DayType.ANNIVERSARY
                                    "生日" -> DayType.BIRTHDAY
                                    else -> DayType.DAYS
                                }
                                val cycle = when (repeatCycle) {
                                    "每周" -> RepeatCycle.WEEKLY
                                    "每月" -> RepeatCycle.MONTHLY
                                    "每年" -> RepeatCycle.YEARLY
                                    else -> RepeatCycle.NONE
                                }
                                val solarDate = if (isLunarMode) safeLunarToSolar(lunarYear, lunarMonth, lunarDay)
                                else LocalDate.of(selectedYear, selectedMonth, selectedDay)
                                val resolvedEmoji: String = selectedEmoji ?: "📅"
                                val day = DayData(
                                    id = editingDay?.id ?: 0L,
                                    emoji = resolvedEmoji,
                                    emojiName = emojiNameMap[resolvedEmoji] ?: "自定义",
                                    name = dayName,
                                    type = type,
                                    repeatCycle = cycle,
                                    targetDate = solarDate?.toString() ?: return@IconButton,
                                    note = dayNote,
                                    weekDays = selectedWeekDays.sorted(),
                                    monthDays = selectedMonthDays.sorted(),
                                    isLunar = isLunarMode,
                                    isPinned = isPinned,
                                    createdAt = editingDay?.createdAt ?: 0L,
                                )
                                if (editingDay == null) {
                                    viewModel.addDay(
                                        emoji = day.emoji,
                                        emojiName = day.emojiName,
                                        name = day.name,
                                        type = day.type,
                                        repeatCycle = day.repeatCycle,
                                        targetDate = day.targetDate,
                                        note = day.note,
                                        weekDays = day.weekDays,
                                        monthDays = day.monthDays,
                                        isLunar = day.isLunar,
                                        isPinned = day.isPinned,
                                    )
                                    showNotice("添加成功")
                                } else {
                                    viewModel.updateDay(day)
                                    showNotice("修改成功")
                                }
                                editingDay = null
                                dismiss?.invoke()
                            }
                        }
                    }) {
                        Icon(imageVector = MiuixIcons.Ok, contentDescription = "确认", tint = MiuixTheme.colorScheme.onBackground)
                    }
                },
                onDismissRequest = {
                    showDateDialog = false
                    showEmojiDialog = false
                    showCustomEmojiDialog = false
                    showWeekDaysDialog = false
                    showMonthDaysDialog = false
                    showBottomSheet = false
                    editingDay = null
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
                            .background(MiuixTheme.colorScheme.secondaryContainer, RoundedCornerShape(16.dp))
                            .clickable { showEmojiDialog = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = selectedEmoji ?: "📅", fontSize = 32.sp)
                    }

                    SmallTitle(text = "信息", insideMargin = PaddingValues(top = 16.dp, bottom = 2.dp, start = 16.dp), modifier = Modifier.fillMaxWidth())
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer)) {
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextField(value = dayName, onValueChange = { dayName = it }, label = "日子名称", modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), maxLines = 1)
                            WindowDropdownPreference(title = "类型", items = typeOptions, selectedIndex = typeOptions.indexOf(dayType), onSelectedIndexChange = { applyTypeChange(typeOptions[it]) }, insideMargin = PaddingValues(horizontal = 16.dp))
                            if (dayType == "倒/正数日") {
                                WindowDropdownPreference(title = "重复周期", items = repeatOptions, selectedIndex = repeatOptions.indexOf(repeatCycle), onSelectedIndexChange = { applyRepeatCycleChange(repeatOptions[it]) }, insideMargin = PaddingValues(horizontal = 16.dp))
                            }
                            Surface(
                                onClick = {
                                    when {
                                        repeatCycle == "每周" -> showWeekDaysDialog = true
                                        repeatCycle == "每月" -> showMonthDaysDialog = true
                                        else -> showDateDialog = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = MiuixTheme.colorScheme.secondaryContainer,
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = if (selectedDate.isEmpty()) 17.dp else 11.dp, bottom = if (selectedDate.isEmpty()) 17.dp else 11.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    if (selectedDate.isNotEmpty()) {
                                        Text(text = "日期选择", color = MiuixTheme.colorScheme.onSecondaryContainer, fontSize = 10.sp)
                                    }
                                    Text(text = if (selectedDate.isEmpty()) "日期选择" else selectedDate, color = if (selectedDate.isEmpty()) MiuixTheme.colorScheme.onSecondaryContainer else MiuixTheme.colorScheme.onBackground, style = MiuixTheme.textStyles.main)
                                }
                            }
                            TextField(value = dayNote, onValueChange = { dayNote = it }, label = "备注", singleLine = false, minLines = 3, maxLines = 5, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp))
                        }
                    }

                    SmallTitle(text = "功能", insideMargin = PaddingValues(top = 16.dp, bottom = 2.dp, start = 16.dp), modifier = Modifier.fillMaxWidth())
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer)) {
                        SwitchPreference(title = "置顶", checked = isPinned, onCheckedChange = { isPinned = it })
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            EmojiPickerDialog(
                show = showEmojiDialog,
                selectedEmoji = selectedEmoji,
                emojiOptions = emojiOptions,
                onDismiss = { showEmojiDialog = false },
                onConfirm = {
                    selectedEmoji = it
                    showEmojiDialog = false
                },
                onCustom = {
                    showEmojiDialog = false
                    showCustomEmojiDialog = true
                },
            )

            CustomEmojiDialog(
                show = showCustomEmojiDialog,
                customEmojiText = customEmojiText,
                onValueChange = { customEmojiText = it },
                onDismiss = { showCustomEmojiDialog = false },
                onConfirm = {
                    selectedEmoji = customEmojiText
                    customEmojiText = ""
                    showCustomEmojiDialog = false
                },
            )

            WeekDaysDialog(show = showWeekDaysDialog, selectedWeekDays = selectedWeekDays, onDismiss = { showWeekDaysDialog = false }, onConfirm = {
                selectedWeekDays = it
                syncDateLabel()
                showWeekDaysDialog = false
            })

            MonthDaysDialog(show = showMonthDaysDialog, selectedMonthDays = selectedMonthDays, onDismiss = { showMonthDaysDialog = false }, onConfirm = {
                selectedMonthDays = it
                syncDateLabel()
                showMonthDaysDialog = false
            })

            DatePickerDialog(
                show = showDateDialog,
                isLunarMode = isLunarMode,
                selectedYear = selectedYear,
                selectedMonth = selectedMonth,
                selectedDay = selectedDay,
                lunarYear = lunarYear,
                lunarMonth = lunarMonth,
                lunarDay = lunarDay,
                onDismiss = { showDateDialog = false },
                onSolarChanged = { year, month, day ->
                    selectedYear = year
                    selectedMonth = month
                    selectedDay = day
                    syncLunarFromSolar()
                },
                onLunarModeChanged = {
                    if (it && !isLunarMode) syncLunarFromSolar()
                    isLunarMode = it
                    syncDateLabel()
                },
                onLunarChanged = { year, month, day ->
                    lunarYear = year
                    lunarMonth = month
                    lunarDay = day
                },
                onConfirm = {
                    if (isLunarMode) {
                        val solar = safeLunarToSolar(lunarYear, lunarMonth, lunarDay) ?: return@DatePickerDialog
                        selectedYear = solar.year
                        selectedMonth = solar.monthValue
                        selectedDay = solar.dayOfMonth
                    } else {
                        syncLunarFromSolar()
                    }
                    hasPickedDate = true
                    syncDateLabel()
                    showDateDialog = false
                },
            )
        }
    }
}

@Composable
private fun EmojiPickerDialog(
    show: Boolean,
    selectedEmoji: String?,
    emojiOptions: List<EmojiOption>,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit,
    onCustom: () -> Unit,
) {
    WindowDialog(title = "选择表情", show = show, onDismissRequest = onDismiss) {
        var tempSelectedEmoji by remember { mutableStateOf(selectedEmoji) }
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            LazyVerticalGrid(columns = GridCells.Fixed(6), modifier = Modifier.fillMaxWidth().height(208.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), userScrollEnabled = false) {
                items(emojiOptions.size, span = { GridItemSpan(1) }) { index ->
                    val option = emojiOptions[index]
                    val isSelected = tempSelectedEmoji == option.emoji
                    Surface(onClick = { tempSelectedEmoji = option.emoji }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = if (isSelected) MiuixTheme.colorScheme.primary.copy(alpha = 0.18f) else MiuixTheme.colorScheme.secondaryContainer) {
                        Box(modifier = Modifier.fillMaxWidth().height(44.dp), contentAlignment = Alignment.Center) {
                            Text(text = option.emoji, fontSize = 24.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.padding(bottom = 16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(text = "取消", onClick = onDismiss, modifier = Modifier.weight(1f))
                TextButton(text = "自定义", onClick = onCustom, modifier = Modifier.weight(1f))
                TextButton(text = "确定", onClick = { onConfirm(tempSelectedEmoji) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.textButtonColorsPrimary())
            }
        }
    }
}

@Composable
private fun CustomEmojiDialog(
    show: Boolean,
    customEmojiText: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    WindowDialog(title = "自定义表情", show = show, onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            TextField(value = customEmojiText, onValueChange = onValueChange, label = "输入表情或文字", useLabelAsPlaceholder = true, modifier = Modifier.fillMaxWidth(), maxLines = 1)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(text = "取消", onClick = onDismiss, modifier = Modifier.weight(1f))
                TextButton(text = "确定", onClick = onConfirm, modifier = Modifier.weight(1f), colors = ButtonDefaults.textButtonColorsPrimary())
            }
        }
    }
}

@Composable
private fun WeekDaysDialog(
    show: Boolean,
    selectedWeekDays: Set<Int>,
    onDismiss: () -> Unit,
    onConfirm: (Set<Int>) -> Unit,
) {
    WindowDialog(title = "选择每周重复", show = show, onDismissRequest = onDismiss) {
        var tempWeekDays by remember { mutableStateOf(selectedWeekDays) }
        val weekDayOptions = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                weekDayOptions.forEachIndexed { index, label ->
                    val isSelected = tempWeekDays.contains(index)
                    Surface(onClick = { tempWeekDays = if (isSelected) tempWeekDays - index else tempWeekDays + index }, shape = RoundedCornerShape(12.dp), color = if (isSelected) MiuixTheme.colorScheme.primary.copy(alpha = 0.18f) else MiuixTheme.colorScheme.secondaryContainer) {
                        Text(text = label, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), color = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
            Spacer(modifier = Modifier.padding(bottom = 16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(text = "取消", onClick = onDismiss, modifier = Modifier.weight(1f))
                TextButton(text = "确定", onClick = { onConfirm(tempWeekDays) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.textButtonColorsPrimary())
            }
        }
    }
}

@Composable
private fun MonthDaysDialog(
    show: Boolean,
    selectedMonthDays: Set<Int>,
    onDismiss: () -> Unit,
    onConfirm: (Set<Int>) -> Unit,
) {
    WindowDialog(title = "选择每月重复", show = show, onDismissRequest = onDismiss) {
        var tempMonthDays by remember { mutableStateOf(selectedMonthDays) }
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth().height(240.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), userScrollEnabled = false) {
                items(31, span = { GridItemSpan(1) }) { index ->
                    val day = index + 1
                    val isSelected = tempMonthDays.contains(day)
                    Surface(onClick = { tempMonthDays = if (isSelected) tempMonthDays - day else tempMonthDays + day }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = if (isSelected) MiuixTheme.colorScheme.primary.copy(alpha = 0.18f) else MiuixTheme.colorScheme.secondaryContainer) {
                        Box(modifier = Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.Center) {
                            Text(text = day.toString(), color = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(text = "取消", onClick = onDismiss, modifier = Modifier.weight(1f))
                TextButton(text = "确定", onClick = { onConfirm(tempMonthDays) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.textButtonColorsPrimary())
            }
        }
    }
}

@Composable
private fun DatePickerDialog(
    show: Boolean,
    isLunarMode: Boolean,
    selectedYear: Int,
    selectedMonth: Int,
    selectedDay: Int,
    lunarYear: Int,
    lunarMonth: Int,
    lunarDay: Int,
    onDismiss: () -> Unit,
    onSolarChanged: (Int, Int, Int) -> Unit,
    onLunarModeChanged: (Boolean) -> Unit,
    onLunarChanged: (Int, Int, Int) -> Unit,
    onConfirm: () -> Unit,
) {
    if (!show) return
    WindowDialog(title = "选择日期", show = show, onDismissRequest = onDismiss) {
        var tempLunarMode by remember(isLunarMode) { mutableStateOf(isLunarMode) }
        var tempSelectedYear by remember(selectedYear) { mutableIntStateOf(selectedYear) }
        var tempSelectedMonth by remember(selectedMonth) { mutableIntStateOf(selectedMonth) }
        var tempSelectedDay by remember(selectedDay) { mutableIntStateOf(selectedDay) }
        var tempLunarYear by remember(lunarYear) { mutableIntStateOf(lunarYear) }
        var tempLunarMonth by remember(lunarMonth) { mutableIntStateOf(lunarMonth) }
        var tempLunarDay by remember(lunarDay) { mutableIntStateOf(lunarDay) }
        val currentLunarMonths = lunarMonthList(tempLunarYear)
        val normalizedLunarMonth = currentLunarMonths.firstOrNull { it.first == tempLunarMonth }?.first ?: currentLunarMonths.firstOrNull()?.first ?: 1
        val lunarDayCount = lunarMonthDayCount(tempLunarYear, normalizedLunarMonth).coerceAtLeast(1)

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            val tabs = listOf("公历", "农历")
            var selectedTabIndex by remember(tempLunarMode) { mutableIntStateOf(if (tempLunarMode) 1 else 0) }
            TabRowWithContour(tabs = tabs, selectedTabIndex = selectedTabIndex, onTabSelected = {
                selectedTabIndex = it
                tempLunarMode = it == 1
            }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            if (!tempLunarMode) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Top) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "年", style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.onBackgroundVariant)
                        NumberPicker(value = tempSelectedYear, onValueChange = {
                            tempSelectedYear = it
                            tempSelectedDay = tempSelectedDay.coerceAtMost(LocalDate.of(tempSelectedYear, tempSelectedMonth, 1).lengthOfMonth())
                        }, range = 1900..2100, wrapAround = true, modifier = Modifier.width(100.dp), label = { it.toString() }, colors = NumberPickerDefaults.colors(selectedTextColor = MiuixTheme.colorScheme.primary))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "月", style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.onBackgroundVariant)
                        NumberPicker(value = tempSelectedMonth, onValueChange = {
                            tempSelectedMonth = it
                            tempSelectedDay = tempSelectedDay.coerceAtMost(LocalDate.of(tempSelectedYear, tempSelectedMonth, 1).lengthOfMonth())
                        }, range = 1..12, wrapAround = true, modifier = Modifier.width(80.dp), label = { it.toString().padStart(2, '0') }, colors = NumberPickerDefaults.colors(selectedTextColor = MiuixTheme.colorScheme.primary))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "日", style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.onBackgroundVariant)
                        NumberPicker(value = tempSelectedDay, onValueChange = { tempSelectedDay = it }, range = 1..LocalDate.of(tempSelectedYear, tempSelectedMonth, 1).lengthOfMonth(), wrapAround = true, modifier = Modifier.width(80.dp), label = { it.toString().padStart(2, '0') }, colors = NumberPickerDefaults.colors(selectedTextColor = MiuixTheme.colorScheme.primary))
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Top) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "年", style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.onBackgroundVariant)
                        NumberPicker(value = tempLunarYear, onValueChange = {
                            tempLunarYear = it.coerceIn(1901, 2100)
                            val months = lunarMonthList(tempLunarYear)
                            tempLunarMonth = months.firstOrNull { month -> month.first == tempLunarMonth }?.first ?: months.firstOrNull()?.first ?: 1
                            tempLunarDay = tempLunarDay.coerceAtMost(lunarMonthDayCount(tempLunarYear, tempLunarMonth).coerceAtLeast(1))
                        }, range = 1901..2100, wrapAround = true, modifier = Modifier.width(100.dp), label = { it.toString() }, colors = NumberPickerDefaults.colors(selectedTextColor = MiuixTheme.colorScheme.primary))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "月", style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.onBackgroundVariant)
                        NumberPicker(value = currentLunarMonths.indexOfFirst { it.first == normalizedLunarMonth }.coerceAtLeast(0), onValueChange = { pickerIndex ->
                            tempLunarMonth = currentLunarMonths.getOrNull(pickerIndex)?.first ?: currentLunarMonths.firstOrNull()?.first ?: 1
                            tempLunarDay = tempLunarDay.coerceAtMost(lunarMonthDayCount(tempLunarYear, tempLunarMonth).coerceAtLeast(1))
                        }, range = 0 until currentLunarMonths.size, wrapAround = true, modifier = Modifier.width(90.dp), label = { index -> currentLunarMonths[index].second }, colors = NumberPickerDefaults.colors(selectedTextColor = MiuixTheme.colorScheme.primary))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "日", style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.onBackgroundVariant)
                        NumberPicker(value = tempLunarDay, onValueChange = { tempLunarDay = it }, range = 1..lunarDayCount, wrapAround = true, modifier = Modifier.width(90.dp), label = { dayValue -> LunarDate(0, 1, dayValue).dayName }, colors = NumberPickerDefaults.colors(selectedTextColor = MiuixTheme.colorScheme.primary))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(text = "取消", onClick = onDismiss, modifier = Modifier.weight(1f))
                TextButton(text = "确定", onClick = {
                    onSolarChanged(tempSelectedYear, tempSelectedMonth, tempSelectedDay)
                    onLunarModeChanged(tempLunarMode)
                    onLunarChanged(tempLunarYear, tempLunarMonth, tempLunarDay)
                    onConfirm()
                }, modifier = Modifier.weight(1f), colors = ButtonDefaults.textButtonColorsPrimary())
            }
        }
    }
}

@Composable
internal fun ThoseDaysInlineDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, fontSize = 14.sp, color = MiuixTheme.colorScheme.onBackgroundVariant)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = value, fontSize = 15.sp, color = MiuixTheme.colorScheme.onBackground)
    }
}

private fun buildDaySortDirectionMenuTitle(direction: SortDirection): String {
    return "正倒序（${sortDirectionLabel(direction)}）"
}

private fun sortDirectionLabel(direction: SortDirection): String {
    return when (direction) {
        SortDirection.ASC -> "升序"
        SortDirection.DESC -> "倒序"
    }
}

private data class EmojiOption(
    val name: String,
    val emoji: String,
)
