package com.example.diplomski.repository

import com.example.diplomski.entity.Note
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NoteRepository : JpaRepository<Note, Long> {
    fun findByFolderId(folderId: Long): List<Note>
}