package com.deva.ollama.model.response

data class HealthResponse(
    val status: String,
    val ollamaReachable: Boolean,
    val latencyMs: Long,
    val defaultModel: String
)
