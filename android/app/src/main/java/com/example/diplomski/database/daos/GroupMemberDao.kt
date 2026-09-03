package com.example.diplomski.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.diplomski.database.tables.Group
import com.example.diplomski.database.tables.GroupMember
import com.example.diplomski.database.tables.User
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupMemberDao {
    @Query("SELECT u.* FROM users u, groupmember g WHERE u.id == g.userId AND g.groupId == :id")
    fun getGroupMembers(id: Int): Flow<List<User>>

    @Query("SELECT u.* FROM `groups` u, groupmember g WHERE g.userId == :id AND g.groupId == u.id")
    fun getUserGroups(id: Int): Flow<List<Group>>

    @Query("SELECT * FROM groupmember WHERE userId == :userId AND groupId == :groupId")
    suspend fun getGroupMember(userId: Int, groupId:Int): GroupMember?

    @Query("SELECT * FROM groupmember g WHERE g.groupId == :id")
    fun getGroupMemberObjects(id: Int): Flow<List<GroupMember>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addGroupMember(member: GroupMember): Long

    @Query("DELETE FROM `groupmember` WHERE userId = :id AND groupId == :groupId")
    suspend fun delete(id: Int, groupId: Int)

    @Query("DELETE FROM `groupmember` WHERE userId = :id")
    suspend fun deleteByUserId(id: Int)

    @Query("DELETE FROM `groupmember` WHERE groupId = :id")
    suspend fun deleteByGroupId(id: Int)

    @Query("SELECT * FROM `groupmember` WHERE syncId IS NULL")
    suspend fun getUnsynced(): List<GroupMember>

    @Query("UPDATE `groupmember` SET syncId = :syncId WHERE id = :id")
    suspend fun updateSyncId(id: Int, syncId: Long)

    @Query("SELECT * FROM `groupmember` WHERE syncId = :syncId LIMIT 1")
    suspend fun getBySyncId(syncId: Long): GroupMember?

    @Query("SELECT * FROM `groupmember` WHERE id = :id")
    suspend fun getById(id: Int): GroupMember?

    @Query("SELECT syncId FROM `groupmember` WHERE syncId IS NOT NULL")
    suspend fun getAllSyncIds(): List<Long>

    @Query("SELECT syncId FROM `groupmember` WHERE syncId IS NOT NULL AND groupId = :groupId")
    suspend fun getAllSyncIdsForGroup(groupId: Int): List<Long>

    @Query("SELECT * FROM `groupmember` WHERE groupId = :groupId")
    suspend fun getGroupMembersList(groupId: Int): List<GroupMember>

}