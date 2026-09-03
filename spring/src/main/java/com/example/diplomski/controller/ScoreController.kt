package com.example.diplomski.controller

import com.example.diplomski.dto.AuthRequest
import com.example.diplomski.dto.AuthResponse
import com.example.diplomski.dto.ScoreDto
import com.example.diplomski.entity.Quiz
import com.example.diplomski.entity.Score
import com.example.diplomski.entity.User
import com.example.diplomski.repository.ScoreRepository
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
@RequestMapping("/score")
class ScoreController(private val scoreRepository: ScoreRepository) {

    @GetMapping("/{userId}")
    fun getScores(@PathVariable userId: Long): List<ScoreDto> {
        return scoreRepository.findByUserId(userId).map {
            ScoreDto(it.id, it.score, it.user.id, it.quiz.id)
        }
    }

    @PostMapping
    fun createScore(@RequestBody dto: ScoreDto): ScoreDto {
        val saved = scoreRepository.save(
            Score(score = dto.score,
                user = User(dto.userId, "", ""),
                quiz = Quiz(dto.quizId, "", User(0, "", "")))
        )
        return dto.copy(id = saved.id)
    }
}