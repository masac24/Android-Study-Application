package com.example.diplomski.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.diplomski.database.tables.User
import com.example.diplomski.elements.Header
import com.example.diplomski.retrofit.PreferencesManager
import com.example.diplomski.ui.theme.AccentSky
import com.example.diplomski.ui.theme.PrimaryColor
import com.example.diplomski.ui.theme.SurfaceColor
import com.example.diplomski.viewmodels.UserViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

enum class State{
    SETTINGS,
    CHANGE_PASS,
    CHANGE_USERNAME,
    SET_DAILY_GOAL,
    NUMBER_OF_QUESTIONS,
    DELETE_ACCOUNT
}

@Composable
fun Settings(
    userViewModel: UserViewModel,
    loggedUser: User?,
    backToLogin: () -> Unit
) {
    LaunchedEffect(Unit) {
        userViewModel.setMessage("")
    }
    val scrollState = rememberScrollState()
    var state by remember { mutableStateOf(State.SETTINGS) }
    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)) {
        Row(horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()) {
            Text(
                "Settings",
                fontWeight = FontWeight.Bold,
                fontSize = 35.sp,
                modifier = Modifier.padding(16.dp),
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
        if(state == State.DELETE_ACCOUNT && loggedUser != null) {
            Dialog(onDismissRequest = {state = State.SETTINGS},
                ) {
                Column(Modifier.fillMaxWidth().padding(12.dp).background(SurfaceColor, shape = RoundedCornerShape(16.dp)),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Are you sure you want to delete your account?",
                        modifier = Modifier.padding(12.dp))
                    Button(onClick = {userViewModel.delete(loggedUser)
                                     backToLogin()},
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)) {
                        Text("Confirm")
                    }
                    Button(onClick = {state = State.SETTINGS}) {
                        Text("Exit")
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        when (state) {
            State.SETTINGS, State.DELETE_ACCOUNT -> {
                CardLook("Change password", onClick = {state = State.CHANGE_PASS})
                CardLook("Change username", onClick = {state = State.CHANGE_USERNAME})
                CardLook("Set daily goal", onClick = {state = State.SET_DAILY_GOAL})
                CardLook("AI quiz: set number of questions", onClick = {state = State.NUMBER_OF_QUESTIONS})
                CardLook("Delete account", onClick = {state = State.DELETE_ACCOUNT})
                LogoutButton({userViewModel.logout()}, backToLogin)
            }
            State.CHANGE_PASS -> {
                if(loggedUser == null) return
                changeCredential("Change password",
                    userViewModel.uiState.userDetails.password,
                    {userViewModel.setPassword(it)},
                    "password", {u, p -> userViewModel.changePassword(u, p)},
                    loggedUser, true, {userViewModel.setMessage(it)},
                    userViewModel.uiState.message
                    )
            }
            State.CHANGE_USERNAME-> {
                if(loggedUser == null) return
                changeCredential("Change username",
                    userViewModel.uiState.userDetails.username,
                    {userViewModel.setName(it)},
                    "username", {u, p -> userViewModel.changeUsername(u, p)},
                    loggedUser, false,{userViewModel.setMessage(it)},
                    userViewModel.uiState.message
                )
            }
            State.SET_DAILY_GOAL -> {
                var number by rememberSaveable { mutableStateOf("") }
                changeNumber("Set daily goal", number, {number = it},
                    userViewModel.uiState.message,
                    {PreferencesManager.saveDailyGoal(it)},
                    {userViewModel.setMessage(it)}
                    )
            }
            State.NUMBER_OF_QUESTIONS -> {
                var number by rememberSaveable { mutableStateOf("") }
                changeNumber("Set number of questions", number, {number = it},
                    userViewModel.uiState.message,
                    {PreferencesManager.saveNumberOfQuestions(it)},
                    {userViewModel.setMessage(it)}
                )
            }
        }


    }
}

@Composable
fun CardLook(label: String, onClick: () -> Unit){
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 3.dp, horizontal = 12.dp)
        .clickable(
            onClick = onClick
        ),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)) {
        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp)) {
            Text(label,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp)
        }
    }
}

@Composable
fun changeCredential(
    label : String,
    credential: String,
    changeCredential: (s: String) -> Unit,
    label2: String,
    update: (u: User, p: String) -> Unit,
    user: User,
    pass: Boolean,
    setMessage: (s: String) -> Unit,
    message: String
) {
    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label,
            color = PrimaryColor,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp
        )
        OutlinedTextField(
            value = credential,
            onValueChange = {changeCredential(it)},
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = PrimaryColor,
                unfocusedLabelColor = PrimaryColor,
                cursorColor = PrimaryColor,
                focusedContainerColor = SurfaceColor
            ),
            visualTransformation = if (pass) PasswordVisualTransformation()  else VisualTransformation.None,
            label = {Text(text = label2)},
            modifier = Modifier.padding(8.dp)
        )
        var password by rememberSaveable { mutableStateOf("") }
        if(pass) {
            OutlinedTextField(
                value = password,
                onValueChange = {password = it},
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = PrimaryColor,
                    unfocusedLabelColor = PrimaryColor,
                    cursorColor = PrimaryColor,
                    focusedContainerColor = SurfaceColor
                ),
                visualTransformation = PasswordVisualTransformation(),
                label = {Text(text = label2)},
                modifier = Modifier.padding(8.dp)
            )
        }
        Text(message)
        Button(
            onClick = {
                if(pass && (credential != password)) {
                    setMessage("Password and confirmation are not the same")
                }else{
                    update(user, credential)
                    setMessage("Successfully changed!")
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentSky,
                contentColor = Color.White
            ),
            enabled = credential.isNotBlank(),
            modifier = Modifier.padding(8.dp)

        ) {
            Text("Save")
        }
    }
}

@Composable
fun changeNumber(label: String,
                 number: String,
                 changeNumber: (s:String) -> Unit,
                 message: String,
                 update: (n: Int) -> Unit,
                 setMessage: (s: String) -> Unit
){
    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label,
            color = PrimaryColor,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp
        )
        OutlinedTextField(
            value = number,
            onValueChange = {changeNumber(it)},
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = PrimaryColor,
                unfocusedLabelColor = PrimaryColor,
                cursorColor = PrimaryColor,
                focusedContainerColor = SurfaceColor
            ),
            modifier = Modifier.padding(8.dp)
        )
        Text(message)
        Button(
            onClick = {
                try{
                    update(number.toInt())
                    setMessage("Successfully changed!")
                }catch (e: Exception){
                    setMessage("You must enter a number")
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentSky,
                contentColor = Color.White
            ),
            enabled = number.isNotBlank(),
            modifier = Modifier.padding(8.dp)

        ) {
            Text("Save")
        }
    }
}
