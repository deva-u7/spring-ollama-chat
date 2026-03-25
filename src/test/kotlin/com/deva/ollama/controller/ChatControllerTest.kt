package com.deva.ollama.controller

import com.deva.ollama.model.request.ChatRequest
import com.deva.ollama.model.response.ChatResponse
import com.deva.ollama.service.ChatService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@WebFluxTest(ChatController::class)
class ChatControllerTest {

    @Autowired
    lateinit var client: WebTestClient

    @MockitoBean
    lateinit var chatService: ChatService

    @Test
    fun `POST chat returns response`() {
        whenever(chatService.chat(any())).thenReturn(
            Mono.just(ChatResponse(response = "Hello!", model = "phi4-mini"))
        )

        client.post().uri("/chat")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(ChatRequest(prompt = "Hi"))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.response").isEqualTo("Hello!")
            .jsonPath("$.model").isEqualTo("phi4-mini")
    }

    @Test
    fun `POST chat stream returns SSE`() {
        whenever(chatService.chatStream(any())).thenReturn(
            Flux.just("Hello", " world", "!")
        )

        client.post().uri("/chat/stream")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(ChatRequest(prompt = "Hi"))
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
            .returnResult(String::class.java)
            .responseBody
            .take(3)
            .collectList()
            .block()
            .let { tokens ->
                assert(tokens != null && tokens.isNotEmpty())
            }
    }
}
