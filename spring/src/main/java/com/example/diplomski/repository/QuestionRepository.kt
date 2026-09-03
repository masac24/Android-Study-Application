package com.example.diplomski.repository

import com.example.diplomski.entity.Question
import com.example.diplomski.entity.Quiz
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface QuestionRepository : JpaRepository<Question, Long> {
    fun findByQuizId(quizId: Long): List<Question>
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM Question q WHERE q.quiz = :quizId")
    fun deleteAllByQuizId(quizId: Quiz)
}