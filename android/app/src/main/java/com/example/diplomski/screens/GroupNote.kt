package com.example.diplomski.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsEndWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.ShortText
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShortText
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.diplomski.database.tables.Comment
import com.example.diplomski.database.tables.Note
import com.example.diplomski.database.tables.Quiz
import com.example.diplomski.database.tables.User
import com.example.diplomski.elements.InitialAvatar
import com.example.diplomski.elements.PdfViewer
import com.example.diplomski.ui.theme.AccentBlue
import com.example.diplomski.ui.theme.AccentDarkBlue
import com.example.diplomski.ui.theme.AccentDarkPink
import com.example.diplomski.ui.theme.AccentDarkTeal
import com.example.diplomski.ui.theme.AccentPink
import com.example.diplomski.ui.theme.AccentTeal
import com.example.diplomski.ui.theme.TextPrimary
import com.example.diplomski.ui.theme.TextSecondary
import com.example.diplomski.viewmodels.FolderViewModel
import com.example.diplomski.viewmodels.GroupViewModel
import java.io.File

@Composable
fun GroupNote(
    noteId: Int,
    loggedUser: User?,
    viewModel: GroupViewModel,
    goToGroup: (Int) -> Unit
){
    var fullScreenNote by remember {
        mutableStateOf<Note?>(null)
    }

    if (fullScreenNote != null) {
        FullScreenNoteViewer(
            note = fullScreenNote!!,
            onBack = {
                fullScreenNote = null
            }
        )
        return
    }

    LaunchedEffect(noteId) {
        viewModel.getNote(noteId)
    }
    val note = viewModel.notePreview.value.note
    val files = viewModel.group.collectAsState().value?.notes
    var index = -1
    files?.forEachIndexed { ind, file ->
        if (file.note == note){
            index = ind
        }
    }
    if (files?.isEmpty() == true || index == -1) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val pagerState = rememberPagerState(initialPage = index, pageCount = { files?.size ?: 0 })
    val groupId = viewModel.group.collectAsState().value?.group?.id
    HorizontalPager(state = pagerState) { page ->
        GroupNoteViewer(note = files?.get(page)?.note ?: return@HorizontalPager,
            files[page].comments, viewModel.group.collectAsState().value?.group?.user ?: return@HorizontalPager,
            loggedUser, groupId,
            {t,u,g,n,q -> viewModel.saveComment(t,u,g,n,q)},
            onOpenNote = { selectedNote ->
                fullScreenNote = selectedNote
            },
            goToGroup = goToGroup,
            delete = {i, g-> viewModel.deleteComment(i, g )}
        )
    }
}

@Composable
fun GroupNoteViewer(note: Note,
                    comments: List<Comment>?,
                    author: String,
                    loggedUser: User?,
                    groupId: Int?,
                    saveComment: (text: String, userId: String, groupId: Int, noteId: Int?, quizId:Int?) -> Unit,
                    onOpenNote: (Note) -> Unit,
                    goToGroup: (Int) -> Unit,
                    delete: (Int, Int) -> Unit
){
    Column(modifier = Modifier.fillMaxSize()) {


        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().fillMaxHeight().weight(0.4f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(Icons.Filled.ArrowBackIosNew, null,
                    modifier = Modifier.clickable(onClick = {
                        if(groupId != null)goToGroup(groupId)}))
                Text(note.title, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            }
            if (note.type == "IMAGE") {
                AsyncImage(
                    model = note.path,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.height(300.dp).fillMaxWidth().padding(12.dp)
                        .clickable {
                        onOpenNote(note)
                    }
                )
            } else{
                Card(
                    modifier = Modifier.fillMaxWidth().padding(12.dp).height(300.dp)
                        .clickable {
                        onOpenNote(note)
                    },
                    colors = CardDefaults.cardColors(if(note.type == "PDF")AccentPink
                    else AccentBlue
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(15.dp).fillMaxSize()
                    ) {
                        Icon(
                            if(note.type == "PDF")Icons.Default.PictureAsPdf else
                                Icons.AutoMirrored.Filled.Notes, null,
                            tint = if(note.type == "PDF") AccentDarkPink else AccentDarkBlue
                        )
                        Text("Tap to open", color = if(note.type == "PDF")AccentDarkPink else
                        AccentDarkBlue)
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(12.dp),
                    thickness = 0.8.dp,
                    color = TextPrimary.copy(alpha = 0.1f)
                )
            }
        }
        if (comments != null && !comments.isEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp).fillMaxHeight().weight(0.5f)
            ) {

                items(comments, key = { it.id }) { comment ->
                    CommentCard(comment, comment.user, author, loggedUser,
                        delete)

                }
            }
        }
        else{
            Column(Modifier.fillMaxWidth().fillMaxHeight().weight(0.5f),
                verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                androidx.compose.material.Text(
                    "No comments yet",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp).fillMaxHeight().weight(0.1f)
        ) {
            LeaveComment(
                loggedUser = loggedUser,
                note = note,
                quiz = null,
                saveComment = saveComment,
                groupId = groupId
            )
        }
    }
}

