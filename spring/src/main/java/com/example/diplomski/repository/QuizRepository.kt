package com.example.diplomski.repository

import com.example.diplomski.entity.Question
import com.example.diplomski.entity.Quiz
import com.example.diplomski.entity.User
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface QuizRepository : JpaRepository<Quiz, Long> {
    fun findByUserId(userId: Long): List<Quiz>
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM Quiz q WHERE q.user = :userId")
    fun deleteAllByUserId(userId: User)
}