package com.example.diplomski.controller

import com.example.diplomski.dto.MessageDto
import com.example.diplomski.entity.Group
import com.example.diplomski.entity.Message
import com.example.diplomski.entity.Note
import com.example.diplomski.entity.Quiz
import com.example.diplomski.repository.GroupRepository
import com.example.diplomski.repository.MessageRepository
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

@RestController
@RequestMapping("/message")
class MessageController(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val quizRepository: QuizRepository,
    private val noteRepository: NoteRepository,
    private val groupRepository: GroupRepository,
    private val webSocketHandler: AppWebSocketHandler
) {
    @GetMapping("/{userId}")
    fun getMessages(@PathVariable userId: Long): List<MessageDto> {
        return messageRepository.findByReceiverId(userId).map {
            MessageDto(
                id = it.id,
                sender = it.sender.username,
                quizId = it.quiz?.id,
                groupId = it.group?.id,
                noteId = it.note?.id,
                receiver = it.receiver.id
            )
        }
    }

    @GetMapping("/{userId}/{groupId}")
    fun getMessage(@PathVariable userId: Long, @PathVariable groupId: Long): ResponseEntity<MessageDto> {
        val user = userRepository.findById(userId).get()
        val message = messageRepository.findByReceiverAndGroupId(user, groupId)
        if (message == null) {
            return ResponseEntity.notFound().build()
        }

        return ResponseEntity.ok(
            MessageDto(
                id = message.id,
                sender = message.sender.username,
                quizId = message.quiz?.id,
                groupId = message.group?.id,
                noteId = message.note?.id,
                receiver = message.receiver.id
            )
        )
    }


    @PostMapping
    fun createMessage(@RequestBody dto: MessageDto): ResponseEntity<MessageDto> {
        val sender = userRepository.findByUsername(dto.sender)
            ?: return ResponseEntity.badRequest().build()
        val receiver = userRepository.findById(dto.receiver).orElse(null)
            ?: return ResponseEntity.badRequest().build()
        var quiz : Quiz? = null
        var group : Group? = null
        var note : Note? = null
        if(dto.quizId != null) quiz = quizRepository.findById(dto.quizId).orElse(null)
        if(dto.noteId != null) note = noteRepository.findById(dto.noteId).orElse(null)
        if(dto.groupId != null) group = groupRepository.findById(dto.groupId).orElse(null)
        val saved = messageRepository.save(
            Message(sender = sender, receiver = receiver, quiz = quiz, note = note, group = group)
        )
        val response = MessageDto(
            id = saved.id,
            sender = saved.sender.username,
            quizId = saved.quiz?.id,
            receiver = saved.receiver.id,
            groupId = saved.group?.id,
            noteId = saved.note?.id
        )

        webSocketHandler.sendToUser(
            receiver.id,
            WebSocketEvent(
                type = "MESSAGE_RECEIVED",
                userId = receiver.id,
                entityId = saved.id,
                message = "${sender.username} sent you a message"
            )
        )

        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    fun deleteMessage(@PathVariable id: Long): ResponseEntity<Unit> {
        messageRepository.deleteById(id)
        return ResponseEntity.ok().build()
    }
}