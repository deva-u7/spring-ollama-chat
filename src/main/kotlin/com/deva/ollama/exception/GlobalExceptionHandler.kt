package com.deva.ollama.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException

data class ErrorResponse(val error: String, val message: String)

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(OllamaException::class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    fun handleOllamaException(ex: OllamaException): ErrorResponse {
        log.error("Ollama error: ${ex.message}")
        return ErrorResponse("OLLAMA_ERROR", ex.message ?: "Ollama request failed")
    }

    @ExceptionHandler(WebClientResponseException::class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    fun handleWebClientResponse(ex: WebClientResponseException): ErrorResponse {
        log.error("Ollama HTTP ${ex.statusCode}: ${ex.responseBodyAsString}")
        return ErrorResponse("OLLAMA_HTTP_ERROR", "Ollama returned ${ex.statusCode}")
    }

    @ExceptionHandler(WebClientRequestException::class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    fun handleWebClientRequest(ex: WebClientRequestException): ErrorResponse {
        log.error("Cannot reach Ollama: ${ex.message}")
        return ErrorResponse("OLLAMA_UNREACHABLE", "Cannot connect to Ollama — is it running?")
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgument(ex: IllegalArgumentException): ErrorResponse {
        return ErrorResponse("BAD_REQUEST", ex.message ?: "Invalid request")
    }
}
