package com.example.diplomski.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomski.ui.theme.AccentDarkPink
import com.example.diplomski.ui.theme.AccentTeal
import com.example.diplomski.ui.theme.PrimaryColor
import com.example.diplomski.ui.theme.SurfaceColor
import com.example.diplomski.ui.theme.TextSecondary
import com.example.diplomski.viewmodels.SessionViewModel

@Composable
fun ActiveSession(id: Int,
                  viewModel : SessionViewModel,
                  backToSession: () -> Unit
) {

    LaunchedEffect(id) {
        viewModel.startSession(id)
    }

    BackHandler(enabled = viewModel.uiStateSession.isRunning) {
        viewModel.onMovementDetected()
    }

    if(viewModel.uiStateSession.showWarning){
        WarningWindow(
            {viewModel.continueSession()},
            {viewModel.cancelSession()}
        )
    }
    if (viewModel.uiStateSession.canceled){
        CancelledWindow(backToSession)
    }
    if (viewModel.uiStateSession.completed){
        FinishedWindow(backToSession)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { viewModel.uiStateSession.progress.toFloat() / (viewModel.uiStateSession.duration * 60f) },
                modifier = Modifier.size(300.dp),
                strokeWidth = 8.dp,
                color = PrimaryColor,
                trackColor = PrimaryColor.copy(alpha = 0.1f)
            )
            val remaining = viewModel.uiStateSession.duration * 60f - viewModel.uiStateSession.progress
            val hours = (remaining/3600).toInt()
            val minutes = (remaining / 60).toInt()
            val seconds = (remaining % 60).toInt()

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "remaining",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarningWindow(
    continueSession: () -> Unit,
    cancelSession: () -> Unit
){
    BasicAlertDialog(
        onDismissRequest = {}
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(SurfaceColor, shape = RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Text("WARNING",
                color = AccentDarkPink,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 20.sp
            )
            Text("You are interrupting your session! If you continue your session will " +
                    "be cancelled. Stay focused, you can do it!",
                fontSize = 15.sp
            )
            Button(onClick = {
                continueSession()
            }) { Text("Continue")}
            Button(onClick = {
                cancelSession()
            }) { Text("Cancel session") }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CancelledWindow(
    goToSession: () -> Unit
){
    BasicAlertDialog(
        onDismissRequest = {}
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(SurfaceColor, shape = RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Text("SESSION CANCELLED",
                color = AccentDarkPink,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 20.sp
            )
            Text("Your session is cancelled. Try again when you are more concentrated",
                fontSize = 15.sp
            )
            Button(onClick = {
                goToSession()
            }) { Text("OK")}

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinishedWindow(
    goToSession: () -> Unit
){
    BasicAlertDialog(
        onDismissRequest = {}
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(SurfaceColor, shape = RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Text("SESSION COMPLETED!",
                color = PrimaryColor,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 20.sp
            )
            Text("Congrats! Your session is completed. Keep up the work!",
                fontSize = 15.sp
            )
            Button(onClick = {
                goToSession()
            }) { Text("OK")}

        }
    }
}