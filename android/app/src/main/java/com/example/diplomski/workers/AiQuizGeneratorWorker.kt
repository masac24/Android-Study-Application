package com.example.diplomski.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.diplomski.database.AppDatabase
import com.example.diplomski.database.tables.Question
import com.example.diplomski.database.tables.Quiz
import com.example.diplomski.retrofit.ai.AIRepository
import com.example.diplomski.retrofit.ai.OCRExtractor
import com.example.diplomski.retrofit.ai.PDFExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull

class QuizGeneratorWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val notePaths = inputData.getStringArray("notePaths") ?: return Result.failure()
            val quizName = inputData.getString("quizName") ?: return Result.failure()
            val username = inputData.getString("username")
            val numberOfQuestions = inputData.getInt("numberOfQuestions", 10)

            setProgress(workDataOf("progress" to 0, "status" to "Extracting text..."))

            val builder = StringBuilder()
            notePaths.forEachIndexed { index, path ->
                val note = AppDatabase.getDatabase(applicationContext,
                    CoroutineScope(SupervisorJob())
                )
                    .noteDao().getNoteByPath(path).firstOrNull()

                when (note?.type) {
                    "PDF" -> builder.append(PDFExtractor.extract(applicationContext, path))
                    "IMAGE" -> builder.append(OCRExtractor.extract( path))
                }
                builder.append("\n\n")

                val extractProgress = ((index + 1).toFloat() / notePaths.size * 50).toInt()
                setProgress(workDataOf("progress" to extractProgress, "status" to "Extracting text..."))
            }

            setProgress(workDataOf("progress" to 50, "status" to "Generating questions..."))

            val questions = AIRepository.generateQuiz(builder.toString())

            setProgress(workDataOf("progress" to 80, "status" to "Saving quiz..."))

            val db = AppDatabase.getDatabase(applicationContext, CoroutineScope(SupervisorJob()))
            if(username != null){
                val quizId = db.quizDao().insert(Quiz(name = quizName, user = username))
                questions.forEach { q ->
                    db.questionDao().insert(
                        Question(
                            text = q.question,
                            answer = q.answer,
                            type = "MULTIPLE",
                            option1 = q.option1,
                            option2 = q.option2,
                            option3 = q.option3,
                            quizId = quizId.toInt()
                        )
                    )
            }

            }

            setProgress(workDataOf("progress" to 100, "status" to "Done!"))
            Result.success()
        } catch (e: Exception) {
            Log.e("WORKER", "Quiz generation failed: ${e.message}")
            Result.failure(workDataOf("error" to e.message))
        }
    }
}