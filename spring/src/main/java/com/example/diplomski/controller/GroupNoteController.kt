package com.example.diplomski.controller

import com.example.diplomski.dto.GroupNoteDto
import com.example.diplomski.entity.GroupNote
import com.example.diplomski.repository.GroupMemberRepository
import com.example.diplomski.repository.GroupNoteRepository
import com.example.diplomski.repository.GroupRepository
import com.example.diplomski.repository.NoteRepository
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
@RequestMapping("/groupnote")
class GroupNoteController(
    private val groupNoteRepository: GroupNoteRepository,
    private val noteRepository: NoteRepository,
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    private val webSocketHandler: AppWebSocketHandler,
    private val groupMemberRepository: GroupMemberRepository
) {

    @GetMapping("/{groupId}")
    fun getGroupNotes(@PathVariable groupId: Long): List<GroupNoteDto> {
        return groupNoteRepository.findByGroupId(groupId).map {
            GroupNoteDto(
                id = it.id,
                noteId = it.note.id,
                groupId = it.group.id,
                username = it.user.username,
            )
        }
    }

    @PostMapping
    fun createGroupNote(@RequestBody dto: GroupNoteDto): GroupNoteDto {
        val note = noteRepository.findById(dto.noteId).get()
        val group = groupRepository.findById(dto.groupId).get()
        val user = userRepository.findByUsername(dto.username) ?: throw RuntimeException("Folder not found")
        val saved = groupNoteRepository.save(
            GroupNote(note = note, group = group, user = user)
        )
        val members = groupMemberRepository
            .findByGroupId(group.id)

        val memberIds = members
            .map { it.user.id }
            .filter { it != user.id }
        println("Sending GROUP_NOTE_ADDED to members: $memberIds")
        webSocketHandler.sendToUsers(
            memberIds,
            WebSocketEvent(
                type = "GROUP_NOTE_ADDED",
                groupId = group.id,
                entityId = saved.id,
                message = "${saved.user.username} added a note in group ${saved.group.name}"
            )
        )
        return dto.copy(id = saved.id)
    }


    @DeleteMapping("/{id}")
    fun deleteItem(@PathVariable id: Long): ResponseEntity<Unit> {
        val groupnote = groupNoteRepository.findById(id).get()
        val group = groupnote.group
        val user = groupnote.user
        groupNoteRepository.deleteById(id)
        val memberIds = groupMemberRepository
            .findByGroupId(group.id)
            .map { it.user.id }
            .filter { it != user.id }
        webSocketHandler.sendToUsers(
            memberIds,
            WebSocketEvent(
                type = "GROUP_NOTE_DELETED",
                groupId = group.id,
                entityId = id,
                message = "${group.user.username} deleted a note from group ${group.name}"
            )
        )
        return ResponseEntity.ok().build()
    }
}