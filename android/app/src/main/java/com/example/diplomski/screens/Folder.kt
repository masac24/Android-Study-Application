package com.example.diplomski.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.widget.Space
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMoveRtl
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
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
import androidx.work.WorkInfo
import androidx.work.WorkManager
import coil.compose.AsyncImage
import com.example.diplomski.CameraPhotoSaver
import com.example.diplomski.database.tables.Folder
import com.example.diplomski.database.tables.Group
import com.example.diplomski.database.tables.Note
import com.example.diplomski.database.tables.User
import com.example.diplomski.elements.Header
import com.example.diplomski.elements.MyRadioButton
import com.example.diplomski.elements.RenameWindow
import com.example.diplomski.elements.ShareWindow
import com.example.diplomski.elements.SubHeader
import com.example.diplomski.ui.theme.AccentBlue
import com.example.diplomski.ui.theme.AccentDarkBlue
import com.example.diplomski.ui.theme.LighterBackground
import com.example.diplomski.ui.theme.PrimaryColor
import com.example.diplomski.ui.theme.SurfaceColor
import com.example.diplomski.ui.theme.TextPrimary
import com.example.diplomski.ui.theme.TextSecondary
import com.example.diplomski.viewmodels.FolderState
import com.example.diplomski.viewmodels.FolderViewModel
import com.example.diplomski.viewmodels.GroupViewModel
import com.example.diplomski.viewmodels.GroupWithMember
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Folder(
    viewModel: FolderViewModel,
    folderId: Int,
    viewNote: (i: Int) -> Unit,
    loggedUser: User?,
    moveToFolder: (i: Int) -> Unit,
    profile: () -> Unit,
    notifications: () -> Unit,
    count: Int,
    groupViewModel: GroupViewModel
) {

    val groups by groupViewModel.groups.collectAsState(listOf())
    LaunchedEffect(folderId) {
        viewModel.getUserFolders(loggedUser?.id ?: 0)
        viewModel.getFiles(folderId)
    }

    val files by viewModel.files.collectAsState()
    val folders by viewModel.folders.collectAsState(listOf())
    val context = LocalContext.current
    val launcherFile = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = CameraPhotoSaver.getFileName(context, uri) ?: "file_${System.currentTimeMillis()}"
            val path = CameraPhotoSaver.copyFileToInternalStorage(context, uri, fileName)
            viewModel.insertFile(path, folderId, fileName)
        }
    }

    val workRequestId = viewModel.workRequestId
    val workInfo = workRequestId?.let { id ->
        WorkManager.getInstance(context)
            .getWorkInfoByIdLiveData(id)
            .observeAsState()
    }

    val progress = workInfo?.value?.progress?.getInt("progress", 0) ?: 0
    val status = workInfo?.value?.progress?.getString("status") ?: ""
    val isRunning = workInfo?.value?.state == WorkInfo.State.RUNNING


    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Header(viewModel.uiStateNotes.name, profile, notifications, count)
        Spacer(Modifier.height(8.dp))

        SubHeader("FILES")

        var showWindow by rememberSaveable { mutableStateOf(false)}
        if(showWindow) {
            ChooseFiles(files, {showWindow = false}, {viewModel.mergeNotes(it)})
        }

        Card(
            modifier = Modifier.padding(horizontal = 12.dp),
            colors = CardDefaults.cardColors(AccentBlue),
            border = BorderStroke(0.5.dp, AccentDarkBlue),
            shape = RoundedCornerShape(12.dp),
            onClick = {
                showWindow = true
            }
        ) {
            Row(Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Default.AutoAwesome, null, tint = AccentDarkBlue)
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text("Merge notes with AI", fontSize = 15.sp, color = AccentDarkBlue)
                    Text("Choose files and make one document", fontSize = 12.sp, color = AccentDarkBlue)
                }
            }
        }
        if(isRunning) {
            Spacer(Modifier.height(8.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(SurfaceColor, shape = RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(files) { file ->
                FileCard(
                    note = file,
                    folderName = viewModel.uiStateNotes.name,
                    onClick = {viewNote(it)},
                    delete = { i -> viewModel.deleteNote(i) },
                    update = { i, n -> viewModel.updateName(i, n as Note) },
                    changeName = { i -> viewModel.setNoteName(i) },
                    name = viewModel.uiStateNotes.newNoteName,
                    folders = folders,
                    updateFolder = {i, n -> viewModel.updateFolder(i, n)},
                    moveToFolder = moveToFolder,
                    sendNote = {n,i,nt -> viewModel.sendNote(n,i,nt)},
                    loggedUser,
                    groups,
                    {p,t,i,u -> groupViewModel.uploadNote(p,t,i,u)},
                    viewModel.message.value, {viewModel.resetMessage()}
                )
            }
            item {
                NewFileCard(folderId, viewModel, {
                    launcherFile.launch(arrayOf("application/pdf", "image/*"))
                })
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileCard(
    note: Note,
    folderName: String,
    onClick: (i: Int) -> Unit,
    delete: (i: Note) -> Unit,
    update: (name: String, note: Any) -> Unit,
    changeName: (i: String) -> Unit,
    name: String,
    folders: List<FolderState>,
    updateFolder: (i: Int, note: Note) -> Unit,
    moveToFolder: (i: Int) -> Unit,
    sendNote: (n: String, id: String, note: Note) -> Unit,
    loggedUser: User?,
    groups: List<GroupWithMember>,
    uploadNote: (path: String, title: String, group: Group, u: User) -> Unit,
    message: String,
    resetMessage: () -> Unit
){
    var showMenu by remember { mutableStateOf(false) }
    var showWindow by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }


    if (showWindow){
        RenameWindow(
            {it -> showWindow = it},
            name,
            changeName,
            update,
            note
        )
    }

    if(showDialog) {
        Dialog(
            onDismissRequest = { showDialog = false }
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
                        if(f.folder.name != folderName)
                        MyRadioButton(
                            label = f.folder.name,
                            selected = f.folder.name == folder.name,
                            onClick = { folder = f.folder }
                        )
                    }
                }
                Button(
                    onClick = { showDialog = false
                        updateFolder(folder.id, note)
                        moveToFolder(folder.id)
                    }
                ) {
                    Text("Confirm")
                }
                Button(
                    onClick = { showDialog = false }
                ) {
                    Text("Exit")
                }
            }
        }
    }
    Card(
        onClick = {onClick(note.id)},
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, Color(0x26000000))
    ) {
        Column() {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .background(
                        if (note.type == "TEXT") AccentBlue
                        else Color(0xFFE1F5EE)
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { showMenu = true }
                        )
                    }
                , contentAlignment = Alignment.Center
            ){
                if (note.type == "IMAGE") {
                    AsyncImage(
                        model = note.path,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if(note.type == "PDF"){
                    val bitmap by produceState<Bitmap?>(null) {
                        value = pdfThumbnail(note.path)
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
                }else {
                    Icon(
                        imageVector = if (note.type == "TEXT") Icons.AutoMirrored.Filled.Notes
                        else Icons.Default.Image,
                        contentDescription = null,
                        tint = if (note.type == "TEXT") AccentDarkBlue else Color(0xFF0F6E56),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        Column(modifier = Modifier.padding(8.dp)) {
            Text(note.title, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(note.type, fontSize = 11.sp, color = Color.Gray)
        }
        var shareWindow by rememberSaveable {mutableStateOf(false)}
        var groupWindow by rememberSaveable {mutableStateOf(false)}
        var username by rememberSaveable {mutableStateOf("") }
        if(shareWindow) {
            ShareWindow(
                setShow = {shareWindow = it},
                name = username,
                changeName = {username = it},
                sendToUser = {sendNote(username, loggedUser?.username ?: return@ShareWindow, note)},
                message = message,
                resetMessage
            )
        }
        if(groupWindow){
            GroupWindow(
                quiz = null,
                note = note,
                groups = groups,
                setShow = {groupWindow = it},
                uploadQuiz = {i, u -> },
                uploadNote = uploadNote,
                loggedUser ?: return@Card
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Rename file") },
                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                onClick = {
                    showMenu = false
                    showWindow = true
                }
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                onClick = {
                    showMenu = false
                    delete(note)
                }
            )
            DropdownMenuItem(
                text = { Text("Move to folder") },
                leadingIcon = { Icon(Icons.Default.DriveFileMoveRtl, contentDescription = null) },
                onClick = {
                    showMenu = false
                    showDialog = true
                }
            )
            DropdownMenuItem(
                text = { Text("Upload to group") },
                leadingIcon = { Icon(Icons.Filled.Upload, contentDescription = null) },
                onClick = {
                    showMenu = false
                    groupWindow = true
                }
            )
            DropdownMenuItem(
                text = { Text("Share with friend") },
                leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
                onClick = {
                    showMenu = false
                    shareWindow = true
                }
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewFileCard(id: Int, viewModel: FolderViewModel,
                launch: () -> Unit
    ){
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val imageFile = remember { CameraPhotoSaver.createFileWithoutName(context.filesDir) }
    val imageUri = remember {
        FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
    }

    var showDialog by remember { mutableStateOf(false) }
    var pendingBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val launcherFull =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
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
                    value = viewModel.uiStateNotes.newNoteName,
                    onValueChange = { viewModel.setNoteName(it) },
                    label = { Text("Name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    val path = CameraPhotoSaver.savePictureToFile(
                        context, pendingBitmap, "image/jpeg",
                        viewModel.uiStateNotes.newNoteName
                    )
                    viewModel.insertFile(path, id, viewModel.uiStateNotes.newNoteName)
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    val path = CameraPhotoSaver.savePictureToFile(
                        context, pendingBitmap, "image/jpeg", ""
                    )
                    viewModel.insertFile(path, id, "")
                }) { Text("Skip") }
            }
        )
    }

    Box {
        Card(
            onClick = { expanded = true },
            colors = CardDefaults.cardColors(
                containerColor = LighterBackground
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(0.5.dp, TextPrimary.copy(alpha = 0.4f)),
            modifier = Modifier.width(190.dp).height(140.dp)
        ) {
            Column(Modifier.padding(14.dp).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.height(6.dp))
                Text("Add file",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = TextPrimary)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Load file") },
                leadingIcon = { Icon(Icons.Default.FileUpload, contentDescription = null) },
                onClick = {
                    expanded = false
                    launch()
                }
            )
            DropdownMenuItem(
                text = { Text("Take picture") },
                leadingIcon = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                onClick = {
                    expanded = false
                    launcherFull.launch(imageUri)
                }
            )
        }
    }
}

fun pdfThumbnail(path: String): Bitmap {

    val descriptor =
        ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY)

    val renderer = PdfRenderer(descriptor)

    val page = renderer.openPage(0)

    val bitmap = Bitmap.createBitmap(
        page.width,
        page.height,
        Bitmap.Config.ARGB_8888
    )

    page.render(
        bitmap,
        null,
        null,
        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
    )

    page.close()
    renderer.close()
    descriptor.close()

    return bitmap
}

@Composable
fun ChooseFiles(
    files : List<Note>,
    setShow: (Boolean) -> Unit,
    generate: (List<Note>) -> Unit
){
    Dialog(
        onDismissRequest = {setShow(false)}
    ) {
        val titles = remember {mutableStateListOf<Note>( )}
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(SurfaceColor, shape = RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {

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
                    generate(titles)
                    setShow(false)
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