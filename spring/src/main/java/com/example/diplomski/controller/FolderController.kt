package com.example.diplomski.controller

import com.example.diplomski.dto.FolderDto
import com.example.diplomski.entity.Folder
import com.example.diplomski.entity.User
import com.example.diplomski.repository.FolderRepository
import com.example.diplomski.repository.NoteRepository
import jakarta.transaction.Transactional
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/folder")
class FolderController(private val folderRepository: FolderRepository, private val noteRepository: NoteRepository) {

    @GetMapping("/{userId}")
    fun getFolders(@PathVariable userId: Long): List<FolderDto> {
        return folderRepository.findByUserId(userId).map {
            FolderDto(it.id, it.name, it.user.id)
        }
    }

    @PostMapping
    fun createFolder(@RequestBody dto: FolderDto): FolderDto {
        val saved = folderRepository.save(
            Folder(name = dto.name, user = User(dto.userId, "", ""))
        )
        return dto.copy(id = saved.id)
    }

    @Transactional
    @DeleteMapping("/{id}")
    fun deleteFolder(@PathVariable id: Long): ResponseEntity<Unit> {
        val notes = noteRepository.findByFolderId(id)
        notes.forEach { note ->
            noteRepository.delete(note)
        }
        folderRepository.deleteById(id)
        return ResponseEntity.ok().build()
    }

    @PutMapping("/update/{id}")
    fun updateFolder(
        @PathVariable id: Long,
        @RequestBody dto: FolderDto
    ): FolderDto {

        val folder = folderRepository.findById(id)
            .orElseThrow { RuntimeException("Quiz not found") }
        val updated = folderRepository.save(Folder(folder.id, dto.name, folder.user))

        return FolderDto(updated.id, updated.name, updated.user.id)
    }
}
