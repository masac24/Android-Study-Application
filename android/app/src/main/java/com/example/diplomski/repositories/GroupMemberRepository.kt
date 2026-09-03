package com.example.diplomski.repositories

import android.util.Log
import com.example.diplomski.database.daos.GroupDao
import com.example.diplomski.database.daos.GroupMemberDao
import com.example.diplomski.database.tables.Group
import com.example.diplomski.database.tables.GroupMember
import com.example.diplomski.retrofit.RetrofitClient
import com.example.diplomski.retrofit.TokenManager
import com.example.diplomski.retrofit.dto.GroupMemberDto
import kotlinx.coroutines.flow.Flow

class GroupMemberRepository(private val groupMemberDao: GroupMemberDao, private val groupDao: GroupDao) {

    fun getUserGroups(id: Int): Flow<List<Group>>{
        return groupMemberDao.getUserGroups(id)
    }

    suspend fun leave(userId: Int, groupId: Int){
        val item = groupMemberDao.getGroupMember(userId, groupId)
        groupMemberDao.delete(userId, groupId)
        try {
            item?.syncId?.let { RetrofitClient.api.deleteGroupMember(it) }
        } catch (e: Exception) {
            Log.e("SYNC", "GroupMember delete sync failed: ${e.message}")
        }
    }

    suspend fun deleteByGroup(groupId: Int){
        val group = groupDao.getById(groupId)
        try {
            if(group != null && group.syncId != null)RetrofitClient.api.deleteAllGroupMembers(group.syncId)
        } catch (e: Exception) {
            Log.e("SYNC", "GroupMember delete sync failed: ${e.message}")
        }
        groupMemberDao.deleteByGroupId(groupId)
    }

    suspend fun addMember(member: GroupMember): Long{
        val id = groupMemberDao.addGroupMember(member)
        try {
            val localGroup = groupDao.getById(member.groupId)
            if(localGroup != null && localGroup.syncId != null) {
                val group = RetrofitClient.api.getGroupById(localGroup.syncId)
                val response = RetrofitClient.api.createGroupMember(
                    GroupMemberDto(
                        userId = TokenManager.getUserId(),
                        groupId = group.id
                    )
                )
                groupMemberDao.updateSyncId(id.toInt(), response.id)
            }
        } catch (e: Exception) {
            Log.e("SYNC", "GroupMember sync failed: ${e.message}")
        }
        return id
    }
}