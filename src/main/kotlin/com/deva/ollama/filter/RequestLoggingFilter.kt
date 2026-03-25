package com.deva.ollama.filter

import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.util.UUID

const val CORRELATION_ID_HEADER = "X-Correlation-Id"

@Component
@Order(1)
class RequestLoggingFilter : WebFilter {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request
        val correlationId = request.headers.getFirst(CORRELATION_ID_HEADER)
            ?: UUID.randomUUID().toString().substring(0, 8)

        val mutated = exchange.mutate()
            .request { it.header(CORRELATION_ID_HEADER, correlationId) }
            .build()

        mutated.response.headers.set(CORRELATION_ID_HEADER, correlationId)

        val start = System.currentTimeMillis()

        log.info(
            "[{}] --> {} {}",
            correlationId,
            request.method,
            request.path.value()
        )

        return chain.filter(mutated).doFinally {
            val duration = System.currentTimeMillis() - start
            val status = mutated.response.statusCode?.value() ?: 0
            log.info(
                "[{}] <-- {} {} {} ({}ms)",
                correlationId,
                request.method,
                request.path.value(),
                status,
                duration
            )
        }
    }
}
