package com.deva.ollama.client

import com.deva.ollama.model.ollama.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class OllamaClient(private val ollamaWebClient: WebClient) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun chat(request: OllamaChatRequest): Mono<OllamaChatResponse> =
        ollamaWebClient.post()
            .uri("/api/chat")
            .bodyValue(request.copy(stream = false))
            .retrieve()
            .bodyToMono<OllamaChatResponse>()
            .doOnSuccess { log.debug("chat complete [model={}]", it.model) }
            .doOnError   { log.error("chat failed [model={}]: {}", request.model, it.message) }

    fun chatStream(request: OllamaChatRequest): Flux<OllamaChatResponse> =
        ollamaWebClient.post()
            .uri("/api/chat")
            .bodyValue(request.copy(stream = true))
            .retrieve()
            .bodyToFlux<OllamaChatResponse>()
            .filter { it.message?.content?.isNotEmpty() == true }
            .doOnComplete { log.debug("chat stream complete [model={}]", request.model) }
            .doOnError    { log.error("chat stream failed [model={}]: {}", request.model, it.message) }

    fun generate(request: OllamaGenerateRequest): Mono<OllamaGenerateResponse> {
        val start = System.currentTimeMillis()
        return ollamaWebClient.post()
            .uri("/api/generate")
            .bodyValue(request.copy(stream = false))
            .retrieve()
            .bodyToMono<OllamaGenerateResponse>()
            .doOnSuccess { log.debug("generate complete [model={}, latency={}ms]", it.model, System.currentTimeMillis() - start) }
            .doOnError   { log.error("generate failed [model={}]: {}", request.model, it.message) }
    }

    fun listModels(): Mono<OllamaTagsResponse> =
        ollamaWebClient.get()
            .uri("/api/tags")
            .retrieve()
            .bodyToMono<OllamaTagsResponse>()
            .doOnSuccess { log.debug("listModels: {} model(s) available", it.models.size) }

    fun ping(): Mono<Long> {
        val start = System.currentTimeMillis()
        return ollamaWebClient.get()
            .uri("/api/tags")
            .retrieve()
            .bodyToMono<OllamaTagsResponse>()
            .map { System.currentTimeMillis() - start }
            .doOnSuccess { log.debug("ping latency: {}ms", it) }
            .doOnError   { log.warn("ping failed: {}", it.message) }
    }
}
