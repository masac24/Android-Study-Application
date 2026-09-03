package com.example.diplomski.repositories

import android.util.Log
import com.example.diplomski.database.daos.ChecklistDao
import com.example.diplomski.database.tables.Checklist
import com.example.diplomski.retrofit.RetrofitClient
import com.example.diplomski.retrofit.TokenManager
import com.example.diplomski.retrofit.dto.ChecklistDto
import kotlinx.coroutines.flow.Flow

class ChecklistRepository(private val checklistDao: ChecklistDao) {

    fun getAllItems(id: Int): Flow<List<Checklist>> {
        return checklistDao.getItems(id)
    }

    suspend fun insert(checklist: Checklist) {
        val id = checklistDao.insert(checklist).toInt()
        try {
            val response = RetrofitClient.api.createChecklist(
                ChecklistDto(
                    text = checklist.text,
                    done = checklist.done,
                    userId = TokenManager.getUserId()
                )
            )
            checklistDao.updateSyncId(id, response.id)
        } catch (e: Exception) {
            Log.e("SYNC", "Checklist sync failed: ${e.message}")
        }
    }

    suspend fun delete(id: Int) {
        val item = checklistDao.getById(id)
        checklistDao.delete(id)
        try {
            item?.syncId?.let { RetrofitClient.api.deleteChecklist(it) }
        } catch (e: Exception) {
            Log.e("SYNC", "Checklist delete sync failed: ${e.message}")
        }
    }
}