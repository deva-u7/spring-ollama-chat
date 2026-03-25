package com.deva.ollama.model.ollama

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OllamaTagsResponse(
    val models: List<OllamaModelInfo> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OllamaModelInfo(
    val name: String = "",
    val model: String = "",
    val size: Long = 0
)
