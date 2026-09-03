package com.example.diplomski.repositories

import android.util.Log
import com.example.diplomski.database.daos.ChecklistDao
import com.example.diplomski.database.daos.FolderDao
import com.example.diplomski.database.daos.MessageDao
import com.example.diplomski.database.daos.NoteDao
import com.example.diplomski.database.daos.QuestionDao
import com.example.diplomski.database.daos.QuizDao
import com.example.diplomski.database.daos.ReminderDao
import com.example.diplomski.database.daos.ScoreDao
import com.example.diplomski.database.daos.SessionDao
import com.example.diplomski.database.daos.UserDao
import com.example.diplomski.database.tables.User
import com.example.diplomski.retrofit.RetrofitClient
import com.example.diplomski.retrofit.TokenManager
import com.example.diplomski.retrofit.dto.AuthRequest
import com.example.diplomski.retrofit.dto.UserDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class UserRepository(private val userDao: UserDao,
    private val quizDao: QuizDao,
    private val questionDao: QuestionDao,
    private val noteDao: NoteDao,
    private val folderDao: FolderDao,
    private val scoreDao: ScoreDao,
    private val sessionDao: SessionDao,
    private val checklistDao: ChecklistDao,
    private val reminderDao: ReminderDao,
    private val messageDao: MessageDao) {

    val logged = userDao.getLogged()

    suspend fun insert(user: User, register: Boolean) {
        userDao.insert(user)
        if(register){
            try {
                RetrofitClient.api.register(AuthRequest(user.username, user.password))
            } catch (e: Exception) {
                Log.e("SYNC", "User register sync failed: ${e.message}")
            }
        }
    }

    suspend fun update(user: User){
        userDao.update(user)
        try {

            RetrofitClient.api.updateUser(
                TokenManager.getUserId(),
                UserDto(TokenManager.getUserId(), user.username, user.password)
            )

        } catch (e: Exception) {
            Log.e("SYNC", "User update sync failed: ${e.message}")
        }
    }

    suspend fun search(username: String) : Long? {
        try {
            val response = RetrofitClient.api.searchUser(username)
            return response.id
        } catch (e: Exception){
            Log.e("SYNC", "User search sync failed: ${e.message}")
        }
        return null
    }

    fun findUser(user: String, pass: String) : Flow<User?> {
        return userDao.findUser(user, pass)
    }

    suspend fun getBySyncId(id: Int): User? {
        return userDao.getBySyncId(id.toLong())
    }
    fun checkIfExist(user: String): Flow<User?> {
        return userDao.checkIfExist(user)
    }

    suspend fun changeLogged(user : User) {
        return userDao.changeLogged(user)
    }

    suspend fun delete(user : User){
        val id = user.id
        checklistDao.deleteByUserId(id)
        sessionDao.deleteByUserId(id)
        reminderDao.deleteByUserId(id)
        scoreDao.deleteByUserId(id)
        messageDao.deleteByUser(user.username)
        val quizzes = quizDao.getAllQuizzesForUser(user.username)
        quizzes.firstOrNull()?.forEach { quiz ->
            questionDao.deleteByQuizId(quiz.id.toLong())
            quizDao.deleteQuiz(quiz.id)
        }
        val folders = folderDao.getAllUserFolders(id)
        folders.firstOrNull()?.forEach { folder->
            noteDao.deleteByFolderId(folder.id)
            folderDao.delete(folder.id)
        }
        userDao.delete(id)
        try {
            user.syncId?.let { RetrofitClient.api.deleteUser(it) }

        } catch (e: Exception) {
            Log.e("SYNC", "User delete sync failed: ${e.message}")
        }
    }
}