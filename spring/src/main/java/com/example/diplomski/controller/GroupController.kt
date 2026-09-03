package com.example.diplomski.controller

import com.example.diplomski.dto.FolderDto
import com.example.diplomski.dto.GroupDto
import com.example.diplomski.dto.QuizDto
import com.example.diplomski.entity.Folder
import com.example.diplomski.entity.Group
import com.example.diplomski.entity.User
import com.example.diplomski.repository.GroupRepository
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
@RequestMapping("/group")
class GroupController(private val groupRepository: GroupRepository) {

    @GetMapping("/{userId}")
    fun getGroups(@PathVariable userId: Long): List<GroupDto> {
        return groupRepository.findByUserId(userId).map {
            GroupDto(it.id, it.name, it.user.id)
        }
    }

    @GetMapping("/id/{groupId}")
    fun getGroupById(@PathVariable groupId: Long): GroupDto  {

        val group = groupRepository.findById(groupId)
            .orElseThrow { RuntimeException("Quiz not found") }

        return GroupDto(groupId, group.name, group.user.id)
    }

    @PutMapping("/update/{id}")
    fun updateGroup(@PathVariable id: Long,
                    @RequestBody dto: GroupDto)  : GroupDto {
        val group = groupRepository.findById(id)
            .orElseThrow { RuntimeException("Quiz not found") }
        val updated = groupRepository.save(Group(group.id, dto.name, group.user))

        return GroupDto(updated.id, updated.name, updated.user.id)
    }

    @PostMapping
    fun createGroup(@RequestBody dto: GroupDto): GroupDto {
        val saved = groupRepository.save(
            Group(name = dto.name, user = User(dto.userId, "", ""))
        )
        return dto.copy(id = saved.id)
    }


    @DeleteMapping("/{id}")
    fun deleteItem(@PathVariable id: Long): ResponseEntity<Unit> {
        groupRepository.deleteById(id)
        return ResponseEntity.ok().build()
    }
}