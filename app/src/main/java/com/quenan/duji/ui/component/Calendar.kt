package com.quenan.duji.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quenan.duji.ui.util.solarToLunar
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.NumberPicker
import top.yukonga.miuix.kmp.basic.NumberPickerDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Forward
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

private const val CALENDAR_PAGE_COUNT = 10_001
private const val CALENDAR_INITIAL_PAGE = CALENDAR_PAGE_COUNT / 2
private const val CALENDAR_DEFAULT_WEEK_COUNT = 6
private const val CALENDAR_BEYOND_VIEWPORT_PAGE_COUNT = 1
private val calendarRowSpacing = 8.dp
private val weekLabels = listOf("一", "二", "三", "四", "五", "六", "日")
private val weekendColor = Color(0xFF4D8DFF)

@Composable
fun DuJiCalendar(
    modifier: Modifier = Modifier,
    initialDate: LocalDate = LocalDate.now(),
    badgeColors: Map<LocalDate, Color> = emptyMap(),
    onDateSelected: (LocalDate) -> Unit = {},
) {
    key(initialDate) {
        val today = initialDate
        val baseMonth = remember { YearMonth.from(today) }
        val coroutineScope = rememberCoroutineScope()
        val calendarPagerState = rememberPagerState(
            initialPage = CALENDAR_INITIAL_PAGE,
            pageCount = { CALENDAR_PAGE_COUNT },
        )
        val calendarContentReady = rememberCalendarContentReady()
        var selectedDate by remember { mutableStateOf(today) }
        var displayedMonth by remember { mutableStateOf(baseMonth) }
        var showMonthPicker by remember { mutableStateOf(false) }
        var collapseProgress by remember { mutableFloatStateOf(0f) }
        var collapseAnimationJob by remember { mutableStateOf<Job?>(null) }
        val collapseDistancePx = with(LocalDensity.current) { 240.dp.toPx() }

        LaunchedEffect(calendarPagerState, baseMonth) {
            snapshotFlow { calendarPagerState.settledPage }
                .collect { page ->
                    val settledMonth = calendarMonthForPage(page, baseMonth)
                    displayedMonth = settledMonth
                    selectedDate = selectedDateInMonth(settledMonth, selectedDate)
                }
        }

        fun changeToMonth(newMonth: YearMonth) {
            selectedDate = selectedDateInMonth(newMonth, selectedDate)
            coroutineScope.launch {
                calendarPagerState.animateScrollToPage(calendarPageForMonth(newMonth, baseMonth))
            }
        }

        fun changeMonth(monthDelta: Int) {
            coroutineScope.launch {
                val targetPage = (calendarPagerState.currentPage + monthDelta)
                    .coerceIn(0, CALENDAR_PAGE_COUNT - 1)
                calendarPagerState.animateScrollToPage(targetPage)
            }
        }

        Column(
            modifier = modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    var totalDrag = 0f
                    detectVerticalDragGestures(
                        onDragStart = {
                            totalDrag = 0f
                            collapseAnimationJob?.cancel()
                        },
                        onVerticalDrag = { _, dragAmount ->
                            collapseAnimationJob?.cancel()
                            totalDrag += dragAmount
                            collapseProgress = (collapseProgress - dragAmount / collapseDistancePx)
                                .coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            if (abs(totalDrag) >= 48.dp.toPx()) {
                                val target = if (collapseProgress >= 0.5f) 1f else 0f
                                collapseAnimationJob = coroutineScope.launch {
                                    val animation = Animatable(collapseProgress)
                                    animation.animateTo(target, animationSpec = tween(260)) {
                                        collapseProgress = value
                                    }
                                }
                            }
                        },
                        onDragCancel = {
                            collapseAnimationJob?.cancel()
                        },
                    )
                },
        ) {
            CalendarHeader(
                month = displayedMonth,
                onMonthClick = { showMonthPicker = true },
                onPreviousMonth = { changeMonth(-1) },
                onNextMonth = { changeMonth(1) },
            )
            HorizontalPager(
                state = calendarPagerState,
                beyondViewportPageCount = if (calendarContentReady) {
                    CALENDAR_BEYOND_VIEWPORT_PAGE_COUNT
                } else {
                    0
                },
                modifier = Modifier.fillMaxWidth(),
            ) { page ->
                CalendarMonthContent(
                    month = calendarMonthForPage(page, baseMonth),
                    today = today,
                    selectedDate = selectedDate,
                    badgeColors = badgeColors,
                    collapseProgress = collapseProgress,
                    onDateClick = {
                        selectedDate = it
                        onDateSelected(it)
                    },
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = 0.42f)),
            )
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
}

