package com.example.stashly.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StashDao {

    @Query("SELECT * FROM stash_items WHERE isArchived = 0 ORDER BY updatedAt DESC")
    fun getInboxItems(): Flow<List<StashItem>>

    @Query("SELECT * FROM stash_items WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedItems(): Flow<List<StashItem>>

    @Query("SELECT * FROM stash_items ORDER BY updatedAt DESC")
    fun getAllItems(): Flow<List<StashItem>>

    @Query("SELECT * FROM stash_items WHERE id = :id")
    suspend fun getItemById(id: Long): StashItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: StashItem): Long

    @Update
    suspend fun updateItem(item: StashItem): Int

    @Delete
    suspend fun deleteItem(item: StashItem): Int

    @Query("UPDATE stash_items SET isArchived = :isArchived, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setArchivedStatus(id: Long, isArchived: Boolean, updatedAt: Long): Int
}
