package com.quenan.duji.ui.screen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var dayName by remember { mutableStateOf("") }
    var dayType by remember { mutableStateOf(typeOptions[0]) }
    var repeatCycle by remember { mutableStateOf(repeatOptions[0]) }
    var selectedDate by remember { mutableStateOf("") }
    var dayNote by remember { mutableStateOf("") }
    var isPinned by remember { mutableStateOf(false) }
    var selectedEmoji by remember { mutableStateOf<String?>(null) }

    val calendar = remember { Calendar.getInstance() }
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH) + 1
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    var selectedYear by remember { mutableIntStateOf(currentYear) }
    var selectedMonth by remember { mutableIntStateOf(currentMonth) }
    var selectedDay by remember { mutableIntStateOf(currentDay) }

    fun resetAddForm() {
        dayName = ""
        dayType = typeOptions[0]
        repeatCycle = repeatOptions[0]
        selectedDate = ""
        dayNote = ""
        isPinned = false
        selectedEmoji = null
        selectedYear = currentYear
        selectedMonth = currentMonth
        selectedDay = currentDay
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
                    IconButton(onClick = { dismiss?.invoke() }) {
                        Icon(
                            imageVector = MiuixIcons.Close,
                            contentDescription = "关闭",
                            tint = MiuixTheme.colorScheme.onBackground,
                        )
                    }
                },
                endAction = {
                    val dismiss = LocalDismissState.current
                    IconButton(onClick = { dismiss?.invoke() }) {
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
                        insideMargin = PaddingValues(16.dp),
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
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 1,
                            )

                            WindowDropdownPreference(
                                title = "类型",
                                items = typeOptions,
                                selectedIndex = typeOptions.indexOf(dayType),
                                onSelectedIndexChange = { dayType = typeOptions[it] },
                            )

                            WindowDropdownPreference(
                                title = "重复周期",
                                items = repeatOptions,
                                selectedIndex = repeatOptions.indexOf(repeatCycle),
                                onSelectedIndexChange = { repeatCycle = repeatOptions[it] },
                            )

                            Surface(
                                onClick = { showDateDialog = true },
                                modifier = Modifier.fillMaxWidth(),
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
                                modifier = Modifier.fillMaxWidth(),
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
                        colors = CardDefaults.defaultColors(
                            color = MiuixTheme.colorScheme.surfaceContainer,
                        ),
                    ) {
                        SwitchPreference(
                            title = "置顶",
                            checked = isPinned,
                            onCheckedChange = { isPinned = it },
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
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        emojiOptions.chunked(6).forEach { rowOptions ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Top,
                            ) {
                                rowOptions.forEach { option ->
                                    val isSelected = tempSelectedEmoji == option.emoji
                                    Column(
                                        modifier = Modifier.width(44.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(
                                                    color = if (isSelected) {
                                                        MiuixTheme.colorScheme.primary.copy(alpha = 0.18f)
                                                    } else {
                                                        Color.Transparent
                                                    },
                                                    shape = RoundedCornerShape(12.dp),
                                                )
                                                .clickable { tempSelectedEmoji = option.emoji },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = option.emoji,
                                                fontSize = 24.sp,
                                            )
                                        }
                                        Text(
                                            text = option.name,
                                            fontSize = 10.sp,
                                            color = MiuixTheme.colorScheme.onBackgroundVariant,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                        )
                                    }
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
                            text = "默认",
                            onClick = {
                                selectedEmoji = null
                                showEmojiDialog = false
                            },
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            text = "取消",
                            onClick = { showEmojiDialog = false },
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
                title = "选择日期",
                show = showDateDialog,
                onDismissRequest = { showDateDialog = false },
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
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

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

                    Spacer(modifier = Modifier.padding(bottom = 16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                                selectedDate = "$selectedYear-${selectedMonth.toString().padStart(2, '0')}-${selectedDay.toString().padStart(2, '0')}"
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
