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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.quenan.duji.data.checkin.CheckInItem
import com.quenan.duji.ui.component.DuJiCalendar
import com.quenan.duji.ui.component.EmptyStateCard
import com.quenan.duji.ui.component.rememberNoticeAction
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.ColorPalette
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowDialog

private const val DEFAULT_CHECK_IN_EMOJI = "🏋️"
private const val DEFAULT_CHECK_IN_COLOR_ARGB = 0xFF5EBD7DL
private val defaultCheckInColor = Color(DEFAULT_CHECK_IN_COLOR_ARGB)
private val checkInBlockSize = 4.dp

@Composable
fun CheckInScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val scrollBehavior = MiuixScrollBehavior()
    val viewModel: CheckInViewModel = viewModel()
    val cardModels by viewModel.cardModels.collectAsStateWithLifecycle()
    val showNotice = rememberNoticeAction()
    val emojiOptions = remember {
        listOf(
            "🏋️", "🏃", "🚶", "🚴", "🏊", "🧘", "🤸", "⚽",
            "🏀", "🏸", "🎾", "🥊", "💪", "📚", "✍️", "🥗",
            "💧", "💊", "🌙", "☀️", "🧹", "🎸", "🧠", "🪴",
        )
    }

    var fabVisible by remember { mutableStateOf(true) }
    var scrollDistance by remember { mutableFloatStateOf(0f) }
    val fabBottomOffset by animateDpAsState(
        targetValue = if (fabVisible) 0.dp else 240.dp,
        animationSpec = tween(durationMillis = 300),
        label = "check-in-fab-offset",
    )
    val fabScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                scrollDistance += available.y
                if (scrollDistance < -50f) {
                    fabVisible = false
                    scrollDistance = 0f
                } else if (scrollDistance > 50f) {
                    fabVisible = true
                    scrollDistance = 0f
                }
                return Offset.Zero
            }
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var showEmojiDialog by remember { mutableStateOf(false) }
    var showCustomEmojiDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var checkInName by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf(DEFAULT_CHECK_IN_EMOJI) }
    var customEmojiText by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(defaultCheckInColor) }
    var selectedCard by remember { mutableStateOf<CheckInCardUiModel?>(null) }
    var editingItem by remember { mutableStateOf<CheckInItem?>(null) }
    var showDetailBottomSheet by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    fun resetAddForm() {
        checkInName = ""
        selectedEmoji = DEFAULT_CHECK_IN_EMOJI
        customEmojiText = ""
        selectedColor = defaultCheckInColor
        showEmojiDialog = false
        showCustomEmojiDialog = false
        showColorDialog = false
    }

    fun populateForm(item: CheckInItem) {
        checkInName = item.name
        selectedEmoji = item.emoji
        customEmojiText = ""
        selectedColor = Color(item.colorArgb)
        showEmojiDialog = false
        showCustomEmojiDialog = false
        showColorDialog = false
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = "打卡",
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .nestedScroll(fabScrollConnection)
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding)
                    .padding(horizontal = 12.dp),
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                if (cardModels.isEmpty()) {
                    EmptyStateCard(
                        title = "还没有打卡项",
                        summary = "点击右下角按钮添加一个吧",
                    )
                } else {
                    cardModels.forEach { card ->
                        CheckInCard(
                            card = card,
                            onCheckIn = {
                                viewModel.checkIn(card.item.id) { isSuccess ->
                                    showNotice(if (isSuccess) "打卡成功" else "请勿重复打卡")
                                }
                            },
                            onClick = {
                                selectedCard = card
                                showDetailBottomSheet = true
                            },
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(contentPadding)
                    .padding(end = 16.dp, bottom = 16.dp)
                    .offset { IntOffset(x = 0, y = fabBottomOffset.roundToPx()) },
                onClick = {
                    resetAddForm()
                    editingItem = null
                    showAddDialog = true
                },
            ) {
                Icon(
                    imageVector = MiuixIcons.Add,
                    contentDescription = "添加打卡项",
                    tint = Color.White,
                )
            }
        }
    }

    CheckInItemDialog(
        show = showAddDialog,
        title = if (editingItem == null) "添加打卡项" else "编辑打卡项",
        name = checkInName,
        emoji = selectedEmoji,
        color = selectedColor,
        onNameChange = { checkInName = it },
        onEmojiClick = { showEmojiDialog = true },
        onColorClick = { showColorDialog = true },
        onDismiss = {
            showAddDialog = false
            showEmojiDialog = false
            showCustomEmojiDialog = false
            showColorDialog = false
            editingItem = null
        },
        onConfirm = {
            val name = checkInName.trim()
            if (name.isEmpty()) {
                showNotice("请输入打卡名")
                return@CheckInItemDialog
            }
            val colorArgb = selectedColor.toArgb().toLong() and 0xFFFF_FFFFL
            editingItem?.let { item ->
                viewModel.updateItem(
                    item.copy(
                        emoji = selectedEmoji,
                        name = name,
                        colorArgb = colorArgb,
                    ),
                )
                showNotice("保存成功")
            } ?: viewModel.addItem(
                emoji = selectedEmoji,
                name = name,
                colorArgb = colorArgb,
            )
            showAddDialog = false
            editingItem = null
        },
    )

    CheckInEmojiPickerDialog(
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

    CheckInCustomEmojiDialog(
        show = showCustomEmojiDialog,
        customEmojiText = customEmojiText,
        onValueChange = { customEmojiText = it },
        onDismiss = { showCustomEmojiDialog = false },
        onConfirm = {
            customEmojiText.trim().takeIf { it.isNotEmpty() }?.let { selectedEmoji = it }
            showCustomEmojiDialog = false
        },
    )

    CheckInColorPickerDialog(
        show = showColorDialog,
        selectedColor = selectedColor,
        onDismiss = { showColorDialog = false },
        onConfirm = {
            selectedColor = it
            showColorDialog = false
        },
    )

    selectedCard?.let { detailCard ->
        WindowBottomSheet(
            show = showDetailBottomSheet,
            title = "打卡详情",
            backgroundColor = MiuixTheme.colorScheme.surface,
            insideMargin = DpSize(10.dp, 12.dp),
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
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(
                            imageVector = MiuixIcons.Delete,
                            contentDescription = "删除",
                            tint = Color(0xFFFF3B30),
                        )
                    }
                    IconButton(onClick = {
                        populateForm(detailCard.item)
                        editingItem = detailCard.item
                        showDetailBottomSheet = false
                        dismiss?.invoke()
                        showAddDialog = true
                    }) {
                        Icon(
                            imageVector = MiuixIcons.Edit,
                            contentDescription = "编辑",
                            tint = MiuixTheme.colorScheme.onBackground,
                        )
                    }
                }
            },
            onDismissRequest = { showDetailBottomSheet = false },
            onDismissFinished = { selectedCard = null },
        ) {
            CheckInDetailContent(detailCard)
        }
    }

    if (showDeleteConfirmDialog && selectedCard != null) {
        WindowDialog(
            title = "删除打卡项",
            summary = "确认删除 ${selectedCard?.item?.name} 及其全部打卡记录吗？",
            show = true,
            onDismissRequest = { showDeleteConfirmDialog = false },
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
                        selectedCard?.let { card ->
                            viewModel.deleteItem(card.item.id)
                            showNotice("删除成功")
                        }
                        showDeleteConfirmDialog = false
                        showDetailBottomSheet = false
                        selectedCard = null
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
        }
    }
}

