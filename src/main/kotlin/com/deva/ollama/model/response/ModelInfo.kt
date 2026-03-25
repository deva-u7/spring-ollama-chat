package com.deva.ollama.model.response

data class ModelInfo(
    val name: String,
    val sizeMb: Long
)

data class ModelsResponse(
    val models: List<ModelInfo>
)
