package com.example.diplomski.repositories

import android.util.Log
import com.example.diplomski.database.daos.GroupDao
import com.example.diplomski.database.daos.GroupQuizDao
import com.example.diplomski.database.daos.QuizDao
import com.example.diplomski.database.tables.GroupQuiz
import com.example.diplomski.retrofit.RetrofitClient
import com.example.diplomski.retrofit.dto.GroupQuizDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GroupQuizRepository(private val groupQuizDao: GroupQuizDao,
    private val quizDao: QuizDao, private val groupDao: GroupDao) {

    fun getGroupQuizzes(id: Int) : Flow<List<GroupQuiz>>{
        return groupQuizDao.getGroupQuizzes(id)
    }

    fun getQuiz(id: Int) : Flow<List<GroupQuiz>>{
        return groupQuizDao.getQuiz(id)
    }

    suspend fun insert(note: GroupQuiz) : Long {
        val id = groupQuizDao.insert(note)
        try {
            val quiz = quizDao.getQuiz(note.quizId).firstOrNull()
            val group = groupDao.getById(note.groupId)
            val response = RetrofitClient.api.createGroupQuiz(
                GroupQuizDto(quizId = quiz?.syncId ?: return id, groupId = group?.syncId
                    ?: return id)
            )
            groupQuizDao.updateSyncId(id.toInt(), response.id)
        } catch (e: Exception) {
            Log.e("SYNC", "GroupQuiz sync failed: ${e.message}")
        }
        return id
    }

    suspend fun delete(id: Int, groupId: Int){
        val item = groupQuizDao.getSpecQuiz(id, groupId)
        groupQuizDao.delete(id, groupId)
        try {
            item?.syncId?.let { RetrofitClient.api.deleteGroupQuiz(it) }
        } catch (e: Exception) {
            Log.e("SYNC", "GroupQuiz delete sync failed: ${e.message}")
        }
    }

    suspend fun deleteForGroup(id: Int){
        val quizzes = groupQuizDao.getGroupQuizzes(id)
        groupQuizDao.deleteByGroupId(id)
        quizzes.firstOrNull()?.forEach { quiz ->
            try {
                quiz.syncId?.let { RetrofitClient.api.deleteGroupQuiz(it) }
            } catch (e: Exception) {
                Log.e("SYNC", "GroupQuiz delete sync failed: ${e.message}")
            }
        }
    }
}