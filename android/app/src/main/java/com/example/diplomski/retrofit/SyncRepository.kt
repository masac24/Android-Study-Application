package com.example.diplomski.retrofit

import android.util.Log
import com.example.diplomski.database.daos.ChecklistDao
import com.example.diplomski.database.daos.CommentDao
import com.example.diplomski.database.daos.FolderDao
import com.example.diplomski.database.daos.GroupDao
import com.example.diplomski.database.daos.GroupMemberDao
import com.example.diplomski.database.daos.GroupNoteDao
import com.example.diplomski.database.daos.GroupQuizDao
import com.example.diplomski.database.daos.MessageDao
import com.example.diplomski.database.daos.NoteDao
import com.example.diplomski.database.daos.QuestionDao
import com.example.diplomski.database.daos.QuizDao
import com.example.diplomski.database.daos.ScoreDao
import com.example.diplomski.database.daos.SessionDao
import com.example.diplomski.database.daos.UserDao
import com.example.diplomski.database.tables.Checklist
import com.example.diplomski.database.tables.Comment
import com.example.diplomski.database.tables.Folder
import com.example.diplomski.database.tables.Group
import com.example.diplomski.database.tables.GroupMember
import com.example.diplomski.database.tables.GroupNote
import com.example.diplomski.database.tables.GroupQuiz
import com.example.diplomski.database.tables.Message
import com.example.diplomski.database.tables.Note
import com.example.diplomski.database.tables.Question
import com.example.diplomski.database.tables.Quiz
import com.example.diplomski.database.tables.Score
import com.example.diplomski.database.tables.Session
import com.example.diplomski.repositories.NoteRepository
import com.example.diplomski.retrofit.dto.ChecklistDto
import com.example.diplomski.retrofit.dto.CommentDto
import com.example.diplomski.retrofit.dto.FolderDto
import com.example.diplomski.retrofit.dto.GroupDto
import com.example.diplomski.retrofit.dto.GroupMemberDto
import com.example.diplomski.retrofit.dto.GroupNoteDto
import com.example.diplomski.retrofit.dto.GroupQuizDto
import com.example.diplomski.retrofit.dto.MessageDto
import com.example.diplomski.retrofit.dto.NoteDto
import com.example.diplomski.retrofit.dto.QuestionDto
import com.example.diplomski.retrofit.dto.QuizDto
import com.example.diplomski.retrofit.dto.ScoreDto
import com.example.diplomski.retrofit.dto.SessionDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

