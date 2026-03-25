package com.deva.ollama.config

import org.slf4j.LoggerFactory
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component
import jakarta.annotation.PostConstruct

@Component
class PromptLoader(private val resourceLoader: ResourceLoader) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val templates = mutableMapOf<String, String>()

    @PostConstruct
    fun load() {
        listOf("code-review", "commit-message", "summarize").forEach { name ->
            val resource = resourceLoader.getResource("classpath:prompts/$name.txt")
            templates[name] = resource.inputStream.bufferedReader().readText()
            log.info("Loaded prompt template: {}", name)
        }
    }

    fun render(name: String, vararg pairs: Pair<String, String>): String {
        val template = templates[name]
            ?: throw IllegalStateException("Prompt template not found: $name")
        return pairs.fold(template) { acc, (key, value) -> acc.replace("{$key}", value) }
    }
}
