package com.deva.ollama.model.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class CodeReviewResponse(
    val score: Int,
    val summary: String,
    val issues: List<ReviewIssue>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ReviewIssue(
    val severity: IssueSeverity,
    val description: String,
    val suggestion: String
)

enum class IssueSeverity { CRITICAL, WARNING, INFO }
