package com.example.diplomski.repository

import com.example.diplomski.entity.Quiz
import com.example.diplomski.entity.Score
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ScoreRepository : JpaRepository<Score, Long> {
    fun findByUserId(userId: Long): List<Score>
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM Score q WHERE q.quiz = :quizId")
    fun deleteAllByQuizId(quizId: Quiz)
}