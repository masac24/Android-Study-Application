package com.example.diplomski.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.diplomski.Files
import com.example.diplomski.database.tables.Folder
import com.example.diplomski.database.tables.Group
import com.example.diplomski.database.tables.Note
import com.example.diplomski.database.tables.Quiz
import com.example.diplomski.database.tables.User
import com.example.diplomski.elements.Header
import com.example.diplomski.elements.MyRadioButton
import com.example.diplomski.elements.RenameWindow
import com.example.diplomski.elements.ShareWindow
import com.example.diplomski.elements.SubHeader
import com.example.diplomski.ui.theme.AccentBlue
import com.example.diplomski.ui.theme.AccentCoral
import com.example.diplomski.ui.theme.AccentCoralDeeper
import com.example.diplomski.ui.theme.AccentDarkBlue
import com.example.diplomski.ui.theme.AccentDarkPink
import com.example.diplomski.ui.theme.AccentDarkTeal
import com.example.diplomski.ui.theme.AccentPink
import com.example.diplomski.ui.theme.AccentSky
import com.example.diplomski.ui.theme.AccentTeal
import com.example.diplomski.ui.theme.BackgroundColor
import com.example.diplomski.ui.theme.LighterBackground
import com.example.diplomski.ui.theme.PrimaryColor
import com.example.diplomski.ui.theme.SecondaryColor
import com.example.diplomski.ui.theme.SurfaceColor
import com.example.diplomski.ui.theme.TextPrimary
import com.example.diplomski.ui.theme.TextSecondary
import com.example.diplomski.viewmodels.FolderViewModel
import com.example.diplomski.viewmodels.GroupViewModel
import com.example.diplomski.viewmodels.GroupWithMember
import com.example.diplomski.viewmodels.QuizViewModel
import com.google.android.datatransport.runtime.scheduling.jobscheduling.Uploader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableState", "CoroutineCreationDuringComposition")
@Composable
fun Quiz(
    viewModel: QuizViewModel,
    folderViewModel: FolderViewModel,
    groupViewModel: GroupViewModel,
    loggedUser: User?,
    createTest: () -> Unit,
    takeTest: (q : Int) -> Unit,
    profile: () -> Unit,
    notifications: () -> Unit,
    count: Int
) {

    LaunchedEffect(loggedUser) {
        if(loggedUser != null) {
            viewModel.getUserQuizes(loggedUser.username, loggedUser.id)
            folderViewModel.getUserFolders(loggedUser.id)
            groupViewModel.getGroups(loggedUser.id)
        }
    }

    val quizes by viewModel.quizzesWithCount.collectAsState()
    val folders by folderViewModel.folders.collectAsState(listOf())
    val groups by groupViewModel.groups.collectAsState(listOf())
    val coroutineScope : CoroutineScope = rememberCoroutineScope()
    var showDialog by remember {mutableStateOf(false)}
    val context = LocalContext.current
    val workRequestId = viewModel.workRequestId

    val workInfo = workRequestId?.let { id ->
        WorkManager.getInstance(context)
            .getWorkInfoByIdLiveData(id)
            .observeAsState()
    }

    val progress = workInfo?.value?.progress?.getInt("progress", 0) ?: 0
    val status = workInfo?.value?.progress?.getString("status") ?: ""
    val isRunning = workInfo?.value?.state == WorkInfo.State.RUNNING

    if (isRunning) {
        Dialog(onDismissRequest = {}){
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(SurfaceColor, shape = RoundedCornerShape(16.dp))
                    .padding(24.dp)){
                Text(status, fontSize = 14.sp, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = PrimaryColor
                )
                Spacer(Modifier.height(4.dp))
                Text("$progress%", fontSize = 12.sp, color = TextSecondary)
            }
        }
    }

    if(showDialog){
        Dialog(
            onDismissRequest = {showDialog = false}
        ){
            var folderChosen by remember {mutableStateOf(false)}
            var folder by remember { mutableStateOf(Folder(0,"", 0)) }
            val files by folderViewModel.files.collectAsState()
            val titles = remember {mutableStateListOf<Note>( )}
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(SurfaceColor, shape = RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {

                if(!folderChosen){
                    Text("Select folder")
                    LazyColumn(horizontalAlignment = Alignment.Start) {
                        items(folders, key = { it.folder.id }) { f ->
                            MyRadioButton(
                                label = f.folder.name,
                                selected = f.folder.name == folder.name,
                                onClick = {folder = f.folder}
                            )
                        }
                    }
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                folderViewModel.getFiles(folder.id)
                                folderChosen = true
                            }


                        }
                    ) {
                        Text("Confirm")
                    }
                }else{
                    Text("Select files")
                    LazyColumn(horizontalAlignment = Alignment.Start) {
                        items(files, key = { it.id }) { f ->
                            Row() {
                                Checkbox(
                                    checked = f in titles,
                                    onCheckedChange = {titles.add(f)}
                                )
                                Text(f.title)
                            }
                        }
                    }
                    Button(
                        onClick = {
                            folderChosen = false
                            showDialog = false
                            viewModel.generateQuiz(folder.name, loggedUser?.username ?: "", titles)
                        }
                    ) {
                        Text("Confirm")
                    }
                }
                Button(onClick = {showDialog = false}) {
                    Text("Exit")
                }
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {

        Header("Quizzes", profile, notifications, count)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .clickable(onClick = createTest)
                ,
                colors = CardDefaults.cardColors(
                    containerColor = AccentTeal,
                )
            ){
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = AccentDarkTeal,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("Create quiz",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = AccentDarkTeal)
                    Text("Add questions manually",
                        fontSize = 11.sp,
                        color = AccentDarkTeal.copy(alpha = 0.7f)
                    )
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .clickable(onClick = {showDialog = true})
                ,
                colors = CardDefaults.cardColors(
                    containerColor = AccentBlue,
                )
            ){
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = AccentDarkBlue,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("Generate with AI",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = AccentDarkBlue)
                    Text("From your notes",
                        fontSize = 11.sp,
                        color = AccentDarkBlue.copy(alpha = 0.7f)
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        SubHeader("MY QUIZZES")
        if(quizes.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally){
                Text("You haven't made any test yet!",
                    fontSize = 16.sp,
                    color = AccentCoral)
            }
        } else{
            LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                items(quizes, key = { it.quiz.id }) { quiz ->
                    var score by mutableStateOf(0)
                    var showMenu by remember { mutableStateOf(false) }
                    coroutineScope.launch {
                        score = viewModel.getMaxScore(loggedUser?.id ?: 0, quiz.quiz.id)
                    }
                    Card(Modifier.fillMaxWidth().padding(16.dp)
                    ) {

                        Row(Modifier.fillMaxWidth()){
                            Column(modifier = Modifier.fillMaxWidth().weight(2f)){
                                Text(quiz.quiz.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 25.sp,
                                    letterSpacing = 5.sp,
                                    modifier = Modifier.padding(start = 20.dp, top = 40.dp))
                                Text("${quiz.questionCount} questions",
                                    fontWeight = FontWeight.Light,
                                    fontSize = 15.sp,
                                    modifier = Modifier.padding(start = 20.dp))
                            }
                            Column(modifier = Modifier.fillMaxWidth().weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally) {
                                Card(colors = CardDefaults.cardColors(
                                    containerColor = AccentPink
                                ), modifier = Modifier.padding(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("best",
                                            color = AccentDarkPink
                                        )
                                        if(score != -1)
                                            Text("$score/${quiz.questionCount}",
                                                color = AccentDarkPink
                                            )
                                        else
                                            Text("-",
                                                color = AccentDarkPink)
                                    }
                                }
                            }

                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().weight(2f)) {
                                Button(
                                    onClick = { takeTest(quiz.quiz.id) },
                                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AccentTeal
                                    )
                                ) {
                                    Icon(imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = AccentDarkTeal)
                                    Text("Start quiz",
                                        color = AccentDarkTeal)
                                }
                            }
                            Column(modifier = Modifier.fillMaxWidth().weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally){
                                Button(
                                    onClick = {showMenu = true},
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BackgroundColor
                                    )
                                ) {
                                    Icon(imageVector = Icons.Filled.Menu,
                                        contentDescription = null,
                                        tint = TextPrimary)
                                }

                                var showWindow by rememberSaveable {mutableStateOf(false)}
                                var name by rememberSaveable {mutableStateOf("")}

                                if(showWindow){
                                    RenameWindow(
                                        { showWindow = it},
                                        name,
                                        { name = it},
                                        {name, quiz -> viewModel.updateName(name, quiz)},
                                        quiz.quiz
                                    )
                                }
                                var shareWindow by rememberSaveable {mutableStateOf(false)}
                                var groupWindow by rememberSaveable {mutableStateOf(false)}
                                var username by rememberSaveable {mutableStateOf("") }
                                val mess = viewModel.message.value
                                if(shareWindow){
                                    ShareWindow({shareWindow = it},
                                        username, {username = it},
                                        {if(loggedUser != null)
                                        {viewModel.sendQuiz(username, loggedUser.username, quiz.quiz)}},
                                        mess, {viewModel.resetMessage()})
                                }
                                if(groupWindow){
                                    GroupWindow(
                                        quiz = quiz.quiz,
                                        note = null,
                                        groups = groups,
                                        setShow = {groupWindow = it},
                                        uploadQuiz = {q, g -> groupViewModel.uploadQuiz(q,g)},
                                        uploadNote = {p, t, u , n -> },
                                        loggedUser ?: return@Card
                                    )
                                }

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = {showMenu = false}
                                ) {

                                    DropdownMenuItem(
                                        text = {Text("Delete")},
                                        onClick = {viewModel.deleteQuiz(quiz.quiz.id)
                                            showMenu = false},
                                        leadingIcon = {Icon(imageVector = Icons.Filled.Delete, contentDescription = null)}
                                    )
                                    DropdownMenuItem(
                                        text = {Text("Rename")},
                                        onClick = {showWindow = true
                                            showMenu = false},
                                        leadingIcon = {Icon(imageVector = Icons.Filled.Edit, contentDescription = null)}
                                    )
                                    DropdownMenuItem(
                                        text = {Text("Share")},
                                        onClick = { showMenu = false
                                                  shareWindow = true},
                                        leadingIcon = {Icon(imageVector = Icons.Filled.Share, contentDescription = null)}
                                    )
                                    DropdownMenuItem(
                                        text = {Text("Upload to group")},
                                        onClick = { showMenu = false
                                            groupWindow = true},
                                        leadingIcon = {Icon(imageVector = Icons.Filled.Upload, contentDescription = null)}
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                    }
                    Spacer(Modifier.height(16.dp))

                }
            }
        }


    }
}

@Composable
fun GroupWindow(
    quiz: Quiz?,
    note: Note?,
    groups: List<GroupWithMember>,
    setShow: (b: Boolean) -> Unit,
    uploadQuiz: (q: Quiz, g: Group) -> Unit,
    uploadNote: (path: String, title: String, g: Group, u: User) -> Unit,
    user: User
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
            var group by remember { mutableStateOf(Group(0, "", "")) }
            Text("Select group")
            LazyColumn(horizontalAlignment = Alignment.Start) {
                items(groups, key = { it.group.id }) { f ->
                    MyRadioButton(
                        label = f.group.name,
                        selected = f.group.name == group.name,
                        onClick = { group = f.group }
                    )
                }
            }
            Button(
                onClick = {
                    setShow(false)
                    if(quiz != null) uploadQuiz(quiz, group)
                    else if(note != null)uploadNote(note.path, note.title, group, user)
                }
            ) {
                Text("Confirm")
            }
            Button(
                onClick = {
                    setShow(false)
                }
            ) {
                Text("Exit")
            }
        }
    }
}

