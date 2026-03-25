package com.deva.ollama

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import com.deva.ollama.config.OllamaProperties

@SpringBootApplication
@EnableConfigurationProperties(OllamaProperties::class)
class OllamaChatApplication

fun main(args: Array<String>) {
    runApplication<OllamaChatApplication>(*args)
}
