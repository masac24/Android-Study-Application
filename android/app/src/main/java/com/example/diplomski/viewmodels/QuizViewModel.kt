package com.example.diplomski.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.diplomski.StudyApplication
import com.example.diplomski.database.tables.Message
import com.example.diplomski.database.tables.Note
import com.example.diplomski.database.tables.Question
import com.example.diplomski.database.tables.Quiz
import com.example.diplomski.database.tables.Score
import com.example.diplomski.repositories.GroupQuizRepository
import com.example.diplomski.repositories.MessageRepository
import com.example.diplomski.repositories.QuestionRepository
import com.example.diplomski.repositories.QuizRepository
import com.example.diplomski.repositories.ScoreRepository
import com.example.diplomski.repositories.UserRepository
import com.example.diplomski.retrofit.ai.AIQuizGenerator
import com.example.diplomski.retrofit.ai.AIRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID


data class QuizWithCount(
    val quiz: Quiz,
    val questionCount: Int,
    val avgScore: Int
)

data class QuestionDetails(
    val id: Int = 0,
    val question: String = "",
    val answer: String = "",
    val quiz: Int = 0,
    val type: String = "WRITTEN",
    val opt1: String = "",
    val opt2: String = "",
    val opt3: String = "",
    )

data class uiStateQuiz(
    val name: String = "",
    val start: Boolean = false,
    val index: Int = 0,
    val questionDetails : QuestionDetails = QuestionDetails()
)

