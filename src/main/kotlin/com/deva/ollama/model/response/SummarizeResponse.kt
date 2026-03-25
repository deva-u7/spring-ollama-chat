package com.deva.ollama.model.response

data class SummarizeResponse(
    val summary: String,
    val style: String,
    val model: String
)
