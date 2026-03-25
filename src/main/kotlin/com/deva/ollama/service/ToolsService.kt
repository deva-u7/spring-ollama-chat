package com.deva.ollama.service

import com.deva.ollama.client.OllamaClient
import com.deva.ollama.config.OllamaProperties
import com.deva.ollama.config.PromptLoader
import com.deva.ollama.model.ollama.OllamaGenerateRequest
import com.deva.ollama.model.request.CodeReviewRequest
import com.deva.ollama.model.request.CommitRequest
import com.deva.ollama.model.request.SummarizeRequest
import com.deva.ollama.model.request.SummaryStyle
import com.deva.ollama.model.response.CodeReviewResponse
import com.deva.ollama.model.response.CommitResponse
import com.deva.ollama.model.response.SummarizeResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ToolsService(
    private val ollamaClient: OllamaClient,
    private val properties: OllamaProperties,
    private val objectMapper: ObjectMapper,
    private val promptLoader: PromptLoader
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun reviewCode(request: CodeReviewRequest): Mono<CodeReviewResponse> {
        val model = request.model ?: properties.defaultModel
        val prompt = promptLoader.render(
            "code-review",
            "language" to request.language,
            "code" to request.code
        )
        log.debug("Invoking code-review prompt [model={}]", model)

        return ollamaClient.generate(OllamaGenerateRequest(model = model, prompt = prompt))
            .map { parseOrFallback(it.response, CodeReviewResponse::class.java) { defaultCodeReview() } }
    }

    fun generateCommitMessage(request: CommitRequest): Mono<CommitResponse> {
        val model = request.model ?: properties.defaultModel
        val prompt = promptLoader.render("commit-message", "diff" to request.diff)
        log.debug("Invoking commit-message prompt [model={}]", model)

        return ollamaClient.generate(OllamaGenerateRequest(model = model, prompt = prompt))
            .map { parseOrFallback(it.response, CommitResponse::class.java) { defaultCommitResponse() } }
    }

    fun summarize(request: SummarizeRequest): Mono<SummarizeResponse> {
        val model = request.model ?: properties.defaultModel
        val styleInstruction = when (request.style) {
            SummaryStyle.PARAGRAPH -> "Write a concise paragraph summary."
            SummaryStyle.BULLETS   -> "Write a bullet point list of the key points. Use '- ' for each bullet."
            SummaryStyle.TLDR      -> "Write a TL;DR summary in 2-3 sentences."
            SummaryStyle.ONE_LINER -> "Summarize in exactly one sentence."
        }
        val prompt = promptLoader.render(
            "summarize",
            "styleInstruction" to styleInstruction,
            "text" to request.text
        )
        log.debug("Invoking summarize prompt [style={}, model={}]", request.style, model)

        return ollamaClient.generate(OllamaGenerateRequest(model = model, prompt = prompt))
            .map { SummarizeResponse(summary = it.response.trim(), style = request.style.name, model = model) }
    }

    private fun <T> parseOrFallback(raw: String, type: Class<T>, fallback: () -> T): T {
        val cleaned = raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        return try {
            objectMapper.readValue(cleaned, type)
        } catch (e: Exception) {
            log.warn("Failed to parse model response as {} — using fallback. Cause: {}", type.simpleName, e.message)
            fallback()
        }
    }

    private fun defaultCodeReview() = CodeReviewResponse(
        score = 0,
        summary = "Unable to parse review response from model.",
        issues = emptyList()
    )

    private fun defaultCommitResponse() = CommitResponse(
        message = "chore: update code",
        type = "chore",
        scope = null,
        description = "Unable to parse commit message from model."
    )
}
