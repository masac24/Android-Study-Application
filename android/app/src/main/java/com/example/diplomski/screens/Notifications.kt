package com.example.diplomski.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomski.CameraPhotoSaver
import com.example.diplomski.database.tables.Message
import com.example.diplomski.database.tables.User
import com.example.diplomski.elements.Header
import com.example.diplomski.retrofit.SyncRepository
import com.example.diplomski.ui.theme.AccentDarkPink
import com.example.diplomski.ui.theme.AccentDarkTeal
import com.example.diplomski.ui.theme.AccentPink
import com.example.diplomski.ui.theme.AccentTeal
import com.example.diplomski.ui.theme.PrimaryColor
import com.example.diplomski.ui.theme.TextSecondary
import com.example.diplomski.viewmodels.MessageInfo
import com.example.diplomski.viewmodels.MessageViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Notifications(
    loggedUser: User?,
    viewModel: MessageViewModel
) {

    LaunchedEffect(loggedUser) {
        if(loggedUser != null) {
            viewModel.getMessages(loggedUser)
        }
    }
    val messages by viewModel.messages.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Notifications",
                fontWeight = FontWeight.Bold,
                fontSize = 35.sp,
                modifier = Modifier.padding(16.dp),
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
        Spacer(Modifier.height(12.dp))

        if(!messages.isEmpty()) {
            LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                items(messages, key = { it.message.id }) { message ->
                    if (loggedUser != null) {
                        MessageCard(
                            message, { viewModel.deny(it) },
                            { i, u -> viewModel.accept(i, u) },
                            loggedUser,
                            { i, u -> viewModel.join(i, u) },
                            { n, u -> viewModel.save(n, u) }
                        )
                    }
                }
            }
        }else {
            Column(
                Modifier.fillMaxWidth().fillMaxHeight().weight(0.8f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                androidx.compose.material.Text(
                    "You don't have any messages", fontSize = 15.sp, fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
        }
    }

}

@Composable
fun MessageCard(
    m: MessageInfo,
    deny:(id: Int) -> Unit,
    accept: (i: MessageInfo, userId: String) -> Unit,
    user: User,
    join: (i: MessageInfo, u: Int) -> Unit,
    save: (i: MessageInfo, userId: Int) -> Unit,
){

    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)) {
            if(m.message.quizId != null)
                Text("${m.message.sender} shared quiz ${m.quiz?.name} with you")
            if(m.message.groupId != null)
                Text("${m.message.sender} invited you to join group ${m.group?.name}")
            if(m.message.noteId != null)
                Text("${m.message.sender} shared note ${m.note?.title} with you")
            Row(modifier = Modifier
                .fillMaxWidth()) {
                Button(onClick = {
                    if(m.message.quizId != null) {
                        accept(m, user.username)
                    }
                    else if(m.message.groupId != null) {
                        join(m, user.id)
                    } else{
                        save(m, user.id)
                    }
                    deny(m.message.id)
                    }
                    ,
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)) {
                    Text("Accept",
                        color = AccentDarkTeal
                    )
                }

                Button(onClick = {deny(m.message.id)},
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp)) {
                    Text("Deny",
                        color = AccentDarkPink
                    )
                }
            }
        }
    }
}