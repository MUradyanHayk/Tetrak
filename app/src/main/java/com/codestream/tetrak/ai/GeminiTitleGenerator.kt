package com.codestream.tetrak.ai

import android.content.Context
import com.codestream.tetrak.BuildConfig
import com.codestream.tetrak.R
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class GeminiTitleGenerator(private val context: Context) {

    fun generateTitle(description: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        if (apiKey.isBlank()) {
            throw IllegalStateException(context.getString(R.string.ai_title_api_key_missing))
        }

        val prompt = buildString {
            append("Generate a short, natural note title from this description. ")
            append("Use the same language as the description. ")
            append("Maximum 5 words. Return only the title, no quotes, no markdown.")
            append("Description:")
            append(description.trim())
        }

        val requestJson = JSONObject()
            .put(
                "contents",
                JSONArray().put(
                    JSONObject().put(
                        "parts",
                        JSONArray().put(JSONObject().put("text", prompt))
                    )
                )
            )
            .put(
                "generationConfig",
                JSONObject()
                    .put("temperature", 0.35)
                    .put("maxOutputTokens", 24)
            )

        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/${BuildConfig.GEMINI_MODEL}:generateContent?key=$apiKey")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 20_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }

        return try {
            connection.outputStream.use { output ->
                output.write(requestJson.toString().toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val body = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }

            if (responseCode !in 200..299) {
                throw IllegalStateException(parseGeminiError(body) ?: context.getString(R.string.ai_title_generation_failed))
            }

            parseTitle(body).ifBlank {
                throw IllegalStateException(context.getString(R.string.ai_title_generation_failed))
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseTitle(body: String): String {
        val candidates = JSONObject(body).optJSONArray("candidates") ?: return ""
        val firstCandidate = candidates.optJSONObject(0) ?: return ""
        val parts = firstCandidate.optJSONObject("content")?.optJSONArray("parts") ?: return ""
        val text = parts.optJSONObject(0)?.optString("text").orEmpty()
        return text
            .lineSequence()
            .firstOrNull { it.isNotBlank() }
            .orEmpty()
            .trim()
            .trim('"', '\'', '“', '”')
            .take(80)
    }

    private fun parseGeminiError(body: String): String? = runCatching {
        JSONObject(body).optJSONObject("error")?.optString("message")?.takeIf { it.isNotBlank() }
    }.getOrNull()
}
