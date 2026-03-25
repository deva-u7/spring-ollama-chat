package com.deva.ollama.controller

import com.deva.ollama.model.response.HealthResponse
import com.deva.ollama.model.response.ModelsResponse
import com.deva.ollama.service.SystemService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class SystemController(private val systemService: SystemService) {

    @GetMapping("/models")
    fun listModels(): Mono<ModelsResponse> = systemService.listModels()

    @GetMapping("/health")
    fun health(): Mono<HealthResponse> = systemService.health()
}
