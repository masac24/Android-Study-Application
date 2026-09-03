package com.example.diplomski.repository

import com.example.diplomski.entity.Checklist
import com.example.diplomski.entity.User
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ChecklistRepository : JpaRepository<Checklist, Long> {
    fun findByUserId(userId: Long): List<Checklist>
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Checklist q WHERE q.user = :userId")
    fun deleteAllByUserId(userId: User)
}