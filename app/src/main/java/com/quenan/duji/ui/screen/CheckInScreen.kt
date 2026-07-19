package com.quenan.duji.ui.screen

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Forward
import top.yukonga.miuix.kmp.theme.MiuixTheme

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

    fun changeMonth(monthDelta: Long) {
        val (newMonth, newSelectedDate) = moveToMonth(displayedMonth, selectedDate, monthDelta)
        displayedMonth = newMonth
        selectedDate = newSelectedDate
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
                Text(
                    text = "${displayedMonth.year}年${displayedMonth.monthValue}月",
                    modifier = Modifier.padding(top = 12.dp, bottom = 18.dp),
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

                val firstDay = displayedMonth.atDay(1)
                val gridStart = firstDay.minusDays((firstDay.dayOfWeek.value - 1).toLong())
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    repeat(6) { weekIndex ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            repeat(7) { dayIndex ->
                                val date = gridStart.plusDays((weekIndex * 7 + dayIndex).toLong())
                                CheckInDayCell(
                                    date = date,
                                    isCurrentMonth = YearMonth.from(date) == displayedMonth,
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
            text = calendarSubLabel(date, lunar),
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

private fun calendarSubLabel(date: LocalDate, lunar: com.quenan.duji.ui.util.LunarDate?): String {
    val solarFestival = mapOf(
        "01-01" to "元旦",
        "02-14" to "情人节",
        "03-08" to "妇女节",
        "05-01" to "劳动节",
        "06-01" to "儿童节",
        "07-01" to "建党节",
        "08-01" to "建军节",
        "10-01" to "国庆节",
        "12-25" to "圣诞节",
    )["%02d-%02d".format(date.monthValue, date.dayOfMonth)]
    if (solarFestival != null) return solarFestival

    val lunarFestival = when {
        lunar == null -> null
        lunar.absoluteMonth == 1 && lunar.day == 1 -> "春节"
        lunar.absoluteMonth == 1 && lunar.day == 15 -> "元宵节"
        lunar.absoluteMonth == 5 && lunar.day == 5 -> "端午节"
        lunar.absoluteMonth == 7 && lunar.day == 7 -> "七夕节"
        lunar.absoluteMonth == 8 && lunar.day == 15 -> "中秋节"
        lunar.absoluteMonth == 9 && lunar.day == 9 -> "重阳节"
        else -> null
    }
    return lunarFestival ?: lunar?.dayName.orEmpty()
}
