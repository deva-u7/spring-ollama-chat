package com.deva.ollama.service

import com.deva.ollama.client.OllamaClient
import com.deva.ollama.config.OllamaProperties
import com.deva.ollama.model.ollama.OllamaChatRequest
import com.deva.ollama.model.ollama.OllamaMessage
import com.deva.ollama.model.request.ChatRequest
import com.deva.ollama.model.response.ChatResponse
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class ChatService(
    private val ollamaClient: OllamaClient,
    private val properties: OllamaProperties
) {

    fun chat(request: ChatRequest): Mono<ChatResponse> {
        val ollamaRequest = buildRequest(request, stream = false)
        return ollamaClient.chat(ollamaRequest)
            .map { ChatResponse(it.message?.content ?: "", it.model) }
    }

    fun chatStream(request: ChatRequest): Flux<String> {
        val ollamaRequest = buildRequest(request, stream = true)
        return ollamaClient.chatStream(ollamaRequest)
            .filter { it.message?.content?.isNotEmpty() == true }
            .map { it.message!!.content }
    }

    private fun buildRequest(request: ChatRequest, stream: Boolean): OllamaChatRequest {
        val messages = mutableListOf<OllamaMessage>()
        request.systemPrompt?.let { messages.add(OllamaMessage(role = "system", content = it)) }
        messages.add(OllamaMessage(role = "user", content = request.prompt))
        return OllamaChatRequest(
            model = request.model ?: properties.defaultModel,
            messages = messages,
            stream = stream
        )
    }
}
