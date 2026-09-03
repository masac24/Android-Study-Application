package com.example.diplomski.repositories

import android.util.Log
import com.example.diplomski.database.daos.GroupDao
import com.example.diplomski.database.daos.MessageDao
import com.example.diplomski.database.daos.NoteDao
import com.example.diplomski.database.daos.QuizDao
import com.example.diplomski.database.tables.Group
import com.example.diplomski.database.tables.Message
import com.example.diplomski.database.tables.Note
import com.example.diplomski.database.tables.Quiz
import com.example.diplomski.retrofit.RetrofitClient
import com.example.diplomski.retrofit.dto.MessageDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class MessageRepository(private val messageDao: MessageDao,
    private val quizDao: QuizDao, private val groupDao: GroupDao, private val noteDao: NoteDao) {

    fun getAllMessages(id: Int): Flow<List<Message>> {
        return messageDao.getMessages(id)
    }

    suspend fun insert(message: Message) {
        val id = messageDao.insert(message).toInt()
        try {
            var quiz: Quiz? = null
            var group: Group? = null
            var note: Note? = null
            if(message.quizId != null)quiz = quizDao.getQuiz(message.quizId).firstOrNull()
            if(message.groupId != null) group = groupDao.getById(message.groupId)
            if(message.noteId != null) note = noteDao.getNote(message.noteId).firstOrNull()
            val response = RetrofitClient.api.createMessage(
                MessageDto(
                    sender = message.sender,
                    quizId = quiz?.syncId,
                    receiver = message.receiver.toLong(),
                    noteId = note?.syncId,
                    groupId = group?.syncId
                )
            )
            messageDao.updateSyncId(id, response.id)
        } catch (e: Exception) {
            Log.e("SYNC", "Message sync failed: ${e.message}")
        }
    }

    suspend fun delete(id: Int) {
        val item = messageDao.getById(id)
        messageDao.delete(id)
        try {
            item?.syncId?.let { RetrofitClient.api.deleteMessage(it) }
        } catch (e: Exception) {
            Log.e("SYNC", "Message delete sync failed: ${e.message}")
        }
    }

    suspend fun deleteAll(){
        messageDao.deleteAll()
    }
}