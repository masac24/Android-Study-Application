package com.example.diplomski.repositories

import android.util.Log
import com.example.diplomski.database.daos.GroupDao
import com.example.diplomski.database.daos.GroupNoteDao
import com.example.diplomski.database.daos.NoteDao
import com.example.diplomski.database.tables.GroupNote
import com.example.diplomski.retrofit.RetrofitClient
import com.example.diplomski.retrofit.dto.GroupNoteDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GroupNoteRepository(private val groupNoteDao: GroupNoteDao,
    private val noteDao: NoteDao, private val groupDao: GroupDao) {

    fun getGroupNotes(id: Int) : Flow<List<GroupNote>>{
        return groupNoteDao.getGroupNotes(id)
    }

    fun getNote(id: Int) : Flow<List<GroupNote>>{
        return groupNoteDao.getNote(id)
    }

    suspend fun insert(note: GroupNote) : Long {
        val id = groupNoteDao.insert(note)
        try {
            val n = noteDao.getNote(note.noteId).firstOrNull()
            val group = groupDao.getById(note.groupId)
            if(n == null || n.syncId == null || group == null || group.syncId == null) return id
            val response = RetrofitClient.api.createGroupNote(
                GroupNoteDto(noteId = n.syncId,
                    groupId = group.syncId, username = note.username)
            )
            groupNoteDao.updateSyncId(id.toInt(), response.id)
        } catch (e: Exception) {
            Log.e("SYNC", "GroupNote sync failed: ${e.message}")
        }
        return id
    }

    suspend fun delete(id: Int, gId: Int) {
        val item = groupNoteDao.getSpecNote(id, gId)
        try {
            item?.syncId?.let { RetrofitClient.api.deleteGroupNote(it) }
        } catch (e: Exception) {
            Log.e("SYNC", "GroupNote delete sync failed: ${e.message}")
        }
        groupNoteDao.delete(id, gId)
    }

    suspend fun deleteByGroup(id: Int){
        val notes = groupNoteDao.getGroupNotes(id)
        notes.firstOrNull()?.forEach {note ->
            try {
                note.syncId?.let { RetrofitClient.api.deleteGroupNote(it) }
            } catch (e: Exception) {
                Log.e("SYNC", "GroupNote delete sync failed: ${e.message}")
            }
        }
        groupNoteDao.deleteByGroupId(id)
    }
}