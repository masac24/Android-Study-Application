package com.example.diplomski.repository

import com.example.diplomski.entity.Group
import com.example.diplomski.entity.User
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface GroupRepository: JpaRepository<Group, Long> {
    fun findByUserId(userId: Long): List<Group>
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Group q WHERE q.user = :userId")
    fun deleteAllByUserId(userId: User)
}