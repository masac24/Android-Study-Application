package com.example.diplomski.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.diplomski.database.tables.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {


    @Query("SELECT * FROM note WHERE folderId = :folderId ORDER BY id DESC")
    fun getFolderFiles(folderId: Int?): Flow<List<Note>>

    @Query("SELECT * FROM note WHERE id = :id")
    fun getNote(id: Int): Flow<Note>

    @Query("SELECT * FROM note WHERE path = :path")
    fun getNoteByPath(path: String): Flow<Note>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note: Note) : Long

    @Update
    suspend fun update(note:Note)

    @Query("DELETE FROM note WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM note WHERE folderId = :id")
    suspend fun deleteByFolderId(id: Int)

    @Query("SELECT COUNT(*) FROM note WHERE folderId = :folderId")
    suspend fun getNumberOfFiles(folderId: Int): Int

    @Query("UPDATE note SET syncId = :syncId WHERE id = :id")
    suspend fun updateSyncId(id: Int, syncId: Long)

    @Query("SELECT * FROM note WHERE syncId = :syncId LIMIT 1")
    suspend fun getBySyncId(syncId: Long): Note?

    @Query("UPDATE note SET serverPath = :serverPath WHERE id = :id")
    suspend fun updateServerPath(id: Int, serverPath: String)

    @Query("SELECT * FROM note WHERE syncId IS NULL")
    suspend fun getUnsynced(): List<Note>

}