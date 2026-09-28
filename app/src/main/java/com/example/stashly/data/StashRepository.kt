package com.example.stashly.data

import kotlinx.coroutines.flow.Flow

class StashRepository(private val dao: StashDao) {

    fun getInboxItems(): Flow<List<StashItem>> = dao.getInboxItems()

    fun getArchivedItems(): Flow<List<StashItem>> = dao.getArchivedItems()

    fun getAllItems(): Flow<List<StashItem>> = dao.getAllItems()

    suspend fun getItemById(id: Long): StashItem? = dao.getItemById(id)

    suspend fun insertItem(item: StashItem): Long = dao.insertItem(item)

    suspend fun updateItem(item: StashItem) = dao.updateItem(item)

    suspend fun deleteItem(item: StashItem) = dao.deleteItem(item)

    suspend fun setArchivedStatus(id: Long, isArchived: Boolean, updatedAt: Long = System.currentTimeMillis()) {
        dao.setArchivedStatus(id, isArchived, updatedAt)
    }
}
