package com.quenan.duji.data.item

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ItemEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class DuJiDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}
