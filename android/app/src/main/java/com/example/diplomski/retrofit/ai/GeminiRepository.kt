package com.example.diplomski.retrofit.ai

import com.example.diplomski.BuildConfig
import com.example.diplomski.retrofit.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class QuestionDTO(
    val question: String,
    val answer: String,
    val option1: String,
    val option2: String,
    val option3: String
)

object AIRepository {

    private val num = PreferencesManager.getNumberOfQuestions()
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateQuiz(material: String):  List<QuestionDTO> =
        withContext(Dispatchers.IO) {

            val prompt = """
            You are a quiz generator.
            Based ONLY on the following study material generate exactly $num multiple choice questions.
            Rules:
            - Generate exactly $num questions.
            - Every question must have ONE CORRECT answer(answer field) and THREE INCORRECT answers(
            option1, option2 and option3 fields), so 4 DIFFERENT choices in total.
            - Return ONLY valid JSON.
            - Do not use markdown.
            - Do not write explanations. 
            - Write in English.
            Study material:
            $material
            Return exactly this format:
            [
              {
                "question":"...",
                "answer":"...",
                "option1":"...",
                "option2":"...",
                "option3":"..."
              }
            ]
            """.trimIndent()

            val body = JSONObject().apply {
                put("model", "nvidia/nemotron-3-ultra-550b-a55b:free")
                put("messages", JSONArray().put(
                        JSONObject().apply {
                            put("role", "user")
                            put("content", prompt)
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

            val response = client.newCall(request).execute()

            val responseBody = response.body.string()

            if (!response.isSuccessful) { throw Exception(responseBody) }

            val responseJson = JSONObject(responseBody)
            val content = responseJson.getJSONArray("choices").getJSONObject(0)
                .getJSONObject("message").getString("content")

            val startIndex = content.indexOf('[')
            val endIndex = content.lastIndexOf(']')
            if (startIndex == -1 || endIndex == -1) {
                throw Exception("No JSON array found in response")
            }
            val cleanJson = content
                .substring(startIndex, endIndex + 1)
                .replace(Regex("[^\\x20-\\x7E\\n\\r\\t]"), "")
            val array = JSONArray(cleanJson)

            val questions = mutableListOf<QuestionDTO>()

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)

                questions.add(
                    QuestionDTO(
                        question = obj.getString("question"),
                        answer = obj.getString("answer"),
                        option1 = obj.getString("option1"),
                        option2 = obj.getString("option2"),
                        option3 = obj.getString("option3")
                    )
                )
            }

            return@withContext questions
        }

    suspend fun mergeNotes(
        material: String
    ): String =
        withContext(Dispatchers.IO) {

            val prompt = """
            You are a study notes organizer.

            Combine the following study notes into one clear,
            well-structured study document.

            IMPORTANT RULES:

            - Preserve the original language of the notes.
            - If the notes are written in Serbian Cyrillic,
              keep the final document in Serbian Cyrillic.
            - If the notes are written in Serbian Latin,
              keep the final document in Serbian Latin.
            - Do NOT translate the material.
            - Do NOT add information that is not present
              in the provided notes.
            - Remove unnecessary repetitions.
            - Preserve all important facts.
            - Preserve definitions.
            - Preserve formulas.
            - Preserve mathematical notation.
            - Preserve important examples.
            - Organize the material using meaningful headings
              and subheadings.
            - Make the final text easy for a student to study.
            - Correct obvious OCR/transcription mistakes only
              when the intended meaning is completely clear.
            - Do not invent missing information.
            - Return ONLY the final study document.
            - Do not include explanations about what you changed.

            STUDY NOTES:

            $material
        """.trimIndent()

            val body = JSONObject().apply {
                put(
                    "model",
                    "nvidia/nemotron-3-ultra-550b-a55b:free"
                )
                put("messages", JSONArray().put(JSONObject()
                    .apply {
                        put("role", "user")
                        put("content", prompt)
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

            val response = client.newCall(request).execute()

            val responseBody = response.body.string()
            if (!response.isSuccessful) {
                throw Exception("OpenRouter error ${response.code}: $responseBody")
            }
            val json = JSONObject(responseBody)
            if (!json.has("choices")) {
                throw Exception("OpenRouter response does not contain choices: $responseBody")
            }

            json.getJSONArray("choices").getJSONObject(0)
                .getJSONObject("message").getString("content")
                .trim()
        }
}
