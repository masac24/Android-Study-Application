package com.example.diplomski.repositories

import android.util.Log
import com.example.diplomski.database.daos.GroupDao
import com.example.diplomski.database.daos.GroupMemberDao
import com.example.diplomski.database.tables.Group
import com.example.diplomski.database.tables.GroupMember
import com.example.diplomski.retrofit.RetrofitClient
import com.example.diplomski.retrofit.dto.GroupDto

class GroupRepository(private val groupDao: GroupDao, private val groupMemberDao: GroupMemberDao) {

    suspend fun getGroupBySyncId(id: Long): Group?{
        return groupDao.getBySyncId(id)
    }

    suspend fun getGroup(id: Int): Group? {
        return groupDao.getById(id)
    }

    suspend fun update(group: Group){
        groupDao.update(group)
        try {
            group.syncId?.let { syncId ->
                RetrofitClient.api.updateGroup(
                    syncId,
                    GroupDto(group.syncId, group.name, -1)
                )
            }
        } catch (e: Exception) {
            Log.e("SYNC", "Group update sync failed: ${e.message}")
        }
    }

    suspend fun insert(group: Group, userId: Int?): Long{
        val id = groupDao.insert(group)
        if(userId != null) {
            groupMemberDao.addGroupMember(GroupMember(0, userId, id.toInt()))
            try {
                val response = RetrofitClient.api.createGroup(
                    GroupDto(userId = userId.toLong(), name = group.name)
                )
                groupDao.updateSyncId(id.toInt(), response.id)
            } catch (e: Exception) {
                Log.e("SYNC", "Group sync failed: ${e.message}")
            }
        }
        return id
    }

    suspend fun delete(id: Int){
        val item = groupDao.getById(id)
        groupDao.delete(id)
        try {
            item?.syncId?.let { RetrofitClient.api.deleteGroup(it) }
        } catch (e: Exception) {
            Log.e("SYNC", "Group delete sync failed: ${e.message}")
        }
    }
}