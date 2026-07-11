package com.quenan.duji.data.day

import com.quenan.duji.ui.util.LunarDate
import com.quenan.duji.ui.util.lunarToSolar
import com.quenan.duji.ui.util.solarToLunar
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class DayType {
    DAYS,
    ANNIVERSARY,
    BIRTHDAY,
}

enum class RepeatCycle {
    NONE,
    WEEKLY,
    MONTHLY,
    YEARLY,
}

data class DayData(
    val id: Long,
    val emoji: String,
    val emojiName: String,
    val name: String,
    val type: DayType,
    val repeatCycle: RepeatCycle,
    val targetDate: String,
    val note: String,
    val weekDays: List<Int>,
    val monthDays: List<Int>,
    val isLunar: Boolean,
    val isPinned: Boolean,
    val createdAt: Long,
)

data class DayStatus(
    val diff: Int,
    val dateText: String,
    val statusText: String,
)

fun DayData.typeLabel(): String = when (type) {
    DayType.DAYS -> "倒/正数日"
    DayType.ANNIVERSARY -> "纪念日"
    DayType.BIRTHDAY -> "生日"
}

fun DayData.repeatLabel(): String = when (repeatCycle) {
    RepeatCycle.NONE -> "不重复"
    RepeatCycle.WEEKLY -> "每周"
    RepeatCycle.MONTHLY -> "每月"
    RepeatCycle.YEARLY -> "每年"
}

fun DayData.targetDateFormatted(): String {
    val date = parseDayDate(targetDate) ?: return targetDate
    return when {
        type == DayType.BIRTHDAY && isLunar -> {
            val lunar = solarToLunar(date.year, date.monthValue, date.dayOfMonth)
            if (lunar != null) "每年 农历${lunar.formatted}" else targetDate
        }
        type == DayType.BIRTHDAY -> "每年 ${date.monthValue}月${date.dayOfMonth}日"
        isLunar -> {
            val lunar = solarToLunar(date.year, date.monthValue, date.dayOfMonth)
            if (lunar != null) "农历 ${lunar.year}年${lunar.formatted}" else targetDate
        }
        repeatCycle == RepeatCycle.WEEKLY && weekDays.isNotEmpty() -> {
            val weekLabels = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
            weekDays.sorted().joinToString(" ") { weekLabels[it] }
        }
        repeatCycle == RepeatCycle.MONTHLY && monthDays.isNotEmpty() -> {
            "每月 ${monthDays.sorted().joinToString(" ") { "$it 日" }}"
        }
        else -> targetDate
    }
}

