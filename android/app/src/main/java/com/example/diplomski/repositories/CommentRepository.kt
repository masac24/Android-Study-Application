package com.example.diplomski.repositories

import android.util.Log
import com.example.diplomski.database.daos.CommentDao
import com.example.diplomski.database.daos.GroupDao
import com.example.diplomski.database.daos.NoteDao
import com.example.diplomski.database.daos.QuizDao
import com.example.diplomski.database.tables.Comment
import com.example.diplomski.database.tables.Note
import com.example.diplomski.database.tables.Quiz
import com.example.diplomski.retrofit.RetrofitClient
import com.example.diplomski.retrofit.TokenManager
import com.example.diplomski.retrofit.dto.CommentDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class CommentRepository(private val commentDao: CommentDao,
    private val quizDao: QuizDao, private val noteDao: NoteDao, private val groupDao: GroupDao) {

    fun getQuizComments(id: Int): Flow<List<Comment>> {
        return commentDao.getQuizComments(id)
    }

    fun getNoteComments(id: Int): Flow<List<Comment>> {
        return commentDao.getNoteComments(id)
    }

    suspend fun delete(i: Int){
        val item = commentDao.getById(i)
        commentDao.delete(i)
        try {
            item?.syncId?.let { RetrofitClient.api.deleteComment(it) }
        } catch (e: Exception) {
            Log.e("SYNC", "Comment delete sync failed: ${e.message}")
        }
    }

    suspend fun deleteByGroup(i: Int){
        val comments = commentDao.getByGroup(i)
        commentDao.deleteByGroup(i)
        comments.firstOrNull()?.forEach { comment ->
            try {
                comment.syncId?.let { RetrofitClient.api.deleteComment(it) }
            } catch (e: Exception) {
                Log.e("SYNC", "Comment delete sync failed: ${e.message}")
            }
        }
    }


    suspend fun insert(comment: Comment) {
        val id = commentDao.insert(comment).toInt()
        try {
            val group = groupDao.getById(comment.groupId)
            var quiz : Quiz? = null
            var note: Note? =  null
            if(comment.quizId != null)quiz = quizDao.getQuiz(comment.quizId).firstOrNull()
            if(comment.noteId != null)note = noteDao.getNote(comment.noteId).firstOrNull()
            val response = RetrofitClient.api.createComment(
                CommentDto(
                    text = comment.text,
                    groupId = group?.syncId ?: return,
                    quizId = quiz?.syncId,
                    noteId = note?.syncId,
                    createdAt = comment.createdAt,
                    userId = TokenManager.getUserId()
                )
            )
            commentDao.updateSyncId(id, response.id)
        } catch (e: Exception) {
                Log.e("SYNC", "Comment sync failed: ${e.message}")
        }
    }
}