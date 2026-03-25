package com.deva.ollama.client

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import com.deva.ollama.model.ollama.OllamaChatRequest
import com.deva.ollama.model.ollama.OllamaGenerateRequest
import com.deva.ollama.model.ollama.OllamaMessage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier

class OllamaClientTest {

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock: WireMockExtension = WireMockExtension.newInstance().build()
    }

    private fun buildClient(): OllamaClient {
        val webClient = WebClient.builder()
            .baseUrl(wiremock.baseUrl())
            .build()
        return OllamaClient(webClient)
    }

    @Test
    fun `chat returns response`() {
        wiremock.stubFor(
            post(urlEqualTo("/api/chat"))
                .willReturn(
                    okJson(
                        """{"model":"phi4-mini","message":{"role":"assistant","content":"Hello!"},"done":true}"""
                    )
                )
        )

        val client = buildClient()
        val request = OllamaChatRequest(
            model = "phi4-mini",
            messages = listOf(OllamaMessage("user", "Hi")),
            stream = false
        )

        StepVerifier.create(client.chat(request))
            .expectNextMatches { it.message?.content == "Hello!" }
            .verifyComplete()
    }

    @Test
    fun `generate returns response`() {
        wiremock.stubFor(
            post(urlEqualTo("/api/generate"))
                .willReturn(
                    okJson(
                        """{"model":"phi4-mini","response":"Generated text","done":true}"""
                    )
                )
        )

        val client = buildClient()
        val request = OllamaGenerateRequest(model = "phi4-mini", prompt = "Write something")

        StepVerifier.create(client.generate(request))
            .expectNextMatches { it.response == "Generated text" }
            .verifyComplete()
    }

    @Test
    fun `listModels returns model list`() {
        wiremock.stubFor(
            get(urlEqualTo("/api/tags"))
                .willReturn(
                    okJson(
                        """{"models":[{"name":"phi4-mini","model":"phi4-mini","size":2048000000}]}"""
                    )
                )
        )

        val client = buildClient()

        StepVerifier.create(client.listModels())
            .expectNextMatches { it.models.size == 1 && it.models[0].name == "phi4-mini" }
            .verifyComplete()
    }

    @Test
    fun `ping returns latency`() {
        wiremock.stubFor(
            get(urlEqualTo("/api/tags"))
                .willReturn(
                    okJson("""{"models":[]}""")
                )
        )

        val client = buildClient()

        StepVerifier.create(client.ping())
            .expectNextMatches { it >= 0 }
            .verifyComplete()
    }

    @Test
    fun `chatStream returns token chunks`() {
        val ndjson = """
            {"model":"phi4-mini","message":{"role":"assistant","content":"Hello"},"done":false}
            {"model":"phi4-mini","message":{"role":"assistant","content":" world"},"done":true}
        """.trimIndent()

        wiremock.stubFor(
            post(urlEqualTo("/api/chat"))
                .willReturn(
                    aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_NDJSON_VALUE)
                        .withBody(ndjson)
                )
        )

        val client = buildClient()
        val request = OllamaChatRequest(
            model = "phi4-mini",
            messages = listOf(OllamaMessage("user", "Hi")),
            stream = true
        )

        StepVerifier.create(client.chatStream(request))
            .expectNextCount(2)
            .verifyComplete()
    }
}
