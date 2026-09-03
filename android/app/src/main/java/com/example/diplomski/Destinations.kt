package com.example.diplomski

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.RingVolume
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.diplomski.screens.Settings

interface Destination {
    val icon: ImageVector
    val route: String
}

object Register : Destination {
    override val icon = Icons.Filled.Phone
    override val route = "register"
}

object Login : Destination {
    override val icon = Icons.Default.Phone
    override val route = "login"
}

object Profile : Destination {
    override val icon = Icons.Default.Person
    override val route = "profile"
}

object StartPage : Destination {
    override val icon = Icons.Default.Person
    override val route = "start"
}

object Checklist : Destination {
    override val icon = Icons.Filled.Checklist
    override val route = "checklist"
}

object Notes : Destination {
    override val icon = Icons.AutoMirrored.Filled.Note
    override val route = "notes"
}

object StudySession : Destination {
    override val icon = Icons.Filled.Book
    override val route = "session"
}

object ActiveSession : Destination {
    override val icon = Icons.Filled.Book
    override val route = "activesession"
}

object Quiz: Destination {
    override val icon = Icons.Filled.Quiz
    override val route = "quiz"
}

object CreateTest: Destination {
    override val icon = Icons.Filled.Quiz
    override val route = "createtest"
}

object TakeTest: Destination {
    override val icon = Icons.Filled.Quiz
    override val route = "taketest"
}
object Files: Destination {
    override val icon = Icons.Filled.Quiz
    override val route = "files"
}

object ViewNote: Destination {
    override val icon = Icons.Filled.Quiz
    override val route = "viewnote"
}

object GroupNote: Destination {
    override val icon = Icons.Filled.Quiz
    override val route = "groupnote"
}

object Reminder: Destination {
    override val icon = Icons.Filled.RingVolume
    override val route = "reminder"
}

object Settings: Destination {
    override val icon = Icons.Filled.Settings
    override val route = "settings"
}

object Notifications: Destination {
    override val icon = Icons.Filled.Notifications
    override val route = "notifications"
}

object Group: Destination {
    override val icon = Icons.Filled.Group
    override val route = "groups"
}

object GroupView: Destination {
    override val icon = Icons.Filled.Group
    override val route = "groupview"
}

object QuizComment: Destination {
    override val icon = Icons.Filled.Group
    override val route = "quizcomment"
}

val Destinations = listOf(Register, Login, Checklist, Notes, StudySession, Quiz, Reminder,
    CreateTest, TakeTest, Files, ViewNote, ActiveSession, Profile, StartPage, Settings, Notifications,
    Group, GroupView, GroupNote, QuizComment
)