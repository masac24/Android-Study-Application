package com.example.diplomski.repository

import com.example.diplomski.entity.Session
import com.example.diplomski.entity.User
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SessionRepository : JpaRepository<Session, Long> {
    fun findByUserId(userId: Long): List<Session>
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM Session q WHERE q.user = :userId")
    fun deleteAllByUserId(userId: User)
}