@Composable
fun CommentCard(
    comment: Comment,
    username: String,
    author: String,
    loggedUser: User?,
    deleteComment: (i: Int, g: Int) -> Unit
){
    var showMenu by rememberSaveable { mutableStateOf(false) }
    val allowed = if(loggedUser != null )loggedUser.username == comment.user else false
    val time = formatDateTime(comment.createdAt).split(" ")[1]
    Row(verticalAlignment = Alignment.Top) {
        InitialAvatar(username)
        Spacer(Modifier.width(12.dp))
        Card(
            colors = CardDefaults.cardColors(
                if(username == author) AccentTeal else Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(horizontal = 12.dp).pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { showMenu = true }
                )
            }
        ) {
            Column(modifier = Modifier.fillMaxWidth()){
                Row(modifier =  Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.fillMaxWidth().weight(1f),
                        horizontalAlignment = Alignment.Start) {
                        Text(username, color = TextSecondary,
                            modifier = Modifier.padding(start = 8.dp, top = 8.dp)) }
                    Column(Modifier.fillMaxWidth().weight(1f),
                        horizontalAlignment = Alignment.End) {
                        Text(time, color = TextSecondary,
                            modifier = Modifier.padding(end = 8.dp, top = 8.dp)) }
                }
                Text(comment.text,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp))
            }
            DropdownMenu(
                expanded = showMenu && allowed,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Delete comment") },
                    leadingIcon = {
                        androidx.compose.material3.Icon(
                            Icons.Filled.Delete,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        showMenu = false
                        deleteComment(comment.id, comment.groupId)
                    }
                )
            }

        }
    }

}

@Composable
fun FullScreenNoteViewer(
    note: Note,
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Filled.ArrowBackIosNew,
                contentDescription = "Back",
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        onBack()
                    }
            )

            Spacer(Modifier.width(12.dp))

            Text(
                text = note.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            NoteViewer(note)
        }
    }
}

@Composable
fun LeaveComment(
    loggedUser: User?,
    groupId: Int?,
    note: Note?,
    quiz: Quiz?,
    saveComment: (String, String, Int, Int?, Int?) -> Unit
){
    Row(modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically) {
        if(loggedUser != null)InitialAvatar(loggedUser.username)
        Spacer(Modifier.width(12.dp))
        Card(
            colors = CardDefaults.cardColors(Color.White),
            border = BorderStroke(0.5.dp, Color.LightGray),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.width(350.dp)
        ) {
            var comm by rememberSaveable { mutableStateOf("") }
            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = comm,
                    onValueChange = {comm = it},
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.width(300.dp)
                )
                Spacer(Modifier.width(20.dp))
                Icon(Icons.Default.Send, null,
                    tint = AccentDarkTeal, modifier = Modifier.clickable(
                        onClick = {
                            if(loggedUser != null && groupId != null)
                                saveComment(comm, loggedUser.username,
                                    groupId, note?.id, quiz?.id)
                            comm = ""
                        }
                    ))
            }
        }

    }
}