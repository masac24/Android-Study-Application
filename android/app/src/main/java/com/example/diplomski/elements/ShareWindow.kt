package com.example.diplomski.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.diplomski.ui.theme.AccentDarkPink
import com.example.diplomski.ui.theme.SurfaceColor

@Composable
fun ShareWindow(
    setShow: (b: Boolean) -> Unit,
    name: String,
    changeName: (s: String) -> Unit,
    sendToUser: () -> Unit,
    message: String,
    resetMessage: () -> Unit
){
    Dialog(
        onDismissRequest = {setShow(false)}
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(SurfaceColor, shape = RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Text("Send to: ")
            OutlinedTextField(
                value = name,
                onValueChange = changeName,
                label = {Text("Name")}
            )
            Text(message, color = AccentDarkPink)
            Button(onClick = {
                sendToUser()
                changeName("")
            }) { Text("Send") }
            Button(onClick = {
                setShow(false)
                resetMessage()
            }) { Text("Exit")}
        }
    }
}