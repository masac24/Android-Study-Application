package com.example.diplomski.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.diplomski.database.tables.Score
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {

    @Query("SELECT * FROM score WHERE userId = :userId AND quizId  = :quizId ORDER BY id DESC")
    fun getUserQuizScores(userId: Int, quizId: Int): Flow<List<Score>>

    @Query("SELECT AVG(score) FROM score WHERE userId = :userId AND quizId  = :quizId ORDER BY id DESC")
    suspend fun avgScore(userId: Int, quizId: Int): Int

    @Query("SELECT * FROM score WHERE userId = :userId  ORDER BY id DESC")
    fun getAllUserScores(userId: Int): Flow<List<Score>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(score: Score): Long

    @Query("DELETE FROM score WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM score WHERE quizId = :id")
    suspend fun deleteByQuizId(id: Int)

    @Query("DELETE FROM score WHERE userId = :id")
    suspend fun deleteByUserId(id: Int)

    @Query("SELECT * FROM score WHERE syncId IS NULL")
    suspend fun getUnsynced(): List<Score>

    @Query("UPDATE score SET syncId = :syncId WHERE id = :id")
    suspend fun updateSyncId(id: Int, syncId: Long)

    @Query("SELECT * FROM score WHERE syncId = :syncId LIMIT 1")
    suspend fun getBySyncId(syncId: Long): Score?

    @Query("SELECT * FROM score WHERE id = :id")
    suspend fun getById(id: Int): Score?

    @Query("SELECT syncId FROM score WHERE syncId IS NOT NULL AND userId = :userId")
    suspend fun getAllSyncIds(userId: Int): List<Long>
}