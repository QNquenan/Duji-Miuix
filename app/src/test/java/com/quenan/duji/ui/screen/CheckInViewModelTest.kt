package com.quenan.duji.ui.screen

import com.quenan.duji.data.checkin.CheckInItem
import com.quenan.duji.data.checkin.CheckInRecord
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckInViewModelTest {
    @Test
    fun buildCheckInCardModelsPrecomputesDatesAndCurrentMonthDays() {
        val item = CheckInItem(
            id = 1L,
            emoji = "??",
            name = "??",
            colorArgb = 0xFF5EBD7DL,
            createdAt = 0L,
        )
        val records = listOf(
            CheckInRecord(itemId = 1L, date = "2026-07-20"),
            CheckInRecord(itemId = 1L, date = "2026-07-15"),
            CheckInRecord(itemId = 1L, date = "2026-06-30"),
            CheckInRecord(itemId = 1L, date = "invalid"),
            CheckInRecord(itemId = 2L, date = "2026-07-20"),
        )

        val card = buildCheckInCardModels(
            items = listOf(item),
            allRecords = records,
            today = LocalDate.of(2026, 7, 20),
        ).single()

        assertEquals(4, card.records.size)
        assertEquals(
            setOf(
                LocalDate.of(2026, 7, 20),
                LocalDate.of(2026, 7, 15),
                LocalDate.of(2026, 6, 30),
            ),
            card.recordDates,
        )
        assertEquals(31, card.currentMonthDayCount)
        assertEquals(setOf(15, 20), card.currentMonthCompletedDays)
        assertTrue(card.checkedToday)
    }

    @Test
    fun buildCheckInCardModelsDoesNotMarkAnotherDateAsToday() {
        val item = CheckInItem(1L, "??", "??", 0xFF5EBD7DL, 0L)

        val card = buildCheckInCardModels(
            items = listOf(item),
            allRecords = listOf(CheckInRecord(itemId = 1L, date = "2026-07-19")),
            today = LocalDate.of(2026, 7, 20),
        ).single()

        assertFalse(card.checkedToday)
    }
}
