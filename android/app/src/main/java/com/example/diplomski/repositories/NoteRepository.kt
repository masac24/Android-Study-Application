package com.example.diplomski.repositories

import android.content.Context
import android.util.Log
import com.example.diplomski.database.daos.FolderDao
import com.example.diplomski.database.daos.NoteDao
import com.example.diplomski.database.tables.Note
import com.example.diplomski.retrofit.RetrofitClient
import com.example.diplomski.retrofit.dto.NoteDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class NoteRepository(private val noteDao: NoteDao, private val folderDao: FolderDao, private val context: Context) {

    fun getFolderNotes(id: Int?) : Flow<List<Note>> {
        return noteDao.getFolderFiles(id)
    }

    fun getNote(id: Int): Flow<Note> {
        return noteDao.getNote(id)
    }

    suspend fun getNumberOfFiles(id: Int) : Int {
        return noteDao.getNumberOfFiles(id)
    }

    suspend fun insert(note: Note) : Long{
        val localId = noteDao.insert(note)
        try {
            val serverPath = uploadFile(note.path)
            val folder = folderDao.getFolder(note.folderId).firstOrNull()
            val response = RetrofitClient.api.createNote(
                NoteDto(
                    title = note.title,
                    path = serverPath,
                    type = note.type,
                    folderId = folder?.syncId
                )
            )
            noteDao.updateSyncId(localId.toInt(), response.id)
            noteDao.updateServerPath(localId.toInt(), serverPath)

        } catch (e: Exception) {
            Log.e("SYNC", "Note sync failed: ${e.message}")
        }
        return localId
    }
    suspend fun uploadFile(path: String): String {
        val file = File(path)
        val mediaType = when (file.extension.lowercase()) {
            "pdf" -> "application/pdf"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            else -> "application/octet-stream"
        }

        val requestBody = file.asRequestBody(mediaType.toMediaType())
        val multipart = MultipartBody.Part.createFormData("file", file.name, requestBody)
        val response = RetrofitClient.api.uploadFile(multipart)
        return response["path"] ?: ""
    }

    suspend fun downloadAndSave(serverPath: String): String {
        val response = RetrofitClient.api.downloadFile(serverPath)
        val localFile = File(context.filesDir, serverPath)
        localFile.parentFile?.mkdirs()
        localFile.outputStream().use { output ->
            response.byteStream().use { input ->
                input.copyTo(output)
            }
        }
        return localFile.absolutePath
    }

    suspend fun update(note: Note){
        noteDao.update(note)
        try {
            note.syncId?.let { syncId ->
                if(note.serverPath == null)return
                val folderId = folderDao.getFolder(note.folderId).firstOrNull()?.syncId ?: return
                RetrofitClient.api.updateNote(
                    syncId,
                    NoteDto(note.syncId, note.title, note.serverPath, note.type, folderId)
                )
            }
        } catch (e: Exception) {
            Log.e("SYNC", "Note update sync failed: ${e.message}")
        }
    }

    suspend fun delete(id: Int){
        val note = noteDao.getNote(id).firstOrNull()
        noteDao.delete(id)
        try {
            note?.syncId?.let { RetrofitClient.api.deleteNote(it) }
        } catch (e: Exception) {
            Log.e("SYNC", "Note delete sync failed: ${e.message}")
        }
    }
}