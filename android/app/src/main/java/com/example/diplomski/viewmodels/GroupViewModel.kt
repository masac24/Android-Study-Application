package com.example.diplomski.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.diplomski.StudyApplication
import com.example.diplomski.database.tables.Comment
import com.example.diplomski.database.tables.Group
import com.example.diplomski.database.tables.GroupMember
import com.example.diplomski.database.tables.GroupNote
import com.example.diplomski.database.tables.GroupQuiz
import com.example.diplomski.database.tables.Message
import com.example.diplomski.database.tables.Note
import com.example.diplomski.database.tables.Quiz
import com.example.diplomski.database.tables.User
import com.example.diplomski.repositories.CommentRepository
import com.example.diplomski.repositories.GroupMemberRepository
import com.example.diplomski.repositories.GroupNoteRepository
import com.example.diplomski.repositories.GroupQuizRepository
import com.example.diplomski.repositories.GroupRepository
import com.example.diplomski.repositories.MessageRepository
import com.example.diplomski.repositories.NoteRepository
import com.example.diplomski.repositories.QuestionRepository
import com.example.diplomski.repositories.QuizRepository
import com.example.diplomski.repositories.UserRepository
import com.example.diplomski.retrofit.RetrofitClient
import com.example.diplomski.websocket.GroupRefreshManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch


data class GroupWithMember(
    val group: Group,
    val members: List<String>,
    val notes: List<NoteWithUser>,
    val quizzes: List<QuizWithUser>
)

data class QuizWithUser(
    val quiz: Quiz,
    val questionCount: Int
)

data class NoteWithUser(
    val note: Note,
    val username: String,
    val comments: List<Comment>
)

data class NoteWithComments(
    val note: Note = Note(0,null, "","",""),
    val comments: List<Comment> = listOf()
)

data class QuizWithComments(
    val quiz: Quiz = Quiz(0, "","",0),
    val comments: List<Comment> = listOf()
)