@Composable
private fun CheckInCard(
    card: CheckInCardUiModel,
    onCheckIn: () -> Unit,
    onClick: () -> Unit,
) {
    val itemColor = remember(card.item.colorArgb) { Color(card.item.colorArgb) }
    val completedDays = card.currentMonthCompletedDays
    val monthlyCount = completedDays.size
    val inactiveColor = MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = 0.35f)
    val buttonColor = if (card.checkedToday) itemColor.copy(alpha = 0.2f) else itemColor
    val buttonContentColor = if (card.checkedToday) itemColor else Color.White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        insideMargin = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = card.item.emoji, fontSize = 26.sp)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = card.item.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onBackground,
                )
                Text(
                    text = "本月${monthlyCount}次 • 共${card.records.size}次",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onBackgroundVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(card.currentMonthDayCount) { index ->
                        val day = index + 1
                        Box(
                            modifier = Modifier
                                .size(checkInBlockSize)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (day in completedDays) itemColor else inactiveColor),
                        )
                    }
                }
            }
            Button(
                onClick = onCheckIn,
                modifier = Modifier.width(84.dp),
                cornerRadius = 50.dp,
                minWidth = 84.dp,
                minHeight = 50.dp,
                colors = ButtonDefaults.buttonColors(
                    color = buttonColor,
                    contentColor = buttonContentColor,
                ),
            ) {
                Text(
                    text = if (card.checkedToday) "已打卡" else "打卡",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun CheckInDetailContent(card: CheckInCardUiModel) {
    val itemColor = remember(card.item.colorArgb) { Color(card.item.colorArgb) }
    val monthlyCount = card.currentMonthCompletedDays.size
    val badgeColors = remember(card.recordDates, itemColor) {
        card.recordDates.associateWith { itemColor }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = card.item.emoji, fontSize = 28.sp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = card.item.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onBackground,
                )
                Text(
                    text = "本月${monthlyCount}次 • 共${card.records.size}次",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onBackgroundVariant,
                )
            }
        }
        DuJiCalendar(
            badgeColors = badgeColors,
            allowCollapse = false,
            prefetchAdjacentMonths = false,
            monthTitleFontSize = 32.sp,
            monthTitleToWeekSpacing = 8.dp,
            onDateSelected = {},
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CheckInItemDialog(
    show: Boolean,
    title: String,
    name: String,
    emoji: String,
    color: Color,
    onNameChange: (String) -> Unit,
    onEmojiClick: () -> Unit,
    onColorClick: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    WindowDialog(
        title = title,
        show = show,
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextField(
                value = name,
                onValueChange = onNameChange,
                label = "打卡名",
                useLabelAsPlaceholder = true,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Button(
                onClick = onEmojiClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(),
            ) {
                Text(text = emoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "选择表情")
            }
            Button(
                onClick = onColorClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(),
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(color),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "选择颜色")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(
                    text = "取消",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = "确定",
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
        }
    }
}

@Composable
private fun CheckInEmojiPickerDialog(
    show: Boolean,
    selectedEmoji: String,
    emojiOptions: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onCustom: () -> Unit,
) {
    WindowDialog(
        title = "选择表情",
        show = show,
        onDismissRequest = onDismiss,
    ) {
        var tempSelectedEmoji by remember(selectedEmoji) { mutableStateOf(selectedEmoji) }
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
                items(emojiOptions) { emoji ->
                    val isSelected = tempSelectedEmoji == emoji
                    Surface(
                        onClick = { tempSelectedEmoji = emoji },
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
                                .height(44.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = emoji, fontSize = 24.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(text = "取消", onClick = onDismiss, modifier = Modifier.weight(1f))
                TextButton(text = "自定义", onClick = onCustom, modifier = Modifier.weight(1f))
                TextButton(
                    text = "确定",
                    onClick = { onConfirm(tempSelectedEmoji) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
        }
    }
}

@Composable
private fun CheckInCustomEmojiDialog(
    show: Boolean,
    customEmojiText: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    WindowDialog(
        title = "自定义表情",
        show = show,
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TextField(
                value = customEmojiText,
                onValueChange = onValueChange,
                label = "输入表情或文字",
                useLabelAsPlaceholder = true,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(text = "取消", onClick = onDismiss, modifier = Modifier.weight(1f))
                TextButton(
                    text = "确定",
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
        }
    }
}

@Composable
private fun CheckInColorPickerDialog(
    show: Boolean,
    selectedColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (Color) -> Unit,
) {
    WindowDialog(
        title = "选择颜色",
        show = show,
        onDismissRequest = onDismiss,
    ) {
        var temporaryColor by remember(selectedColor) { mutableStateOf(selectedColor) }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ColorPalette(
                color = temporaryColor,
                onColorChanged = { temporaryColor = it },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(text = "取消", onClick = onDismiss, modifier = Modifier.weight(1f))
                TextButton(
                    text = "确定",
                    onClick = { onConfirm(temporaryColor) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
        }
    }
}
