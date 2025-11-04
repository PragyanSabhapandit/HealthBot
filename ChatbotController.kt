package com.example.chatbot.controller

import com.example.chatbot.service.ChatService
import com.example.chatbot.service.ChatRequest
import com.example.chatbot.service.ChatResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/chat")
class ChatbotController(private val chatService: ChatService) {

    @PostMapping
    fun chat(@RequestBody request: ChatRequest): ResponseEntity<ChatResponse> {
        val resp = chatService.getReply(request)
        return ResponseEntity.ok(resp)
    }
}
