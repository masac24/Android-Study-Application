package com.example.diplomski.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomski.database.tables.Group
import com.example.diplomski.database.tables.User
import com.example.diplomski.elements.Header
import com.example.diplomski.ui.theme.AccentDarkTeal
import com.example.diplomski.ui.theme.AccentExtremeDarkTeal
import com.example.diplomski.ui.theme.AccentTeal
import com.example.diplomski.ui.theme.SurfaceColor
import com.example.diplomski.ui.theme.TextSecondary
import com.example.diplomski.ui.theme.folderColors
import com.example.diplomski.ui.theme.folderIconColors
import com.example.diplomski.viewmodels.GroupViewModel
import com.example.diplomski.viewmodels.GroupWithMember

@Composable
fun Group(
    viewModel: GroupViewModel,
    loggedUser: User?,
    goToProfile: () -> Unit,
    goToNotifications: () -> Unit,
    messageCount: Int,
    goToGroup: (id: Int) -> Unit
){
    LaunchedEffect(Unit) {
        if(loggedUser != null)viewModel.getGroups(loggedUser.id)
    }

    var showWindow by rememberSaveable { mutableStateOf(false) }
    val groups by viewModel.groups.collectAsState()
    var name by rememberSaveable { mutableStateOf("")}

    if(showWindow && loggedUser != null){
        CreateGroup(
            setShow = {showWindow = it},
            name = name,
            changeName = {name = it},
            createGroup = {n: String, id: String, i: Int -> viewModel.createGroup(n, id, i)},
            loggedUser.username, loggedUser.id
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Header("GROUPS", goToProfile, goToNotifications, messageCount)
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
            itemsIndexed(groups) { index, g ->
                GroupCard(g, index, {goToGroup(it)})
            }
        }
        Button(
            onClick = {
                showWindow = true
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentExtremeDarkTeal
            ),
            border = BorderStroke(0.5.dp, AccentDarkTeal.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(start = 3.dp,
                end = 12.dp, top = 8.dp, bottom = 8.dp)
        ){
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center) {
                Icon(Icons.Default.Add, null, tint = AccentDarkTeal)
                Text("Create new group",
                    color = AccentDarkTeal
                )
            }
        }
    }
}

@Composable
fun GroupCard(
    group: GroupWithMember,
    index: Int,
    goToGroup: (id: Int) -> Unit
){
    Card(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).clickable(
            onClick = {goToGroup(group.group.id)}
        ),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth().weight(0.2f)) {
                Card(
                    modifier = Modifier.padding(12.dp),
                    colors = CardDefaults.cardColors(containerColor = folderColors[index % 4]),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.Group, null,
                        tint = folderIconColors[index % 4], modifier = Modifier.padding(8.dp))
                }
            }

            Column(horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth().weight(0.7f)) {
                Text(group.group.name,
                    fontWeight = FontWeight.Bold, fontSize = 20.sp,
                    modifier = Modifier.padding(horizontal = 2.dp))
                Text("${group.members.size} members ", fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 2.dp), color = TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth().weight(0.1f)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForwardIos, null,
                    tint = TextSecondary, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroup(
    setShow: (b:Boolean) -> Unit,
    name: String,
    changeName: (s: String) -> Unit,
    createGroup: (n: String, id: String, Int) -> Unit,
    username: String,
    id: Int
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
            Text("Enter group name")
            OutlinedTextField(
                value = name,
                onValueChange = changeName,
                label = {Text("Name")}
            )
            Button(onClick = {
                createGroup(name, username, id)
                setShow(false)
                changeName("")
            }) { Text("Confirm") }
            Button(onClick = {setShow(false)}) { Text("Exit")}
        }
    }
}