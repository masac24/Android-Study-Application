package com.example.diplomski.controller

import com.example.diplomski.dto.GroupMemberDto
import com.example.diplomski.entity.GroupMember
import com.example.diplomski.entity.User
import com.example.diplomski.repository.GroupMemberRepository
import com.example.diplomski.repository.GroupRepository
import com.example.diplomski.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/groupmember")
class GroupMemberController(
    private val groupMemberRepository: GroupMemberRepository,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository
) {

    @GetMapping("/{groupId}")
    fun getGroupMembers(@PathVariable groupId: Long): List<GroupMemberDto> {
        return groupMemberRepository.findByGroupId(groupId).map {
            GroupMemberDto(
                id = it.id,
                userId = it.user.id,
                groupId = it.group.id
            )
        }
    }

    @GetMapping("/user/{userId}")
    fun getUserGroups(@PathVariable userId: Long): List<GroupMemberDto> {
        return groupMemberRepository.findByUserId(userId).map {
            GroupMemberDto(
                id = it.id,
                userId = it.user.id,
                groupId = it.group.id
            )
        }
    }

    @PostMapping
    fun createGroupMember(@RequestBody dto: GroupMemberDto): GroupMemberDto {
        val user = userRepository.findById(dto.userId).get()
        val group = groupRepository.findById(dto.groupId).get()
        val saved = groupMemberRepository.save(
            GroupMember(user = user, group = group)
        )
        return dto.copy(id = saved.id)
    }


    @DeleteMapping("/{id}")
    fun deleteItem(@PathVariable id: Long): ResponseEntity<Unit> {
        groupMemberRepository.deleteById(id)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/delete/{id}")
    fun deleteAll(@PathVariable id: Long): ResponseEntity<Unit> {
        val group = groupRepository.findById(id).get()
        groupMemberRepository.deleteAllByGroupId(group)
        return ResponseEntity.ok().build()
    }
}