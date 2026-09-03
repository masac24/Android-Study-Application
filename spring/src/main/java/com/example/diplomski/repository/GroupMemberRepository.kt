package com.example.diplomski.repository

import com.example.diplomski.entity.Group
import com.example.diplomski.entity.GroupMember
import com.example.diplomski.entity.User
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface GroupMemberRepository: JpaRepository<GroupMember, Long> {
    fun findByUserId(userId: Long): List<GroupMember>
    fun findByGroupId(groupId: Long): List<GroupMember>
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM GroupMember q WHERE q.user = :userId")
    fun deleteAllByUserId(userId: User)

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM GroupMember q WHERE q.group = :group")
    fun deleteAllByGroupId(group: Group)
}