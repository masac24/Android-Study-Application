package com.example.diplomski.controller
import com.example.diplomski.dto.QuestionDto
import com.example.diplomski.dto.QuizDto
import com.example.diplomski.entity.Question
import com.example.diplomski.entity.Quiz
import com.example.diplomski.entity.User
import com.example.diplomski.repository.QuestionRepository
import com.example.diplomski.repository.QuizRepository
import com.example.diplomski.repository.ScoreRepository
import jakarta.transaction.Transactional
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
@RequestMapping("/quiz")
class QuizController(
    private val quizRepository: QuizRepository,
    private val questionRepository: QuestionRepository,
    private val scoreRepository: ScoreRepository
) {
    @GetMapping("/{userId}")
    fun getQuizzes(@PathVariable userId: Long): List<QuizDto> {
        return quizRepository.findByUserId(userId).map {
            QuizDto(it.id, it.name, it.user.id)
        }
    }

    @PutMapping("/update/{id}")
    fun updateQuiz(
        @PathVariable id: Long,
        @RequestBody dto: QuizDto
    ): QuizDto {

        val quiz = quizRepository.findById(id)
            .orElseThrow { RuntimeException("Quiz not found") }

        val updated = quizRepository.save(Quiz(
            quiz.id, dto.name, quiz.user
        ))

        return QuizDto(
            updated.id,
            updated.name,
            updated.user.id
        )
    }

    @GetMapping("/id/{quizId}")
    fun getQuizById(@PathVariable quizId: Long): QuizDto  {

        val quiz = quizRepository.findById(quizId)
            .orElseThrow { RuntimeException("Quiz not found") }

        return QuizDto(
            quiz.id,
            quiz.name,
            quiz.user.id
        )
    }

    @PostMapping
    fun createQuiz(@RequestBody dto: QuizDto): QuizDto {
        val saved = quizRepository.save(Quiz(name = dto.name, user = User(dto.userId, "", "")))
        return QuizDto(saved.id, saved.name, saved.user.id)
    }

    @GetMapping("/questions/{quizId}")
    fun getQuestions(@PathVariable quizId: Long): List<QuestionDto> {
        return questionRepository.findByQuizId(quizId).map {
            QuestionDto(it.id, it.text, it.answer, it.type, it.option1, it.option2, it.option3, it.quiz.id)
        }
    }

    @PostMapping("/questions")
    fun createQuestion(@RequestBody dto: QuestionDto): QuestionDto {
        val saved = questionRepository.save(
            Question(text = dto.text, answer = dto.answer, type = dto.type,
                option1 = dto.option1, option2 = dto.option2, option3 = dto.option3,
                quiz = Quiz(dto.quizId, "", User(0, "", "")))
        )
        return dto.copy(id = saved.id)
    }
    @Transactional
    @DeleteMapping("/{id}")
    fun deleteItem(@PathVariable id: Long): ResponseEntity<Unit> {
        val quiz = quizRepository.getReferenceById(id)
        scoreRepository.deleteAllByQuizId(quiz)
        questionRepository.deleteAllByQuizId(quiz)
        quizRepository.deleteById(id)
        return ResponseEntity.ok().build()
    }
}