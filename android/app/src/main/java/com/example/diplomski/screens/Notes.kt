package com.example.diplomski.screens

import android.graphics.fonts.FontFamily
import android.widget.GridLayout
import android.widget.Space
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.diplomski.database.tables.Folder
import com.example.diplomski.database.tables.User
import com.example.diplomski.elements.Header
import com.example.diplomski.elements.RenameWindow
import com.example.diplomski.elements.SubHeader
import com.example.diplomski.ui.theme.BackgroundColor
import com.example.diplomski.ui.theme.LighterBackground
import com.example.diplomski.ui.theme.SurfaceColor
import com.example.diplomski.ui.theme.TextPrimary
import com.example.diplomski.ui.theme.TextSecondary
import com.example.diplomski.ui.theme.folderColors
import com.example.diplomski.ui.theme.folderIconColors
import com.example.diplomski.viewmodels.FolderViewModel
import kotlin.math.log

@Composable
fun Notes(
    viewModel: FolderViewModel,
    loggedUser: User?,
    openFolder: (id: Int) -> Unit,
    profile: ()-> Unit,
    notifications: () -> Unit,
    count: Int
) {

    LaunchedEffect(loggedUser) {
        if(loggedUser != null)
            viewModel.getUserFolders(loggedUser.id)
    }
    val folders by viewModel.folders.collectAsState(listOf())
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
                Text("Set folder name")
                OutlinedTextField(
                    value = viewModel.uiStateNotes.name,
                    onValueChange = {viewModel.setName(it)},
                    label = {Text("Name")}
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

    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally) {

        Header("My Notes", profile, notifications, count)

        Spacer(Modifier.height(8.dp))

        SubHeader("FOLDERS")


        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(folders) { index, folder ->
                FolderCard(folder = folder.folder,
                    onClick = { openFolder(folder.folder.id) },
                    num = folder.numberOfFiles,
                    clr = index,
                    delete = {i -> viewModel.deleteFolder(i)},
                    updateName = {n, f -> viewModel.updateFolderName(n, f as Folder)}
                )
            }
            item {
                NewFolderCard(onClick = { showDialog = true})
            }
        }
    }
}

@Composable
fun FolderCard(folder: Folder,
               onClick: () -> Unit,
               num: Int,
               clr: Int,
               delete: (i: Int) -> Unit,
               updateName: (n: String, f: Any) -> Unit
               ){
    val color = folderColors[clr % folderColors.size]
    val iconColor = folderIconColors[clr % folderColors.size]
    var showMenu by remember { mutableStateOf(false) }
    var showWindow by remember { mutableStateOf(false) }
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = color
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, iconColor.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp).pointerInput(Unit) {
            detectTapGestures(
                onLongPress = { showMenu = true }
            )
        }
            ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(folder.name,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = iconColor)
            Text("$num files",
                fontSize = 11.sp,
                color = iconColor.copy(alpha = 0.7f)
            )
        }
        var name by remember { mutableStateOf("") }
        if (showWindow) {
            RenameWindow(
                setShow = {showWindow = it},
                name = name,
                changeName = {name = it},
                update = {n, f -> updateName(n,f)
                         name = ""},
                obj = folder
            )
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = {showMenu = false}
        ) {
            DropdownMenuItem(
                text = {Text("Delete")},
                onClick = {delete(folder.id)
                          showMenu = false},
                leadingIcon = {Icon(imageVector = Icons.Filled.Delete, contentDescription = null)}
            )
            DropdownMenuItem(
                text = {Text("Rename")},
                onClick = {showWindow = true
                    showMenu = false},
                leadingIcon = {Icon(imageVector = Icons.Filled.Edit, contentDescription = null)}
            )
        }
    }
}

@Composable
fun NewFolderCard(onClick: () -> Unit){
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = LighterBackground
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, TextPrimary.copy(alpha = 0.4f))

    ) {
        Column(Modifier.padding(14.dp).height(90.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text("New folder",
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = TextPrimary)
        }
    }
}