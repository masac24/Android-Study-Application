package com.example.diplomski.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.diplomski.database.tables.GroupQuiz
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupQuizDao {
    @Query("SELECT * FROM `groupquiz` WHERE groupId = :id ORDER BY id DESC")
    fun getGroupQuizzes(id: Int): Flow<List<GroupQuiz>>

    @Query("SELECT * FROM `groupquiz` WHERE quizId = :id ORDER BY id DESC")
    fun getQuiz(id: Int): Flow<List<GroupQuiz>>

    @Query("SELECT * FROM `groupquiz` WHERE quizId = :id AND groupId == :g ORDER BY id DESC")
    suspend fun getSpecQuiz(id: Int, g: Int): GroupQuiz?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(groupQuiz: GroupQuiz): Long

    @Query("DELETE FROM `groupquiz` WHERE quizId = :id AND groupId == :groupId")
    suspend fun delete(id: Int, groupId: Int)

    @Query("DELETE FROM `groupquiz` WHERE groupId = :id")
    suspend fun deleteByGroupId(id: Int)

    @Query("SELECT * FROM `groupquiz` WHERE syncId IS NULL")
    suspend fun getUnsynced(): List<GroupQuiz>

    @Query("UPDATE `groupquiz` SET syncId = :syncId WHERE id = :id")
    suspend fun updateSyncId(id: Int, syncId: Long)

    @Query("SELECT * FROM `groupquiz` WHERE syncId = :syncId LIMIT 1")
    suspend fun getBySyncId(syncId: Long): GroupQuiz?

    @Query("SELECT * FROM `groupquiz` WHERE id = :id")
    suspend fun getById(id: Int): GroupQuiz?

    @Query("SELECT syncId FROM `groupquiz` WHERE syncId IS NOT NULL AND groupId = :groupId")
    suspend fun getAllSyncIdsForGroup(groupId: Int): List<Long>

}