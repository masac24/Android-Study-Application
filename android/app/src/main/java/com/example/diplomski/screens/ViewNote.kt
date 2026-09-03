package com.example.diplomski.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.diplomski.CameraPhotoSaver
import com.example.diplomski.database.tables.Note
import com.example.diplomski.elements.PdfViewer
import com.example.diplomski.viewmodels.FolderViewModel
import java.io.File

@Composable
fun ViewNote(
    noteId: Int,
    viewModel: FolderViewModel
){
    LaunchedEffect(noteId) {
        viewModel.getNote(noteId)
    }
    val note = viewModel.notePreview.note
    val files = viewModel.files.collectAsState().value
    val index = files.indexOf(note)
    if (files.isEmpty() || index == -1) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val pagerState = rememberPagerState(initialPage = index, pageCount = { files.size })
    HorizontalPager(state = pagerState) { page ->
        NoteViewer(note = files[page])
    }
}

@Composable
fun NoteViewer(note: Note){

    if (note.type == "IMAGE"){
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = note.path,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    } else if(note.type == "PDF"){
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                PdfViewer(note.path)
            }
        }
    }else{
        val text = remember (note.path) { File(note.path).readText() }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(text = text, fontSize = 14.sp, lineHeight = 22.sp)
        }
    }

}

