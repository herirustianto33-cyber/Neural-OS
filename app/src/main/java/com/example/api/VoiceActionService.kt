package com.example.api

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import retrofit2.HttpException

@Serializable
data class VoiceActionResponse(
    val action: String,
    val parameter: String? = null
)

suspend fun parseVoiceCommand(command: String): VoiceActionResponse? = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
        return@withContext null
    }

    val prompt = """
        You are a voice-to-action parser for a robot connectivity and monitoring dashboard.
        The user said: "$command"
        
        Determine the intent of the user. Return ONLY a valid JSON object matching this schema:
        {
          "action": "String (one of: SHOW_CONNECTIVITY, SHOW_LATENCY, CALIBRATE_SENSORS, SHOW_INSIGHTS, SHOW_DASHBOARD, SHOW_HARDWARE, OPEN_CHAT, UNKNOWN)",
          "parameter": "String (any additional context, or null)"
        }
        
        Examples:
        - "Show me the last hour of latency" -> SHOW_LATENCY
        - "Calibrate the sensors" -> CALIBRATE_SENSORS
        - "Open the terminal" -> SHOW_CONNECTIVITY
        - "Check predictive models" -> SHOW_INSIGHTS
        - "Show hardware status" -> SHOW_HARDWARE
        - "Thermal map" -> SHOW_HARDWARE
        
        Do not include markdown blocks or any other text, just the raw JSON object.
    """.trimIndent()

    val request = GenerateContentRequest(
        contents = listOf(
            Content(
                parts = listOf(Part(text = prompt))
            )
        )
    )

    try {
        val response = RetrofitClient.service.generateContent(apiKey, request)
        val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
            ?: return@withContext null
            
        // Clean up markdown block if model accidentally included it
        val cleanJson = text.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        
        Json { ignoreUnknownKeys = true }.decodeFromString<VoiceActionResponse>(cleanJson)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