class GroupViewModel(
    private val groupRepository: GroupRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepository,
    private val groupNoteRepository: GroupNoteRepository,
    private val groupQuizRepository: GroupQuizRepository,
    private val quizRepository: QuizRepository,
    private val noteRepository: NoteRepository,
    private val questionRepository: QuestionRepository,
    private val commentRepository: CommentRepository
) : ViewModel() {

    init {
        viewModelScope.launch {

            GroupRefreshManager.refreshEvents.collect { serverUserId ->

                Log.d(
                    "GROUP_REFRESH",
                    "WebSocket refresh received for user $serverUserId"
                )

                refreshGroups(serverUserId)
            }
        }
    }
    private val _groups = MutableStateFlow<List<GroupWithMember>>(emptyList())
    val groups = _groups.asStateFlow()

    private val _group = MutableStateFlow<GroupWithMember?>(null)
    val group = _group.asStateFlow()

    private val _message = mutableStateOf("")
    val message = _message

    private var groupsJob: Job? = null

    var notePreview = mutableStateOf(NoteWithComments())
        private set

    var quizPreview = mutableStateOf(QuizWithComments())
        private set

    fun refreshGroups(serverUserId: Long) {

        viewModelScope.launch {

            val localUser =
                userRepository
                    .getBySyncId(serverUserId.toInt())
                    ?: return@launch

            loadGroups(localUser.id)
        }
    }

//    fun getGroups(id: Int){
//        viewModelScope.launch {
//            groupMemberRepository.getUserGroups(id).collect { groups ->
//                val withCounts = groups.map { group ->
//                    val groupQuizzes = groupQuizRepository
//                        .getGroupQuizzes(group.id)
//                        .first()
//
//                    val quizzes = groupQuizzes.map { groupQuiz ->
//                        val quiz = quizRepository.getQuiz(groupQuiz.quizId).first()
//                        val count = questionRepository.getNumberOfQuestions(quiz.id)
//                        QuizWithUser(quiz = quiz, questionCount = count)
//                    }
//                    val groupNotes = groupNoteRepository.getGroupNotes(group.id).first()
//                    val notes = groupNotes.map { groupNote ->
//                        val note = noteRepository.getNote(groupNote.noteId).first()
//                        val comments = commentRepository.getNoteComments(note.id).first()
//                        NoteWithUser(note, groupNote.username,comments)
//                    }
//                    val members = mutableStateListOf<String>()
//                    try {
//                        RetrofitClient.api.getGroupMembers(group.syncId ?: return@collect).map {
//                                memberDto -> val user = RetrofitClient.api.getUserById(memberDto.userId)
//                            members.add(user.username)
//                        }
//                    } catch (e: Exception){
//                        Log.e("NO INTERNET", "Cant get members")
//                    }
//                    GroupWithMember(
//                        group = group,
//                        members = members,
//                        notes = notes,
//                        quizzes = quizzes
//                    )
//                }
//                _groups.value = withCounts
//            }
//        }
//    }

    fun getGroups(userId: Int) {
        loadGroups(userId)
    }

    private fun loadGroups(userId: Int) {

        groupsJob?.cancel()

        groupsJob = viewModelScope.launch {

            groupMemberRepository.getUserGroups(userId).collect { userGroups ->

                val withCounts = userGroups.map { group ->

                    val groupQuizzes =
                        groupQuizRepository
                            .getGroupQuizzes(group.id)
                            .first()

                    val quizzes = groupQuizzes.map { groupQuiz ->

                        val quiz =
                            quizRepository
                                .getQuiz(groupQuiz.quizId)
                                .first()

                        val count =
                            questionRepository
                                .getNumberOfQuestions(quiz.id)

                        QuizWithUser(
                            quiz = quiz,
                            questionCount = count
                        )
                    }

                    val groupNotes =
                        groupNoteRepository
                            .getGroupNotes(group.id)
                            .first()

                    val notes = groupNotes.map { groupNote ->

                        val note =
                            noteRepository
                                .getNote(groupNote.noteId)
                                .first()

                        val comments =
                            commentRepository
                                .getNoteComments(note.id)
                                .first()

                        NoteWithUser(
                            note = note,
                            username = groupNote.username,
                            comments = comments
                        )
                    }

                    val members = mutableListOf<String>()

                    try {
                        if (group.syncId != null) {

                            RetrofitClient.api
                                .getGroupMembers(group.syncId.toLong())
                                .forEach { memberDto ->

                                    val user =
                                        RetrofitClient.api
                                            .getUserById(memberDto.userId)

                                    members.add(user.username)
                                }
                        }

                    } catch (e: Exception) {

                        Log.e(
                            "NO INTERNET",
                            "Cant get members",
                            e
                        )
                    }

                    GroupWithMember(
                        group = group,
                        members = members,
                        notes = notes,
                        quizzes = quizzes
                    )
                }

                _groups.value = withCounts

                _group.value?.let { currentGroup ->
                    if (withCounts.any {
                            it.group.id == currentGroup.group.id
                        }) {

                        getGroup(currentGroup.group.id)
                    }
                }
            }
        }
    }

    fun getGroup(i: Int) {
        viewModelScope.launch {
            val group = groupRepository.getGroup(i) ?: return@launch
            val groupQuizzes = groupQuizRepository
                .getGroupQuizzes(group.id)
                .first()

            val quizzes = groupQuizzes.map { groupQuiz ->
                val quiz = quizRepository.getQuiz(groupQuiz.quizId).first()
                val count = questionRepository.getNumberOfQuestions(quiz.id)
                QuizWithUser(quiz = quiz,questionCount = count)
            }
            val groupNotes = groupNoteRepository.getGroupNotes(group.id).first()
            val notes = groupNotes.map { groupNote ->
                val note = noteRepository.getNote(groupNote.noteId).first()
                val comments = commentRepository.getNoteComments(note.id).first()
                NoteWithUser(note, groupNote.username,comments)
            }
            val members = mutableStateListOf<String>()
            try{
                RetrofitClient.api.getGroupMembers(group.syncId?.toLong() ?: return@launch).map {
                        memberDto -> val user = RetrofitClient.api.getUserById(memberDto.userId)
                    members.add(user.username)
                }
            }catch (e: Exception){
                Log.e("NO INTERNET", "Cant get members")
            }
            val gwm = GroupWithMember(
                group = group,
                members = members,
                notes = notes,
                quizzes = quizzes

            )
            _group.value = gwm
        }
    }

    fun inviteFriend(n: String, userSender: String){
        viewModelScope.launch {
            val receiver = userRepository.search(n)
            if(receiver == null){
                _message.value = "No user found"
                return@launch
            }else{
                _message.value = "Successfully sent"
            }
            val response = RetrofitClient.api.getMessage(
                receiver,
                group.value!!.group.syncId!!
            )

            when {
                response.isSuccessful -> {
                    _message.value = "Request already sent"
                    return@launch
                }

                response.code() == 404 -> {

                    messageRepository.insert(
                        Message(
                            id = 0,
                            sender = userSender,
                            quizId = null,
                            groupId = group.value!!.group.id,
                            noteId = null,
                            receiver = receiver.toInt()
                        )
                    )

                    _message.value = "Successfully sent"
                }

                else -> {
                    _message.value = "Error checking request"
                }
            }
        }
    }

    fun uploadQuiz(q: Quiz, g: Group){
        viewModelScope.launch {
            groupQuizRepository.insert(GroupQuiz(0, q.id, g.id))
        }
    }

    fun uploadNote(path: String, title: String, g: Group, user: User){
        val last = path.split(".").last()
        var type = "IMAGE"
        if(last !in listOf("png", "jpg", "jpeg")) type = "PDF"
        if(last == "txt") type = "TEXT"
        viewModelScope.launch {
            val id = noteRepository.insert(Note(0, null, title, path, type))
            groupNoteRepository.insert(GroupNote(0, noteId = id.toInt(), groupId = g.id, username = user.username))
        }
    }

    fun renameNote(name: String, note: Note) {
        viewModelScope.launch {
            noteRepository.update(Note(note.id, note.folderId,name, note.path,
                note.type, note.syncId))
            getGroup(group.value?.group?.id ?: return@launch)
        }
    }

    fun createGroup(n: String, user: String, id: Int) {
        viewModelScope.launch {
            groupRepository.insert(Group(0, n, user), id)
        }
    }

    fun getNote(i: Int) {
        viewModelScope.launch {
            val note = noteRepository.getNote(i).firstOrNull()
            if(note != null) {
                val comments = commentRepository.getNoteComments(note.id).first()
                notePreview.value = NoteWithComments(note, comments)
            }
        }
    }

    fun saveNote(note: Note, folderId: Int){
        viewModelScope.launch {
            noteRepository.insert(Note(0, folderId, note.title, note.path, note.type))
        }
    }

    fun getQuiz(i: Int) {
        viewModelScope.launch {
            val quiz = quizRepository.getQuiz(i).firstOrNull()
            if(quiz != null){
                val comments = commentRepository.getQuizComments(quiz.id).first()
                quizPreview.value = QuizWithComments(quiz, comments)
            }
        }
    }

    fun deleteFile(id: Int, groupId: Int, folderId: Int?){
        viewModelScope.launch {
            groupNoteRepository.delete(id, groupId)
            if(folderId == null) noteRepository.delete(id)
            getGroup(groupId)
        }
    }

    fun deleteQuiz(id: Int, groupId: Int){
        viewModelScope.launch {
            groupQuizRepository.delete(id, groupId)
            getGroup(groupId)
        }
    }

    fun deleteComment(id: Int, groupId: Int){
        viewModelScope.launch {
            commentRepository.delete(id)
            getGroup(groupId)
        }
    }

    fun saveComment(text: String, username: String, groupId: Int, noteId: Int?, quizId:Int?){
        viewModelScope.launch {
            commentRepository.insert(Comment(text = text,
                user = username, groupId = groupId, noteId = noteId, quizId = quizId,
                createdAt = System.currentTimeMillis()))
            if(noteId != null)getNote(noteId)
            if(quizId != null)getQuiz(quizId)
            getGroup(groupId)
        }
    }

    fun renameGroup( name : String, group : Group){
        viewModelScope.launch {
            groupRepository.update(Group(group.id, name, group.user, group.syncId))
            getGroup(group.id)
        }
    }

    fun leaveGroup(userId: Int, groupId: Int){
        viewModelScope.launch {
            groupMemberRepository.leave(userId, groupId)
        }
    }

    fun deleteGroup(id: Int){
        viewModelScope.launch {
            commentRepository.deleteByGroup(id)
            groupNoteRepository.deleteByGroup(id)
            groupQuizRepository.deleteForGroup(id)
            groupMemberRepository.deleteByGroup(id)
            groupRepository.delete(id)
        }
    }

    fun resetMessage(){
        _message.value = ""
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val groupRepository = (this[APPLICATION_KEY] as StudyApplication).groupRepository
                val groupMemberRepository = (this[APPLICATION_KEY] as StudyApplication).groupMemberRepository
                val userRepository = (this[APPLICATION_KEY] as StudyApplication).userRepository
                val messageRepository = (this[APPLICATION_KEY] as StudyApplication).messageRepository
                val groupNoteRepository = (this[APPLICATION_KEY] as StudyApplication).groupNoteRepository
                val groupQuizRepository = (this[APPLICATION_KEY] as StudyApplication).groupQuizRepository
                val quizRepository = (this[APPLICATION_KEY] as StudyApplication).quizRepository
                val noteRepository = (this[APPLICATION_KEY] as StudyApplication).noteRepository
                val questionRepository = (this[APPLICATION_KEY] as StudyApplication).questionRepository
                val commentRepository = (this[APPLICATION_KEY] as StudyApplication).commentRepository
                return@initializer GroupViewModel(groupRepository, groupMemberRepository,
                    userRepository, messageRepository, groupNoteRepository, groupQuizRepository,
                    quizRepository, noteRepository, questionRepository, commentRepository)
            }
        }
    }
}