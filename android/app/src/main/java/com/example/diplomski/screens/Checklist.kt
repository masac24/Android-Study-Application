package com.example.diplomski.screens

import android.text.Layout
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.diplomski.database.tables.User
import com.example.diplomski.elements.Header
import com.example.diplomski.ui.theme.AccentDarkTeal
import com.example.diplomski.ui.theme.AccentTeal
import com.example.diplomski.ui.theme.BackgroundColor
import com.example.diplomski.ui.theme.PrimaryColor
import com.example.diplomski.ui.theme.SurfaceColor
import com.example.diplomski.ui.theme.TextPrimary
import com.example.diplomski.viewmodels.ChecklistViewModel

@Composable
fun Checklist(
    viewModel: ChecklistViewModel,
    loggedUser: User?,
    profile: () -> Unit,
    notifications: () -> Unit,
    count: Int
) {

    LaunchedEffect(loggedUser) {
        if (loggedUser != null)
            viewModel.getItems(loggedUser.id)
    }

    val items by viewModel.items.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    if(showDialog){
        Dialog(
            onDismissRequest = {showDialog = false}
        ){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(SurfaceColor, shape = RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Text("Insert task")
                OutlinedTextField(
                    value = viewModel.taskState.text,
                    onValueChange = {viewModel.setText(it)},
                    label = {Text("Text")}
                )
                Button(onClick = {
                    showDialog = false
                    viewModel.insert()
                }) { Text("Confirm")}
                Button(onClick = {showDialog = false}) {
                    Text("Exit")
                }
            }
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight().weight(6f)) {

            Header("Checklist", profile, notifications, count)

            Spacer(Modifier.height(8.dp))

            LazyColumn(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxHeight()) {
                items(items, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(0.5.dp, Color.LightGray)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = item.done,
                                onCheckedChange = { viewModel.delete(item.id) },
                                colors = CheckboxDefaults.colors(uncheckedColor = PrimaryColor)
                            )
                            Text(item.text,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }

                }
            }
            Spacer(Modifier.height(8.dp))

        }

        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight().weight(1f),
            verticalArrangement = Arrangement.Bottom) {
            Button(
                onClick = {showDialog = true},
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentTeal
                ),
                border = BorderStroke(0.5.dp, AccentDarkTeal.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
                    .fillMaxWidth().padding(12.dp)
            ) {
                Icon(imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = AccentDarkTeal)
                Text("Add new",
                    color = AccentDarkTeal
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }

}