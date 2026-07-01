package com.quenan.duji.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.sample
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NumberPicker
import top.yukonga.miuix.kmp.basic.NumberPickerColors
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
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun MyItemsScreen() {
    val scrollBehavior = MiuixScrollBehavior()
    val scrollState = rememberScrollState()
    var previousScroll by remember { mutableIntStateOf(0) }
    var showFab by remember { mutableStateOf(true) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var itemName by remember { mutableStateOf("") }
    var itemPrice by remember { mutableStateOf("") }
    var itemDate by remember { mutableStateOf("") }
    var itemNote by remember { mutableStateOf("") }
    var showDateDialog by remember { mutableStateOf(false) }
    var showIconDialog by remember { mutableStateOf(false) }
    var showCustomIconDialog by remember { mutableStateOf(false) }
    var selectedIcon by remember { mutableStateOf("📦") }
    var customIconText by remember { mutableStateOf("") }
    var isPinned by remember { mutableStateOf(false) }
    val calendar = remember { Calendar.getInstance() }
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH) + 1
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    var selectedYear by remember { mutableIntStateOf(currentYear) }
    var selectedMonth by remember { mutableIntStateOf(currentMonth) }
    var selectedDay by remember { mutableIntStateOf(currentDay) }

    // 监听滚动方向：向下滚隐藏 FAB，向上滚显示 FAB
    @OptIn(FlowPreview::class)
    LaunchedEffect(Unit) {
        snapshotFlow { scrollState.value }
            .sample(100)  // 每 100ms 采样一次，避免过于频繁
            .distinctUntilChanged()
            .collect { current ->
                if (current > previousScroll && current > 0) {
                    showFab = false  // 向下滚动 → 隐藏
                } else if (current < previousScroll) {
                    showFab = true   // 向上滚动 → 显示
                }
                previousScroll = current
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "我的物品",
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatsCard()
                ItemListCard(
                    icon = "📱",
                    name = "iPhone 15 Pro",
                    date = "2024-09-22",
                    avgPrice = "¥18/天",
                    totalPrice = "¥8999"
                )
                ItemListCard(
                    icon = "💻",
                    name = "MacBook Air",
                    date = "2023-06-15",
                    avgPrice = "¥12/天",
                    totalPrice = "¥7999"
                )
            }

            // FAB
            AnimatedVisibility(
                visible = showFab,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp),
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                FloatingActionButton(
                    onClick = { showBottomSheet = true },
                ) {
                    Icon(
                        imageVector = MiuixIcons.Add,
                        contentDescription = "添加",
                        tint = Color.White
                    )
                }
            }

            // WindowBottomSheet - 点击 FAB 时弹出
            WindowBottomSheet(
                show = showBottomSheet,
                title = "添加物品",
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
                onDismissRequest = { showBottomSheet = false },
            ) {
                // BottomSheet 内容区域 - 可滚动
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 12.dp),
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

                    // 名称
                    SmallTitle(text = "名称", modifier = Modifier.fillMaxWidth())
                    TextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = "请输入名称",
                        useLabelAsPlaceholder = true,
                        maxLines = 1,
                    )

                    // 价格
                    SmallTitle(text = "价格", modifier = Modifier.fillMaxWidth())
                    TextField(
                        value = itemPrice,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() }) {
                                itemPrice = it
                            }
                        },
                        label = "请输入价格",
                        useLabelAsPlaceholder = true,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    )

                    // 购买日期
                    SmallTitle(text = "购买日期", modifier = Modifier.fillMaxWidth())
                    Surface(
                        onClick = { showDateDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MiuixTheme.colorScheme.secondaryContainer,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = if (itemDate.isEmpty()) "请选择购买日期" else itemDate,
                                color = if (itemDate.isEmpty()) MiuixTheme.colorScheme.onSecondaryContainer else MiuixTheme.colorScheme.onBackground,
                                style = MiuixTheme.textStyles.main,
                            )
                        }
                    }

                    // 备注
                    SmallTitle(text = "备注", modifier = Modifier.fillMaxWidth())
                    TextField(
                        value = itemNote,
                        onValueChange = { itemNote = it },
                        label = "备注",
                        useLabelAsPlaceholder = true,
                        singleLine = false,
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // 置顶开关
                    SwitchPreference(
                        title = "置顶",
                        checked = isPinned,
                        onCheckedChange = { isPinned = it },
                    )

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
                                                color = if (isSelected) MiuixTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
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

@Composable
private fun ItemListCard(
    icon: String,
    name: String,
    date: String,
    avgPrice: String,
    totalPrice: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.defaultColors(
            color = Color(0xFFF2F2F7),
            borderColor = Color(0xFFD1D1D6)
        ),
        borderWidth = 1.5.dp,
        insideMargin = PaddingValues(16.dp),
        onClick = { },
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
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = date,
                    fontSize = 13.sp,
                    color = Color(0xFF8E8E93),
                )
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
                    color = Color.White,
                )
                Text(
                    text = totalPrice,
                    fontSize = 13.sp,
                    color = Color(0xFF8E8E93),
                )
            }
        }
    }
}

// ... 以下 StatsCard / StatItem 保持原样 ...
@Composable
private fun StatsCard() {
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
                    value = "¥6157",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "物品数量",
                    value = "3",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "总日均价格",
                    value = "¥17",
                    modifier = Modifier.weight(1f)
                )
            }
        }
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