class SyncRepository(
    private val api: BackendApi,
    private val quizDao: QuizDao,
    private val questionDao: QuestionDao,
    private val folderDao: FolderDao,
    private val noteDao: NoteDao,
    private val sessionDao: SessionDao,
    private val scoreDao: ScoreDao,
    private val checklistDao: ChecklistDao,
    private val noteRepository: NoteRepository,
    private val messageDao: MessageDao,
    private val groupDao: GroupDao,
    private val groupMemberDao: GroupMemberDao,
    private val userDao: UserDao,
    private val groupNoteDao: GroupNoteDao,
    private val groupQuizDao: GroupQuizDao,
    private val commentDao: CommentDao
) {
    private var isSyncing = false

    suspend fun sync(userId: Int) {
        try {
            if (isSyncing) {
                Log.d("SYNC", "Sync already in progress, skipping")
                return
            }
            isSyncing = true
            syncGroups(userId)
            syncFolders(userId)
            syncQuizzes(userId)
            syncSessions(userId)
            syncScores(userId)
            syncChecklist(userId)
            syncNotes(userId)
            messageDao.deleteAll()
            syncMessages(userId)

        } catch (e: Exception) {
            Log.e("SYNC", "Sync failed: ${e.message}")
        }finally {
            isSyncing = false
        }
    }

    private suspend fun syncQuizzes(userId: Int) {
        val unsyncedQuizzes = quizDao.getUnsynced()

        unsyncedQuizzes.forEach { quiz ->
            val response = api.createQuiz(QuizDto(name = quiz.name, userId = userId.toLong()))
            quizDao.updateSyncId(quiz.id, response.id)
        }

        val localUser = userDao.getBySyncId(userId.toLong()) ?: return
        val localQuizzes = quizDao.getAllQuizzesForUser(localUser.username).first()

        localQuizzes.forEach { quiz ->
            val serverQuizId = quiz.syncId ?: return@forEach
            val questions = questionDao.getAllQuestionsForQuiz(quiz.id).first()
            questions.forEach { question ->
                if (question.syncId != null) return@forEach
                val response = api.createQuestion(
                    QuestionDto(
                        text = question.text,
                        answer = question.answer,
                        type = question.type,
                        option1 = question.option1,
                        option2 = question.option2,
                        option3 = question.option3,
                        quizId = serverQuizId
                    )
                )
                questionDao.updateSyncId(question.id, response.id)
            }
        }

        val serverQuizzes = api.getQuizzes(userId.toLong())
        serverQuizzes.forEach { dto ->
            val localQuiz = quizDao.getBySyncId(dto.id)
            val localQuizId = localQuiz?.id ?: quizDao.insert(
                        Quiz(
                            name = dto.name,
                            user = localUser.username,
                            syncId = dto.id
                        )
                    ).toInt()

            val serverQuestions = api.getQuestions(dto.id)
            val existingQuestionSyncIds = questionDao.getAllSyncIds().toSet()
            val syncIds = serverQuestions.map { it.id }
            val localQ = questionDao.getAllQuestionsForQuiz(localQuizId).firstOrNull()

            localQ?.forEach { q ->
                if(q.syncId !in syncIds) questionDao.deleteQuestion(q.id)
            }
            serverQuestions.forEach { qDto ->
                if (qDto.id !in existingQuestionSyncIds) {
                    questionDao.insert(Question(
                        text = qDto.text,
                        answer = qDto.answer,
                        type = qDto.type,
                        option1 = qDto.option1,
                        option2 = qDto.option2,
                        option3 = qDto.option3,
                        quizId = localQuizId,
                        syncId = qDto.id
                    ))
                }
            }
        }
        val syncIds = serverQuizzes.map { it.id }
        localQuizzes.forEach { quiz ->
            if(quiz.syncId !in syncIds) quizDao.deleteQuiz(quiz.id)
        }
    }
    private suspend fun syncSessions(userId: Int) {
        val unsyncedSessions = sessionDao.getUnsynced()
        unsyncedSessions.forEach { session ->
            val response = api.createSession(
                SessionDto(
                    time = session.time,
                    datetime = session.datetime,
                    completed = session.completed,
                    userId = userId.toLong()
                )
            )
            sessionDao.updateSyncId(session.id, response.id)
        }
        val localId = userDao.getBySyncId(userId.toLong())?.id ?: return
        val existingSyncIds = sessionDao.getAllSyncIds(localId).toSet()
        val serverSessions = api.getSessions(userId.toLong())
        val syncIds = serverSessions.map { it.id }

        existingSyncIds.forEach { id ->
            val ses = sessionDao.getBySyncId(id)
            if(id !in syncIds) sessionDao.deleteSession(ses?.id ?: return@forEach)
        }

        serverSessions.forEach { dto ->
            if (sessionDao.getBySyncId(dto.id) == null) {
                if (dto.id !in existingSyncIds) {
                    sessionDao.insert(
                        Session(
                            time = dto.time,
                            datetime = dto.datetime,
                            completed = dto.completed,
                            userId = localId,
                            syncId = dto.id
                        )
                    )
                }
            }
        }
    }

    private suspend fun syncScores(userId: Int) {
        val unsyncedScores = scoreDao.getUnsynced()
        unsyncedScores.forEach { score ->
            val quiz = quizDao.getQuiz(score.quizId).first()
            quiz.syncId?.let { quizSyncId ->
                val response = api.createScore(
                    ScoreDto(score = score.score, userId = userId.toLong(), quizId = quizSyncId)
                )
                scoreDao.updateSyncId(score.id, response.id)
            }
        }
        val localId = userDao.getBySyncId(userId.toLong())?.id ?: return
        val existingSyncIds = scoreDao.getAllSyncIds(localId).toSet()
        val serverScores = api.getScores(userId.toLong())
        val syncIds = serverScores.map { it.id }
        existingSyncIds.forEach { id ->
            val sc = scoreDao.getBySyncId(id)
            if(id !in syncIds) scoreDao.delete(sc?.id ?: return@forEach)
        }
        serverScores.forEach { dto ->
            if (scoreDao.getBySyncId(dto.id) == null) {
                if (dto.id !in existingSyncIds) {
                    val localQuiz = quizDao.getBySyncId(dto.quizId)
                    localQuiz?.let {
                        scoreDao.insert(
                            Score(
                                score = dto.score,
                                userId = localId,
                                quizId = it.id,
                                syncId = dto.id
                            )
                        )
                    }
                }
            }
        }
    }

    private suspend fun syncChecklist(userId: Int) {
        val unsyncedItems = checklistDao.getUnsynced()
        unsyncedItems.forEach { item ->
            val response = api.createChecklist(
                ChecklistDto(text = item.text, done = item.done, userId = userId.toLong())
            )
            checklistDao.updateSyncId(item.id, response.id)
        }
        val localId = userDao.getBySyncId(userId.toLong())?.id ?: return
        val existingSyncIds = checklistDao.getAllSyncIds(localId).toSet()

        val serverItems = api.getChecklist(userId.toLong())
        val syncIds = serverItems.map { it.id }
        existingSyncIds.forEach { id ->
            val sc = checklistDao.getBySyncId(id)
            if(id !in syncIds) checklistDao.delete(sc?.id ?: return@forEach)
        }
        serverItems.forEach { dto ->
            if (checklistDao.getBySyncId(dto.id) == null) {
                if (dto.id !in existingSyncIds) {
                    checklistDao.insert(
                        Checklist(
                            text = dto.text,
                            done = dto.done,
                            userId = localId,
                            syncId = dto.id
                        )
                    )
                }
            }
        }
    }


    suspend fun syncGroups(userId: Int) {
        val localUser = userDao.getBySyncId(userId.toLong()) ?: return
        val localUserId = localUser.id

        val unsyncedGroups = groupDao.getUnsynced()
        unsyncedGroups.forEach { group ->
            if (group.user != localUser.username) return@forEach
            try {
                val response = api.createGroup(GroupDto(name = group.name, userId = userId.toLong()))
                groupDao.updateSyncId(group.id, response.id)
            } catch (e: Exception) {
                Log.e("SYNC", "Group push failed: ${e.message}")
            }
        }

        val localGroups = groupMemberDao.getUserGroups(localUser.id).first()
        localGroups.forEach { group ->
            val serverGroupId = group.syncId ?: return@forEach

            val members = groupMemberDao.getGroupMemberObjects(group.id).first()
            members.forEach { member ->
                if (member.syncId != null) return@forEach
                try {
                    val localMemberUser = userDao.getUser(member.userId).firstOrNull() ?: return@forEach
                    val serverUserId = localMemberUser.syncId ?: return@forEach
                    val response = api.createGroupMember(
                        GroupMemberDto(userId = serverUserId, groupId = serverGroupId)
                    )
                    groupMemberDao.updateSyncId(member.id, response.id)
                } catch (e: Exception) {
                    Log.e("SYNC", "GroupMember push failed: ${e.message}")
                }
            }

            val notes = groupNoteDao.getGroupNotes(group.id).first()
            notes.forEach { note ->
                if(note.syncId != null) return@forEach
                try {
                    val localNote = noteDao.getNote(note.noteId).firstOrNull() ?: return@forEach
                    val serverNoteId = localNote.syncId ?: return@forEach
                    val response = api.createGroupNote(
                        GroupNoteDto(groupId = serverGroupId, noteId = serverNoteId, username = note.username)
                    )
                    groupNoteDao.updateSyncId(note.id, response.id)
                    val comments = commentDao.getNoteComments(note.noteId).firstOrNull()
                    comments?.forEach { comment->
                        if(comment.syncId != null) return@forEach
                        val response = api.createComment(CommentDto(
                            text = comment.text, noteId = serverNoteId, quizId = null,
                            createdAt = comment.createdAt, groupId = serverGroupId, userId = userId.toLong()
                        ))
                        commentDao.updateSyncId(comment.id, response.id)
                    }
                }catch (e: Exception) {
                    Log.e("SYNC", "GroupNote push failed: ${e.message}")
                }
            }

            val quizzes = groupQuizDao.getGroupQuizzes(group.id).first()
            quizzes.forEach { quiz ->
                if(quiz.syncId != null) return@forEach
                try {
                    val localQuiz = quizDao.getQuiz(quiz.quizId).firstOrNull() ?: return@forEach
                    val serverQuizId = localQuiz.syncId ?: return@forEach
                    val response = api.createGroupQuiz(
                        GroupQuizDto(groupId = serverGroupId, quizId = serverQuizId)
                    )
                    groupQuizDao.updateSyncId(quiz.id, response.id)
                    val comments = commentDao.getQuizComments(quiz.quizId).firstOrNull()
                    comments?.forEach { comment->
                        if(comment.syncId != null) return@forEach
                        val response = api.createComment(CommentDto(
                            text = comment.text, noteId = null, quizId = serverQuizId,
                            createdAt = comment.createdAt, groupId = serverGroupId, userId = userId.toLong()
                        ))
                        commentDao.updateSyncId(comment.id, response.id)
                    }
                }catch (e: Exception) {
                    Log.e("SYNC", "GroupQuiz push failed: ${e.message}")
                }
            }
        }

        val memberships = api.getUserGroups(userId.toLong())
        val serverGroupIds = memberships.map { it.groupId }.toSet()

        memberships.forEach { membership ->
            val serverGroupId = membership.groupId
            var localGroup = groupDao.getBySyncId(serverGroupId)
            if (localGroup == null) {
                try {
                    val serverGroup = api.getGroupById(serverGroupId)
                    val author = api.getUserById(serverGroup.userId)
                    val newLocalId = groupDao.insert(
                        Group(name = serverGroup.name, user = author.username, syncId = serverGroup.id)
                    )
                    localGroup = groupDao.getById(newLocalId.toInt())
                } catch (e: Exception) {
                    Log.e("SYNC", "Group pull failed: ${e.message}")
                    return@forEach
                }
            }

            val localGroup2 = localGroup ?: return@forEach
            val existingMemberSyncIds = groupMemberDao.getAllSyncIdsForGroup(localGroup2.id).toSet()
            if (membership.id !in existingMemberSyncIds) {
                groupMemberDao.addGroupMember(GroupMember(userId = localUserId, groupId = localGroup.id, syncId = membership.id))
            }


            try {
                val serverNotes = api.getGroupNotes(serverGroupId)
                val serverNoteSyncIds = serverNotes.map { it.id }
                val existingNoteSyncIds = groupNoteDao.getAllSyncIdsForGroup(localGroup2.id)

                serverNotes.forEach { noteDto ->
                    var localNote = noteDao.getBySyncId(noteDto.noteId)
                    if (localNote == null) {
                        try {
                            val serverNote = api.getNoteById(noteDto.noteId)
                            val localPath = noteRepository.downloadAndSave(serverNote.path)
                            val newNote = noteDao.insert(
                                Note(
                                    folderId = null,
                                    title = serverNote.title,
                                    path = localPath,
                                    type = serverNote.type,
                                    serverPath = serverNote.path,
                                    syncId = serverNote.id
                                )
                            )
                            localNote = noteDao.getNote(newNote.toInt()).firstOrNull()
                        } catch (e: Exception) {
                            Log.e("SYNC", "Note fetch failed: ${e.message}")
                            return@forEach
                        }
                    }

                    val localMember = localNote ?: return@forEach
                    if (noteDto.id !in existingNoteSyncIds) {
                        groupNoteDao.insert(
                            GroupNote(
                                noteId = localMember.id,
                                groupId = localGroup2.id,
                                syncId = noteDto.id,
                                username = noteDto.username
                            )
                        )
                    }

                    val serverComments = api.getNoteComments(noteDto.noteId)
                    val serverSyncIds = serverComments.map { it.id }
                    val localComments = commentDao.getNoteComments(localMember.id)
                    localComments.firstOrNull()?.forEach { comment ->
                        if(comment.syncId !in serverSyncIds) commentDao.delete(comment.id)
                    }
                    serverComments.forEach { commentDto ->
                        val localComment = commentDao.getBySyncId(commentDto.id)
                        if (localComment == null) {
                            try {
                                val user = api.getUserById(commentDto.userId)
                                commentDao.insert(
                                    Comment(
                                        text = commentDto.text,
                                        createdAt = commentDto.createdAt,
                                        groupId = localGroup.id,
                                        noteId = localNote.id,
                                        quizId = null,
                                        user = user.username,
                                        syncId = commentDto.id
                                    )
                                )
                            } catch (e: Exception) {
                                Log.e("SYNC", "Note fetch failed: ${e.message}")
                                return@forEach
                            }
                        }
                    }

                }
                existingNoteSyncIds.forEach { id ->
                    val n = groupNoteDao.getBySyncId(id)
                    if(id !in serverNoteSyncIds && n != null) {
                        groupNoteDao.delete(n.id, localGroup2.id)
                        val note = noteDao.getNote(n.noteId).firstOrNull()
                        if (note != null && note.folderId == null) {
                            noteDao.delete(note.id)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SYNC", "GroupNotes pull failed: ${e.message}")
            }

            try {
                val serverQuizzes = api.getGroupQuizzes(serverGroupId)
                val serverQuizSyncIds = serverQuizzes.map { it.id }
                val existingQuizSyncIds = groupQuizDao.getAllSyncIdsForGroup(localGroup2.id).toSet()

                serverQuizzes.forEach { quizDto ->
                    var localQuiz = quizDao.getBySyncId(quizDto.quizId)
                    val serverQuiz = api.getQuizById(quizDto.quizId)
                    if (localQuiz == null) {
                        try {
                            val serverUser = api.getUserById(serverQuiz.userId)
                            val newQuiz = quizDao.insert(
                                Quiz(
                                    name = serverQuiz.name,
                                    user = serverUser.username,
                                    syncId = quizDto.quizId
                                )
                            )
                            localQuiz = quizDao.getQuiz(newQuiz.toInt()).firstOrNull()

                        } catch (e: Exception) {
                            Log.e("SYNC", "Note fetch failed: ${e.message}")
                            return@forEach
                        }
                    }
                    val serverQuestions = api.getQuestions(serverQuiz.id)
                    val existingQuestionSyncIds = questionDao.getAllSyncIds().toSet()

                    serverQuestions.forEach { qDto ->
                        if (qDto.id !in existingQuestionSyncIds) {
                            questionDao.insert(Question(
                                text = qDto.text,
                                answer = qDto.answer,
                                type = qDto.type,
                                option1 = qDto.option1,
                                option2 = qDto.option2,
                                option3 = qDto.option3,
                                quizId = localQuiz?.id ?: return,
                                syncId = qDto.id
                            ))
                        }
                    }
                    val localMember = localQuiz ?: return@forEach
                    if (quizDto.id !in existingQuizSyncIds){
                        groupQuizDao.insert(
                            GroupQuiz(
                                quizId = localMember.id,
                                groupId = localGroup2.id,
                                syncId = quizDto.id
                            )
                        )
                    }


                    val serverComments = api.getQuizComments(quizDto.quizId)
                    val serverSyncIds = serverComments.map { it.id }
                    val localComments = commentDao.getQuizComments(localMember.id)
                    localComments.firstOrNull()?.forEach { comment ->
                        if(comment.syncId !in serverSyncIds) commentDao.delete(comment.id)
                    }
                    serverComments.forEach { commentDto ->
                        val localComment = commentDao.getBySyncId(commentDto.id)
                        if (localComment == null) {
                            try {
                                val user = api.getUserById(commentDto.userId)
                                commentDao.insert(
                                    Comment(
                                        text = commentDto.text,
                                        createdAt = commentDto.createdAt,
                                        groupId = localGroup.id,
                                        noteId = null,
                                        quizId = localQuiz.id,
                                        user = user.username,
                                        syncId = commentDto.id
                                    )
                                )
                            } catch (e: Exception) {
                                Log.e("SYNC", "Note fetch failed: ${e.message}")
                                return@forEach
                            }
                        }
                    }
                }
                existingQuizSyncIds.forEach { id ->
                    val q = groupQuizDao.getBySyncId(id)
                    if(id !in serverQuizSyncIds && q != null) {
                        groupQuizDao.delete(q.id, localGroup2.id)
                    }
                }
            } catch (e: Exception) {
                Log.e("SYNC", "GroupNotes pull failed: ${e.message}")
            }
            val serverMembers = api.getGroupMembers(serverGroupId)
            val serverMemberSyncIds = serverMembers.map { it.id }.toSet()
            val localMembers = groupMemberDao.getGroupMembersList(localGroup2.id)
            localMembers.forEach { member ->
                val syncId = member.syncId ?: return@forEach
                if (syncId !in serverMemberSyncIds) {
                    groupMemberDao.delete(member.userId, member.groupId)
                }
            }
        }

        localGroups.forEach { group ->
            val syncId = group.syncId ?: return@forEach
            if (syncId !in serverGroupIds) {
                groupDao.delete(group.id)
            }
        }
    }

    private suspend fun syncFolders(userId: Int) {
        val unsyncedFolders = folderDao.getUnsynced()
        unsyncedFolders.forEach { folder ->
            val response = api.createFolder(
                FolderDto(name = folder.name, userId = userId.toLong())
            )
            folderDao.updateSyncId(folder.id, response.id)
        }
        val localId = userDao.getBySyncId(userId.toLong())?.id ?: return
        val existingSyncIds = folderDao.getAllSyncIds(localId).toSet()

        val serverFolders = api.getFolders(userId.toLong())
        val syncIds = serverFolders.map { it.id }
        existingSyncIds.forEach { id ->
            val sc = folderDao.getBySyncId(id)
            if(id !in syncIds) folderDao.delete(sc?.id ?: return@forEach)
        }
        serverFolders.forEach { dto ->
            if (dto.id !in existingSyncIds) {
                folderDao.insert(Folder(name = dto.name, userId = localId, syncId = dto.id))
            }
        }
    }

    private suspend fun syncNotes(userId: Int) {
        val unsyncedNotes = noteDao.getUnsynced()
        unsyncedNotes.forEach { note ->
            try {
                val serverPath = if (note.serverPath == null) {
                    val uploaded = noteRepository.uploadFile(note.path)
                    noteDao.updateServerPath(note.id, uploaded)
                    uploaded
                } else {
                    note.serverPath
                }
                val folderSyncId = note.folderId?.let { folderDao.getFolder(it).firstOrNull()?.syncId }
                val response = api.createNote(
                    NoteDto(
                        title = note.title,
                        path = serverPath,
                        type = note.type,
                        folderId = folderSyncId
                    )
                )
                noteDao.updateSyncId(note.id, response.id)

            } catch (e: Exception) {
                Log.e("SYNC", "Note push failed: ${e.message}")
            }
        }

        val serverFolders = api.getFolders(userId.toLong())
        serverFolders.forEach { folderDto ->
            val localFolder = folderDao.getBySyncId(folderDto.id) ?: return@forEach
            val localNotes = noteDao.getFolderFiles(localFolder.id).firstOrNull() ?: emptyList()
            val serverNotes = api.getNotes(folderDto.id)
            val serverSyncIds = serverNotes.map { it.id }.toSet()
            localNotes.forEach { note ->
                if (note.syncId != null && note.syncId !in serverSyncIds) {
                    noteDao.delete(note.id)
                }
            }
            serverNotes.forEach { noteDto ->
                if (noteDao.getBySyncId(noteDto.id) == null) {
                    val localPath = noteRepository.downloadAndSave(noteDto.path)
                    noteDao.insert(
                        Note(
                            folderId = localFolder.id,
                            title = noteDto.title,
                            path = localPath,
                            type = noteDto.type,
                            serverPath = noteDto.path,
                            syncId = noteDto.id
                        )
                    )
                }
            }
        }
    }

     suspend fun syncMessages(userId: Int) {
         val localUser = userDao.getBySyncId(userId.toLong()) ?: return
         val localUserId = localUser.id
        val unsyncedMessages = messageDao.getUnsynced()
        unsyncedMessages.forEach { message ->
            try {
                var quiz: Quiz? = null
                var group: Group? = null
                var note: Note? = null
                if(message.quizId != null)quiz = quizDao.getQuiz(message.quizId).firstOrNull()
                if(message.groupId != null) group = groupDao.getById(message.groupId)
                if(message.noteId != null) note = noteDao.getNote(message.noteId).firstOrNull()
                val response = api.createMessage(
                    MessageDto(
                        sender = message.sender,
                        quizId = quiz?.syncId,
                        groupId = group?.syncId,
                        noteId = note?.syncId,
                        receiver = message.receiver.toLong(),
                    )
                )
                messageDao.updateSyncId(message.id, response.id)

            } catch (e: Exception) {
                Log.e("SYNC", "Message push failed: ${e.message}")
            }
        }

        val serverMessages = api.getMessages(userId.toLong())
        serverMessages.forEach { dto ->
            if (messageDao.getBySyncId(dto.id) == null) {
                messageDao.insert(
                    Message(
                        sender = dto.sender,
                        quizId = dto.quizId?.toInt(),
                        receiver = localUserId,
                        syncId = dto.id,
                        groupId = dto.groupId?.toInt(),
                        noteId = dto.noteId?.toInt()
                    )
                )
            }
        }
    }
}