package com.example.diplomski.viewmodels

import android.app.Application
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
import com.example.diplomski.CameraPhotoSaver
import com.example.diplomski.StudyApplication
import com.example.diplomski.database.tables.Folder
import com.example.diplomski.database.tables.Group
import com.example.diplomski.database.tables.GroupMember
import com.example.diplomski.database.tables.Message
import com.example.diplomski.database.tables.Note
import com.example.diplomski.database.tables.Question
import com.example.diplomski.database.tables.Quiz
import com.example.diplomski.database.tables.User
import com.example.diplomski.repositories.FolderRepository
import com.example.diplomski.repositories.GroupMemberRepository
import com.example.diplomski.repositories.GroupRepository
import com.example.diplomski.repositories.MessageRepository
import com.example.diplomski.repositories.NoteRepository
import com.example.diplomski.repositories.QuestionRepository
import com.example.diplomski.repositories.QuizRepository
import com.example.diplomski.repositories.UserRepository
import com.example.diplomski.retrofit.RetrofitClient
import com.example.diplomski.retrofit.SyncRepository
import com.example.diplomski.retrofit.TokenManager
import com.example.diplomski.screens.Notes
import com.example.diplomski.screens.Quiz
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class MessageInfo(
    val message: Message,
    val quiz: Quiz?,
    val group: Group?,
    val note: Note?
)

class MessageViewModel( private val messageRepository: MessageRepository,
    private val quizRepository: QuizRepository,
    private val questionRepository: QuestionRepository,
    private val syncRepository: SyncRepository,
                        private val groupRepository: GroupRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository,
    private val userRepository: UserRepository
): ViewModel(){

    private val _messages = MutableStateFlow<List<MessageInfo>>(emptyList())
    val messages = _messages.asStateFlow()


//    fun loadMessages(userId: Int) {
//        viewModelScope.launch {
//            try {
//                messageRepository.deleteAll()
//                syncRepository.syncMessages(userId)
//            } catch (e: Exception) {
//                Log.e("SYNC", "Message sync failed: ${e.message}")
//            }
//        }
//    }

    fun getMessages(user: User) {
        viewModelScope.launch {
            messageRepository.getAllMessages(user.id).collect { messages ->
                val withInfo = messages.map { message ->
                    var quiz : Quiz? = null
                    var group : Group? = null
                    var note: Note? = null
                    if(message.quizId != null){
                        val qdto = RetrofitClient.api.getQuizById(message.quizId.toLong())
                        val user = RetrofitClient.api.getUserById(qdto.userId)
                        quiz = Quiz(name = qdto.name, user = user.username, syncId = qdto.id)
                    }
                    if(message.groupId != null) {
                        val gdto = RetrofitClient.api.getGroupById(message.groupId.toLong())
                        val user = RetrofitClient.api.getUserById(gdto.userId)
                        group = Group(name = gdto.name, user = user.username, syncId = gdto.id)
                    }
                    if(message.noteId != null) {
                        val ndto = RetrofitClient.api.getNoteById(message.noteId.toLong())
                        note = Note(folderId = null, title = ndto.title, path = ndto.path, type = ndto.type,
                            syncId = ndto.id)
                    }
                    MessageInfo(message, quiz, group, note)
                }
                _messages.value = withInfo
            }
        }
    }

    fun deny(id: Int){
        viewModelScope.launch {
            messageRepository.delete(id)
        }
    }

    fun accept(info: MessageInfo, userId: String){
        viewModelScope.launch {
            if(info.quiz == null) return@launch
            val localQuizId = quizRepository.insert(
                Quiz(
                    name = info.quiz.name,
                    user = userId,
                    syncId = info.quiz.syncId
                )
            )
            if(info.quiz.syncId == null)return@launch
            val serverQuestions = RetrofitClient.api.getQuestions(info.quiz.syncId)
            serverQuestions.forEach { qDto ->
                questionRepository.insert(
                    Question(
                        text = qDto.text,
                        answer = qDto.answer,
                        type = qDto.type,
                        option1 = qDto.option1,
                        option2 = qDto.option2,
                        option3 = qDto.option3,
                        quizId = localQuizId.toInt(),
                        syncId = qDto.id
                    )
                )
            }
        }
    }

    fun join(info: MessageInfo, userId: Int){
        viewModelScope.launch {
            if(info.group == null || info.group.syncId == null) return@launch
            var localGroup = groupRepository.getGroupBySyncId(info.group.syncId)
            if(localGroup == null){
                val id = groupRepository.insert(Group(name = info.group.name, user = info.group.user,
                    syncId = info.group.syncId), null)
                localGroup = groupRepository.getGroup(id.toInt())
            }
            val localGroupId = groupMemberRepository.addMember(
                GroupMember(
                    userId = userId, groupId = localGroup?.id ?: return@launch
                )
            )
        }
    }

    fun save(info : MessageInfo, userId: Int){
        viewModelScope.launch {
            if(info.note == null) return@launch
            val folder = folderRepository.getFolderByName("Received", userId).firstOrNull()
            var folderId : Long? = folder?.id?.toLong()
            if(folder == null){
                folderId = folderRepository.insert(
                    Folder(0, "Received", userId)
                )
            }
            val localPath = noteRepository.downloadAndSave(info.note.path)
            val localNoteId = noteRepository.insert(
                Note(0, folderId?.toInt(), info.note.title, localPath, info.note.type,
                    serverPath = info.note.path)
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val messageRepository = (this[APPLICATION_KEY] as StudyApplication).messageRepository
                val quizRepository = (this[APPLICATION_KEY] as StudyApplication).quizRepository
                val questionRepository = (this[APPLICATION_KEY] as StudyApplication).questionRepository
                val syncRepository = (this[APPLICATION_KEY] as StudyApplication).syncRepository
                val noteRepository = (this[APPLICATION_KEY] as StudyApplication).noteRepository
                val groupMemberRepository = (this[APPLICATION_KEY] as StudyApplication).groupMemberRepository
                val groupRepository = (this[APPLICATION_KEY] as StudyApplication).groupRepository
                val folderRepository = (this[APPLICATION_KEY] as StudyApplication).folderRepository
                val userRepository  = (this[APPLICATION_KEY] as StudyApplication).userRepository
                return@initializer MessageViewModel(messageRepository, quizRepository, questionRepository, syncRepository,
                    groupRepository,groupMemberRepository, noteRepository, folderRepository, userRepository)
            }
        }
    }
}