class QuizViewModel(
    application: Application,
    private val quizRepository: QuizRepository,
    private val questionRepository: QuestionRepository,
    private val scoreRepository: ScoreRepository,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val groupQuizRepository: GroupQuizRepository
) : AndroidViewModel(application) {

    var questions = mutableStateListOf<QuestionDetails>()
    var uiStateQuiz by mutableStateOf(uiStateQuiz())
        private set

    private val _quizzesWithCount = MutableStateFlow<List<QuizWithCount>>(emptyList())
    val quizzesWithCount = _quizzesWithCount.asStateFlow()
    private val _userScores = MutableStateFlow<List<Score>>(emptyList())
    val userScores = _userScores.asStateFlow()

    private val _message = mutableStateOf("")
    val message = _message
    var workRequestId: UUID? = null

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                val quizRepository = (this[APPLICATION_KEY] as StudyApplication).quizRepository
                val questionRepository = (this[APPLICATION_KEY] as StudyApplication).questionRepository
                val scoreRepository = (this[APPLICATION_KEY] as StudyApplication).scoreRepository
                val messageRepository = (this[APPLICATION_KEY] as StudyApplication).messageRepository
                val userRepository = (this[APPLICATION_KEY] as StudyApplication).userRepository
                val groupQuizRepository = (this[APPLICATION_KEY] as StudyApplication).groupQuizRepository
                return@initializer QuizViewModel(application, quizRepository,questionRepository,
                    scoreRepository, messageRepository, userRepository, groupQuizRepository)
            }
        }
    }

    fun getUserQuizes(id: String, i: Int) {
        viewModelScope.launch {
            quizRepository.getUserQuizzes(id).collect { quizzes ->
                val withCounts = quizzes.map { quiz ->
                    QuizWithCount(
                        quiz = quiz,
                        questionCount = questionRepository.getNumberOfQuestions(quiz.id),
                        avgScore = scoreRepository.getAvgScore(i, quiz.id)
                    )
                }
                _quizzesWithCount.value = withCounts
            }
        }
    }

    fun setIndex(index: Int){
        uiStateQuiz = uiStateQuiz.copy(index = index)
    }

    fun setName(name : String) {
        uiStateQuiz = uiStateQuiz.copy(name = name)
    }

    fun setType(type: String){
        uiStateQuiz = uiStateQuiz.copy(questionDetails = uiStateQuiz.questionDetails.copy(type = type))
    }

    fun setQuestion(q : String) {
        uiStateQuiz = uiStateQuiz.copy(questionDetails = uiStateQuiz.questionDetails.copy(question = q))
    }

    fun setAnswer(a : String) {
        uiStateQuiz = uiStateQuiz.copy(questionDetails = uiStateQuiz.questionDetails.copy(answer = a))
    }

    fun setOpt1(a : String) {
        uiStateQuiz = uiStateQuiz.copy(questionDetails = uiStateQuiz.questionDetails.copy(opt1 = a))
    }
    fun setOpt2(a : String) {
        uiStateQuiz = uiStateQuiz.copy(questionDetails = uiStateQuiz.questionDetails.copy(opt2 = a))

    }fun setOpt3(a : String) {
        uiStateQuiz = uiStateQuiz.copy(questionDetails = uiStateQuiz.questionDetails.copy(opt3 = a))
    }

    fun deleteQuiz(id: Int){
        viewModelScope.launch {
            val groupquizzes = groupQuizRepository.getQuiz(id).firstOrNull()
            if(groupquizzes != null && !groupquizzes.isEmpty())
                groupquizzes.forEach { quiz->
                    groupQuizRepository.delete(quiz.quizId, quiz.groupId)
                }
            quizRepository.deleteQuiz(id)
        }
    }

    fun updateName(name: String, quiz: Any){
        val q = quiz as Quiz
        viewModelScope.launch {
            quizRepository.update(Quiz(q.id, name, q.user, q.syncId))
        }
    }

    fun setStart(s: Boolean) {
        uiStateQuiz = uiStateQuiz.copy(start = s)
    }

    fun setQuestion(){
        questions.add(uiStateQuiz.questionDetails)
        uiStateQuiz = uiStateQuiz.copy(index = 0, questionDetails = QuestionDetails())
    }

    suspend fun getMaxScore(user: Int, quiz: Int) : Int{
        val scores = scoreRepository.getUserQuizScores(user, quiz).first()
        return scores.maxOfOrNull { it.score } ?: -1
    }

    suspend fun getUserScores(user: Int) {
        _userScores.value = scoreRepository.getAllUserScores(user).first()
    }


    fun loadQuestion(question: QuestionDetails) {
        uiStateQuiz = uiStateQuiz.copy(questionDetails = question)
    }

    fun removeFromList(){
        questions.remove(uiStateQuiz.questionDetails)
    }

    fun deleteQuestion() {
        removeFromList()
        uiStateQuiz = uiStateQuiz.copy(questionDetails = QuestionDetails())
    }
    fun save(userId: String) {
        uiStateQuiz = uiStateQuiz.copy(start = false)
        viewModelScope.launch {
            val quizId = quizRepository.insert(Quiz(0, uiStateQuiz.name, userId))
            questions.forEach { question ->
                questionRepository.insert(Question(0, question.question,
                    question.answer, quizId.toInt(), question.type, question.opt1,
                    question.opt2, question.opt3))
            }
        }
    }

    fun average(): Float{
        if (userScores.value.isEmpty()) return 0f
        var sum = 0f
        quizzesWithCount.value.forEach { (quiz, questionCount, avgScore) ->
            sum += avgScore  / questionCount.toFloat()
        }
        return sum / quizzesWithCount.value.size * 100
    }

    fun sendQuiz(name: String, id: String, quiz: Quiz){
        viewModelScope.launch {
            val receiver = userRepository.search(name)
            if(receiver == null){
                _message.value = "No user found"
                return@launch
            }else{
                _message.value = "Succesfully sent!"
            }
            if(quiz.syncId != null){
                messageRepository.insert(Message(0, id, quiz.id, null, null,
                    receiver.toInt() ))
            }

        }
    }

    fun resetMessage(){
        _message.value = ""
    }

    private val appContext = getApplication<Application>()

    fun generateQuiz(name: String, userId: String, notes : List<Note>){
        workRequestId = AIQuizGenerator.generateAiQuiz(notes, name, userId, 10, appContext)
//        viewModelScope.launch {
//            val questions = AIQuizGenerator.generate(appContext, notes)
//            val quizId = quizRepository.insert(Quiz(0, name, userId))
//            questions.forEach {
//                questionRepository.insert(Question(id = 0,it.question, it.answer,quizId.toInt(),
//                    "MULTIPLE", it.option1,option2 = it.option2, option3 = it.option3))
//            }
//        }
    }


}