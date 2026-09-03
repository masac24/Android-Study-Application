package com.example.diplomski.repository

import com.example.diplomski.entity.Message
import com.example.diplomski.entity.User
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MessageRepository : JpaRepository<Message, Long> {
    fun findByReceiverId(receiverId: Long): List<Message>
    fun findBySenderId(senderId: Long): List<Message>
    fun findByReceiverAndGroupId(receiverId: User, groupId: Long): Message?
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Message q WHERE q.sender = :userId")
    fun deleteAllByUserId(userId: User)
}