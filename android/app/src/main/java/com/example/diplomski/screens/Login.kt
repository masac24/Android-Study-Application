package com.example.diplomski.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomski.viewmodels.UserViewModel
import com.example.diplomski.ui.theme.AccentCoral
import com.example.diplomski.ui.theme.AccentSky
import com.example.diplomski.ui.theme.BackgroundColor
import com.example.diplomski.ui.theme.LighterBackground
import com.example.diplomski.ui.theme.PrimaryColor
import com.example.diplomski.ui.theme.PrimaryDark
import com.example.diplomski.ui.theme.SecondaryColor
import com.example.diplomski.ui.theme.SurfaceColor
import com.example.diplomski.ui.theme.TextPrimary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun Login(
    goToRegister : () -> Unit,
    viewModel: UserViewModel,
    goToMain: () -> Unit
    ) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        val coroutineScope : CoroutineScope = rememberCoroutineScope()

        Text(
            text= "Login",
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(8.dp),
            color = PrimaryColor
        )
        OutlinedTextField(
            value = viewModel.uiState.userDetails.username,
            onValueChange = {viewModel.setName(it)},
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = PrimaryColor,
                unfocusedLabelColor = PrimaryColor,
                cursorColor = PrimaryColor,
                focusedContainerColor = SurfaceColor
            ),
            label = {Text(text = "username")},
            modifier = Modifier.padding(8.dp)
        )

        OutlinedTextField(
            value = viewModel.uiState.userDetails.password,
            onValueChange = {viewModel.setPassword(it)},
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = PrimaryColor,
                unfocusedLabelColor = PrimaryColor,
                cursorColor = PrimaryColor,
                focusedContainerColor = SurfaceColor
            ),
            visualTransformation = PasswordVisualTransformation(),
            label = {Text(text = "password")},
            modifier = Modifier.padding(8.dp)
        )
        if(viewModel.uiState.message != "")
            Text(viewModel.uiState.message, color = AccentCoral)

        Button(
            onClick = {
                coroutineScope.launch {
                    val success = viewModel.login()
                    if(success) {
                        viewModel.clear()
                        goToMain()
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentSky,
                contentColor = Color.White
            ),
            enabled = viewModel.uiState.userDetails.username.isNotBlank() &&
                    viewModel.uiState.userDetails.password.isNotBlank(),
            modifier = Modifier.padding(8.dp)

        ) {
            Text("Login")
        }

        Text(
            text = "Don't have an account? Register now!",
            modifier = Modifier.clickable(onClick = goToRegister),
            color = TextPrimary
        )
//        Text(
//            text = "Forgot password? Reset via email",
//            modifier = Modifier.clickable(onClick = {}),
//            color = TextPrimary
//        )
    }
}