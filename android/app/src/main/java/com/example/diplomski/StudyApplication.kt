package com.example.diplomski

import android.app.Application
import com.example.diplomski.database.AppDatabase
import com.example.diplomski.repositories.ChecklistRepository
import com.example.diplomski.repositories.CommentRepository
import com.example.diplomski.repositories.FolderRepository
import com.example.diplomski.repositories.GroupMemberRepository
import com.example.diplomski.repositories.GroupNoteRepository
import com.example.diplomski.repositories.GroupQuizRepository
import com.example.diplomski.repositories.GroupRepository
import com.example.diplomski.repositories.MessageRepository
import com.example.diplomski.repositories.NoteRepository
import com.example.diplomski.repositories.QuestionRepository
import com.example.diplomski.repositories.QuizRepository
import com.example.diplomski.repositories.ReminderRepository
import com.example.diplomski.repositories.ScoreRepository
import com.example.diplomski.repositories.SessionRepository
import com.example.diplomski.repositories.UserRepository
import com.example.diplomski.retrofit.RetrofitClient
import com.example.diplomski.retrofit.SyncRepository
import com.example.diplomski.websocket.WebSocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class StudyApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { AppDatabase.getDatabase(this, applicationScope) }

    val quizRepository by lazy { QuizRepository(database.quizDao(), database.questionDao(),
        database.scoreDao()) }
    val questionRepository by lazy { QuestionRepository(database.questionDao(), database.quizDao()) }
    val scoreRepository by lazy { ScoreRepository(database.scoreDao()) }
    val folderRepository by lazy { FolderRepository(database.folderDao(), database.noteDao()) }
    val noteRepository by lazy { NoteRepository(database.noteDao(), database.folderDao(), this) }
    val checklistRepository by lazy { ChecklistRepository(database.checklistDao()) }
    val reminderRepository by lazy { ReminderRepository(database.reminderDao()) }
    val sessionRepository by lazy { SessionRepository(database.sessionDao()) }

    val groupNoteRepository by lazy { GroupNoteRepository(database.groupNoteDao(), database.noteDao(),
        database.groupDao()) }
    val groupQuizRepository by lazy { GroupQuizRepository(database.groupQuizDao(), database.quizDao(),
        database.groupDao()) }
    val messageRepository by lazy { MessageRepository(database.messageDao(), database.quizDao(),
        database.groupDao(), database.noteDao()) }

    val commentRepository by lazy { CommentRepository(database.commentDao(), database.quizDao(),
        database.noteDao(), database.groupDao()) }
    val groupRepository by lazy { GroupRepository(database.groupDao(), database.groupMemberDao()) }
    val groupMemberRepository by lazy { GroupMemberRepository(database.groupMemberDao(),
        database.groupDao()) }
    val userRepository by lazy { UserRepository(database.userDao(), database.quizDao(),
        database.questionDao(), database.noteDao(),database.folderDao(),  database.scoreDao(),
        database.sessionDao(), database.checklistDao(), database.reminderDao(), database.messageDao()
        ) }
    val syncRepository by lazy { SyncRepository(RetrofitClient.api, database.quizDao(),
        database.questionDao(), database.folderDao(), database.noteDao(),
        database.sessionDao(), database.scoreDao(), database.checklistDao(),
        noteRepository, database.messageDao(), database.groupDao(), database.groupMemberDao(),
        database.userDao(), database.groupNoteDao(), database.groupQuizDao(),
        database.commentDao()) }

    val webSocketManager by lazy {
        WebSocketManager()
    }
}