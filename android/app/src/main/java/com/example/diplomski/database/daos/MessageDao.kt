package com.example.diplomski.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.diplomski.database.tables.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM message WHERE receiver = :id ORDER BY id DESC")
    fun getMessages(id: Int): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(message: Message): Long

    @Query("DELETE FROM message WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM message")
    suspend fun deleteAll()

    @Query("DELETE FROM message WHERE sender = :name")
    suspend fun deleteByUser(name: String)
    @Query("SELECT * FROM message WHERE syncId IS NULL")
    suspend fun getUnsynced(): List<Message>

    @Query("UPDATE message SET syncId = :syncId WHERE id = :id")
    suspend fun updateSyncId(id: Int, syncId: Long)

    @Query("SELECT * FROM message WHERE syncId = :syncId LIMIT 1")
    suspend fun getBySyncId(syncId: Long): Message?

    @Query("SELECT * FROM message WHERE id = :id")
    suspend fun getById(id: Int): Message?
}