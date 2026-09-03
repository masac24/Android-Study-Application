package com.example.diplomski.retrofit

import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.diplomski.database.AppDatabase
import com.example.diplomski.repositories.NoteRepository
import com.example.diplomski.retrofit.dto.FcmTokenDto
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.d("FCM", "New token: $token")
        // sačuvaj lokalno
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()

        // pošalji na backend ako je korisnik ulogovan
        val userId = TokenManager.getUserId()
        if (userId != -1L) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    RetrofitClient.api.updateFcmToken(FcmTokenDto(userId, token))
                } catch (e: Exception) {
                    Log.e("FCM", "Token update failed: ${e.message}")
                }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val type = message.data["type"]
        val userId = TokenManager.getUserId().toInt()
        if (userId == -1) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext, this)
                val noteRepository = NoteRepository(db.noteDao(), db.folderDao(), applicationContext)
                val syncRepository = SyncRepository(
                    api = RetrofitClient.api,
                    quizDao = db.quizDao(),
                    questionDao = db.questionDao(),
                    folderDao = db.folderDao(),
                    noteDao = db.noteDao(),
                    sessionDao = db.sessionDao(),
                    scoreDao = db.scoreDao(),
                    checklistDao = db.checklistDao(),
                    noteRepository = noteRepository,
                    messageDao = db.messageDao(),
                    groupDao = db.groupDao(),
                    groupMemberDao = db.groupMemberDao(),
                    userDao = db.userDao(),
                    groupNoteDao = db.groupNoteDao(),
                    groupQuizDao = db.groupQuizDao(),
                    commentDao = db.commentDao()
                )

                when (type) {
                    "MESSAGE" -> syncRepository.syncMessages(userId)
                    "GROUP_NOTE", "GROUP_QUIZ" -> syncRepository.syncGroups(userId)
                    else -> syncRepository.sync(userId)
                }
            } catch (e: Exception) {
                Log.e("FCM", "Sync after notification failed: ${e.message}")
            }
        }
    }
}