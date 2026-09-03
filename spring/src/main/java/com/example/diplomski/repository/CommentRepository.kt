package com.example.diplomski.repository

import com.example.diplomski.entity.Comment
import com.example.diplomski.entity.User
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface CommentRepository : JpaRepository<Comment, Long> {
    fun findByUserId(userId: Long): List<Comment>
    fun findByNoteId(noteId: Long): List<Comment>
    fun findByQuizId(quizId: Long): List<Comment>
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Comment q WHERE q.user = :userId")
    fun deleteAllByUserId(userId: User)

}