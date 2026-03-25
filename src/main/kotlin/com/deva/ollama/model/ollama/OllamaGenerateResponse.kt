package com.deva.ollama.model.ollama

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OllamaGenerateResponse(
    val model: String = "",
    val response: String = "",
    val done: Boolean = false
)
