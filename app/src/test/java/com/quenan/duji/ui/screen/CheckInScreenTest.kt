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

    @Test
    fun selectedDateInMonthClampsDateChosenFromMonthPicker() {
        val result = selectedDateInMonth(
            month = YearMonth.of(2026, 2),
            selectedDate = LocalDate.of(2026, 1, 31),
        )

        assertEquals(LocalDate.of(2026, 2, 28), result)
    }

    @Test
    fun calendarWeekCountIncludesSixthRowWhenNeeded() {
        assertEquals(4, calendarWeekCount(YearMonth.of(2027, 2)))
        assertEquals(6, calendarWeekCount(YearMonth.of(2026, 8)))
        assertEquals(5, calendarWeekCount(YearMonth.of(2026, 7)))
    }

    @Test
    fun calendarWeekIndexUsesCurrentDateRow() {
        assertEquals(
            3,
            calendarWeekIndex(
                month = YearMonth.of(2026, 7),
                date = LocalDate.of(2026, 7, 20),
            ),
        )
    }

    @Test
    fun calendarClipWindowShowsFullGridWhenExpanded() {
        val window = calendarClipWindow(
            rowHeightPx = 46f,
            rowSpacingPx = 8f,
            weekCount = 5,
            currentWeekIndex = 3,
            collapseProgress = 0f,
        )

        assertEquals(0f, window.topPx, 0.001f)
        assertEquals(262f, window.heightPx, 0.001f)
    }

    @Test
    fun calendarClipWindowPutsCurrentDateRowAtTopWhenCollapsed() {
        val window = calendarClipWindow(
            rowHeightPx = 46f,
            rowSpacingPx = 8f,
            weekCount = 5,
            currentWeekIndex = 3,
            collapseProgress = 1f,
        )

        assertEquals(162f, window.topPx, 0.001f)
        assertEquals(46f, window.heightPx, 0.001f)
    }

    @Test
    fun calendarClipWindowInterpolatesOnlyTheViewportDuringDrag() {
        val window = calendarClipWindow(
            rowHeightPx = 46f,
            rowSpacingPx = 8f,
            weekCount = 5,
            currentWeekIndex = 3,
            collapseProgress = 0.5f,
        )

        assertEquals(81f, window.topPx, 0.001f)
        assertEquals(154f, window.heightPx, 0.001f)
    }
}
