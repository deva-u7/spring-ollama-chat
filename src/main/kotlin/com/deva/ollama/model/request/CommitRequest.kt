package com.deva.ollama.model.request

data class CommitRequest(
    val diff: String,
    val model: String? = null
)
