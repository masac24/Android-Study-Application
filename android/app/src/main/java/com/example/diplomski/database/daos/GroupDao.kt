package com.example.diplomski.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.diplomski.database.tables.Group
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {

    @Query("SELECT * FROM `groups` WHERE user = :id ORDER BY id DESC")
    fun getUserGroups(id: String): Flow<List<Group>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(group: Group): Long

    @Query("DELETE FROM `groups` WHERE id = :id")
    suspend fun delete(id: Int)

    @Update
    suspend fun update(group: Group)

    @Query("DELETE FROM `groups` WHERE user = :id")
    suspend fun deleteByUsername(id: String)

    @Query("SELECT * FROM `groups` WHERE syncId IS NULL")
    suspend fun getUnsynced(): List<Group>

    @Query("UPDATE `groups` SET syncId = :syncId WHERE id = :id")
    suspend fun updateSyncId(id: Int, syncId: Long)

    @Query("SELECT * FROM `groups` WHERE syncId = :syncId LIMIT 1")
    suspend fun getBySyncId(syncId: Long): Group?

    @Query("SELECT * FROM `groups` WHERE id = :id")
    suspend fun getById(id: Int): Group?

    @Query("SELECT syncId FROM `groups` WHERE syncId IS NOT NULL AND user = :userId")
    suspend fun getAllSyncIds(userId: String): List<Long>
}