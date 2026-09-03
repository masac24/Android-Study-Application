package com.example.diplomski.viewmodels

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.diplomski.StudyApplication
import com.example.diplomski.database.tables.Folder
import com.example.diplomski.database.tables.Message
import com.example.diplomski.database.tables.Note
import com.example.diplomski.database.tables.Quiz
import com.example.diplomski.repositories.FolderRepository
import com.example.diplomski.repositories.GroupNoteRepository
import com.example.diplomski.repositories.MessageRepository
import com.example.diplomski.repositories.NoteRepository
import com.example.diplomski.repositories.UserRepository
import com.example.diplomski.retrofit.ai.AIMergeGenerator
import com.example.diplomski.retrofit.ai.AIQuizGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class FolderState(
    val folder: Folder = Folder(0,"", 0),
    val numberOfFiles : Int = 0
)

data class uiStateNotes(
    val name: String = "",
    val user: Int = 0,
    val newNoteName : String = ""
)

data class NotePreview(
    val note: Note = Note(0, 0, "", "", ""),
)

class FolderViewModel( private val folderRepository: FolderRepository,
                       private val noteRepository: NoteRepository,
                        private val userRepository: UserRepository,
                        private val messageRepository: MessageRepository,
                        private val groupNoteRepository: GroupNoteRepository,
                       application: Application
) : AndroidViewModel(application) {

    private val _folders = MutableStateFlow<List<FolderState>>(emptyList())
    val folders = _folders.asStateFlow()
    private val _files = MutableStateFlow<List<Note>>(emptyList())
    val files = _files.asStateFlow()

    private val _message = mutableStateOf("")
    val message = _message
    var uiStateNotes by mutableStateOf(uiStateNotes())
        private set

    var notePreview by mutableStateOf(NotePreview())
        private set

    fun getUserFolders(user: Int){
        uiStateNotes = uiStateNotes.copy(user = user)
        viewModelScope.launch {
            folderRepository.getUserFolders(user).collect { folders ->
                val folder = folders.map { f ->
                    FolderState(
                        folder = f,
                        numberOfFiles = noteRepository.getNumberOfFiles(f.id)
                    )
                }
                _folders.value = folder
            }
        }
    }

    fun getFiles(id: Int?) {
        getFolder(id)
        viewModelScope.launch {
            noteRepository.getFolderNotes(id).collect {files ->
                _files.value = files
            }
        }
    }

    fun setNoteName(s : String){
        uiStateNotes = uiStateNotes.copy(newNoteName = s)
    }

    fun updateFolderName(name : String, folder: Folder){
        viewModelScope.launch {
            folderRepository.update(Folder(folder.id, name, folder.userId, folder.syncId))
        }
    }

    fun deleteNote(note: Note){
        viewModelScope.launch {
            val groupnotes = groupNoteRepository.getNote(note.id).firstOrNull()
            if (groupnotes == null || groupnotes.isEmpty())
                noteRepository.delete(note.id)
            else
                noteRepository.update(note.copy(folderId = null))
        }
    }

    fun updateName(name: String, note: Note){
        uiStateNotes = uiStateNotes.copy(newNoteName = "")
        viewModelScope.launch {
            noteRepository.update(Note(note.id, note.folderId, name, note.path, note.type, note.syncId, note.serverPath))
        }
    }

    fun updateFolder(id: Int, note: Note){
        viewModelScope.launch {
            noteRepository.update(Note(note.id, id, note.title, note.path, note.type, note.syncId, note.serverPath))
        }
    }
    private val appContext = getApplication<Application>()
    var workRequestId: UUID? = null

    fun mergeNotes(notes: List<Note>) {
        if (notes.isEmpty())return
        workRequestId = AIMergeGenerator.merge(notes, notes[0].folderId, appContext)
    }

    fun insertFile(path: String, id: Int?, title: String){
        uiStateNotes = uiStateNotes.copy(newNoteName = "")
        val last = path.split(".").last()
        var type = "IMAGE"
        if(last !in listOf("png", "jpg", "jpeg")) type = "PDF"
        if(last == "txt") type = "TEXT"
        viewModelScope.launch {
            noteRepository.insert(Note(0, id, title, path, type))
        }
    }

    fun getFolder(id: Int?){
        viewModelScope.launch {
            val folder = folderRepository.getFolder(id).firstOrNull()
            if(folder != null)
                uiStateNotes = uiStateNotes.copy(name = folder.name, user = folder.userId)
        }
    }

    fun setName(n : String){
        uiStateNotes = uiStateNotes.copy(name = n)
    }

    fun insert() {
        viewModelScope.launch {
            folderRepository.insert(Folder(0, uiStateNotes.name, uiStateNotes.user))
        }
    }


    fun deleteFolder(i: Int){
        uiStateNotes = uiStateNotes.copy(name = "")
        viewModelScope.launch {
            folderRepository.delete(i)
        }
    }

    fun getNote(i: Int) {
        viewModelScope.launch {
            val note = noteRepository.getNote(i).firstOrNull()
            if(note != null) {
                notePreview = notePreview.copy(note = note)
                getFiles(note.folderId)
            }
        }
    }

    fun sendNote(name: String, id: String, note: Note){
        viewModelScope.launch {
            val receiver = userRepository.search(name)
            if(receiver == null){
                _message.value = "No user found"
                return@launch
            }else{
                _message.value = "Succesfully sent!"
            }
            if(note.syncId != null){
                messageRepository.insert(Message(0, id, null, null, note.id,
                    receiver.toInt() ))
            }

        }
    }

    fun resetMessage(){
        _message.value = ""
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val folderRepository = (this[APPLICATION_KEY] as StudyApplication).folderRepository
                val noteRepository = (this[APPLICATION_KEY] as StudyApplication).noteRepository
                val userRepository = (this[APPLICATION_KEY] as StudyApplication).userRepository
                val messageRepository = (this[APPLICATION_KEY] as StudyApplication).messageRepository
                val groupNoteRepository = (this[APPLICATION_KEY] as StudyApplication).groupNoteRepository
                val application = this[APPLICATION_KEY] as Application
                return@initializer FolderViewModel(folderRepository, noteRepository, userRepository,
                    messageRepository, groupNoteRepository, application)
            }
        }
    }
}