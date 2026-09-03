package com.example.diplomski.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.diplomski.CameraPhotoSaver
import com.example.diplomski.database.tables.Folder
import com.example.diplomski.database.tables.Group
import com.example.diplomski.database.tables.Note
import com.example.diplomski.database.tables.Quiz
import com.example.diplomski.database.tables.User
import com.example.diplomski.elements.FilterPill
import com.example.diplomski.elements.Header
import com.example.diplomski.elements.InitialAvatar
import com.example.diplomski.elements.InvitePill
import com.example.diplomski.elements.MyRadioButton
import com.example.diplomski.elements.Pill
import com.example.diplomski.elements.RenameWindow
import com.example.diplomski.elements.ShareWindow
import com.example.diplomski.elements.SubHeader
import com.example.diplomski.ui.theme.AccentDarkBlue
import com.example.diplomski.ui.theme.AccentDarkTeal
import com.example.diplomski.ui.theme.AccentTeal
import com.example.diplomski.ui.theme.SurfaceColor
import com.example.diplomski.ui.theme.TextSecondary
import com.example.diplomski.ui.theme.folderColors
import com.example.diplomski.ui.theme.folderIconColors
import com.example.diplomski.viewmodels.FolderState
import com.example.diplomski.viewmodels.FolderViewModel
import com.example.diplomski.viewmodels.GroupViewModel
import com.example.diplomski.viewmodels.GroupWithMember
import com.example.diplomski.viewmodels.NoteWithUser
import com.example.diplomski.viewmodels.QuizViewModel
import com.example.diplomski.viewmodels.QuizWithUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun GroupView(
    groupId: Int,
    viewModel: GroupViewModel,
    quizViewModel: QuizViewModel,
    folderViewModel: FolderViewModel,
    loggedUser: User?,
    takeTest: (q : Int) -> Unit,
    groupNote: (i: Int) -> Unit,
    quizComments: (i: Int) -> Unit,
    goToGroups: () -> Unit
){
    LaunchedEffect(groupId) {
        if(loggedUser != null){
            viewModel.getGroup(groupId)
            folderViewModel.getUserFolders(loggedUser.id)
        }
    }

    val groupState by viewModel.group.collectAsState()
    val group = groupState
    val folders by folderViewModel.folders.collectAsState(listOf())
    var shareWindow by rememberSaveable { mutableStateOf(false) }
    var username by rememberSaveable { mutableStateOf("") }
    val mess = viewModel.message.value

    if(shareWindow){
        ShareWindow(
            { shareWindow = it },
            username,
            { username = it },
            { if(loggedUser != null) { viewModel.inviteFriend(username, loggedUser.username) } },
            mess,
            { viewModel.resetMessage() }
        )
    }

    if(group != null) {
        var selected by remember { mutableStateOf("All") }
        val tabs = listOf("All", "Notes", "Quizzes", "Members")

        var expanded by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val imageFile = remember { CameraPhotoSaver.createFileWithoutName(context.filesDir) }
        val imageUri = remember {
            FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
        }

        var showDialog by remember { mutableStateOf(false) }
        var pendingBitmap by remember { mutableStateOf<Bitmap?>(null) }
        var name by rememberSaveable { mutableStateOf("") }

        val launcherFull = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                val imageBitmap = CameraPhotoSaver.loadBitmapFromUri(context, imageUri)
                pendingBitmap = imageBitmap?.asAndroidBitmap()
                showDialog = true
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Enter name for this photo") },
                text = {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog = false
                        val path = CameraPhotoSaver.savePictureToFile(context, pendingBitmap, "image/jpeg", name)
                        if(loggedUser != null) viewModel.uploadNote(path, name, group.group, loggedUser)
                    }) { Text("Confirm") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                        val path = CameraPhotoSaver.savePictureToFile(context, pendingBitmap, "image/jpeg", "")
                        if(loggedUser != null) viewModel.uploadNote(path, "", group.group, loggedUser)
                    }) { Text("Skip") }
                }
            )
        }

        val launcherFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                val fileName = CameraPhotoSaver.getFileName(context, uri) ?: "file_${System.currentTimeMillis()}"
                val path = CameraPhotoSaver.copyFileToInternalStorage(context, uri, fileName)
                viewModel.uploadNote(
                    path,
                    fileName,
                    viewModel.group.value?.group ?: return@let,
                    loggedUser ?: return@let
                )
            }
        }

        // --- GLAVNA KOREKCIJA LAYOUT-A ---
        // Zamenjena je Column(verticalScroll) sa LazyColumn bez unutrašnjih LazyColumn/LazyGrid komponente
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Header
            item {
                GroupHeader(
                    group, loggedUser,
                    { i, g -> viewModel.renameGroup(i, g as Group) },
                    { u, g -> viewModel.leaveGroup(u, g) },
                    goToGroups,
                    { viewModel.deleteGroup(it) }
                )
                Spacer(Modifier.height(8.dp))
            }

            // Tabovi / Filteri
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    tabs.forEach { tab ->
                        FilterPill(
                            text = tab,
                            selected = selected == tab,
                            onClick = { selected = tab }
                        )
                    }
                }
            }

            // BELEŠKE (SHARED NOTES)
            if (selected in listOf("All", "Notes")) {
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.Start
                        ) {
                            SubHeader("SHARED NOTES")
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Card(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .clickable { expanded = true },
                                colors = CardDefaults.cardColors(AccentTeal),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.5.dp, AccentDarkTeal.copy(alpha = 0.4f))
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Upload, null, tint = AccentDarkTeal)
                                    Text(
                                        "share note",
                                        color = AccentDarkTeal,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Load file") },
                                    leadingIcon = { androidx.compose.material3.Icon(Icons.Default.FileUpload, null) },
                                    onClick = {
                                        expanded = false
                                        launcherFile.launch(arrayOf("application/pdf", "image/*"))
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Take picture") },
                                    leadingIcon = { androidx.compose.material3.Icon(Icons.Default.CameraAlt, null) },
                                    onClick = {
                                        expanded = false
                                        launcherFull.launch(imageUri)
                                    }
                                )
                            }
                        }
                    }
                }

                // Umesto LazyVerticalGrid, prikazujemo grid u parovima unutar glavne LazyColumn
                val notes = group.notes
                val chunkedNotes = notes.chunked(2)

                items(chunkedNotes) { rowNotes ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (file in rowNotes) {
                            Box(modifier = Modifier.weight(1f)) {
                                NoteCard(
                                    onClick = groupNote,
                                    note = file,
                                    delete = { i, g, f -> viewModel.deleteFile(i, g, f) },
                                    groupId = groupId,
                                    loggedUser = loggedUser,
                                    rename = { i, n -> viewModel.renameNote(i, n as Note) },
                                    save = { n, i -> viewModel.saveNote(n, i) },
                                    folders = folders
                                )
                            }
                        }
                        if (rowNotes.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // KVIZOVI (SHARED QUIZZES)
            if (selected in listOf("All", "Quizzes")) {
                item {
                    SubHeader("SHARED QUIZZES")
                }

                val quizzes = group.quizzes
                items(quizzes, key = { it.quiz.id }) { quiz ->
                    QuizCard(
                        quiz = quiz,
                        takeTest = takeTest,
                        quizViewModel = quizViewModel,
                        loggedUser = loggedUser,
                        delete = { i, q -> viewModel.deleteQuiz(i, q) },
                        groupId = groupId,
                        comments = quizComments
                    )
                }
            }

            // ČLANOVI (MEMBERS)
            if (selected in listOf("All", "Members")) {
                item {
                    SubHeader("MEMBERS")
                }

                item {
                    val members = group.members
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    ) {
                        items(members, key = { it }) { member ->
                            Pill(member)
                        }
                        item {
                            InvitePill("Invite friends", onClick = { shareWindow = true })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupHeader(
    group : GroupWithMember,
    loggedUser: User?,
    rename: (String, Any) -> Unit,
    leave: (Int, Int) -> Unit,
    goToGroups: () -> Unit,
    delete: (Int) -> Unit
){
    val index = group.group.id % 4
    var showMenu by rememberSaveable { mutableStateOf(false) }
    var showRename by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    val allowed = if(loggedUser != null) group.group.user == loggedUser.username else false

    if (showRename){
        RenameWindow(
            setShow = {showRename = it},
            name = name,
            changeName = {name = it},
            update = {i,g -> rename(i, g)},
            obj = group.group
        )
    }

    Row(modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.2f)) {
            Card(
                modifier = Modifier.padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = folderColors[index]),
                shape = RoundedCornerShape(12.dp)
            ) {
                androidx.compose.material3.Icon(
                    Icons.Outlined.Group, null,
                    tint = folderIconColors[index], modifier = Modifier
                        .padding(8.dp)
                        .size(50.dp)
                )
            }
        }

        Column(horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f)) {
            Text(group.group.name,
                fontWeight = FontWeight.Bold, fontSize = 30.sp,
                modifier = Modifier.padding(horizontal = 2.dp))
            Text("${group.members.size} members ", fontSize = 15.sp,
                modifier = Modifier.padding(horizontal = 2.dp), color = TextSecondary
            )
        }
        Column(horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.1f)
                .clickable(onClick = {
                    showMenu = true
                })

        ) {
            androidx.compose.material3.Icon(
                Icons.Filled.Settings, null,
                tint = TextSecondary, modifier = Modifier.padding(8.dp)
            )
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                if(allowed)
                    DropdownMenuItem(
                        text = { Text("Delete group") },
                        leadingIcon = {
                            androidx.compose.material3.Icon(
                                Icons.Filled.Delete,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            showMenu = false
                            delete(group.group.id)
                        }
                    )
                DropdownMenuItem(
                    text = { Text("Rename group") },
                    leadingIcon = {
                        androidx.compose.material3.Icon(
                            Icons.Filled.Edit,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        showMenu = false
                        showRename = true
                    }
                )
                DropdownMenuItem(
                    text = { Text("Leave group") },
                    leadingIcon = {
                        androidx.compose.material3.Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        showMenu = false
                        if(loggedUser != null)leave(loggedUser.id, group.group.id)
                        goToGroups()
                    }
                )
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState", "CoroutineCreationDuringComposition")
@Composable
fun QuizCard(
    quiz: QuizWithUser,
    takeTest: (q : Int) -> Unit,
    quizViewModel: QuizViewModel,
    loggedUser: User?,
    delete: (Int, Int) -> Unit,
    groupId: Int,
    comments: (Int) -> Unit
){
    val coroutineScope : CoroutineScope = rememberCoroutineScope()
    var score by mutableStateOf(0)
    coroutineScope.launch {
        score = quizViewModel.getMaxScore(loggedUser?.id ?: 0, quiz.quiz.id)
    }
    var showMenu by rememberSaveable { mutableStateOf(false) }
    val allowed = if(loggedUser != null )loggedUser.username == quiz.quiz.user else false
    Card(
        onClick = {comments(quiz.quiz.id)},
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.65f).pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { showMenu = true }
                        )
                    },
                horizontalAlignment = Alignment.Start
            ) {
                Text(quiz.quiz.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InitialAvatar(quiz.quiz.user)
                    Spacer(Modifier.width(2.dp))
                    Text(quiz.quiz.user + " · " + quiz.questionCount + " questions",
                        fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            Column(modifier = Modifier
                .fillMaxWidth()
                .weight(0.2f),
                horizontalAlignment = Alignment.End) {
                Card(
                    colors = CardDefaults.cardColors(containerColor =
                        if(score != -1) AccentTeal else SurfaceColor
                    )
                ) {
                    Text(if(score != -1)"${score}/${quiz.questionCount}" else "not taken",
                        modifier = Modifier.padding(8.dp),
                        color = if(score != -1) AccentDarkTeal else TextSecondary,
                        fontSize = if(score != -1) 15.sp else 10.sp)
                }
            }
            Column(modifier = Modifier
                .fillMaxWidth()
                .weight(0.15f)
                .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.End) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AccentTeal),
                    modifier = Modifier.clickable(
                        onClick = {takeTest(quiz.quiz.id)}
                    )
                ) {
                    Icon(Icons.Default.PlayArrow,
                        null, tint = AccentDarkTeal,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(25.dp))
                }
            }
            DropdownMenu(
                expanded = showMenu && allowed,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Delete quiz") },
                    leadingIcon = {
                        androidx.compose.material3.Icon(
                            Icons.Filled.Delete,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        showMenu = false
                        delete(quiz.quiz.id, groupId)
                    }
                )
            }
        }
    }
}

