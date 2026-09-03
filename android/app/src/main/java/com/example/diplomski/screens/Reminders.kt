package com.example.diplomski.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.diplomski.database.tables.Reminder
import com.example.diplomski.database.tables.User
import com.example.diplomski.elements.DatePickerDocked
import com.example.diplomski.elements.Header
import com.example.diplomski.elements.MyTimePicker
import com.example.diplomski.elements.SubHeader
import com.example.diplomski.ui.theme.AccentDarkPink
import com.example.diplomski.ui.theme.AccentDarkTeal
import com.example.diplomski.ui.theme.AccentPink
import com.example.diplomski.ui.theme.AccentTeal
import com.example.diplomski.ui.theme.BackgroundColor
import com.example.diplomski.ui.theme.SurfaceColor
import com.example.diplomski.ui.theme.TextPrimary
import com.example.diplomski.ui.theme.TextSecondary
import com.example.diplomski.viewmodels.ReminderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDateTime(timeMillis: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    return sdf.format(Date(timeMillis))
}

@Composable
fun Reminders(
    viewModel: ReminderViewModel,
    loggedUser : User?,
    profile: () -> Unit,
    notifications: () -> Unit,
    count: Int
){

    LaunchedEffect(loggedUser) {
        viewModel.getReminders(loggedUser?.id ?: 0)
    }

    val reminders by viewModel.reminders.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog && loggedUser != null){
        DateTimeWindow(
            {b -> showDialog = b}, {d -> viewModel.setDate(d)},
            viewModel.uiReminderState.name, {n -> viewModel.setName(n)},
            {t -> viewModel.setTime(t)}, loggedUser.id,
            { i -> viewModel.insertReminder(i)}
        )
    }

   Column(Modifier.fillMaxSize()) {
       Column(
           modifier = Modifier.fillMaxWidth().fillMaxHeight().weight(6f),
           horizontalAlignment = Alignment.CenterHorizontally
       ) {
           Header("Reminders", profile, notifications, count)

           LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
               items(reminders, key = { it.id }) { r ->
                   ReminderCard(r, {r -> viewModel.deleteReminder(r)})
               }
           }

       }

       Column(
           modifier = Modifier.fillMaxWidth().fillMaxHeight().weight(1f),
           horizontalAlignment = Alignment.CenterHorizontally,
           verticalArrangement = Arrangement.Bottom
       ) {
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
                   tint = AccentDarkTeal
               )
               Text("Add new",
                   color = AccentDarkTeal
               )
           }

           Spacer(Modifier.height(16.dp))
       }
   }
}

@Composable
fun ReminderCard(r: Reminder,
                 delete: (r: Reminder) -> Unit){
    var showMenu by remember {mutableStateOf(false)}
    Card(
        Modifier.fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .pointerInput(Unit) {
            detectTapGestures(
                onLongPress = { showMenu = true }
            )
        },
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(0.5.dp, Color.LightGray),
        shape = RoundedCornerShape(12.dp),

        ){
        val datetime = formatDateTime(r.time).split(" ")
        Row(Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().weight(1f)
                .padding(12.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center) {
                Text(
                    r.text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    letterSpacing = 2.sp
                )
                Text(
                    datetime[0],
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AccentPink
                    ), modifier = Modifier.padding(12.dp)
                ) {

                    Text(
                        datetime[1],
                        color = AccentDarkPink,
                        fontWeight = FontWeight.Medium,
                        fontSize = 25.sp,
                        modifier = Modifier.padding(8.dp)
                    )


                }
            }
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Delete") },
                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                onClick = {
                    showMenu = false
                    delete(r)
                }
            )
        }
    }
}

@Composable
fun DateTimeWindow(
    setShow : (b: Boolean) -> Unit,
    setDate : (i: Long?) -> Unit,
    name: String,
    setName: (s : String) -> Unit,
    setTime : (t: Long?) -> Unit,
    loggedId : Int,
    insert : (i: Int) -> Unit
)
{
    Dialog(
        onDismissRequest = {setShow(false)}
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(SurfaceColor, shape = RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Text("Set reminder",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                fontFamily = FontFamily.Monospace)

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {n -> setName(n)},
                label = { Text("Reminder name") }
            )

            Spacer(Modifier.height(8.dp))

            DatePickerDocked({d -> setDate(d)})

            Spacer(Modifier.height(8.dp))

            MyTimePicker({
                        t -> setTime(t)
                        setShow(false)
                        insert(loggedId)
                         })

            Button(onClick = {setShow(false)}) {
                Text("Exit")
            }
        }
    }
}

