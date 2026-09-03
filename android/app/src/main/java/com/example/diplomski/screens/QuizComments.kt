package com.example.diplomski.screens

import android.widget.Space
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomski.database.tables.Quiz
import com.example.diplomski.database.tables.User
import com.example.diplomski.ui.theme.TextPrimary
import com.example.diplomski.ui.theme.TextSecondary
import com.example.diplomski.viewmodels.GroupViewModel

@Composable
fun QuizComments(
    quizId: Int,
    viewModel: GroupViewModel,
    loggedUser: User?
){

    LaunchedEffect(quizId) {
        viewModel.getQuiz(quizId)
    }
    val quiz = viewModel.quizPreview.value.quiz
    val comments = viewModel.quizPreview.value.comments
    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(20.dp))
        Column(Modifier.fillMaxWidth().fillMaxHeight().weight(0.1f), horizontalAlignment = Alignment.Start
            ) {
            Text(quiz.name, fontSize = 40.sp, fontWeight = FontWeight.Bold,
                color = TextPrimary, modifier = Modifier.padding(horizontal = 20.dp)
            ) }
        if (!comments.isEmpty())
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp).fillMaxHeight().weight(0.8f)
            ) {

                    items(comments, key = { it.id }) { comment ->
                        CommentCard(comment,
                            comment.user, quiz.user,
                            loggedUser, {i, g -> viewModel.deleteComment(i,g)})
                    }
            }
        else{
            Column(Modifier.fillMaxWidth().fillMaxHeight().weight(0.8f),
                verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No comments yet", fontSize = 15.sp, fontWeight = FontWeight.Medium,
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
                note = null,
                quiz = quiz,
                saveComment = {a,b,c,d,e -> viewModel.saveComment(a,b,c,d,e)},
                groupId = viewModel.group.collectAsState().value?.group?.id
            )
        }
    }
}