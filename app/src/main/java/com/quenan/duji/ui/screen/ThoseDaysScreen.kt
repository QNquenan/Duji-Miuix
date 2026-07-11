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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quenan.duji.ui.util.LunarDate
import com.quenan.duji.ui.util.lunarMonthDayCount
import com.quenan.duji.ui.util.lunarMonthList
import com.quenan.duji.ui.util.lunarToSolar
import com.quenan.duji.ui.util.solarToLunar
import java.time.LocalDate
import java.util.Calendar
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NumberPicker
import top.yukonga.miuix.kmp.basic.NumberPickerDefaults
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun ThoseDaysScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
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
    val emojiOptions = remember {
        listOf(
            EmojiOption("爱心", "❤️"),
            EmojiOption("双心", "💕"),
            EmojiOption("戒指", "💍"),
            EmojiOption("牵手", "🤝"),
            EmojiOption("玫瑰", "🌹"),
            EmojiOption("蛋糕", "🎂"),
            EmojiOption("庆祝", "🎉"),
            EmojiOption("彩花", "🎊"),
            EmojiOption("蝴蝶结", "🎀"),
            EmojiOption("气球", "🎈"),
            EmojiOption("灯笼", "🏮"),
            EmojiOption("红包", "🧧"),
            EmojiOption("礼物", "🎁"),
            EmojiOption("星星", "⭐"),
            EmojiOption("闪光", "✨"),
            EmojiOption("火焰", "🔥"),
            EmojiOption("满分", "💯"),
            EmojiOption("毕业帽", "🎓"),
            EmojiOption("飞机", "✈️"),
            EmojiOption("地图", "🗺️"),
            EmojiOption("家", "🏠"),
            EmojiOption("太阳", "☀️"),
            EmojiOption("月亮", "🌙"),
            EmojiOption("樱花", "🌸"),
        )
    }

    var showBottomSheet by remember { mutableStateOf(false) }
    var showDateDialog by remember { mutableStateOf(false) }
    var showEmojiDialog by remember { mutableStateOf(false) }
    var showWeekDaysDialog by remember { mutableStateOf(false) }
    var showMonthDaysDialog by remember { mutableStateOf(false) }
    var dayName by remember { mutableStateOf("") }
    var dayType by remember { mutableStateOf(typeOptions[0]) }
    var repeatCycle by remember { mutableStateOf(repeatOptions[0]) }
    var selectedDate by remember { mutableStateOf("") }
    var dayNote by remember { mutableStateOf("") }
    var isPinned by remember { mutableStateOf(false) }
    var selectedEmoji by remember { mutableStateOf<String?>(null) }
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

    fun resetAddForm() {
        dayName = ""
        dayType = typeOptions[0]
        repeatCycle = repeatOptions[0]
        selectedDate = ""
        dayNote = ""
        isPinned = false
        selectedEmoji = null
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

    fun syncDateLabel() {
        selectedDate = when {
            dayType == "生日" && isLunarMode -> {
                val lunar = LunarDate(lunarYear, lunarMonth, lunarDay)
                "每年 农历${lunar.formatted}"
            }
            dayType == "生日" -> "每年 ${selectedMonth.toString().padStart(2, '0')}月${selectedDay.toString().padStart(2, '0')}日"
            repeatCycle == "每周" -> {
                if (selectedWeekDays.isEmpty()) {
                    "请选择"
                } else {
                    val weekLabels = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
                    selectedWeekDays.sorted().joinToString(" ") { weekLabels[it] }
                }
            }
            repeatCycle == "每月" -> {
                if (selectedMonthDays.isEmpty()) {
                    "请选择"
                } else {
                    val labels = selectedMonthDays.sorted().joinToString(" ") { "$it 日" }
                    "每月 $labels"
                }
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

    fun syncSolarFromLunar(): Boolean {
        val solar = lunarToSolar(lunarYear, lunarMonth, lunarDay) ?: return false
        selectedYear = solar.year
        selectedMonth = solar.monthValue
        selectedDay = solar.dayOfMonth
        return true
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = "那些日子",
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(fabScrollConnection)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top = innerPadding.calculateTopPadding() + 16.dp,
                        bottom = contentPadding.calculateBottomPadding() + 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SmallTitle(
                    text = "那些日子",
                    insideMargin = PaddingValues(0.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    insideMargin = PaddingValues(16.dp),
                    colors = CardDefaults.defaultColors(
                        color = MiuixTheme.colorScheme.surfaceContainer,
                    ),
                ) {
                    Text(
                        text = "在这里记录纪念日、发薪日、还款日或任何需要被记住的日期。",
                        color = MiuixTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                    )
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
                    showBottomSheet = true
                },
            ) {
                Icon(
                    imageVector = MiuixIcons.Add,
                    contentDescription = "添加",
                    tint = Color.White,
                )
            }

            WindowBottomSheet(
                show = showBottomSheet,
                title = "添加日子",
                backgroundColor = MiuixTheme.colorScheme.surface,
                startAction = {
                    val dismiss = LocalDismissState.current
                    IconButton(onClick = {
                        showDateDialog = false
                        showEmojiDialog = false
                        showWeekDaysDialog = false
                        showMonthDaysDialog = false
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
                        val shouldPickDate = repeatCycle != "每周" && repeatCycle != "每月"
                        val invalidDate = repeatCycle == "不重复" && dayType == "倒/正数日" && selectedYear < 1900
                        when {
                            dayName.isBlank() -> return@IconButton
                            repeatCycle == "每周" && selectedWeekDays.isEmpty() -> return@IconButton
                            repeatCycle == "每月" && selectedMonthDays.isEmpty() -> return@IconButton
                            shouldPickDate && !hasPickedDate -> return@IconButton
                            invalidDate -> return@IconButton
                            else -> dismiss?.invoke()
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
                    showDateDialog = false
                    showEmojiDialog = false
                    showWeekDaysDialog = false
                    showMonthDaysDialog = false
                    showBottomSheet = false
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
                            .background(
                                color = MiuixTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(16.dp),
                            )
                            .clickable { showEmojiDialog = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (selectedEmoji == null) {
                            Text(
                                text = "📅",
                                fontSize = 32.sp,
                            )
                        } else {
                            Text(
                                text = selectedEmoji.orEmpty(),
                                fontSize = 32.sp,
                            )
                        }
                    }

                    SmallTitle(
                        text = "信息",
                        insideMargin = PaddingValues(16.dp, 2.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        insideMargin = PaddingValues(0.dp),
                        colors = CardDefaults.defaultColors(
                            color = MiuixTheme.colorScheme.surfaceContainer,
                        ),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            TextField(
                                value = dayName,
                                onValueChange = { dayName = it },
                                label = "日子名称",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                maxLines = 1,
                            )

                            WindowDropdownPreference(
                                title = "类型",
                                items = typeOptions,
                                selectedIndex = typeOptions.indexOf(dayType),
                                onSelectedIndexChange = { applyTypeChange(typeOptions[it]) },
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )

                            if (dayType == "倒/正数日") {
                                WindowDropdownPreference(
                                    title = "重复周期",
                                    items = repeatOptions,
                                    selectedIndex = repeatOptions.indexOf(repeatCycle),
                                    onSelectedIndexChange = { applyRepeatCycleChange(repeatOptions[it]) },
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                            }

                            Surface(
                                onClick = {
                                    when {
                                        repeatCycle == "每周" -> showWeekDaysDialog = true
                                        repeatCycle == "每月" -> showMonthDaysDialog = true
                                        else -> showDateDialog = true
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = MiuixTheme.colorScheme.secondaryContainer,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            start = 16.dp,
                                            end = 16.dp,
                                            top = if (selectedDate.isEmpty()) 17.dp else 11.dp,
                                            bottom = if (selectedDate.isEmpty()) 17.dp else 11.dp,
                                        ),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    if (selectedDate.isNotEmpty()) {
                                        Text(
                                            text = "日期选择",
                                            color = MiuixTheme.colorScheme.onSecondaryContainer,
                                            fontSize = 10.sp,
                                        )
                                    }
                                    Text(
                                        text = if (selectedDate.isEmpty()) "日期选择" else selectedDate,
                                        color = if (selectedDate.isEmpty()) {
                                            MiuixTheme.colorScheme.onSecondaryContainer
                                        } else {
                                            MiuixTheme.colorScheme.onBackground
                                        },
                                        style = MiuixTheme.textStyles.main,
                                    )
                                }
                            }

                            TextField(
                                value = dayNote,
                                onValueChange = { dayNote = it },
                                label = "备注",
                                singleLine = false,
                                minLines = 3,
                                maxLines = 5,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                            )
                        }
                    }

                    SmallTitle(
                        text = "功能",
                        insideMargin = PaddingValues(16.dp, 2.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        insideMargin = PaddingValues(0.dp),
                        colors = CardDefaults.defaultColors(
                            color = MiuixTheme.colorScheme.surfaceContainer,
                        ),
                    ) {
                        SwitchPreference(
                            title = "置顶",
                            checked = isPinned,
                            onCheckedChange = { isPinned = it },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        )
                    }

                    Spacer(modifier = Modifier.padding(bottom = 24.dp))
                }
            }

            WindowDialog(
                title = "选择表情",
                show = showEmojiDialog,
                onDismissRequest = { showEmojiDialog = false },
            ) {
                var tempSelectedEmoji by remember { mutableStateOf(selectedEmoji) }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(208.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        userScrollEnabled = false,
                    ) {
                        items(emojiOptions.size, span = { GridItemSpan(1) }) { index ->
                            val option = emojiOptions[index]
                            val isSelected = tempSelectedEmoji == option.emoji
                            Surface(
                                onClick = { tempSelectedEmoji = option.emoji },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) {
                                    MiuixTheme.colorScheme.primary.copy(alpha = 0.18f)
                                } else {
                                    MiuixTheme.colorScheme.secondaryContainer
                                },
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = option.emoji,
                                        fontSize = 24.sp,
                                    )
                                    Text(
                                        text = option.name,
                                        fontSize = 10.sp,
                                        color = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onBackgroundVariant,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.padding(bottom = 16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TextButton(
                            text = "取消",
                            onClick = { showEmojiDialog = false },
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            text = "自定义",
                            onClick = {
                                selectedEmoji = tempSelectedEmoji
                                showEmojiDialog = false
                            },
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            text = "确定",
                            onClick = {
                                selectedEmoji = tempSelectedEmoji
                                showEmojiDialog = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.textButtonColorsPrimary(),
                        )
                    }
                }
            }

            WindowDialog(
                title = "选择每周重复",
                show = showWeekDaysDialog,
                onDismissRequest = { showWeekDaysDialog = false },
            ) {
                var tempWeekDays by remember { mutableStateOf(selectedWeekDays) }
                val weekDayOptions = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        weekDayOptions.forEachIndexed { index, label ->
                            val isSelected = tempWeekDays.contains(index)
                            Surface(
                                onClick = {
                                    tempWeekDays = if (isSelected) tempWeekDays - index else tempWeekDays + index
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) {
                                    MiuixTheme.colorScheme.primary.copy(alpha = 0.18f)
                                } else {
                                    MiuixTheme.colorScheme.secondaryContainer
                                },
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    color = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSecondaryContainer,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.padding(bottom = 16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        TextButton(
                            text = "取消",
                            onClick = { showWeekDaysDialog = false },
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            text = "确定",
                            onClick = {
                                selectedWeekDays = tempWeekDays
                                syncDateLabel()
                                showWeekDaysDialog = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.textButtonColorsPrimary(),
                        )
                    }
                }
            }

            WindowDialog(
                title = "选择每月重复",
                show = showMonthDaysDialog,
                onDismissRequest = { showMonthDaysDialog = false },
            ) {
                var tempMonthDays by remember { mutableStateOf(selectedMonthDays) }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        userScrollEnabled = false,
                    ) {
                        items(31, span = { GridItemSpan(1) }) { index ->
                            val day = index + 1
                            val isSelected = tempMonthDays.contains(day)
                            Surface(
                                onClick = {
                                    tempMonthDays = if (isSelected) tempMonthDays - day else tempMonthDays + day
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) {
                                    MiuixTheme.colorScheme.primary.copy(alpha = 0.18f)
                                } else {
                                    MiuixTheme.colorScheme.secondaryContainer
                                },
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = day.toString(),
                                        color = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        TextButton(
                            text = "取消",
                            onClick = { showMonthDaysDialog = false },
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            text = "确定",
                            onClick = {
                                selectedMonthDays = tempMonthDays
                                syncDateLabel()
                                showMonthDaysDialog = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.textButtonColorsPrimary(),
                        )
                    }
                }
            }

            WindowDialog(
                title = "选择日期",
                show = showDateDialog,
                onDismissRequest = { showDateDialog = false },
            ) {
                val currentLunarMonths = lunarMonthList(lunarYear)
                val normalizedLunarMonth = currentLunarMonths.firstOrNull { it.first == lunarMonth }?.first
                    ?: currentLunarMonths.firstOrNull()?.first
                    ?: 1
                val lunarDayCount = lunarMonthDayCount(lunarYear, normalizedLunarMonth).coerceAtLeast(1)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val tabs = listOf("公历", "农历")
                    var selectedTabIndex by remember(isLunarMode) { mutableStateOf(if (isLunarMode) 1 else 0) }

                    TabRowWithContour(
                        tabs = tabs,
                        selectedTabIndex = selectedTabIndex,
                        onTabSelected = {
                            selectedTabIndex = it
                            if (it == 1 && !isLunarMode) {
                                syncLunarFromSolar()
                            }
                            isLunarMode = it == 1
                            syncDateLabel()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isLunarMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "年",
                                    style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.onBackgroundVariant,
                                )
                                NumberPicker(
                                    value = selectedYear,
                                    onValueChange = {
                                        selectedYear = it
                                        if (selectedDay > LocalDate.of(selectedYear, selectedMonth, 1).lengthOfMonth()) {
                                            selectedDay = LocalDate.of(selectedYear, selectedMonth, 1).lengthOfMonth()
                                        }
                                        syncLunarFromSolar()
                                    },
                                    range = 1900..2100,
                                    wrapAround = true,
                                    modifier = Modifier.width(100.dp),
                                    label = { it.toString() },
                                    colors = NumberPickerDefaults.colors(
                                        selectedTextColor = MiuixTheme.colorScheme.primary,
                                    ),
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "月",
                                    style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.onBackgroundVariant,
                                )
                                NumberPicker(
                                    value = selectedMonth,
                                    onValueChange = {
                                        selectedMonth = it
                                        if (selectedDay > LocalDate.of(selectedYear, selectedMonth, 1).lengthOfMonth()) {
                                            selectedDay = LocalDate.of(selectedYear, selectedMonth, 1).lengthOfMonth()
                                        }
                                        syncLunarFromSolar()
                                    },
                                    range = 1..12,
                                    wrapAround = true,
                                    modifier = Modifier.width(80.dp),
                                    label = { it.toString().padStart(2, '0') },
                                    colors = NumberPickerDefaults.colors(
                                        selectedTextColor = MiuixTheme.colorScheme.primary,
                                    ),
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "日",
                                    style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.onBackgroundVariant,
                                )
                                NumberPicker(
                                    value = selectedDay,
                                    onValueChange = {
                                        selectedDay = it
                                        syncLunarFromSolar()
                                    },
                                    range = 1..LocalDate.of(selectedYear, selectedMonth, 1).lengthOfMonth(),
                                    wrapAround = true,
                                    modifier = Modifier.width(80.dp),
                                    label = { it.toString().padStart(2, '0') },
                                    colors = NumberPickerDefaults.colors(
                                        selectedTextColor = MiuixTheme.colorScheme.primary,
                                    ),
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "年",
                                    style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.onBackgroundVariant,
                                )
                                NumberPicker(
                                    value = lunarYear,
                                    onValueChange = {
                                        lunarYear = it.coerceIn(1901, 2100)
                                        val months = lunarMonthList(lunarYear)
                                        lunarMonth = months.firstOrNull { month -> month.first == lunarMonth }?.first
                                            ?: months.firstOrNull()?.first
                                            ?: 1
                                        lunarDay = lunarDay.coerceAtMost(lunarMonthDayCount(lunarYear, lunarMonth).coerceAtLeast(1))
                                    },
                                    range = 1901..2100,
                                    wrapAround = true,
                                    modifier = Modifier.width(100.dp),
                                    label = { it.toString() },
                                    colors = NumberPickerDefaults.colors(
                                        selectedTextColor = MiuixTheme.colorScheme.primary,
                                    ),
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "月",
                                    style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.onBackgroundVariant,
                                )
                                NumberPicker(
                                    value = currentLunarMonths.indexOfFirst { it.first == normalizedLunarMonth }.coerceAtLeast(0),
                                    onValueChange = { pickerIndex ->
                                        lunarMonth = currentLunarMonths.getOrNull(pickerIndex)?.first ?: currentLunarMonths.firstOrNull()?.first ?: 1
                                        lunarDay = lunarDay.coerceAtMost(lunarMonthDayCount(lunarYear, lunarMonth).coerceAtLeast(1))
                                    },
                                    range = 0 until currentLunarMonths.size,
                                    wrapAround = true,
                                    modifier = Modifier.width(90.dp),
                                    label = { index -> currentLunarMonths[index].second },
                                    colors = NumberPickerDefaults.colors(
                                        selectedTextColor = MiuixTheme.colorScheme.primary,
                                    ),
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "日",
                                    style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.onBackgroundVariant,
                                )
                                NumberPicker(
                                    value = lunarDay,
                                    onValueChange = { lunarDay = it },
                                    range = 1..lunarDayCount,
                                    wrapAround = true,
                                    modifier = Modifier.width(90.dp),
                                    label = { dayValue -> LunarDate(0, 1, dayValue).dayName },
                                    colors = NumberPickerDefaults.colors(
                                        selectedTextColor = MiuixTheme.colorScheme.primary,
                                    ),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        TextButton(
                            text = "取消",
                            onClick = { showDateDialog = false },
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            text = "确定",
                            onClick = {
                                if (isLunarMode) {
                                    val solar = safeLunarToSolar(lunarYear, lunarMonth, lunarDay) ?: return@TextButton
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
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.textButtonColorsPrimary(),
                        )
                    }
                }
            }
        }
    }
}

private data class EmojiOption(
    val name: String,
    val emoji: String,
)
