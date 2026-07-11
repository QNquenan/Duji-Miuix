package com.quenan.duji.ui.util

import com.nlf.calendar.Lunar
import com.nlf.calendar.LunarMonth
import com.nlf.calendar.LunarYear
import java.time.LocalDate
import java.util.Date

private val monthNames = listOf("正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊")
private val dayNames = listOf(
    "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
    "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
    "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十",
)

data class LunarDate(
    val year: Int,
    val month: Int,
    val day: Int,
) {
    val isLeapMonth: Boolean get() = month < 0
    val absoluteMonth: Int get() = kotlin.math.abs(month)
    val monthName: String get() = buildString {
        if (isLeapMonth) append("闰")
        append(monthNames.getOrElse(absoluteMonth - 1) { absoluteMonth.toString() })
        append("月")
    }
    val dayName: String get() = dayNames.getOrElse(day - 1) { day.toString() }
    val formatted: String get() = "$monthName$dayName"
}

fun lunarMonthDayCount(year: Int, month: Int): Int {
    return LunarMonth.fromYm(year, month)?.dayCount ?: 0
}

fun lunarToSolar(year: Int, month: Int, day: Int): LocalDate? {
    return runCatching {
        val lunar = Lunar.fromYmd(year, month, day)
        LocalDate.of(lunar.solar.year, lunar.solar.month, lunar.solar.day)
    }.getOrNull()
}

fun solarToLunar(year: Int, month: Int, day: Int): LunarDate? {
    return runCatching {
        val lunar = Lunar.fromDate(Date.from(LocalDate.of(year, month, day).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()))
        LunarDate(lunar.year, lunar.month, lunar.day)
    }.getOrNull()
}

fun lunarMonthList(year: Int): List<Pair<Int, String>> {
    return LunarYear.fromYear(year).months
        .filter { it.year == year }
        .map { month ->
            val lunar = Lunar.fromYmd(year, month.month, 1)
            month.month to "${lunar.monthInChinese}月"
        }
}
