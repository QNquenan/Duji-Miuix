package com.quenan.duji.data.day

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DayReminderTest {
    @Test
    fun birthdayReminderUsesTheNextBirthday() {
        val day = day(type = DayType.BIRTHDAY, targetDate = "2000-07-23")

        assertEquals(3, day.reminderDaysUntil(LocalDate.of(2026, 7, 20)))
        assertEquals("Alex \u8fd8\u6709 3 \u5929\u751f\u65e5\uff01", day.reminderNotificationText(3))
    }

    @Test
    fun anniversaryReminderRepeatsEveryYear() {
        val day = day(type = DayType.ANNIVERSARY, targetDate = "2016-07-23")

        assertEquals(3, day.reminderDaysUntil(LocalDate.of(2026, 7, 20)))
        assertEquals("\u8ddd\u79bb Alex \u8fd8\u6709 3 \u5929\uff0c\u7eaa\u5ff5\u65e5\u5feb\u5230\u4e86\uff01", day.reminderNotificationText(3))
    }

    @Test
    fun countdownReminderOnlyRunsForFutureDates() {
        val futureDay = day(type = DayType.DAYS, targetDate = "2026-07-23")
        val pastDay = day(type = DayType.DAYS, targetDate = "2026-07-19")

        assertEquals(3, futureDay.reminderDaysUntil(LocalDate.of(2026, 7, 20)))
        assertNull(pastDay.reminderDaysUntil(LocalDate.of(2026, 7, 20)))
        assertEquals("\u8ddd\u79bb Alex \u8fd8\u6709 3 \u5929\uff0c\u4e00\u8d77\u671f\u5f85\u5427\uff01", futureDay.reminderNotificationText(3))
    }

    @Test
    fun recurringWeeklyReminderFindsTheNextConfiguredWeekday() {
        val day = day(
            type = DayType.DAYS,
            targetDate = "2026-07-20",
            repeatCycle = RepeatCycle.WEEKLY,
            weekDays = listOf(3),
        )

        assertEquals(3, day.reminderDaysUntil(LocalDate.of(2026, 7, 20)))
    }

    private fun day(
        type: DayType,
        targetDate: String,
        repeatCycle: RepeatCycle = RepeatCycle.NONE,
        weekDays: List<Int> = emptyList(),
    ) = DayData(
        id = 1L,
        emoji = "\u23f0",
        emojiName = "alarm",
        name = "Alex",
        type = type,
        repeatCycle = repeatCycle,
        targetDate = targetDate,
        note = "",
        weekDays = weekDays,
        monthDays = emptyList(),
        isLunar = false,
        isPinned = false,
        createdAt = 0L,
    )
}
