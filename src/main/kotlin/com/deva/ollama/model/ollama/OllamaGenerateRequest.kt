package com.deva.ollama.model.ollama

data class OllamaGenerateRequest(
    val model: String,
    val prompt: String,
    val system: String? = null,
    val stream: Boolean = false
)
