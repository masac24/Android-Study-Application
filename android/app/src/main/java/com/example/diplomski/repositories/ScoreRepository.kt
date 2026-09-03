package com.example.diplomski.repositories

import android.util.Log
import com.example.diplomski.database.daos.ScoreDao
import com.example.diplomski.database.tables.Score
import com.example.diplomski.retrofit.RetrofitClient
import com.example.diplomski.retrofit.TokenManager
import com.example.diplomski.retrofit.dto.ScoreDto
import kotlinx.coroutines.flow.Flow

class ScoreRepository(private val scoreDao: ScoreDao) {

    suspend fun insert(score: Score) {
        val id = scoreDao.insert(score).toInt()
        try {
            val response = RetrofitClient.api.createScore(
                ScoreDto(
                    score = score.score,
                    userId = TokenManager.getUserId(),
                    quizId = score.quizId.toLong()
                )
            )
            scoreDao.updateSyncId(id, response.id)
        } catch (e: Exception) {
            Log.e("SYNC", "Score sync failed: ${e.message}")
        }
    }

    fun getAllUserScores(userId: Int): Flow<List<Score>> {
        return scoreDao.getAllUserScores(userId)
    }

    fun getUserQuizScores(userId: Int, quizId: Int): Flow<List<Score>> {
        return scoreDao.getUserQuizScores(userId, quizId)
    }

    suspend fun getAvgScore(userId: Int, quizId: Int) : Int{
        return scoreDao.avgScore(userId, quizId)
    }
}