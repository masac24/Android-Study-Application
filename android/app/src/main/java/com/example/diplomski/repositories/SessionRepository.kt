package com.example.diplomski.repositories

import android.util.Log
import com.example.diplomski.database.daos.SessionDao
import com.example.diplomski.database.tables.Session
import com.example.diplomski.retrofit.RetrofitClient
import com.example.diplomski.retrofit.TokenManager
import com.example.diplomski.retrofit.dto.SessionDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class SessionRepository(private val sessionDao: SessionDao) {


    fun getAllSessions(id: Int) : Flow<List<Session>> {
        return sessionDao.getAllSessions(id)
    }

    suspend fun insert(session: Session) {
        val id = sessionDao.insert(session).toInt()
        try {
            val response = RetrofitClient.api.createSession(
                SessionDto(
                    time = session.time,
                    datetime = session.datetime,
                    completed = session.completed,
                    userId = TokenManager.getUserId()
                )
            )
            sessionDao.updateSyncId(id, response.id)
        } catch (e: Exception) {
            Log.e("SYNC", "Session sync failed: ${e.message}")
        }
    }

    suspend fun delete(id: Int){
        val session = sessionDao.getSession(id).firstOrNull()
        sessionDao.deleteSession(id)
        try {
            session?.syncId?.let { RetrofitClient.api.deleteSession(it) }
        } catch (e: Exception) {
            Log.e("SYNC", "Session delete sync failed: ${e.message}")
        }
    }
}