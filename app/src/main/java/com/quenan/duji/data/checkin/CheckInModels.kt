package com.quenan.duji.data.checkin

data class CheckInItem(
    val id: Long,
    val emoji: String,
    val name: String,
    val colorArgb: Long,
    val createdAt: Long,
)

data class CheckInRecord(
    val itemId: Long,
    val date: String,
)