@Composable
private fun rememberCalendarContentReady(): Boolean {
    var contentReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withFrameNanos { }
        contentReady = true
    }

    return contentReady
}

@Composable
private fun CalendarHeader(
    month: YearMonth,
    onMonthClick: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(imageVector = MiuixIcons.Back, contentDescription = "上个月")
            }
            Text(
                text = "${month.year}年${month.monthValue}月",
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onMonthClick),
                color = MiuixTheme.colorScheme.onBackground,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            IconButton(onClick = onNextMonth) {
                Icon(imageVector = MiuixIcons.Forward, contentDescription = "下个月")
            }
        }

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
    }
}

@Composable
private fun CalendarMonthContent(
    month: YearMonth,
    today: LocalDate,
    selectedDate: LocalDate,
    badgeColors: Map<LocalDate, Color>,
    collapseProgress: Float,
    onDateClick: (LocalDate) -> Unit,
) {
    val firstDay = month.atDay(1)
    val gridStart = firstDay.minusDays((firstDay.dayOfWeek.value - 1).toLong())
    val collapsedDate = if (YearMonth.from(today) == month) today else selectedDateInMonth(month, selectedDate)
    val collapsedWeekIndex = calendarWeekIndex(month, collapsedDate)

    Column {
        Spacer(modifier = Modifier.height(12.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val rowHeight = (maxWidth - 24.dp) / 7f
            val weekCount = calendarWeekCount(month)
            val density = LocalDensity.current
            val rowHeightPx = with(density) { rowHeight.toPx() }
            val baseRowSpacingPx = with(density) { calendarRowSpacing.toPx() }
            val rowSpacingPx = calendarRowSpacingPx(
                rowHeightPx = rowHeightPx,
                baseRowSpacingPx = baseRowSpacingPx,
                weekCount = weekCount,
            )
            val expandedHeightPx = calendarGridHeightPx(
                rowHeightPx = rowHeightPx,
                rowSpacingPx = rowSpacingPx,
                weekCount = weekCount,
            )
            val clipWindow = calendarClipWindow(
                rowHeightPx = rowHeightPx,
                rowSpacingPx = baseRowSpacingPx,
                weekCount = weekCount,
                currentWeekIndex = collapsedWeekIndex,
                collapseProgress = collapseProgress,
            )

            CalendarGridViewport(
                viewportHeightPx = clipWindow.heightPx.roundToInt(),
                contentHeightPx = expandedHeightPx.roundToInt(),
                contentTopPx = clipWindow.topPx.roundToInt(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(with(density) { rowSpacingPx.toDp() }),
                ) {
                    repeat(weekCount) { weekIndex ->
                        CalendarWeekRow(
                            month = month,
                            gridStart = gridStart,
                            weekIndex = weekIndex,
                            today = today,
                            selectedDate = selectedDate,
                            badgeColors = badgeColors,
                            onDateClick = onDateClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarGridViewport(
    viewportHeightPx: Int,
    contentHeightPx: Int,
    contentTopPx: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier.clipToBounds(),
        content = content,
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val contentPlaceable = measurables.single().measure(
            Constraints.fixed(width = width, height = contentHeightPx),
        )
        val height = viewportHeightPx.coerceIn(constraints.minHeight, constraints.maxHeight)

        layout(width, height) {
            contentPlaceable.placeRelative(0, -contentTopPx)
        }
    }
}

@Composable
private fun CalendarWeekRow(
    month: YearMonth,
    gridStart: LocalDate,
    weekIndex: Int,
    today: LocalDate,
    selectedDate: LocalDate,
    badgeColors: Map<LocalDate, Color>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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
                badgeColor = badgeColors[date],
                onClick = { onDateClick(date) },
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f),
            )
        }
    }
}

@Composable
private fun CheckInDayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    isWeekend: Boolean,
    badgeColor: Color?,
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

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MiuixTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
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
        badgeColor?.let { color ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 2.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MiuixIcons.Ok,
                    contentDescription = "已打卡",
                    modifier = Modifier.size(10.dp),
                    tint = Color.White,
                )
            }
        }
    }
}

