package com.quenan.duji.data.item

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val icon: String,
    val name: String,
    val date: String,
    val price: Int,
    val note: String,
    val isPinned: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
)
