package com.example.diplomski.repositories

import android.util.Log
import com.example.diplomski.database.daos.FolderDao
import com.example.diplomski.database.daos.NoteDao
import com.example.diplomski.database.tables.Folder
import com.example.diplomski.retrofit.RetrofitClient
import com.example.diplomski.retrofit.TokenManager
import com.example.diplomski.retrofit.dto.FolderDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class FolderRepository(private val folderDao: FolderDao, private val noteDao: NoteDao) {

    fun getUserFolders(id: Int): Flow<List<Folder>>{
        return folderDao.getAllUserFolders(id)
    }

    fun getFolder(id: Int?): Flow<Folder?>{
        return folderDao.getFolder(id)
    }

    fun getFolderByName(name: String, userId: Int): Flow<Folder?>{
        return folderDao.getFolderByName(name, userId)
    }

    suspend fun insert(folder: Folder) : Long {
        val localId = folderDao.insert(folder)
        try {
            val response = RetrofitClient.api.createFolder(
                FolderDto(name = folder.name, userId = TokenManager.getUserId())
            )
            folderDao.updateSyncId(localId.toInt(), response.id)
        } catch (e: Exception) {
            Log.e("SYNC", "Folder sync failed: ${e.message}")
        }
        return localId
    }

    suspend fun update(folder: Folder) {
        folderDao.update(folder)
        try {
            folder.syncId?.let { syncId ->
                RetrofitClient.api.updateFolder(
                    syncId,
                    FolderDto(folder.syncId, folder.name, folder.userId.toLong())
                )
            }
        } catch (e: Exception) {
            Log.e("SYNC", "Folder update sync failed: ${e.message}")
        }
    }

    suspend fun delete(id : Int){
        val folder = folderDao.getFolder(id).firstOrNull()
        if(folder != null ) noteDao.deleteByFolderId(folder.id)
        folderDao.delete(id)
        try {
            folder?.syncId?.let { RetrofitClient.api.deleteFolder(it) }
        } catch (e: Exception) {
            Log.e("SYNC", "Folder delete sync failed: ${e.message}")
        }
    }
}