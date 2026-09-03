package com.example.diplomski.screens

import android.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomski.database.tables.Question
import com.example.diplomski.elements.Spinner
import com.example.diplomski.database.tables.User
import com.example.diplomski.elements.SubHeader
import com.example.diplomski.ui.theme.AccentBlue
import com.example.diplomski.ui.theme.AccentDarkBlue
import com.example.diplomski.ui.theme.AccentDarkPink
import com.example.diplomski.ui.theme.AccentDarkTeal
import com.example.diplomski.ui.theme.AccentExtremeDarkTeal
import com.example.diplomski.ui.theme.AccentLightSky
import com.example.diplomski.ui.theme.AccentPink
import com.example.diplomski.ui.theme.AccentSky
import com.example.diplomski.ui.theme.AccentTeal
import com.example.diplomski.ui.theme.LighterBackground
import com.example.diplomski.ui.theme.PrimaryColor
import com.example.diplomski.ui.theme.SurfaceColor
import com.example.diplomski.ui.theme.TextSecondary
import com.example.diplomski.viewmodels.QuestionDetails
import com.example.diplomski.viewmodels.QuizViewModel

@Composable
fun CreateATest(
    viewModel : QuizViewModel,
    loggedUser: User?,
    goToQuiz : () -> Unit
) {

    val questions = viewModel.questions
    val start = viewModel.uiStateQuiz.start

    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if(!start) Arrangement.Center else Arrangement.Top,
        modifier = Modifier.fillMaxSize()) {

        if(!start){
            Text(
                "Create a test",
                fontWeight = FontWeight.Bold,
                fontSize = 35.sp,
                modifier = Modifier.padding(16.dp),
                fontFamily = FontFamily.Monospace
            )
            OutlinedTextField(
                value = viewModel.uiStateQuiz.name,
                onValueChange = {viewModel.setName(it)},
                label = {Text("Quiz name")},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = SurfaceColor,
                    unfocusedContainerColor = SurfaceColor,
                    focusedLabelColor = PrimaryColor,
                    unfocusedLabelColor = TextSecondary
                )
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.setStart(true)
                },
                colors = ButtonDefaults.buttonColors(
                        containerColor = AccentTeal
                        ),
                border = BorderStroke(0.5.dp, AccentDarkTeal.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
                    .fillMaxWidth().padding(12.dp)
            ){
                Text("Save name",
                    color = AccentDarkTeal)
            }
        } else{
            Row(horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Create a test",
                    fontWeight = FontWeight.Bold,
                    fontSize = 35.sp,
                    modifier = Modifier.padding(16.dp),
                    fontFamily = FontFamily.Monospace
                )
            }

            TextFields(viewModel)

            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {viewModel.setQuestion()},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentTeal
                    ),
                    border = BorderStroke(0.5.dp, AccentDarkTeal.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().weight(0.5f).padding(start = 12.dp,
                        end = 3.dp, top = 8.dp, bottom = 8.dp)
                ){
                    Text("Add question",
                        color = AccentDarkTeal)
                }

                Button(
                    onClick = {
                        goToQuiz()
                        if(loggedUser != null)
                            viewModel.save(loggedUser.username)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentExtremeDarkTeal
                    ),
                    border = BorderStroke(0.5.dp, AccentDarkTeal.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().weight(0.5f).padding(start = 3.dp,
                        end = 12.dp, top = 8.dp, bottom = 8.dp)
                ){
                    Text("Save quiz",
                        color = AccentDarkTeal
                    )
                }
            }


            Spacer(Modifier.height(16.dp))

            Column(horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth().fillMaxHeight().weight(1f)) {
                SubHeader("QUESTIONS")


                LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                    items(questions, key = { it.question }) { question ->
                        QuestionCard(question, viewModel)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TextFields(viewModel : QuizViewModel){
    val type = listOf("WRITTEN", "MULTIPLE")
    Spacer(Modifier.height(16.dp))
    Spinner(
        value = type[viewModel.uiStateQuiz.index],
        onSelect = { metOption -> viewModel.setIndex(type.indexOfFirst { it == metOption })
            viewModel.setType(type[viewModel.uiStateQuiz.index])},
        options = type
    )
    Spacer(Modifier.height(8.dp))
    QuizTextField(viewModel.uiStateQuiz.questionDetails.question,
        {viewModel.setQuestion(it)}, "Question")
    QuizTextField(viewModel.uiStateQuiz.questionDetails.answer,
        {viewModel.setAnswer(it)}, "Answer")

    if(viewModel.uiStateQuiz.questionDetails.type == "MULTIPLE"){
        QuizTextField(viewModel.uiStateQuiz.questionDetails.opt1,
            {viewModel.setOpt1(it)}, "Option 1")
        QuizTextField(viewModel.uiStateQuiz.questionDetails.opt2,
            {viewModel.setOpt2(it)}, "Option 2")
        QuizTextField(viewModel.uiStateQuiz.questionDetails.opt3,
            {viewModel.setOpt3(it)},  "Option 3")
    }
}

@Composable
fun QuizTextField(
    value: String,
    onChange: (s:String) -> Unit,
    label : String
){
    OutlinedTextField(
        value = value ,
        onValueChange = onChange,
        label = {Text(label)},
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryColor,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = SurfaceColor,
            unfocusedContainerColor = SurfaceColor,
            focusedLabelColor = PrimaryColor,
            unfocusedLabelColor = TextSecondary
        ),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
    )
}

@Composable
fun QuestionCard(question: QuestionDetails,
                 viewModel: QuizViewModel){
    Card(
        Modifier.padding(8.dp).fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AccentTeal
        )
    ) {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center) {
            Column(horizontalAlignment = Alignment.Start, modifier =
                Modifier.fillMaxWidth().weight(1f).padding(12.dp)) {
                Text("Question: ${question.question}",
                    fontWeight = FontWeight.Bold, color = AccentDarkTeal,
                    fontSize = 15.sp)
                Text("Answer: ${question.answer}",
                    color = AccentDarkTeal
                )
            }
            Row(horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().weight(1f).fillMaxHeight().padding(12.dp)) {

                Card(modifier = Modifier.padding(horizontal = 4.dp).clickable(
                    onClick = {viewModel.loadQuestion(question)
                        viewModel.deleteQuestion()}
                ),
                    colors = CardDefaults.cardColors(containerColor = AccentPink)) {
                    Icon(Icons.Default.Delete, null,
                        tint = AccentDarkPink, modifier = Modifier.padding(8.dp))
                }
                Card(modifier = Modifier.padding(horizontal = 4.dp).clickable(
                    onClick = {viewModel.loadQuestion(question)
                        viewModel.removeFromList()}
                ),
                    colors = CardDefaults.cardColors(containerColor = AccentPink)) {
                    Icon(Icons.Default.Edit, null,
                        tint = AccentDarkPink, modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}