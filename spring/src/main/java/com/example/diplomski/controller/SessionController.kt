package com.example.diplomski.controller

import com.example.diplomski.dto.AuthRequest
import com.example.diplomski.dto.AuthResponse
import com.example.diplomski.dto.SessionDto
import com.example.diplomski.entity.Session
import com.example.diplomski.entity.User
import com.example.diplomski.repository.SessionRepository
import com.example.diplomski.service.JwtService
import com.example.diplomski.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/session")
class SessionController(private val sessionRepository: SessionRepository) {

    @GetMapping("/{userId}")
    fun getSessions(@PathVariable userId: Long): List<SessionDto> {
        return sessionRepository.findByUserId(userId).map {
            SessionDto(it.id, it.time, it.datetime, it.completed, it.user.id)
        }
    }

    @PostMapping
    fun createSession(@RequestBody dto: SessionDto): SessionDto {
        val saved = sessionRepository.save(
            Session(time = dto.time, datetime = dto.datetime,
                completed = dto.completed, user = User(dto.userId, "", ""))
        )
        return dto.copy(id = saved.id)
    }
}