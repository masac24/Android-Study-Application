package com.example.diplomski.repository

import com.example.diplomski.entity.GroupNote
import com.example.diplomski.entity.User
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface GroupNoteRepository: JpaRepository<GroupNote, Long> {
    fun findByNoteId(noteId: Long): List<GroupNote>
    fun findByGroupId(groupId: Long): List<GroupNote>
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM GroupNote q WHERE q.group = :groupId")
    fun deleteAllByGroupId(groupId: User)
}