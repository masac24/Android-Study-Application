package com.example.diplomski.viewmodels

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.util.rangeTo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.diplomski.StudyApplication
import com.example.diplomski.database.tables.Question
import com.example.diplomski.database.tables.Quiz
import com.example.diplomski.database.tables.Score
import com.example.diplomski.repositories.QuestionRepository
import com.example.diplomski.repositories.QuizRepository
import com.example.diplomski.repositories.ScoreRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch


enum class QuizPhase {
    LOADING,
    INSTRUCTIONS,
    IN_PROGRESS,
    FINISHED,
    RESULTS
}

data class uiStateTest(
    val phase: QuizPhase = QuizPhase.LOADING,
    val countdown: Int = 15,
    val currentQuestion: Int = 0,
    val answer: String = "",
    val score: Int = 0
)

class TestViewModel(private val quizRepository: QuizRepository,
                    private val questionRepository: QuestionRepository,
                    private val scoreRepository: ScoreRepository)
    : ViewModel(){

    var answers = mutableStateListOf<String>()
    var questions = flowOf<List<Question>>()
    var quiz = flowOf<Quiz>()

    var uiState by mutableStateOf(uiStateTest())
        private set
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val quizRepository = (this[APPLICATION_KEY] as StudyApplication).quizRepository
                val questionRepository = (this[APPLICATION_KEY] as StudyApplication).questionRepository
                val scoreRepository = (this[APPLICATION_KEY] as StudyApplication).scoreRepository
                return@initializer TestViewModel(quizRepository,questionRepository, scoreRepository)
            }
        }
    }

    fun getQuestions(quizId: Int){
        viewModelScope.launch {
            questions = questionRepository.getQuizQuestions(quizId)
            quiz = quizRepository.getQuiz(quizId)
            uiState = uiState.copy(phase = QuizPhase.INSTRUCTIONS)
        }
    }

    fun setAnswer(a: String) {
        uiState = uiState.copy(answer = a)
    }

    fun answerQuestion(x : Boolean){
       answers.add(uiState.answer)
        uiState = uiState.copy(answer = "", currentQuestion = uiState.currentQuestion + 1)
        if(x)
            uiState = uiState.copy(phase = QuizPhase.FINISHED)
    }

    fun seeResults() {
        uiState = uiState.copy(phase = QuizPhase.RESULTS)
    }

    fun calculateScore(user: Int) {
        var score = 0
        viewModelScope.launch {
            val currentQuestions = questions.first()
            for(i in currentQuestions.indices){
                if (answers[i] == currentQuestions[i].answer) {
                    score++
                }
            }

            uiState = uiState.copy(score = score)
            scoreRepository.insert(Score(0, user, quiz.firstOrNull()?.id ?: 0, score))
        }
    }

    fun startCountdown() {
        viewModelScope.launch {
            for (i in 8 downTo 1) {
                uiState = uiState.copy(countdown = i)
                delay(1000L)
            }
            uiState = uiState.copy(phase = QuizPhase.IN_PROGRESS)
        }
    }
}