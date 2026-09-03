package com.example.diplomski.screens

import android.annotation.SuppressLint
import android.widget.Space
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomski.viewmodels.UserViewModel
import com.example.diplomski.database.tables.User
import com.example.diplomski.elements.Header
import com.example.diplomski.elements.SubHeader
import com.example.diplomski.ui.theme.AccentAmber
import com.example.diplomski.ui.theme.AccentBlue
import com.example.diplomski.ui.theme.AccentDarkAmber
import com.example.diplomski.ui.theme.AccentDarkBlue
import com.example.diplomski.ui.theme.AccentDarkPink
import com.example.diplomski.ui.theme.AccentDarkTeal
import com.example.diplomski.ui.theme.AccentPink
import com.example.diplomski.ui.theme.AccentTeal
import com.example.diplomski.ui.theme.BackgroundColor
import com.example.diplomski.ui.theme.PrimaryColor
import com.example.diplomski.ui.theme.TextPrimary
import com.example.diplomski.ui.theme.TextSecondary
import com.example.diplomski.viewmodels.QuizViewModel
import com.example.diplomski.viewmodels.SessionViewModel
import java.util.Calendar

@SuppressLint("DefaultLocale")
@Composable
fun Profile(
    viewModel: UserViewModel,
    quizViewModel: QuizViewModel,
    sessionViewModel: SessionViewModel,
    loggedUser: User?,
    backToLogin: () -> Unit,
    goToSettings: () -> Unit
) {
    val scrollState = rememberScrollState()
    if (loggedUser != null) {
        val quizzes = quizViewModel.quizzesWithCount.collectAsState()
        val scores = quizViewModel.userScores.collectAsState()
        val sessions = sessionViewModel.sessions.collectAsState()
        LaunchedEffect(loggedUser.id) {
            quizViewModel.getUserQuizes(loggedUser.username, loggedUser.id)
            quizViewModel.getUserScores(loggedUser.id)
        }

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.Bottom) {
                Column(horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Row() {
                        Icon(Icons.Filled.Person, null, tint = PrimaryColor,
                            modifier = Modifier.size(40.dp))
                        Text(loggedUser.username,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End,
                    modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Icon(Icons.Filled.Settings, null,
                        modifier = Modifier.size(40.dp).clickable(
                            onClick = {goToSettings()}
                        ))
                }

            }

            Spacer(Modifier.height(16.dp))

            SubHeader("QUIZ STATS")
            CardLook( "Times taken", scores.value.size,"Avg score", "${quizViewModel.average()}%")

            Spacer(Modifier.height(16.dp))

            SubHeader("AVERAGE SCORE PER QUIZ")

            Card(Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)) {

                quizzes.value.forEach { (quiz, questionCount, scores) ->
                    val avg = scores/questionCount.toFloat()
                    Row(Modifier.fillMaxWidth().padding(8.dp)) {
                        Column(Modifier.fillMaxWidth().weight(1f),
                            horizontalAlignment = Alignment.Start) {
                            Text(quiz.name,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp)
                                )
                        }
                        Column(Modifier.fillMaxWidth().weight(1f),
                            horizontalAlignment = Alignment.End) {
                            Text("$scores/$questionCount",
                                fontWeight = FontWeight.Bold,
                                color = if(avg > 0.8) PrimaryColor else if (avg < 0.5) AccentDarkPink else AccentDarkAmber,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }
                    LinearProgressIndicator(progress = {avg},
                        color = if(avg > 0.8) PrimaryColor else if (avg < 0.5) AccentDarkPink else AccentAmber,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp))
                    Spacer(Modifier.height(8.dp))
                }
            }

            SubHeader("STUDY SESSION STATS")

            CardLook("Avg per day", String.format("%.2f sessions", sessionViewModel.avgSessionsPerDay()),
                "Total time", "${sessionViewModel.totalTime()}min")
            CardLook("Completed", sessions.value.count { it.completed },
                "Longest session", "${sessions.value.filter{it.completed}.maxByOrNull { it.time }?.time ?: 0}min")

            SubHeader("ACTIVITY THIS WEEK")

            Card(Modifier.fillMaxWidth().padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center) {
                    val date = Calendar.getInstance()
                    date.add(Calendar.DAY_OF_YEAR, -6)
                    val week = listOf( "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
                    val totals = sessionViewModel.totalTimePerDay()
                    val max = totals.max()
                    for (i in  0 until 7){
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.padding(1.dp)) {
                            val percent = if(max > 0) totals[i].toFloat()/max else 1f
                            Card(
                                colors = CardDefaults.cardColors(PrimaryColor)
                            ) {
                                Spacer(Modifier.height((50*percent).dp).width(20.dp))
                                Text("  ", Modifier.height((percent).dp))
                            }
                            val day = date.get(Calendar.DAY_OF_WEEK) - 1
                            Text(week[day], modifier = Modifier.padding(1.dp))
                            date.add(Calendar.DAY_OF_YEAR, 1)
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }

            LogoutButton({viewModel.logout()}, backToLogin)


        }
    }
}

@Composable
fun CardLook(header1: String, val1: Any, header2: String, val2: Any){
    Row(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(8.dp).height(90.dp),
            border = BorderStroke(0.5.dp, TextPrimary),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    header1,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Text(
                    "$val1",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(8.dp).height(90.dp),
            border = BorderStroke(0.5.dp, TextPrimary),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    header2,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Text(
                    "$val2",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
fun LogoutButton(
    logout: () -> Unit,
    backToLogin: ()-> Unit
){
    Card(Modifier.fillMaxWidth().padding(8.dp).clickable(
        onClick = {
            logout()
            backToLogin()
        }
    ),
        border = BorderStroke(0.5.dp, AccentDarkPink),
        colors = CardDefaults.cardColors(containerColor = AccentPink)) {
        Row(Modifier.fillMaxWidth().padding(12.dp)) {
            Column( horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) { Text("Log out",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = AccentDarkPink)}
            Column(horizontalAlignment = Alignment.End,
                modifier = Modifier.fillMaxWidth().weight(1f)){
                Icon(Icons.AutoMirrored.Filled.Logout,
                    null, tint = AccentDarkPink)
            }

        }
    }
}