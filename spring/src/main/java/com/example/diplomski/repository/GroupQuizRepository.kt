package com.example.diplomski.repository

import com.example.diplomski.entity.GroupQuiz
import com.example.diplomski.entity.User
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface GroupQuizRepository: JpaRepository<GroupQuiz, Long> {
    fun findByQuizId(quizId: Long): List<GroupQuiz>
    fun findByGroupId(groupId: Long): List<GroupQuiz>
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM GroupQuiz q WHERE q.group = :groupId")
    fun deleteAllByGroupId(groupId: User)
}