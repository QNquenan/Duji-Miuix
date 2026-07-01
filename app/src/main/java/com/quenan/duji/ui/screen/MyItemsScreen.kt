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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.sample
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
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
                    containerColor = Color(0xFF1A1A1A)
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
                            .background(
                                color = Color(0xFF2C2C2E),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                    }

                    // 名称
                    SmallTitle(text = "名称", modifier = Modifier.fillMaxWidth())
                    TextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = "请输入物品名称",
                    )

                    // 价格
                    SmallTitle(text = "价格", modifier = Modifier.fillMaxWidth())
                    TextField(
                        value = itemPrice,
                        onValueChange = { itemPrice = it },
                        label = "请输入价格",
                        modifier = Modifier.fillMaxWidth(),
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
                        label = "请输入备注",
                        singleLine = false,
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // 底部间距
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // 购买日期选择对话框
            WindowDialog(
                title = "选择日期",
                summary = "test",
                show = showDateDialog,
                onDismissRequest = { showDateDialog = false },
            ) {
                val dismiss = LocalDismissState.current
                Text(
                    text = "test",
                    modifier = Modifier.fillMaxWidth(),
                    color = MiuixTheme.colorScheme.onBackground,
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
