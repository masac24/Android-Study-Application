package com.example.diplomski.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.diplomski.database.tables.Session
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM session WHERE userId = :userId ORDER BY id DESC")
    fun getAllSessions(userId: Int): Flow<List<Session>>

    @Query("SELECT * FROM session WHERE id = :id")
    fun getSession(id: Int): Flow<Session>

    @Query("SELECT AVG(time) FROM session WHERE id = :id GROUP BY datetime")
    suspend fun avgPerDay(id: Int): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(session: Session) : Long

    @Query("DELETE FROM session WHERE id = :id")
    suspend fun deleteSession(id: Int)

    @Query("DELETE FROM session WHERE userId = :id")
    suspend fun deleteByUserId(id: Int)

    @Query("DELETE FROM session")
    suspend fun deleteAll()

    @Query("SELECT * FROM session WHERE syncId IS NULL")
    suspend fun getUnsynced(): List<Session>

    @Query("UPDATE session SET syncId = :syncId WHERE id = :id")
    suspend fun updateSyncId(id: Int, syncId: Long)

    @Query("SELECT * FROM session WHERE syncId = :syncId LIMIT 1")
    suspend fun getBySyncId(syncId: Long): Session?

    @Query("SELECT syncId FROM session WHERE syncId IS NOT NULL AND userId = :userId")
    suspend fun getAllSyncIds(userId: Int): List<Long>

}