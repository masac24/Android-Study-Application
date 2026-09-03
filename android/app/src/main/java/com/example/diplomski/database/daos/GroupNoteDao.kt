package com.example.diplomski.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.diplomski.database.tables.GroupNote
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupNoteDao {
    @Query("SELECT * FROM `groupnote` WHERE groupId = :id ORDER BY id DESC")
    fun getGroupNotes(id: Int): Flow<List<GroupNote>>

    @Query("SELECT * FROM `groupnote` WHERE noteId = :id ORDER BY id DESC")
    fun getNote(id: Int): Flow<List<GroupNote>>

    @Query("SELECT * FROM `groupnote` WHERE noteId = :id AND  groupId == :g")
    suspend fun getSpecNote(id: Int, g: Int): GroupNote?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(groupNote: GroupNote): Long

    @Query("DELETE FROM `groupnote` WHERE noteId = :id AND groupId = :groupId")
    suspend fun delete(id: Int, groupId: Int)

    @Query("DELETE FROM `groupnote` WHERE groupId = :id")
    suspend fun deleteByGroupId(id: Int)

    @Query("SELECT * FROM `groupnote` WHERE syncId IS NULL")
    suspend fun getUnsynced(): List<GroupNote>

    @Query("UPDATE `groupnote` SET syncId = :syncId WHERE id = :id")
    suspend fun updateSyncId(id: Int, syncId: Long)

    @Query("SELECT * FROM `groupnote` WHERE syncId = :syncId LIMIT 1")
    suspend fun getBySyncId(syncId: Long): GroupNote?

    @Query("SELECT * FROM `groupnote` WHERE id = :id")
    suspend fun getById(id: Int): GroupNote?

    @Query("SELECT syncId FROM `groupnote` WHERE syncId IS NOT NULL AND groupId = :groupId")
    suspend fun getAllSyncIdsForGroup(groupId: Int): List<Long>
}