package com.quenan.duji.data.item

import kotlinx.coroutines.flow.Flow

class ItemRepository(
    private val itemDao: ItemDao,
) {
    fun observeItems(): Flow<List<ItemEntity>> = itemDao.observeAll()

    suspend fun addItem(item: ItemEntity) {
        itemDao.insert(item)
    }

    suspend fun deleteItem(item: ItemEntity) {
        itemDao.delete(item)
    }
}
