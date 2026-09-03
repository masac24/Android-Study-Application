package com.example.diplomski.controller

import com.example.diplomski.dto.NoteDto
import com.example.diplomski.dto.QuizDto
import com.example.diplomski.entity.Folder
import com.example.diplomski.entity.Note
import com.example.diplomski.entity.User
import com.example.diplomski.repository.FolderRepository
import com.example.diplomski.repository.NoteRepository
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
@RequestMapping("/note")
class NoteController(private val noteRepository: NoteRepository, private val folderRepository: FolderRepository) {

    @GetMapping("/{folderId}")
    fun getNotes(@PathVariable folderId: Long): List<NoteDto> {
        return noteRepository.findByFolderId(folderId).map {
            NoteDto(it.id, it.title, it.path, it.type, it.folder?.id)
        }
    }

    @GetMapping("/id/{noteId}")
    fun getNoteById(@PathVariable noteId: Long): NoteDto  {

        val note = noteRepository.findById(noteId)
            .orElseThrow { RuntimeException("Note not found") }

        return NoteDto(
            note.id, note.title, note.path, note.type, note.folder?.id,
        )
    }

    @PostMapping
    fun createNote(@RequestBody dto: NoteDto): NoteDto {
        val saved = noteRepository.save(
            Note(title = dto.title, path = dto.path, type = dto.type,
                folder = if(dto.folderId != null)Folder(dto.folderId, "", User(0, "", ""))
            else null)
        )
        return dto.copy(id = saved.id)
    }

    @DeleteMapping("/{id}")
    fun deleteNote(@PathVariable id: Long): ResponseEntity<Unit> {
        noteRepository.deleteById(id)
        return ResponseEntity.ok().build()
    }

    @PutMapping("/update/{id}")
    fun updateFile(
        @PathVariable id: Long,
        @RequestBody dto: NoteDto
    ): NoteDto {

        val note = noteRepository.findById(id)
            .orElseThrow { RuntimeException("Quiz not found") }
        val folder =
            if(dto.folderId != null)folderRepository.findById(dto.folderId).orElseThrow { RuntimeException("Folder not found") }
            else null
        val updated = noteRepository.save(Note(
            id = note.id, title = dto.title, path = note.path, type = note.type,
            folder = folder
        ))

        return NoteDto(updated.id, updated.title, updated.path, updated.type, updated.folder?.id)
    }
}