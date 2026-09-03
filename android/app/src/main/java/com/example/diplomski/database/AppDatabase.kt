package com.example.diplomski.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import com.example.diplomski.database.daos.ReminderDao
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
import com.example.diplomski.database.tables.Reminder
import com.example.diplomski.database.tables.Score
import com.example.diplomski.database.tables.Session
import com.example.diplomski.database.tables.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [User::class, Quiz::class, Question::class, Score::class,
    Folder::class, Note::class, Checklist::class, Reminder::class, Session::class,
    Message::class, Group::class, GroupMember::class, GroupNote::class, GroupQuiz::class,
    Comment::class],
    version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun quizDao(): QuizDao
    abstract fun questionDao(): QuestionDao
    abstract fun scoreDao() : ScoreDao
    abstract fun folderDao() : FolderDao
    abstract fun noteDao(): NoteDao
    abstract fun checklistDao() : ChecklistDao
    abstract fun reminderDao() : ReminderDao
    abstract fun sessionDao() : SessionDao
    abstract fun messageDao(): MessageDao
    abstract fun groupDao(): GroupDao
    abstract fun groupMemberDao() : GroupMemberDao
    abstract fun groupNoteDao() : GroupNoteDao
    abstract fun groupQuizDao() : GroupQuizDao
    abstract fun commentDao(): CommentDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).addCallback(DatabaseCallback(scope)).build()
                INSTANCE = instance
                // return instance
                return@synchronized instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch { populateDatabase(database.userDao(),
                    database.quizDao(), database.questionDao()) }
            }
        }

        suspend fun populateDatabase(userDao: UserDao, quizDao: QuizDao, questionDao: QuestionDao) {
            userDao.deleteAll()
            quizDao.deleteAll()
            questionDao.deleteAll()
        }
    }
}
