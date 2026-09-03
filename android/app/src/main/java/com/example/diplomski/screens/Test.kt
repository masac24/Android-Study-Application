package com.example.diplomski.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.diplomski.elements.MyRadioButton
import com.example.diplomski.database.tables.Question
import com.example.diplomski.database.tables.User
import com.example.diplomski.ui.theme.AccentDarkPink
import com.example.diplomski.ui.theme.AccentDarkTeal
import com.example.diplomski.ui.theme.AccentPink
import com.example.diplomski.ui.theme.AccentSky
import com.example.diplomski.ui.theme.AccentTeal
import com.example.diplomski.ui.theme.BackgroundColor
import com.example.diplomski.ui.theme.LighterBackground
import com.example.diplomski.ui.theme.PrimaryColor
import com.example.diplomski.ui.theme.SurfaceColor
import com.example.diplomski.ui.theme.TextPrimary
import com.example.diplomski.viewmodels.QuestionDetails
import com.example.diplomski.viewmodels.QuizPhase
import com.example.diplomski.viewmodels.TestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Test(loggedUser: User?,
          goToQuiz : () -> Unit,
          quizId: Int
){
    val viewModel: TestViewModel = viewModel(factory = TestViewModel.Factory)
    LaunchedEffect(quizId) {
        if(loggedUser != null)
            viewModel.getQuestions(quizId)
    }
    val questions by viewModel.questions.collectAsState(listOf())
    val state = viewModel.uiState
    val quiz = viewModel.quiz.collectAsState(null).value
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        showExitDialog = true
    }
    if(showExitDialog && (state.phase == QuizPhase.FINISHED || state.phase == QuizPhase.RESULTS) ){
        goToQuiz()
    }

    if(showExitDialog && state.phase != QuizPhase.FINISHED && state.phase != QuizPhase.RESULTS){
        BasicAlertDialog(
            onDismissRequest = {showExitDialog = false}
        ){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(SurfaceColor, shape = RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Text("Are you sure you want to quit this quiz?")
                Button(onClick = {
                    showExitDialog = false
                    goToQuiz()
                }) { Text("Confirm")}
                Button(onClick = {showExitDialog = false}) {
                    Text("Deny")
                }
            }
        }
    }

    when (state.phase) {
        QuizPhase.LOADING -> CircularProgressIndicator()
        QuizPhase.INSTRUCTIONS -> InstructionsScreen(
            countdown = state.countdown,
            onStart = { viewModel.startCountdown()},
            quiz?.name
        )
        QuizPhase.IN_PROGRESS -> QuestionScreen(
            question = questions[state.currentQuestion],
            questionNumber = state.currentQuestion + 1,
            total = questions.size,
            onAnswer = { viewModel.answerQuestion(it) },
            viewModel
        )
        QuizPhase.FINISHED -> ResultScreen(
            user = loggedUser?.id ?: 0,
            calculate = { viewModel.calculateScore(it) },
            score = state.score,
            total = questions.size,
            onClick = {viewModel.seeResults()}
        )
        QuizPhase.RESULTS -> seeResults(
            questions, viewModel.answers
        )
    }
}

@Composable
fun InstructionsScreen(
    countdown : Int,
    onStart : () -> Unit,
    name: String?
){

    LaunchedEffect(Unit) {
        onStart()
    }

    Column (
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text(
            text = "QUIZ",
            fontSize = 60.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryColor,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "$name",
            fontSize = 60.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryColor,
            fontFamily = FontFamily.Monospace
        )
        Spacer(Modifier.height(16.dp))
        Card(
            colors = CardDefaults.cardColors(
                containerColor = SurfaceColor
            ),
            modifier = Modifier.padding(16.dp)
        ){
            Text("You have unlimited time for this quiz. \n" +
                    "Once you go to the next question, you can't go back. \n" +
                    "Try to answer all of the questions. \n" +
                    "Good luck!",
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                letterSpacing = 3.sp,
                modifier = Modifier.padding(16.dp))
        }
        Spacer(Modifier.height(32.dp))
        Text("Quiz starts in...")
        Text(
            text = "$countdown",
            fontSize = 100.sp,
            fontWeight = FontWeight.Black,
            color = AccentSky
        )
    }
}

