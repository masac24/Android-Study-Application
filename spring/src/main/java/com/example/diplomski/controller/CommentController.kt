package com.example.diplomski.controller

import com.example.diplomski.dto.CommentDto
import com.example.diplomski.dto.GroupMemberDto
import com.example.diplomski.entity.Comment
import com.example.diplomski.entity.Note
import com.example.diplomski.entity.Quiz
import com.example.diplomski.repository.CommentRepository
import com.example.diplomski.repository.GroupMemberRepository
import com.example.diplomski.repository.GroupRepository
import com.example.diplomski.repository.NoteRepository
import com.example.diplomski.repository.QuizRepository
import com.example.diplomski.repository.UserRepository
import com.example.diplomski.websocket.AppWebSocketHandler
import com.example.diplomski.websocket.WebSocketEvent
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.socket.WebSocketHandler

@RestController
@RequestMapping("/comment")
class CommentController(
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val quizRepository: QuizRepository,
    private val noteRepository: NoteRepository,
    private val commentRepository: CommentRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val webSocketHandler : AppWebSocketHandler
) {


    @PostMapping
    fun createComment(@RequestBody dto: CommentDto): CommentDto {
        val user = userRepository.findById(dto.userId).get()
        val group = groupRepository.findById(dto.groupId).get()
        var note : Note? = null
        var quiz : Quiz? = null
        if(dto.noteId != null)note = noteRepository.findById(dto.noteId).get()
        if(dto.quizId != null)quiz = quizRepository.findById(dto.quizId).get()
        val saved = commentRepository.save(
            Comment(
                user = user, group = group, note = note, quiz = quiz, text = dto.text, createdAt = dto.createdAt
            )
        )
        val memberIds = groupMemberRepository
            .findByGroupId(saved.group.id)
            .map { it.user.id }
            .filter { it != saved.user.id }
        webSocketHandler.sendToUsers(
            memberIds,
            WebSocketEvent(
                type = "GROUP_COMMENT_ADDED",
                groupId = saved.group.id,
                entityId = saved.id,
                message = "${saved.user.username} left a comment in group ${saved.group.name}"
            )
        )
        return dto.copy(id = saved.id)
    }

    @GetMapping("/note/{noteId}")
    fun getNoteComments(@PathVariable noteId: Long): List<CommentDto> {
        return commentRepository.findByNoteId(noteId).map {
            CommentDto(text = it.text, createdAt = it.createdAt, groupId = it.group.id, userId = it.user.id,
                noteId = noteId, quizId = null, id = it.id)
        }
    }

    @GetMapping("/quiz/{quizId}")
    fun getQuizComments(@PathVariable quizId: Long): List<CommentDto> {
        return commentRepository.findByQuizId(quizId).map {
            CommentDto(text = it.text, createdAt = it.createdAt, groupId = it.group.id, userId = it.user.id,
                noteId = null, quizId = it.quiz?.id,  id = it.id)
        }
    }

    @DeleteMapping("/delete/{id}")
    fun deleteItem(@PathVariable id: Long): ResponseEntity<Unit> {
        val comment = commentRepository.findById(id).get()
        commentRepository.deleteById(id)
        val memberIds = groupMemberRepository
            .findByGroupId(comment.group.id)
            .map { it.user.id }
            .filter { it != comment.user.id }
        webSocketHandler.sendToUsers(
            memberIds,
            WebSocketEvent(
                type = "GROUP_COMMENT_DELETED",
                groupId = comment.group.id,
                entityId = id
            )
        )
        return ResponseEntity.ok().build()
    }
}