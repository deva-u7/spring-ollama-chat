package com.deva.ollama.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ollama")
data class OllamaProperties(
    val host: String,
    val defaultModel: String,
    val timeoutSeconds: Long,
    val cors: CorsProperties
) {
    data class CorsProperties(
        val allowedOrigins: List<String>
    )
}
