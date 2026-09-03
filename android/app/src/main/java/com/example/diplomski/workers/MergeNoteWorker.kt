package com.example.diplomski.workers

import com.example.diplomski.database.tables.Note
import com.example.diplomski.retrofit.ai.AIMergeGenerator
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

class MergeNoteWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        return try {

            val notePaths =
                inputData.getStringArray("notePaths")
                    ?: return Result.failure()

            val folderId =
                inputData.getInt("folderId", 0)

            if (notePaths.isEmpty()) {
                return Result.failure()
            }

            setProgress(
                workDataOf(
                    "progress" to 0,
                    "status" to "Reading notes..."
                )
            )

            val db = AppDatabase.getDatabase(
                applicationContext,
                CoroutineScope(SupervisorJob())
            )

            val builder = StringBuilder()

            notePaths.forEachIndexed { index, path ->

                val note =
                    db.noteDao()
                        .getNoteByPath(path)
                        .firstOrNull()

                if (note == null) {
                    Log.w(
                        "WORKER",
                        "Note not found: $path"
                    )
                    return@forEachIndexed
                }

                Log.d(
                    "WORKER",
                    "Processing note: ${note.title}, type=${note.type}"
                )

                val text = when (note.type) {

                    "PDF" -> {
                        PDFExtractor.extract(
                            applicationContext,
                            path
                        )
                    }

                    "IMAGE" -> {
                        OCRExtractor.extract(path)
                    }

                    "TEXT" -> {
                        File(path).takeIf { it.exists() }?.readText()
                            ?: ""
                    }

                    else -> {
                        Log.w(
                            "WORKER",
                            "Unsupported note type: ${note.type}"
                        )
                        ""
                    }
                }

                if (text.isNotBlank()) {

                    builder.append(
                        "\n\n===== ${note.title} =====\n\n"
                    )

                    builder.append(text)

                    builder.append("\n\n")
                }

                val progress =
                    ((index + 1).toFloat() /
                            notePaths.size * 50)
                        .toInt()

                setProgress(
                    workDataOf(
                        "progress" to progress,
                        "status" to "Reading notes..."
                    )
                )
            }

            val material = builder.toString().trim()

            if (material.isBlank()) {
                throw Exception(
                    "No text could be extracted from the selected notes."
                )
            }

            Log.d(
                "WORKER",
                "Extracted material length: ${material.length}"
            )

            setProgress(
                workDataOf(
                    "progress" to 50,
                    "status" to "Generating merged note..."
                )
            )

            val mergedText =
                AIRepository.mergeNotes(material)

            if (mergedText.isBlank()) {
                throw Exception(
                    "AI returned an empty merged note."
                )
            }

            setProgress(
                workDataOf(
                    "progress" to 80,
                    "status" to "Saving file..."
                )
            )

            val title =
                "Merged note — ${
                    SimpleDateFormat(
                        "dd.MM.yyyy",
                        Locale.getDefault()
                    ).format(Date())
                }"

            val file =
                File(
                    applicationContext.filesDir,
                    "${System.currentTimeMillis()}_merged.txt"
                )

            file.writeText(
                mergedText,
                Charsets.UTF_8
            )

            db.noteDao().insert(
                Note(
                    folderId = folderId,
                    title = title,
                    path = file.absolutePath,
                    type = "TEXT"
                )
            )

            setProgress(
                workDataOf(
                    "progress" to 100,
                    "status" to "Done!"
                )
            )

            Log.d(
                "WORKER",
                "Merged note successfully saved: ${file.absolutePath}"
            )

            Result.success()

        } catch (e: kotlinx.coroutines.CancellationException) {
            Log.e("WORKER", "Merge worker was cancelled", e)
            throw e
        } catch (e: Exception) {
            Log.e("WORKER", "Note merge failed", e)
            Result.failure(
                workDataOf(
                    "error" to (e.message ?: "Unknown error")
                )
            )
        }
    }
}