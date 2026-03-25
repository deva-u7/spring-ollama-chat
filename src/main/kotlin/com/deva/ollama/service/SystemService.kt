package com.deva.ollama.service

import com.deva.ollama.client.OllamaClient
import com.deva.ollama.config.OllamaProperties
import com.deva.ollama.model.response.HealthResponse
import com.deva.ollama.model.response.ModelInfo
import com.deva.ollama.model.response.ModelsResponse
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class SystemService(
    private val ollamaClient: OllamaClient,
    private val properties: OllamaProperties
) {

    fun listModels(): Mono<ModelsResponse> =
        ollamaClient.listModels()
            .map { tags ->
                ModelsResponse(
                    models = tags.models.map { ModelInfo(name = it.name, sizeMb = it.size / 1_048_576) }
                )
            }

    fun health(): Mono<HealthResponse> =
        ollamaClient.ping()
            .map { latency ->
                HealthResponse(
                    status = "UP",
                    ollamaReachable = true,
                    latencyMs = latency,
                    defaultModel = properties.defaultModel
                )
            }
            .onErrorReturn(
                HealthResponse(
                    status = "DOWN",
                    ollamaReachable = false,
                    latencyMs = -1,
                    defaultModel = properties.defaultModel
                )
            )
}
