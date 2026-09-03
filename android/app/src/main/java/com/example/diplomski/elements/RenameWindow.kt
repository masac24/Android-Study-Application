package com.example.diplomski.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.diplomski.ui.theme.SurfaceColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameWindow(
    setShow: (b : Boolean) -> Unit,
    name : String,
    changeName: (n: String) -> Unit,
    update: (n: String, obj : Any) -> Unit,
    obj : Any
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
            Text("Enter new name")
            OutlinedTextField(
                value = name,
                onValueChange = changeName,
                label = {Text("Name")}
            )
            Button(onClick = {
                update(name, obj)
                setShow(false)
                changeName("")
            }) { Text("Confirm") }
            Button(onClick = {setShow(false)}) { Text("Exit")}
        }
    }
}