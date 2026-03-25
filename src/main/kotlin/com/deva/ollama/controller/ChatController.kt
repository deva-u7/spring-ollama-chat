package com.deva.ollama.controller

import com.deva.ollama.model.request.ChatRequest
import com.deva.ollama.model.response.ChatResponse
import com.deva.ollama.service.ChatService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/chat")
class ChatController(private val chatService: ChatService) {

    @PostMapping
    fun chat(@RequestBody request: ChatRequest): Mono<ChatResponse> =
        chatService.chat(request)

    @PostMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun chatStream(@RequestBody request: ChatRequest): Flux<String> =
        chatService.chatStream(request)
}
