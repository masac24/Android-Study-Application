package com.example.diplomski.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.diplomski.database.tables.Quiz
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {

    @Query("SELECT * FROM quiz WHERE user = :userId ORDER BY id DESC")
    fun getAllQuizzesForUser(userId: String): Flow<List<Quiz>>

    @Query("SELECT * FROM quiz WHERE id = :id")
    fun getQuiz(id: Int): Flow<Quiz>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(quiz: Quiz) : Long

    @Update
    suspend fun update(quiz : Quiz)

    @Query("DELETE FROM quiz WHERE id = :quizId")
    suspend fun deleteQuiz(quizId: Int)

    @Query("DELETE FROM quiz")
    suspend fun deleteAll()

    @Query("SELECT * FROM quiz WHERE syncId IS NULL")
    suspend fun getUnsynced(): List<Quiz>

    @Query("UPDATE quiz SET syncId = :syncId WHERE id = :id")
    suspend fun updateSyncId(id: Int, syncId: Long)

    @Query("SELECT * FROM quiz WHERE syncId = :syncId LIMIT 1")
    suspend fun getBySyncId(syncId: Long): Quiz?


}