@Composable
fun NoteCard(
    onClick: (id: Int) -> Unit,
    note: NoteWithUser,
    delete: (Int, Int, Int?) -> Unit,
    groupId: Int,
    loggedUser: User?,
    rename: (String, Any) -> Unit,
    save: (Note, Int) -> Unit,
    folders: List<FolderState>
){
    var showMenu by rememberSaveable { mutableStateOf(false) }
    val allowed = if(loggedUser != null )loggedUser.username == note.username else false

    Card(
        onClick = {onClick(note.note.id)},
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, Color(0x26000000)),
    ) {
        Column() {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { showMenu = true }
                        )
                    }
                    .height(70.dp)
                    .background(
                        if (note.note.type == "PDF") Color(0xFFFAECE7)
                        else Color(0xFFE1F5EE)
                    ), contentAlignment = Alignment.Center
            ) {
                if (note.note.type == "IMAGE") {
                    AsyncImage(
                        model = note.note.path,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if(note.note.type == "PDF"){
                    val bitmap by produceState<Bitmap?>(
                        initialValue = null,
                        key1 = note.note.path
                    ) {
                        value = withContext(Dispatchers.IO) {
                            pdfThumbnail(note.note.path)
                        }
                    }

                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }else{
                    androidx.compose.material3.Icon(
                        imageVector = if (note.note.type == "TEXT") Icons.AutoMirrored.Filled.Notes
                        else Icons.Default.Image,
                        contentDescription = null,
                        tint = if (note.note.type == "TEXT") AccentDarkBlue else Color(0xFF0F6E56),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        Column(modifier = Modifier.padding(8.dp)) {
            Text(note.note.title, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(note.note.type, fontSize = 11.sp, color = Color.Gray)
            Row(verticalAlignment = Alignment.CenterVertically) {
                InitialAvatar(note.username)
                Spacer(Modifier.width(2.dp))
                Text(note.username, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
        var showRename by rememberSaveable {mutableStateOf(false)}
        var name by rememberSaveable {mutableStateOf("")}
        if(showRename){
            RenameWindow(
                setShow = {showRename = false},
                name = name,
                changeName = {name = it},
                update = rename,
                obj = note.note
            )
        }
        var showWindow by rememberSaveable {mutableStateOf(false)}
        if(showWindow){
            chooseFolder(
                setShow = {showWindow = false},
                folders = folders,
                save = save,
                note = note.note
            )
        }
        DropdownMenu(
            expanded = showMenu ,
            onDismissRequest = { showMenu = false }
        ) {
            if(allowed) {
                DropdownMenuItem(
                    text = { Text("Delete") },
                    leadingIcon = {
                        androidx.compose.material3.Icon(
                            Icons.Filled.Delete,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        showMenu = false
                        delete(note.note.id, groupId, note.note.folderId)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Rename") },
                    leadingIcon = {
                        androidx.compose.material3.Icon(
                            Icons.Filled.Edit,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        showMenu = false
                        showRename = true
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("Save") },
                leadingIcon = {
                    androidx.compose.material3.Icon(
                        Icons.Filled.Save,
                        contentDescription = null
                    )
                },
                onClick = {
                    showMenu = false
                    showWindow = true
                }
            )
        }
    }
}

@Composable
fun chooseFolder(
    setShow: (Boolean) -> Unit,
    folders:  List<FolderState>,
    save: (Note, Int) -> Unit,
    note: Note
){
    Dialog(
        onDismissRequest = {setShow(false)}
    ) {
        var folder by remember { mutableStateOf(Folder(0, "", 0)) }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(SurfaceColor, shape = RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {

            Text("Select folder")
            LazyColumn(horizontalAlignment = Alignment.Start) {
                items(folders, key = { it.folder.id }) { f ->
                    MyRadioButton(
                        label = f.folder.name,
                        selected = f.folder.name == folder.name,
                        onClick = { folder = f.folder }
                    )
                }
            }
            androidx.compose.material3.Button(
                onClick = {
                    save(note, folder.id)
                    setShow(false)
                }
            ) {
                Text("Confirm")
            }
            androidx.compose.material3.Button(
                onClick = {
                    setShow(false)
                }
            ) {
                Text("Exit")
            }
        }
    }
}