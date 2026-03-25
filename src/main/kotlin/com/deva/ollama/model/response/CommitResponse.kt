package com.deva.ollama.model.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class CommitResponse(
    val message: String,
    val type: String,
    val scope: String?,
    val description: String
)
