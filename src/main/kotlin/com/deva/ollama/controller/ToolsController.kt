package com.deva.ollama.controller

import com.deva.ollama.model.request.CodeReviewRequest
import com.deva.ollama.model.request.CommitRequest
import com.deva.ollama.model.request.SummarizeRequest
import com.deva.ollama.model.response.CodeReviewResponse
import com.deva.ollama.model.response.CommitResponse
import com.deva.ollama.model.response.SummarizeResponse
import com.deva.ollama.service.ToolsService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/tools")
class ToolsController(private val toolsService: ToolsService) {

    @PostMapping("/review")
    fun reviewCode(@RequestBody request: CodeReviewRequest): Mono<CodeReviewResponse> =
        toolsService.reviewCode(request)

    @PostMapping("/commit")
    fun generateCommitMessage(@RequestBody request: CommitRequest): Mono<CommitResponse> =
        toolsService.generateCommitMessage(request)

    @PostMapping("/summarize")
    fun summarize(@RequestBody request: SummarizeRequest): Mono<SummarizeResponse> =
        toolsService.summarize(request)
}
