# Architecture

## Overview

`spring-ollama-chat` is a reactive Spring Boot application that sits between HTTP clients and a locally running [Ollama](https://ollama.com) instance. It exposes a clean REST API for chat and developer tools (code review, commit messages, summarization) — all powered by local LLMs with no external API calls.

```
HTTP Client
    │
    ▼
┌─────────────────────────────────────────┐
│           Spring Boot App               │
│                                         │
│  Filter → Controller → Service → Client │
│                                         │
└─────────────────────────────────────────┘
    │
    ▼
Ollama  (local / LAN)
```

---

## Request Flow

```
Incoming Request
      │
      ▼
RequestLoggingFilter          ← logs method, path, status, duration
      │                          attaches X-Correlation-Id header
      ▼
CorsWebFilter                 ← allows configured origins (from application.yml)
      │
      ▼
Controller                    ← maps HTTP ↔ domain types, nothing else
      │
      ▼
Service                       ← builds prompts via PromptLoader
      │                          parses and validates model responses
      ▼
OllamaClient                  ← single WebClient bean
      │                          logs model + latency on every call
      ▼
Ollama REST API
(/api/chat, /api/generate, /api/tags)
```

---

## Layer Responsibilities

### Controller (`controller/`)

Thin. Accepts the request, calls one service method, returns the result. No business logic, no prompt building, no parsing.

```
ChatController    → /chat, /chat/stream
ToolsController   → /tools/review, /tools/commit, /tools/summarize
SystemController  → /health, /models
```

### Service (`service/`)

All business logic lives here. Services are the stable layer — they own prompt construction, response parsing, and fallback behaviour. When the underlying LLM integration changes (e.g. switching from raw Ollama to LangChain4j or Spring AI), only the client layer changes. Services stay the same.

```
ChatService      → builds OllamaChatRequest from ChatRequest
                   handles optional systemPrompt as a system message

ToolsService     → loads prompt templates via PromptLoader
                   renders placeholders (language, code, diff, style)
                   parses structured JSON responses from the model
                   returns safe fallbacks on parse failure

SystemService    → delegates listModels + health check to OllamaClient
                   converts raw bytes → MB for model sizes
                   handles Ollama DOWN state via onErrorReturn
```

### Client (`client/OllamaClient`)

One class, one WebClient bean. Owns nothing except the HTTP calls to Ollama. Each method logs the operation outcome and latency at DEBUG level. Does not know about prompts, parsing, or business logic.

```
chat()        → POST /api/chat  stream=false  → Mono<OllamaChatResponse>
chatStream()  → POST /api/chat  stream=true   → Flux<OllamaChatResponse>
generate()    → POST /api/generate            → Mono<OllamaGenerateResponse>
listModels()  → GET  /api/tags                → Mono<OllamaTagsResponse>
ping()        → GET  /api/tags                → Mono<Long>  (latency ms)
```

### Config (`config/`)

```
OllamaProperties   → @ConfigurationProperties(prefix="ollama")
                     host, defaultModel, timeoutSeconds, cors.allowedOrigins
                     no defaults in code — application.yml is source of truth

OllamaConfig       → creates WebClient bean with ReactorClientHttpConnector
                     configures response timeout + connect timeout from properties

WebConfig          → CorsWebFilter — reads allowed origins from OllamaProperties

PromptLoader       → loads prompt templates from resources/prompts/ at startup (@PostConstruct)
                     caches in memory, exposes render(name, vararg pairs) for placeholder substitution
```

### Filter (`filter/`)

```
RequestLoggingFilter   → ordered first (Order=1)
                         generates or echoes X-Correlation-Id per request
                         logs → method path on entry
                         logs ← method path status duration(ms) on completion
```

### Exception Handling (`exception/`)

```
GlobalExceptionHandler   → @RestControllerAdvice
                           WebClientResponseException → 502 BAD_GATEWAY
                           WebClientRequestException  → 503 SERVICE_UNAVAILABLE
                           OllamaException            → 502 BAD_GATEWAY
                           IllegalArgumentException   → 400 BAD_REQUEST
```

---

## Prompt Templates (`resources/prompts/`)

Prompts are `.txt` files, not strings in code. `PromptLoader` reads them at startup and caches them. `ToolsService` calls `promptLoader.render("name", "key" to value, ...)` which does simple `{placeholder}` substitution.

```
code-review.txt      → {language}, {code}
commit-message.txt   → {diff}
summarize.txt        → {styleInstruction}, {text}
```

This means prompts can be tuned without recompiling. Adding a new tool is: add a `.txt` file, add a service method, add a controller endpoint.

---

## Reactive Stack

The app uses Spring WebFlux (not Spring MVC). Every operation returns `Mono<T>` or `Flux<T>` — nothing blocks a thread waiting for Ollama.

| Use case | Type |
|---|---|
| Single response (chat, tools) | `Mono<T>` |
| Streaming tokens | `Flux<String>` via SSE |
| Ollama client calls | `Mono` / `Flux` of response types |

Streaming works by setting `stream=true` on the Ollama request. Ollama returns newline-delimited JSON; WebClient's `bodyToFlux` parses each line as `OllamaChatResponse`. The service filters out empty content chunks and maps to plain `String` tokens. The controller sets `produces = TEXT_EVENT_STREAM_VALUE` which sends each token as an SSE event.

---

## Configuration Profiles

| Profile | Purpose |
|---|---|
| _(default)_ | `application.yml` — points to `localhost:11434` |
| `local` | `application-local.yml` — override for LAN Ollama (gitignored) |

`application-local.yml` is gitignored so machine-specific config (LAN IP, local model name) never reaches the repo.

---

## Model Contract

All Ollama response models use `@JsonIgnoreProperties(ignoreUnknown = true)` so the app doesn't break if Ollama adds new fields in future versions.

Structured tool responses (code review, commit) are parsed from raw model output. The model is instructed to return only valid JSON. `ToolsService.parseOrFallback()` strips markdown fences, attempts Jackson deserialization, and returns a safe default if parsing fails — so a bad model response never causes a 500.

---

## What Plugs In Next

The service layer is the designed extension point.

| Addition | What changes |
|---|---|
| LangChain4j | Replace `OllamaClient` calls in services with LangChain4j chains |
| Spring AI | Swap `OllamaClient` for `ChatClient` bean — service logic unchanged |
| Conversation history | Add `ConversationStore` to `ChatService`, inject message history into request |
| Vector embeddings | New `EmbeddingService` → ChromaDB client, new endpoints |
| Persistent sessions | New `SessionRepository` (JPA/R2DBC), wrap `ChatService` |