fun DayData.computeStatus(today: LocalDate = LocalDate.now()): DayStatus {
    val date = parseDayDate(targetDate) ?: return DayStatus(0, targetDate, "日期无效")

    if (type == DayType.BIRTHDAY) {
        if (isLunar) {
            val lunar = solarToLunar(date.year, date.monthValue, date.dayOfMonth)
            if (lunar != null) {
                val thisYear = safeLunarToSolar(today.year, lunar.month, lunar.day)
                if (thisYear != null && !thisYear.isBefore(today)) {
                    val diff = ChronoUnit.DAYS.between(today, thisYear).toInt()
                    return if (diff == 0) {
                        DayStatus(0, targetDateFormatted(), "就是今天！")
                    } else {
                        DayStatus(diff, targetDateFormatted(), "还有 $diff 天")
                    }
                }
                val nextYear = safeLunarToSolar(today.year + 1, lunar.month, lunar.day)
                if (nextYear != null) {
                    val diff = ChronoUnit.DAYS.between(today, nextYear).toInt()
                    return DayStatus(diff, targetDateFormatted(), "还有 $diff 天")
                }
            }
        }
        val thisYearBirthday = LocalDate.of(today.year, date.monthValue, date.dayOfMonth)
        if (!thisYearBirthday.isBefore(today)) {
            val diff = ChronoUnit.DAYS.between(today, thisYearBirthday).toInt()
            return if (diff == 0) DayStatus(0, targetDateFormatted(), "就是今天！") else DayStatus(diff, targetDateFormatted(), "还有 $diff 天")
        }
        val nextBirthday = LocalDate.of(today.year + 1, date.monthValue, date.dayOfMonth)
        val diff = ChronoUnit.DAYS.between(today, nextBirthday).toInt()
        return DayStatus(diff, targetDateFormatted(), "还有 $diff 天")
    }

    if (type == DayType.ANNIVERSARY) {
        if (date == today) return DayStatus(0, targetDateFormatted(), "就是今天！")
        if (date.isAfter(today)) {
            val daysUntil = ChronoUnit.DAYS.between(today, date).toInt()
            val yearsUntil = date.year - today.year
            val adjustedYears = if (
                date.monthValue > today.monthValue ||
                (date.monthValue == today.monthValue && date.dayOfMonth > today.dayOfMonth)
            ) yearsUntil else yearsUntil - 1
            return if (adjustedYears >= 1) {
                DayStatus(daysUntil, targetDateFormatted(), "还有 $adjustedYears 周年")
            } else {
                DayStatus(daysUntil, targetDateFormatted(), "还有 $daysUntil 天")
            }
        }
        val years = today.year - date.year
        val adjusted = if (
            today.monthValue < date.monthValue ||
            (today.monthValue == date.monthValue && today.dayOfMonth < date.dayOfMonth)
        ) years - 1 else years
        return DayStatus(adjusted, targetDateFormatted(), "已经 $adjusted 周年")
    }

    if (repeatCycle == RepeatCycle.WEEKLY && weekDays.isNotEmpty()) {
        val todayWeek = (today.dayOfWeek.value + 6) % 7
        for (offset in 0..6) {
            val week = (todayWeek + offset) % 7
            if (weekDays.contains(week)) {
                val nextDate = today.plusDays(offset.toLong())
                return if (offset == 0) {
                    DayStatus(0, nextDate.toString(), "就是今天！")
                } else {
                    DayStatus(offset, nextDate.toString(), "还有 $offset 天")
                }
            }
        }
    }

    if (repeatCycle == RepeatCycle.MONTHLY && monthDays.isNotEmpty()) {
        val sorted = monthDays.sorted()
        val todayDay = today.dayOfMonth
        for (day in sorted) {
            if (day == todayDay) return DayStatus(0, today.toString(), "就是今天！")
            if (day > todayDay) {
                val nextDate = LocalDate.of(today.year, today.monthValue, day.coerceAtMost(today.lengthOfMonth()))
                val diff = ChronoUnit.DAYS.between(today, nextDate).toInt()
                return DayStatus(diff, nextDate.toString(), "还有 $diff 天")
            }
        }
        val nextMonth = today.plusMonths(1)
        val first = sorted.first().coerceAtMost(nextMonth.lengthOfMonth())
        val nextDate = LocalDate.of(nextMonth.year, nextMonth.monthValue, first)
        val diff = ChronoUnit.DAYS.between(today, nextDate).toInt()
        return DayStatus(diff, nextDate.toString(), "还有 $diff 天")
    }

    if (isLunar && repeatCycle == RepeatCycle.YEARLY) {
        val lunar = solarToLunar(date.year, date.monthValue, date.dayOfMonth)
        if (lunar != null) {
            val thisYear = safeLunarToSolar(today.year, lunar.month, lunar.day)
            if (thisYear != null && !thisYear.isBefore(today)) {
                val diff = ChronoUnit.DAYS.between(today, thisYear).toInt()
                return if (diff == 0) {
                    DayStatus(0, "农历 ${lunar.monthName}${lunar.dayName} · 今天", "就是今天！")
                } else {
                    DayStatus(diff, "农历 ${lunar.monthName}${lunar.dayName} · $thisYear", "还有 $diff 天")
                }
            }
            val nextYear = safeLunarToSolar(today.year + 1, lunar.month, lunar.day)
            if (nextYear != null) {
                val diff = ChronoUnit.DAYS.between(today, nextYear).toInt()
                return DayStatus(diff, "农历 ${lunar.monthName}${lunar.dayName} · $nextYear", "还有 $diff 天")
            }
        }
    }

    val diff = ChronoUnit.DAYS.between(today, date).toInt()
    return when {
        diff > 0 -> DayStatus(diff, targetDateFormatted(), "还有 $diff 天")
        diff == 0 -> DayStatus(0, targetDateFormatted(), "就是今天！")
        else -> DayStatus(diff, targetDateFormatted(), "已经 ${-diff} 天")
    }
}

fun parseDayDate(dateString: String): LocalDate? {
    return runCatching { LocalDate.parse(dateString) }.getOrNull()
}

private fun safeLunarToSolar(year: Int, month: Int, day: Int): LocalDate? {
    lunarToSolar(year, month, day)?.let { return it }
    for (candidateDay in day - 1 downTo 1) {
        lunarToSolar(year, month, candidateDay)?.let { return it }
    }
    return null
}
