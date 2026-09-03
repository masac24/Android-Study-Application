package com.example.diplomski.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.diplomski.database.tables.Checklist
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistDao {

    @Query("SELECT * FROM checklist WHERE userId = :id ORDER BY id DESC")
    fun getItems(id: Int): Flow<List<Checklist>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(checklist: Checklist): Long

    @Query("DELETE FROM checklist WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM checklist WHERE userId = :id")
    suspend fun deleteByUserId(id: Int)

    @Query("SELECT * FROM checklist WHERE syncId IS NULL")
    suspend fun getUnsynced(): List<Checklist>

    @Query("UPDATE checklist SET syncId = :syncId WHERE id = :id")
    suspend fun updateSyncId(id: Int, syncId: Long)

    @Query("SELECT * FROM checklist WHERE syncId = :syncId LIMIT 1")
    suspend fun getBySyncId(syncId: Long): Checklist?

    @Query("SELECT * FROM checklist WHERE id = :id")
    suspend fun getById(id: Int): Checklist?

    @Query("SELECT syncId FROM checklist WHERE syncId IS NOT NULL AND userId = :userId")
    suspend fun getAllSyncIds(userId: Int): List<Long>
}