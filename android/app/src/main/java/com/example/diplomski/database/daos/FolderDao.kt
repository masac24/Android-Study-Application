package com.example.diplomski.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.diplomski.database.tables.Folder
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Query("SELECT * FROM folder WHERE userId = :userId ORDER BY id DESC")
    fun getAllUserFolders(userId: Int): Flow<List<Folder>>

    @Query("SELECT * FROM folder WHERE id = :id")
    fun getFolder(id: Int?): Flow<Folder?>

    @Query("SELECT * FROM folder WHERE name = :name AND userId == :userId")
    fun getFolderByName(name: String, userId: Int): Flow<Folder?>

    @Query("SELECT syncId FROM folder WHERE syncId IS NOT NULL AND userId = :userId")
    suspend fun getAllSyncIds(userId: Int): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(folder: Folder) : Long

    @Update
    suspend fun update(folder: Folder)

    @Query("DELETE FROM folder WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("SELECT * FROM folder WHERE syncId IS NULL")
    suspend fun getUnsynced(): List<Folder>

    @Query("UPDATE folder SET syncId = :syncId WHERE id = :id")
    suspend fun updateSyncId(id: Int, syncId: Long)

    @Query("SELECT * FROM folder WHERE syncId = :syncId LIMIT 1")
    suspend fun getBySyncId(syncId: Long): Folder?


}