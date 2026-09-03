package com.example.diplomski.repositories

import android.util.Log
import com.example.diplomski.database.daos.QuestionDao
import com.example.diplomski.database.daos.QuizDao
import com.example.diplomski.database.daos.ScoreDao
import com.example.diplomski.database.tables.Quiz
import com.example.diplomski.retrofit.RetrofitClient
import com.example.diplomski.retrofit.TokenManager
import com.example.diplomski.retrofit.dto.QuizDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull


class QuizRepository(private val quizDao: QuizDao, private val questionDao: QuestionDao,
    private val scoreDao: ScoreDao) {

    suspend fun insert(quiz: Quiz) : Long {
        val localId = quizDao.insert(quiz)

        try {
            val user = RetrofitClient.api.searchUser(quiz.user)
            val response = RetrofitClient.api.createQuiz(
                QuizDto(name = quiz.name, userId = user.id)
            )
            quizDao.updateSyncId(localId.toInt(), response.id)
        } catch (e: Exception) {
            Log.e("SYNC", "Quiz sync failed, will retry later: ${e.message}")
        }

        return localId
    }

    suspend fun update(quiz: Quiz) {
        quizDao.update(quiz)
        try {
            quiz.syncId?.let { syncId ->
                RetrofitClient.api.updateQuiz(
                    syncId,
                    QuizDto(
                        id = syncId,
                        name = quiz.name,
                        userId = TokenManager.getUserId()
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("SYNC", "Quiz update sync failed: ${e.message}")
        }
    }

    fun getUserQuizzes(userId: String) : Flow<List<Quiz>>{
        return quizDao.getAllQuizzesForUser(userId)
    }

    suspend fun getBySyncId(id: Long): Quiz? {
        return quizDao.getBySyncId(id)
    }

    suspend fun deleteQuiz(id: Int) {
        val quiz = quizDao.getQuiz(id).firstOrNull()
        questionDao.deleteByQuizId(quiz?.id?.toLong() ?: return)
        scoreDao.deleteByQuizId(quiz.id)
        quizDao.deleteQuiz(id)
        try {
            quiz.syncId?.let { RetrofitClient.api.deleteQuiz(it) }

        } catch (e: Exception) {
            Log.e("SYNC", "Quiz delete sync failed: ${e.message}")
        }
    }

    fun getQuiz(id: Int): Flow<Quiz> {
        return quizDao.getQuiz(id)
    }
}