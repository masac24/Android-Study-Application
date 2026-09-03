package com.example.diplomski.controller

import com.example.diplomski.dto.UserDto
import com.example.diplomski.entity.User
import com.example.diplomski.repository.ChecklistRepository
import com.example.diplomski.repository.FolderRepository
import com.example.diplomski.repository.MessageRepository
import com.example.diplomski.repository.NoteRepository
import com.example.diplomski.repository.QuestionRepository
import com.example.diplomski.repository.QuizRepository
import com.example.diplomski.repository.ScoreRepository
import com.example.diplomski.repository.SessionRepository
import com.example.diplomski.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
class UserController(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val sessionRepository: SessionRepository,
    private val checklistRepository: ChecklistRepository,
    private val folderRepository: FolderRepository,
    private val messageRepository: MessageRepository,
    private val quizRepository: QuizRepository,
    private val noteRepository: NoteRepository,
    private val questionRepository: QuestionRepository,
    private val scoreRepository: ScoreRepository
) {

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): ResponseEntity<UserDto> {
        val user = userRepository.findById(id).orElseThrow { RuntimeException("User not found") }
        return ResponseEntity.ok(UserDto(id = user.id, username = user.username, password = user.password))
    }

    @GetMapping("/search/{username}")
    fun searchUser(@PathVariable username: String): ResponseEntity<UserDto> {
        val user = userRepository.findByUsername(username)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(UserDto(id = user.id, username = user.username, password = user.password))
    }

    @Transactional
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Unit> {
        val user = userRepository.findById(id).orElseThrow()
        sessionRepository.deleteAllByUserId(user)
        checklistRepository.deleteAllByUserId(user)
        val folders = folderRepository.findByUserId(user.id)
        folders.forEach {
            val notes = noteRepository.findByFolderId(it.id)
            notes.forEach { note-> noteRepository.delete(note) }
        }
        folderRepository.deleteAllByUserId(user)
        messageRepository.deleteAllByUserId(user)
        val quizzes = quizRepository.findByUserId(user.id)
        quizzes.forEach {
            val questions = questionRepository.deleteAllByQuizId(it)
            val scores = scoreRepository.deleteAllByQuizId(it)
        }
        quizRepository.deleteAllByUserId(user)
        userRepository.delete(user)
        return ResponseEntity.ok().build()
    }

    @PutMapping("/update/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @RequestBody dto: UserDto
    ): UserDto {

        val user = userRepository.findById(id)
            .orElseThrow { RuntimeException("User not found") }

        val saved = userRepository.save(User(id = id, dto.username, passwordEncoder.encode(dto.password)!!))

        return UserDto(saved.id, saved.username, saved.password)
    }
}