package com.example.diplomski.controller

import com.example.diplomski.dto.GroupQuizDto
import com.example.diplomski.entity.GroupQuiz
import com.example.diplomski.repository.GroupMemberRepository
import com.example.diplomski.repository.GroupQuizRepository
import com.example.diplomski.repository.GroupRepository
import com.example.diplomski.repository.QuizRepository
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
@RequestMapping("/groupquiz")
class GroupQuizController(
    private val groupQuizRepository: GroupQuizRepository,
    private val quizRepository: QuizRepository,
    private val groupRepository: GroupRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val webSocketHandler: AppWebSocketHandler
) {

    @GetMapping("/{groupId}")
    fun getGroupQuizzes(@PathVariable groupId: Long): List<GroupQuizDto> {
        return groupQuizRepository.findByGroupId(groupId).map {
            GroupQuizDto(
                id = it.id,
                quizId = it.quiz.id,
                groupId = it.group.id
            )
        }
    }

    @PostMapping
    fun createGroupQuiz(@RequestBody dto: GroupQuizDto): GroupQuizDto {
        val quiz = quizRepository.findById(dto.quizId).get()
        val group = groupRepository.findById(dto.groupId).get()
        val saved = groupQuizRepository.save(
            GroupQuiz(quiz = quiz, group = group)
        )
        val memberIds = groupMemberRepository
            .findByGroupId(group.id)
            .map { it.user.id }
            .filter { it != quiz.user.id }

        webSocketHandler.sendToUsers(
            memberIds,
            WebSocketEvent(
                type = "GROUP_QUIZ_ADDED",
                groupId = group.id,
                entityId = saved.id,
                message = "${quiz.user.username} added a quiz in group ${saved.group.name}"
            )
        )
        return dto.copy(id = saved.id)
    }


    @DeleteMapping("/{id}")
    fun deleteItem(@PathVariable id: Long): ResponseEntity<Unit> {
        val groupquiz = groupQuizRepository.findById(id).get()
        val group = groupquiz.group
        val quiz = groupquiz.quiz
        groupQuizRepository.deleteById(id)
        val memberIds = groupMemberRepository
            .findByGroupId(group.id)
            .map { it.user.id }
            .filter { it != quiz.user.id }
        webSocketHandler.sendToUsers(
            memberIds,
            WebSocketEvent(
                type = "GROUP_QUIZ_DELETED",
                groupId = group.id,
                entityId = id,
                message = "${group.user.username} deleted a quiz from group ${group.name}"
            )
        )
        return ResponseEntity.ok().build()
    }
}