@Composable
fun QuestionScreen(
    question: Question,
    questionNumber: Number,
    total: Number,
    onAnswer: (x : Boolean) -> Unit,
    viewModel: TestViewModel
){
    Column (
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceColor
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ){
                Text("$questionNumber / $total",
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(12.dp))
            }
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Question: ${question.text}",
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    letterSpacing = 3.sp)

                Spacer(Modifier.height(16.dp))

                if(question.type == "WRITTEN") {
                    OutlinedTextField(
                        value = viewModel.uiState.answer,
                        onValueChange = {viewModel.setAnswer(it)},
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            unfocusedBorderColor = PrimaryColor
                        )
                    )
                } else{
                    var selected by remember { mutableStateOf(0) }
                    val options = remember(question.id) {listOf(question.answer, question.option1,
                        question.option2, question.option3).shuffled()}
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                        for(i in 0 until  4){
                            MyRadioButton(
                                label = options[i],
                                selected = options[i] == options[selected],
                                onClick = {viewModel.setAnswer(options[i])
                                    selected = i}
                            )
                        }
                    }
                }


            }
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onAnswer(total == questionNumber) }
        ){
            Text(
                text = if (total != questionNumber) "Next" else "Finish quiz"
            )
        }
    }
}

@Composable
fun ResultScreen(
    user: Int,
    calculate: (x: Int)-> Unit,
    score: Int,
    total: Int,
    onClick: () -> Unit
){
    LaunchedEffect(user) {
        calculate(user)
    }

    Column (
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text(
            text = if ((score*1f/total) >= 0.5) "Congrats!" else "You can do better",
            textAlign = TextAlign.Center,
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 3.sp,
            color = AccentSky
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "$score / $total",
            fontSize = 30.sp
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onClick
        ) { Text("See results")}
    }
}

@Composable
fun seeResults(
    questions: List<Question>,
    answers : List<String>
){
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(26.dp))
        Text("YOUR ANSWERS",
            fontSize = 40.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = TextPrimary)
        Spacer(Modifier.height(16.dp))
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
            itemsIndexed(questions) { index, q ->
                QuestionCard(q, answers[index])
            }
        }
    }
}

@Composable
fun QuestionCard(
    question: Question,
    answer : String
){
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ){
        Text(
            text = question.text,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            modifier = Modifier.padding(8.dp)
        )

        Spacer(Modifier.height(8.dp))

        if(question.type == "MULTIPLE"){
            val options = remember(question.id) {listOf(question.answer, question.option1,
                question.option2, question.option3).shuffled()}

            options.forEach { option ->
                val correctAnswer = option == question.answer && answer == option
                val myAnswer = option == answer
                Card(colors = CardDefaults.cardColors(
                    containerColor = if (correctAnswer) AccentTeal else if (myAnswer) AccentPink else Color.Transparent
                ), modifier = Modifier.padding(vertical = 2.dp, horizontal = 12.dp)) {
                    Text(
                        modifier = Modifier.padding(horizontal = 5.dp),
                        text = option,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = if (correctAnswer) AccentDarkTeal else if (myAnswer) AccentDarkPink else TextPrimary
                    )
                }
            }
            if(answer != question.answer){
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Answer: ${question.answer}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(8.dp),
                    color = AccentDarkTeal
                )
            }


        } else{
            Card(colors = CardDefaults.cardColors(
                containerColor =  if(answer == question.answer) AccentTeal else AccentPink
            ), modifier = Modifier.padding(vertical = 2.dp, horizontal = 12.dp)) {
                Text(
                    modifier = Modifier.padding(horizontal = 5.dp),
                    text = answer,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = if(answer == question.answer) AccentDarkTeal else AccentDarkPink
                )
            }
            if(answer != question.answer){
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Answer: ${question.answer}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(8.dp),
                    color = AccentDarkTeal
                )
            }

        }
        Spacer(Modifier.height(4.dp))
    }
}