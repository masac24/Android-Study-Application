package com.example.diplomski.controller

import com.example.diplomski.dto.AuthRequest
import com.example.diplomski.dto.AuthResponse
import com.example.diplomski.dto.ChecklistDto
import com.example.diplomski.entity.Checklist
import com.example.diplomski.entity.User
import com.example.diplomski.repository.ChecklistRepository
import com.example.diplomski.service.JwtService
import com.example.diplomski.service.UserService
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
@RequestMapping("/checklist")
class ChecklistController(private val checklistRepository: ChecklistRepository) {

    @GetMapping("/{userId}")
    fun getChecklist(@PathVariable userId: Long): List<ChecklistDto> {
        return checklistRepository.findByUserId(userId).map {
            ChecklistDto(it.id, it.text, it.done, it.user.id)
        }
    }

    @PostMapping
    fun createItem(@RequestBody dto: ChecklistDto): ChecklistDto {
        val saved = checklistRepository.save(
            Checklist(text = dto.text, done = dto.done, user = User(dto.userId, "", ""))
        )
        return dto.copy(id = saved.id)
    }

    @PutMapping("/{id}")
    fun updateItem(@PathVariable id: Long, @RequestBody dto: ChecklistDto): ChecklistDto {
        val saved = checklistRepository.save(
            Checklist(id = id, text = dto.text, done = dto.done, user = User(dto.userId, "", ""))
        )
        return dto.copy(id = saved.id)
    }

    @DeleteMapping("/{id}")
    fun deleteItem(@PathVariable id: Long): ResponseEntity<Unit> {
        checklistRepository.deleteById(id)
        return ResponseEntity.ok().build()
    }
}