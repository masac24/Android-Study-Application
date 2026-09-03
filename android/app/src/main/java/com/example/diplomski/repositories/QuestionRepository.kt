package com.example.diplomski.repositories

import android.util.Log
import com.example.diplomski.database.daos.QuestionDao
import com.example.diplomski.database.daos.QuizDao
import com.example.diplomski.database.tables.Question
import com.example.diplomski.retrofit.RetrofitClient
import com.example.diplomski.retrofit.dto.QuestionDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class QuestionRepository (private val questionDao: QuestionDao, private val quizDao: QuizDao) {

    suspend fun insert(question: Question) {
        val id = questionDao.insert(question).toInt()
        try {
            val quiz = quizDao.getQuiz(question.quizId).firstOrNull()
            quiz?.syncId?.let { quizSyncId ->
                val response = RetrofitClient.api.createQuestion(
                    QuestionDto(
                        text = question.text,
                        answer = question.answer,
                        type = question.type,
                        option1 = question.option1,
                        option2 = question.option2,
                        option3 = question.option3,
                        quizId = quizSyncId
                    )
                )
                questionDao.updateSyncId(id, response.id)
            }
        } catch (e: Exception) {
            Log.e("SYNC", "Question sync failed: ${e.message}")
        }
    }

    fun getQuizQuestions(id: Int): Flow<List<Question>> {
        return questionDao.getAllQuestionsForQuiz(id)
    }

    suspend fun getNumberOfQuestions(id: Int) : Int {
        return questionDao.getNumberOfQuestions(id)
    }

}