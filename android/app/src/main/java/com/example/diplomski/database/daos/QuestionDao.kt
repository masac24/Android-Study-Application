package com.example.diplomski.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.diplomski.database.tables.Question
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {

    @Query("SELECT * FROM question WHERE quizId = :quizId ORDER BY id DESC")
    fun getAllQuestionsForQuiz(quizId: Int): Flow<List<Question>>

    @Query("SELECT COUNT(*) FROM question WHERE quizId = :quizId")
    suspend fun getNumberOfQuestions(quizId: Int): Int

    @Query("SELECT * FROM question WHERE text = :text AND quizId = :quizId LIMIT 1")
    suspend fun getByTextAndQuiz(text: String, quizId: Int): Question?

    @Query("SELECT syncId FROM question WHERE syncId IS NOT NULL")
    suspend fun getAllSyncIds(): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(question: Question): Long

    @Query("DELETE FROM question WHERE id = :id")
    suspend fun deleteQuestion(id: Int)

    @Query("DELETE FROM question")
    suspend fun deleteAll()

    @Query("DELETE FROM question WHERE id = :quizId")
    suspend fun deleteByQuizId(quizId: Long)

    @Query("SELECT * FROM question WHERE syncId IS NULL")
    suspend fun getUnsynced(): List<Question>

    @Query("UPDATE question SET syncId = :syncId WHERE id = :id")
    suspend fun updateSyncId(id: Int, syncId: Long)

    @Query("SELECT * FROM question WHERE syncId = :syncId LIMIT 1")
    suspend fun getBySyncId(syncId: Long): Question?

    @Query("SELECT * FROM question WHERE id = :id")
    suspend fun getById(id: Int): Question?
}