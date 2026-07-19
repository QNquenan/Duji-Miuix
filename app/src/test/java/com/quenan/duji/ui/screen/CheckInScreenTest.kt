package com.quenan.duji.ui.screen

import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

class CheckInScreenTest {
    @Test
    fun moveToNextMonthKeepsSelectedDay() {
        val result = moveToMonth(
            displayedMonth = YearMonth.of(2026, 1),
            selectedDate = LocalDate.of(2026, 1, 15),
            monthDelta = 1,
        )

        assertEquals(YearMonth.of(2026, 2), result.first)
        assertEquals(LocalDate.of(2026, 2, 15), result.second)
    }

    @Test
    fun moveToShorterMonthClampsToMonthEnd() {
        val aprilResult = moveToMonth(
            displayedMonth = YearMonth.of(2026, 3),
            selectedDate = LocalDate.of(2026, 3, 31),
            monthDelta = 1,
        )
        val februaryResult = moveToMonth(
            displayedMonth = YearMonth.of(2026, 1),
            selectedDate = LocalDate.of(2026, 1, 31),
            monthDelta = 1,
        )

        assertEquals(LocalDate.of(2026, 4, 30), aprilResult.second)
        assertEquals(LocalDate.of(2026, 2, 28), februaryResult.second)
    }

    @Test
    fun moveAcrossYearKeepsSelectedDay() {
        val result = moveToMonth(
            displayedMonth = YearMonth.of(2026, 12),
            selectedDate = LocalDate.of(2026, 12, 20),
            monthDelta = 1,
        )

        assertEquals(YearMonth.of(2027, 1), result.first)
        assertEquals(LocalDate.of(2027, 1, 20), result.second)
    }

    @Test
    fun moveToPreviousMonthUsesNegativeDelta() {
        val result = moveToMonth(
            displayedMonth = YearMonth.of(2026, 3),
            selectedDate = LocalDate.of(2026, 3, 10),
            monthDelta = -1,
        )

        assertEquals(YearMonth.of(2026, 2), result.first)
        assertEquals(LocalDate.of(2026, 2, 10), result.second)
    }
}
