package com.deva.ollama.model.request

data class SummarizeRequest(
    val text: String,
    val style: SummaryStyle = SummaryStyle.PARAGRAPH,
    val model: String? = null
)

enum class SummaryStyle { PARAGRAPH, BULLETS, TLDR, ONE_LINER }
