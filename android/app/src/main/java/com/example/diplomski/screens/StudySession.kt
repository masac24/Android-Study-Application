package com.example.diplomski.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomski.database.tables.Session
import com.example.diplomski.database.tables.User
import com.example.diplomski.elements.Header
import com.example.diplomski.elements.SessionDuration
import com.example.diplomski.elements.SubHeader
import com.example.diplomski.retrofit.PreferencesManager
import com.example.diplomski.ui.theme.AccentBlue
import com.example.diplomski.ui.theme.AccentDarkBlue
import com.example.diplomski.ui.theme.AccentDarkPink
import com.example.diplomski.ui.theme.AccentDarkTeal
import com.example.diplomski.ui.theme.AccentPink
import com.example.diplomski.ui.theme.AccentTeal
import com.example.diplomski.ui.theme.BackgroundColor
import com.example.diplomski.ui.theme.PrimaryColor
import com.example.diplomski.ui.theme.SurfaceColor
import com.example.diplomski.ui.theme.TextPrimary
import com.example.diplomski.ui.theme.TextSecondary
import com.example.diplomski.viewmodels.SessionViewModel
import java.util.Calendar
import kotlin.math.log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudySession(viewModel : SessionViewModel,
                 loggedUser: User?,
                 goToSession: () -> Unit,
                 profile: () -> Unit,
                 notifications: () -> Unit,
                 count: Int
) {

    LaunchedEffect(loggedUser) {
        if(loggedUser != null)viewModel.getSessions(loggedUser.id)
    }
    var showWindow by remember { mutableStateOf(false) }
    if (showWindow) {
        window(
            {showWindow = it}, {viewModel.setDuration(it)}, goToSession

        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val sessions by viewModel.sessions.collectAsState()

        Header("Study Session", profile, notifications, count)

        Card(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(0.5.dp, TextPrimary.copy(alpha = 0.4f))
        ) {
            Row(Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxWidth().weight(1f)) {
                    StreakLook("CURRENT STREAK", viewModel.uiStateSession.streak)
                }
                Column(horizontalAlignment = Alignment.End,
                    modifier = Modifier.fillMaxWidth().weight(1f)) {
                    StreakLook("BEST STREAK", viewModel.uiStateSession.maxStreak)
                }
            }
            Spacer(Modifier.height(8.dp))

            Text("Keep it up!",
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 12.dp))

            HorizontalDivider(
                modifier = Modifier.padding(12.dp),
                thickness = 0.8.dp,
                color = TextPrimary.copy(alpha = 0.1f)
            )
            val dailyGoal = PreferencesManager.getDailyGoal()
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                Column(horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Text("Daily goal", fontFamily = FontFamily.Monospace) }
                Column(horizontalAlignment = Alignment.End,
                    modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Text("${viewModel.uiStateSession.doneToday}/${dailyGoal} done", fontFamily = FontFamily.Monospace) }
            }

            LinearProgressIndicator(
                progress = {viewModel.uiStateSession.doneToday / dailyGoal.toFloat() },
                color = PrimaryColor,
                trackColor = PrimaryColor.copy(alpha = 0.1f),
                modifier = Modifier.padding(12.dp)
            )

            Spacer(Modifier.height(8.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp).clickable(
                onClick = {showWindow = true
                }
            ),
            colors = CardDefaults.cardColors(containerColor = AccentTeal)
        ) {
            Row(Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Text(
                        modifier = Modifier.padding(top = 25.dp, start = 12.dp),
                        text = "Start session",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = AccentDarkTeal
                    )
                    Text(
                        modifier = Modifier.padding(bottom = 25.dp, start = 12.dp),
                        text = "Set duration",
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = AccentDarkTeal
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow,
                        null,
                        modifier = Modifier.size(90.dp).padding(8.dp),
                        tint = AccentDarkTeal)
                }
            }
        }


        SubHeader("SESSION HISTORY")

        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
            items(sessions, key = { it.id }) { s ->
                SessionCard(s)
            }
        }

    }
}

@Composable
fun StreakLook(text: String, number: Number) {

    Text(
        text = text,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        fontFamily = FontFamily.Monospace,
        color = TextSecondary,
        modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
    )
    Text(
        text = "$number day" + if (number != 1) "s" else "",
        fontWeight = FontWeight.Bold,
        fontSize = 25.sp,
        fontFamily = FontFamily.Monospace,
        color = TextPrimary,
        modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
        )

}

@Composable
fun SessionCard(
    session: Session
){
    val datetime = Calendar.getInstance().apply { timeInMillis = session.datetime }
    val date = "" + datetime.get(Calendar.DAY_OF_MONTH) + "/" + (datetime[2] + 1) + "/" + datetime[1]
    val time = "" + datetime.get(Calendar.HOUR_OF_DAY) + ":" +
            String.format("%02d", datetime.get(Calendar.MINUTE))
    val endCalendar = Calendar.getInstance().apply {
        timeInMillis = session.datetime + session.time * 60 * 1000
    }
    val time2 = "" + endCalendar.get(Calendar.HOUR_OF_DAY) + ":" +
            String.format("%02d", endCalendar.get(Calendar.MINUTE))
    Card(
        Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth().weight(1f)) {
                Text(
                    text = date,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 12.dp, top = 8.dp)
                )
                Text(
                    text = "$time - $time2",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp, vertical = 16.dp)
            ) {
                Card(colors = CardDefaults.cardColors(
                    containerColor = if(session.completed) AccentTeal else AccentPink)
                    , shape = RoundedCornerShape(36.dp)
                )
                {
                    Text("${session.time}min", modifier = Modifier.padding(8.dp),
                        color = if(session.completed) AccentDarkTeal else AccentDarkPink)
                }
                Row() {
                    Text(
                        text = if(session.completed) "Completed" else "Cancelled",
                        color = if(session.completed) AccentDarkTeal else AccentDarkPink
                    )
                    Icon(imageVector = if (session.completed) Icons.Default.Done else Icons.Default.Cancel, null,
                        tint = if(session.completed) AccentDarkTeal else AccentDarkPink
                    )
                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun window(
    setShow: (b: Boolean) -> Unit,
    setDuration: (d: Int) -> Unit,
    goToSession: () -> Unit
){
    BasicAlertDialog(
        onDismissRequest = {setShow(false)}
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(SurfaceColor, shape = RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Text("Set duration")
            SessionDuration(setDuration)
            Button(onClick = {
                setShow(false)
                goToSession()
            }) { Text("Start session") }
            Button(onClick = {setShow(false)}) { Text("Exit")}
        }
    }
}