internal fun moveToMonth(
    displayedMonth: YearMonth,
    selectedDate: LocalDate,
    monthDelta: Long,
): Pair<YearMonth, LocalDate> {
    val newMonth = displayedMonth.plusMonths(monthDelta)
    return newMonth to selectedDateInMonth(newMonth, selectedDate)
}

internal fun selectedDateInMonth(month: YearMonth, selectedDate: LocalDate): LocalDate {
    return month.atDay(selectedDate.dayOfMonth.coerceAtMost(month.lengthOfMonth()))
}

private fun calendarMonthForPage(page: Int, baseMonth: YearMonth): YearMonth {
    return baseMonth.plusMonths((page - CALENDAR_INITIAL_PAGE).toLong())
}

private fun calendarPageForMonth(month: YearMonth, baseMonth: YearMonth): Int {
    val monthOffset = ChronoUnit.MONTHS.between(baseMonth, month).toInt()
    return (CALENDAR_INITIAL_PAGE + monthOffset).coerceIn(0, CALENDAR_PAGE_COUNT - 1)
}

internal fun calendarWeekCount(month: YearMonth): Int {
    val leadingDays = month.atDay(1).dayOfWeek.value - 1
    return (leadingDays + month.lengthOfMonth() + 6) / 7
}

internal fun calendarWeekIndex(month: YearMonth, date: LocalDate): Int {
    val leadingDays = month.atDay(1).dayOfWeek.value - 1
    val dayOfMonth = if (YearMonth.from(date) == month) date.dayOfMonth else 1
    return (leadingDays + dayOfMonth - 1) / 7
}

internal fun calendarGridHeightPx(
    rowHeightPx: Float,
    rowSpacingPx: Float,
    weekCount: Int,
): Float {
    val safeWeekCount = weekCount.coerceAtLeast(1)
    return rowHeightPx * safeWeekCount + rowSpacingPx * (safeWeekCount - 1)
}

internal fun calendarRowSpacingPx(
    rowHeightPx: Float,
    baseRowSpacingPx: Float,
    weekCount: Int,
): Float {
    val safeWeekCount = weekCount.coerceAtLeast(1)
    if (safeWeekCount >= CALENDAR_DEFAULT_WEEK_COUNT || safeWeekCount == 1) {
        return baseRowSpacingPx
    }

    val reservedHeightPx = calendarGridHeightPx(
        rowHeightPx = rowHeightPx,
        rowSpacingPx = baseRowSpacingPx,
        weekCount = CALENDAR_DEFAULT_WEEK_COUNT,
    )
    return ((reservedHeightPx - rowHeightPx * safeWeekCount) / (safeWeekCount - 1))
        .coerceAtLeast(baseRowSpacingPx)
}

internal data class CalendarClipWindow(
    val topPx: Float,
    val heightPx: Float,
)

internal fun calendarClipWindow(
    rowHeightPx: Float,
    rowSpacingPx: Float,
    weekCount: Int,
    currentWeekIndex: Int,
    collapseProgress: Float,
): CalendarClipWindow {
    val safeWeekCount = weekCount.coerceAtLeast(1)
    val safeCurrentWeekIndex = currentWeekIndex.coerceIn(0, safeWeekCount - 1)
    val progress = collapseProgress.coerceIn(0f, 1f)
    val effectiveRowSpacingPx = calendarRowSpacingPx(
        rowHeightPx = rowHeightPx,
        baseRowSpacingPx = rowSpacingPx,
        weekCount = safeWeekCount,
    )
    val expandedHeightPx = calendarGridHeightPx(
        rowHeightPx = rowHeightPx,
        rowSpacingPx = effectiveRowSpacingPx,
        weekCount = safeWeekCount,
    )
    val currentRowTopPx = (rowHeightPx + effectiveRowSpacingPx) * safeCurrentWeekIndex

    return CalendarClipWindow(
        topPx = currentRowTopPx * progress,
        heightPx = expandedHeightPx + (rowHeightPx - expandedHeightPx) * progress,
    )
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
