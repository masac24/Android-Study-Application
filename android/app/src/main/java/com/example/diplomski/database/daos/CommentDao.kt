package com.example.diplomski.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.diplomski.database.tables.Comment
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    @Query("SELECT * FROM comment WHERE quizId = :id ORDER BY id ASC")
    fun getQuizComments(id: Int): Flow<List<Comment>>

    @Query("SELECT * FROM comment WHERE noteId = :id ORDER BY id ASC")
    fun getNoteComments(id: Int): Flow<List<Comment>>

    @Query("SELECT * FROM comment WHERE groupId = :id ORDER BY id ASC")
    fun getByGroup(id: Int): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(comment: Comment): Long

    @Query("DELETE FROM comment WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM comment WHERE user = :id")
    suspend fun deleteByUsername(id: String)

    @Query("DELETE FROM comment WHERE groupId = :id")
    suspend fun deleteByGroup(id: Int)

    @Query("SELECT * FROM comment WHERE syncId IS NULL")
    suspend fun getUnsynced(): List<Comment>

    @Query("UPDATE comment SET syncId = :syncId WHERE id = :id")
    suspend fun updateSyncId(id: Int, syncId: Long)

    @Query("SELECT * FROM comment WHERE syncId = :syncId LIMIT 1")
    suspend fun getBySyncId(syncId: Long): Comment?

    @Query("SELECT * FROM comment WHERE id = :id")
    suspend fun getById(id: Int): Comment?

    @Query("SELECT syncId FROM comment WHERE syncId IS NOT NULL AND groupId = :groupId")
    suspend fun getAllSyncIds(groupId: Int): List<Long>
}