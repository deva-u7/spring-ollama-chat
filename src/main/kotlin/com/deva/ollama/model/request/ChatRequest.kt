package com.deva.ollama.model.request

data class ChatRequest(
    val prompt: String,
    val model: String? = null,
    val systemPrompt: String? = null
)
