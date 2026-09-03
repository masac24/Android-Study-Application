package com.example.diplomski.retrofit.ai

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.diplomski.BuildConfig
import com.example.diplomski.database.tables.Note
import com.example.diplomski.workers.MergeNoteWorker
import com.example.diplomski.workers.QuizGeneratorWorker
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

object PDFExtractor {
    fun extract(context: Context, path: String): String {
        PDFBoxResourceLoader.init(context)
        val document = PDDocument.load(File(path))
        val stripper = PDFTextStripper()
        val text = stripper.getText(document)
        document.close()
        return text
    }
}

object OCRExtractor {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun extract(path: String): String =
        withContext(Dispatchers.IO) {
            val file = File(path)
            if (!file.exists()) {
                throw Exception("Image file does not exist: $path")
            }

            val bytes = file.readBytes()
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

            val mimeType = when {
                path.endsWith(".png", true) -> "image/png"
                path.endsWith(".webp", true) -> "image/webp"
                else -> "image/jpeg"
            }

            val prompt = """
                Read all visible text in this image.

                The image may contain handwritten study notes.

                Important:
                - Preserve Serbian Cyrillic exactly when the source uses Cyrillic.
                - Preserve Serbian Latin when the source uses Latin.
                - Do not translate the text.
                - Do not summarize.
                - Transcribe the text as accurately as possible.
                - Preserve mathematical formulas, symbols and numbers.
                - Do not invent information.
                - If some handwriting is unclear, make the best possible
                  interpretation based only on the visible image.
                - Return ONLY the transcribed text.
            """.trimIndent()

            val content = JSONArray()
                .put(JSONObject().apply {
                        put("type", "text")
                        put("text", prompt)
                    }
                )
                .put(JSONObject().apply {
                        put("type", "image_url")
                        put(
                            "image_url",
                            JSONObject().apply {
                                put(
                                    "url",
                                    "data:$mimeType;base64,$base64"
                                )
                            }
                        )
                    }
                )

            val body = JSONObject().apply {
                put("model", "openrouter/free")
                put("messages", JSONArray().put(JSONObject().apply {
                            put("role", "user")
                            put("content", content)
                        }
                    )
                )
            }.toString()

            val request = Request.Builder()
                    .url("https://openrouter.ai/api/v1/chat/completions")
                    .addHeader("Authorization", "Bearer ${BuildConfig.API_KEY}")
                    .addHeader("Content-Type", "application/json")
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()

            Log.d("OCR", "Sending image to OpenRouter: $path")

            val response = client.newCall(request).execute()
            val responseBody = response.body.string()

            Log.d("OCR", "OpenRouter response code: ${response.code}")

            if (!response.isSuccessful) {
                throw Exception("OpenRouter error ${response.code}: $responseBody")
            }

            val json = JSONObject(responseBody)
            if (!json.has("choices")) {
                throw Exception(
                    "No choices in response: $responseBody"
                )
            }

            val text = json.getJSONArray("choices").getJSONObject(0)
                    .getJSONObject("message").getString("content")

            Log.d("OCR", "OCR completed. Extracted ${text.length} characters")

            text.trim()
        }
}

object AIQuizGenerator {
    fun generateAiQuiz(
        notes: List<Note>,
        quizName: String,
        username: String,
        numberOfQuestions: Int,
        context: Context
    ) : UUID {
        val inputData = workDataOf(
            "notePaths" to notes.map { it.path }.toTypedArray(),
            "quizName" to quizName,
            "username" to username,
            "numberOfQuestions" to numberOfQuestions
        )

        val request = OneTimeWorkRequestBuilder<QuizGeneratorWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(request)
        return request.id
    }
}

object AIMergeGenerator {
    fun merge(notes: List<Note>, folderId: Int?,
                      context: Context): UUID {
        val inputData = workDataOf(
            "notePaths" to notes.map { it.path }.toTypedArray(),
            "folderId" to folderId
        )

        val request = OneTimeWorkRequestBuilder<MergeNoteWorker>()
            .setInputData(inputData)
            .build()

        WorkManager
            .getInstance(context)
            .enqueueUniqueWork(
                "merge_notes",
                ExistingWorkPolicy.REPLACE,
                request
            )
        return request.id
    }


}