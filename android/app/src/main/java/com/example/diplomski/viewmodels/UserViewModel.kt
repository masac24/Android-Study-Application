package com.example.diplomski.viewmodels
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.diplomski.BroadcastReceiverNotification
import com.example.diplomski.ReminderReceiver
import com.example.diplomski.StudyApplication
import com.example.diplomski.database.AppDatabase
import com.example.diplomski.database.tables.User
import com.example.diplomski.repositories.QuizRepository
import com.example.diplomski.repositories.UserRepository
import com.example.diplomski.retrofit.RetrofitClient
import com.example.diplomski.retrofit.SyncRepository
import com.example.diplomski.retrofit.TokenManager
import com.example.diplomski.retrofit.dto.AuthRequest
import com.example.diplomski.websocket.GroupRefreshManager
import com.example.diplomski.websocket.WebSocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Loading : AuthState()
    object LoggedOut : AuthState()
    data class LoggedIn(val user: User) : AuthState()
}

data class UserDetails(
    val id: Int = 0,
    val username : String = "",
    val password: String = "",
    val logged : Boolean = false
)


data class uiState(
    val userDetails: UserDetails = UserDetails(),
    val message: String = ""
)

fun UserDetails.toUser() = User(
    id = id,
    username = username,
    password = password,
    logged = logged
)


fun User.toUserDetails() = UserDetails(
    id = id,
    username = username,
    password = password
)


