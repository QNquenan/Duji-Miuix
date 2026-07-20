package com.quenan.duji.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quenan.duji.ui.util.solarToLunar
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.abs
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NumberPicker
import top.yukonga.miuix.kmp.basic.NumberPickerDefaults
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Forward
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

private val weekLabels = listOf("一", "二", "三", "四", "五", "六", "日")
private val weekendColor = Color(0xFF4D8DFF)

@Composable
fun CheckInScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val today = remember { LocalDate.now() }
    val scrollBehavior = MiuixScrollBehavior()
    var displayedMonth by remember { mutableStateOf(YearMonth.from(today)) }
    var selectedDate by remember { mutableStateOf(today) }
    var monthTransitionDirection by remember { mutableIntStateOf(1) }
    var showMonthPicker by remember { mutableStateOf(false) }

    fun changeToMonth(newMonth: YearMonth) {
        monthTransitionDirection = if (newMonth.isBefore(displayedMonth)) -1 else 1
        displayedMonth = newMonth
        selectedDate = selectedDateInMonth(newMonth, selectedDate)
    }

    fun changeMonth(monthDelta: Long) {
        val (newMonth, _) = moveToMonth(displayedMonth, selectedDate, monthDelta)
        changeToMonth(newMonth)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = "打卡",
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = { changeMonth(-1) }) {
                        Icon(imageVector = MiuixIcons.Back, contentDescription = "上个月")
                    }
                    IconButton(onClick = { changeMonth(1) }) {
                        Icon(imageVector = MiuixIcons.Forward, contentDescription = "下个月")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(horizontal = 20.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(displayedMonth, selectedDate) {
                        var totalDrag = 0f
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, dragAmount ->
                                totalDrag += dragAmount
                            },
                            onDragEnd = {
                                if (abs(totalDrag) >= 48.dp.toPx()) {
                                    changeMonth(if (totalDrag < 0f) 1 else -1)
                                }
                            },
                            onDragCancel = {},
                        )
                    },
            ) {
                AnimatedContent(
                    targetState = displayedMonth,
                    transitionSpec = {
                        val direction = monthTransitionDirection
                        (slideInHorizontally(tween(260)) { width -> width * direction } + fadeIn(tween(180))) togetherWith
                            (slideOutHorizontally(tween(260)) { width -> -width * direction } + fadeOut(tween(180)))
                    },
                    label = "日历月份切换",
                ) { month ->
                    Column {
                        Text(
                            text = "${month.year}年${month.monthValue}月",
                            modifier = Modifier
                                .padding(top = 12.dp, bottom = 18.dp)
                                .clickable { showMonthPicker = true },
                            color = MiuixTheme.colorScheme.onBackground,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            weekLabels.forEachIndexed { index, label ->
                                Text(
                                    text = label,
                                    modifier = Modifier.weight(1f),
                                    color = if (index >= 5) weekendColor else MiuixTheme.colorScheme.onBackground,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val firstDay = month.atDay(1)
                        val gridStart = firstDay.minusDays((firstDay.dayOfWeek.value - 1).toLong())
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            repeat(5) { weekIndex ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    repeat(7) { dayIndex ->
                                        val date = gridStart.plusDays((weekIndex * 7 + dayIndex).toLong())
                                        CheckInDayCell(
                                            date = date,
                                            isCurrentMonth = YearMonth.from(date) == month,
                                            isToday = date == today,
                                            isSelected = date == selectedDate,
                                            isWeekend = dayIndex >= 5,
                                            onClick = { selectedDate = date },
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    MonthPickerDialog(
        show = showMonthPicker,
        displayedMonth = displayedMonth,
        onDismiss = { showMonthPicker = false },
        onConfirm = {
            changeToMonth(it)
            showMonthPicker = false
        },
    )
}

@Composable
private fun CheckInDayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    isWeekend: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lunar = remember(date) { solarToLunar(date.year, date.monthValue, date.dayOfMonth) }
    val primaryColor = MiuixTheme.colorScheme.onBackground
    val mutedColor = MiuixTheme.colorScheme.onBackground.copy(alpha = 0.28f)
    val dayColor = when {
        isSelected -> MiuixTheme.colorScheme.onPrimary
        !isCurrentMonth -> mutedColor
        isWeekend -> weekendColor
        else -> primaryColor
    }
    val lunarColor = when {
        isSelected -> MiuixTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
        !isCurrentMonth -> mutedColor
        else -> MiuixTheme.colorScheme.onBackgroundVariant
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MiuixTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = dayColor,
            fontSize = 20.sp,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = lunar?.dayName.orEmpty(),
            color = lunarColor,
            fontSize = 11.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

internal fun moveToMonth(
    displayedMonth: YearMonth,
    selectedDate: LocalDate,
    monthDelta: Long,
): Pair<YearMonth, LocalDate> {
    val newMonth = displayedMonth.plusMonths(monthDelta)
    val day = selectedDate.dayOfMonth.coerceAtMost(newMonth.lengthOfMonth())
    return newMonth to newMonth.atDay(day)
}

internal fun selectedDateInMonth(month: YearMonth, selectedDate: LocalDate): LocalDate {
    return month.atDay(selectedDate.dayOfMonth.coerceAtMost(month.lengthOfMonth()))
}

@Composable
private fun MonthPickerDialog(
    show: Boolean,
    displayedMonth: YearMonth,
    onDismiss: () -> Unit,
    onConfirm: (YearMonth) -> Unit,
) {
    if (!show) return

    WindowDialog(
        title = "选择年月",
        show = show,
        onDismissRequest = onDismiss,
    ) {
        var tempYear by remember(displayedMonth) { mutableIntStateOf(displayedMonth.year) }
        var tempMonth by remember(displayedMonth) { mutableIntStateOf(displayedMonth.monthValue) }

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
                        value = tempYear,
                        onValueChange = { tempYear = it },
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
                        value = tempMonth,
                        onValueChange = { tempMonth = it },
                        range = 1..12,
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    text = "取消",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = "确定",
                    onClick = { onConfirm(YearMonth.of(tempYear, tempMonth)) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
        }
    }
}
