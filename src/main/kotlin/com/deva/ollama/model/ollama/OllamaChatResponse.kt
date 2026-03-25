package com.deva.ollama.model.ollama

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class OllamaChatResponse(
    val model: String = "",
    val message: OllamaMessage? = null,
    val done: Boolean = false,
    @JsonProperty("done_reason")
    val doneReason: String? = null
)
