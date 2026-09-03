package com.example.diplomski.repository

import com.example.diplomski.entity.Folder
import com.example.diplomski.entity.User
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface FolderRepository : JpaRepository<Folder, Long> {
    fun findByUserId(userId: Long): List<Folder>
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Folder q WHERE q.user = :userId")
    fun deleteAllByUserId(userId: User)
}