package com.deva.ollama.model.request

data class CodeReviewRequest(
    val code: String,
    val language: String,
    val model: String? = null
)
