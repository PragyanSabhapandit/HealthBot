package com.example.chatbot.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.springframework.stereotype.Service
import java.time.Instant

data class ChatRequest(val message: String)
data class ChatResponse(val reply: String, val timestamp: Instant = Instant.now())

@Service
class ChatService {
    private val client = OkHttpClient()
    private val mapper = ObjectMapper()

    // Simple rule-based reply
    fun getReply(request: ChatRequest): ChatResponse {
        val userMessage = request.message.trim().lowercase()
        val reply = when {
            userMessage.contains("hello") || userMessage.contains("hi") ->
                "Hi there! How can I help you today?"
            userMessage.contains("bye") || userMessage.contains("goodbye") ->
                "Goodbye! Take care."
            userMessage.contains("help") ->
                "Sure — I can help. What do you need assistance with?"
            userMessage.contains("openai") || userMessage.contains("gpt") ->
                // Optionally forward to OpenAI (disabled by default)
                callOpenAi(request.message) ?: "(OpenAI call failed)"
            else ->
                "Sorry, I didn't quite understand that. Could you rephrase?"
        }
        return ChatResponse(reply)
    }

    // Example: synchronous call to OpenAI Chat Completions (optional)
    // To enable: set OPENAI_API_KEY environment variable and ensure it's allowed.
    fun callOpenAi(userMessage: String): String? {
        val apiKey = System.getenv("OPENAI_API_KEY") ?: return null
        val payload = mapper.createObjectNode().apply {
            put("model", "gpt-3.5-turbo")
            set<JsonNode>("messages", mapper.createArrayNode().apply {
                add(mapper.createObjectNode().apply {
                    put("role", "user")
                    put("content", userMessage)
                })
            })
        }.toString()

        val mediaType = "application/json".toMediaType()
        val body = RequestBody.create(mediaType, payload)
        val req = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return null
            val text = resp.body?.string() ?: return null
            val root = mapper.readTree(text)
            val content = root["choices"]?.get(0)?.get("message")?.get("content")?.asText()
            return content
        }
    }
}