class UserViewModel(
    application: Application,
    private val userRepository: UserRepository,
    private val syncRepository: SyncRepository,
    private val webSocketManager: WebSocketManager
) : AndroidViewModel(application) {

    val logged = userRepository.logged
    var uiState by mutableStateOf(uiState())
        private set

    var isRefreshing by mutableStateOf(false)
        private set
    private var webSocketListenerJob: Job? = null
    private val appContext = getApplication<Application>()
    private fun listenForWebSocketEvents(user: User) {

        if (webSocketListenerJob?.isActive == true) {
            Log.d("WEBSOCKET", "Listener already active")
            return
        }

        webSocketListenerJob = viewModelScope.launch {

            webSocketManager.events.collect { event ->

                val serverUserId =
                    user.syncId?.toInt()
                        ?: return@collect

                Log.d(
                    "WEBSOCKET",
                    "Event received: ${event.type}"
                )

                val intent = Intent(appContext, BroadcastReceiverNotification::class.java).apply {
                    putExtra("title", event.message)
                }
                appContext.sendBroadcast(intent)
                val pendingIntent = PendingIntent.getBroadcast(
                    appContext,
                    event.groupId?.toInt() ?: 1,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )



                when (event.type) {

                    "MESSAGE_RECEIVED" -> {

                        try {
                            syncRepository.syncMessages(serverUserId)

                            Log.d(
                                "WEBSOCKET",
                                "Messages synced"
                            )

                        } catch (e: Exception) {
                            Log.e(
                                "WEBSOCKET",
                                "syncMessages failed",
                                e
                            )
                        }
                    }

                    "GROUP_JOINED",
                    "GROUP_NOTE_ADDED",
                    "GROUP_QUIZ_ADDED",
                    "GROUP_NOTE_DELETED",
                    "GROUP_QUIZ_DELETED",
                    "GROUP_COMMENT_ADDED",
                    "GROUP_COMMENT_DELETED"
                            -> {

                        try {

                            Log.d(
                                "WEBSOCKET",
                                "Starting syncGroups..."
                            )

                            syncRepository.syncGroups(serverUserId)

                            Log.d(
                                "WEBSOCKET",
                                "syncGroups SUCCESS"
                            )

                            GroupRefreshManager.refresh(
                                serverUserId.toLong()
                            )

                            Log.d(
                                "WEBSOCKET",
                                "GroupRefreshManager.refresh() called"
                            )

                        } catch (e: Exception) {

                            Log.e(
                                "WEBSOCKET",
                                "syncGroups failed",
                                e
                            )
                        }
                    }

                    else -> {

                        try {
                            syncRepository.sync(serverUserId)
                        } catch (e: Exception) {
                            Log.e(
                                "WEBSOCKET",
                                "General sync failed",
                                e
                            )
                        }
                    }
                }
            }
        }
    }

    fun refreshData() {
        val serverUserId = TokenManager.getUserId()

        viewModelScope.launch {
            isRefreshing = true

            try {
                syncRepository.sync(serverUserId.toInt())
            } catch (e: Exception) {
                Log.e("REFRESH", "Refresh failed", e)
            } finally {
                isRefreshing = false
            }
        }
    }

    override fun onCleared() {
        webSocketListenerJob?.cancel()
        webSocketManager.disconnect()
        super.onCleared()
    }

    fun syncLoggedUser(user: User) {

        val serverUserId = user.syncId ?: return

        viewModelScope.launch {
            listenForWebSocketEvents(user)

            webSocketManager.connect(serverUserId)
        }

        viewModelScope.launch {
            try {

                syncRepository.sync(
                    serverUserId.toInt()
                )

                Log.d(
                    "SYNC",
                    "Initial sync SUCCESS"
                )

            } catch (e: Exception) {

                Log.e(
                    "SYNC",
                    "Initial sync FAILED",
                    e
                )
            }
        }
    }

    val authState: StateFlow<AuthState> = logged
        .map { user ->
            if (user != null) AuthState.LoggedIn(user)
            else AuthState.LoggedOut
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), AuthState.Loading)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                val userRepository = (this[APPLICATION_KEY] as StudyApplication).userRepository
                val syncRepository = (this[APPLICATION_KEY] as StudyApplication).syncRepository
                val webSocketManager = (this[APPLICATION_KEY] as StudyApplication).webSocketManager
                return@initializer UserViewModel(application, userRepository, syncRepository, webSocketManager)
            }
        }
    }


    fun setName(name: String) {
        uiState = uiState(uiState.userDetails.copy(username = name))
    }

    fun setPassword(pass: String) {
        uiState = uiState(uiState.userDetails.copy(password = pass))
    }

    fun setMessage(mess: String) {
        uiState = uiState(uiState.userDetails, mess)
    }

    fun changePassword(user: User, password: String){
        viewModelScope.launch {
            userRepository.update(User(user.id, user.username, password, user.logged, user.syncId))
        }
        uiState = uiState(uiState.userDetails.copy(password = ""))
    }

    fun changeUsername(user: User, username: String){
        viewModelScope.launch {
            val usr = userRepository.search(username)
            if(usr != null){
                uiState = uiState.copy(message = "User with this username already exists")
                return@launch
            }
            userRepository.update(User(user.id, username, user.password, user.logged, user.syncId))
        }
        uiState = uiState(uiState.userDetails.copy(username = ""), message = "")
    }

    suspend fun login(): Boolean {
        return try {
            val response = RetrofitClient.api.login(
                AuthRequest(uiState.userDetails.username, uiState.userDetails.password)
            )
            TokenManager.saveToken(response.token)
            TokenManager.saveUserId(response.userId)

            var user = userRepository.findUser(
                uiState.userDetails.username,
                uiState.userDetails.password
            ).firstOrNull()

            if (user == null) {
                val newId = userRepository.insert(
                    User(0, uiState.userDetails.username, uiState.userDetails.password, true, response.userId),
                    false
                )
                user = userRepository.findUser(
                    uiState.userDetails.username,
                    uiState.userDetails.password
                ).firstOrNull()
            }

            user?.let {
                userRepository.changeLogged(User(it.id, it.username, it.password, true, response.userId))
                uiState = uiState(uiState.userDetails, "")
                listenForWebSocketEvents(it)

                webSocketManager.connect(
                    response.userId.toLong()
                )

                viewModelScope.launch {
                    syncRepository.sync(
                        response.userId.toInt()
                    )
                }
            }
            true

        } catch (e: Exception) {
            Log.e("LOGIN", "Backend login failed, trying local: ${e.message}")

            val user = userRepository.findUser(
                uiState.userDetails.username,
                uiState.userDetails.password
            ).firstOrNull()

            if (user != null) {
                userRepository.changeLogged(User(user.id, user.username, user.password, true))
                uiState = uiState(uiState.userDetails, "")
                true
            } else {
                setMessage("Invalid username or password")
                false
            }
        }
    }


    suspend fun register(): Boolean {
        val user = userRepository.checkIfExist(
            uiState.userDetails.username
        ).firstOrNull()

        return if (user != null) {
            setMessage("User with that username already exists. Try with another one.")
            false
        } else {
            try {
                val response = RetrofitClient.api.register(
                    AuthRequest(uiState.userDetails.username, uiState.userDetails.password)
                )
                Log.d("BACKEND", "Register success: $response")
            } catch (e: Exception) {
                Log.e("BACKEND", "Register failed: ${e.message}")
                e.printStackTrace()
            }
            val user = User(id = 0, username = uiState.userDetails.username, password = uiState.userDetails.password, true)
            userRepository.insert(user, true)
            uiState = uiState(uiState.userDetails, "")
            true
        }
    }

    fun logout() {
        uiState = uiState(uiState.userDetails, "")
        viewModelScope.launch {
            val l = logged.firstOrNull()
            if(l != null)
                userRepository.changeLogged(User(l.id, l.username, l.password, false))
        }
    }

    fun delete(user: User){
        viewModelScope.launch {
            userRepository.delete(user)
        }
    }

    fun clear() {
        uiState = uiState(userDetails = UserDetails(), "")
    }



}

