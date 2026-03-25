# spring-ollama-chat

A Kotlin + Spring Boot application that integrates with [Ollama](https://ollama.com) for local LLM inference.

Rather than basic chat forwarding, this app exposes developer tools powered by local models: code review, conventional commit message generation, and multi-style text summarization — all running on your own hardware with no external API calls.

## Stack

- **Kotlin** + **Spring Boot 3** (WebFlux — reactive, non-blocking)
- **Ollama REST API** — `/api/chat`, `/api/generate`, `/api/tags`
- **Server-Sent Events** for streaming responses
- **WireMock** + **Spring WebFlux Test** for tests

## Endpoints

### Chat

| Method | Path | Description |
|---|---|---|
| POST | `/chat` | Single-turn chat — returns full response |
| POST | `/chat/stream` | Streaming chat — SSE token-by-token |

**Request body:**
```json
{
  "prompt": "Explain reactor pattern in 2 sentences",
  "model": "phi4-mini",
  "systemPrompt": "You are a concise technical writer."
}
```
`model` and `systemPrompt` are optional. Default model is configured in `application.yml`.

---

### Developer Tools

| Method | Path | Description |
|---|---|---|
| POST | `/tools/review` | Code review with score, summary, and issues list |
| POST | `/tools/commit` | Generate a conventional commit message from a git diff |
| POST | `/tools/summarize` | Summarize text in a chosen style |

**Code review:**
```json
{
  "code": "fun divide(a: Int, b: Int) = a / b",
  "language": "Kotlin",
  "model": "phi4-mini"
}
```

Response:
```json
{
  "score": 4,
  "summary": "Function lacks null/zero checks.",
  "issues": [
    {
      "severity": "CRITICAL",
      "description": "Division by zero not handled",
      "suggestion": "Add require(b != 0) before dividing"
    }
  ]
}
```

**Commit message:**
```json
{
  "diff": "diff --git a/src/main/kotlin/Service.kt...",
  "model": "phi4-mini"
}
```

Response:
```json
{
  "type": "feat",
  "scope": "auth",
  "description": "add JWT token validation",
  "message": "feat(auth): add JWT token validation"
}
```

**Summarize:**
```json
{
  "text": "Long article text...",
  "style": "BULLETS",
  "model": "phi4-mini"
}
```

Styles: `PARAGRAPH`, `BULLETS`, `TLDR`, `ONE_LINER`

---

### System

| Method | Path | Description |
|---|---|---|
| GET | `/models` | List available Ollama models with size in MB |
| GET | `/health` | Check Ollama connectivity and latency |

**Health response:**
```json
{
  "status": "UP",
  "ollamaReachable": true,
  "latencyMs": 23,
  "defaultModel": "phi4-mini"
}
```

## Configuration

```yaml
# src/main/resources/application.yml
ollama:
  host: http://localhost:11434   # Ollama server URL
  default-model: phi4-mini      # Model used when not specified in request
  timeout-seconds: 120          # WebClient read timeout
```

For remote Ollama (e.g., a dedicated AI server on your local network):
```yaml
ollama:
  host: http://192.168.1.11:11434
```

## Running

Requires a running Ollama instance with at least one model pulled.

```bash
# Pull a model first
ollama pull phi4-mini

# Run the app
./gradlew bootRun
```

The API is available at `http://localhost:8080`.

## Tests

Tests use WireMock to mock Ollama — no Ollama required to run them.

```bash
./gradlew test
```

## Architecture

```
controller/       ← Thin REST layer (request/response mapping)
service/          ← Business logic (prompt engineering, response parsing)
client/           ← Ollama HTTP client (WebClient)
model/
  ollama/         ← Ollama API types (internal)
  request/        ← Inbound DTOs
  response/       ← Outbound DTOs
config/           ← Spring configuration beans
exception/        ← Global error handling
```

The service layer is the abstraction point. When adding LangChain4j or Spring AI later, only the client layer needs to change.

## What's Next

- [ ] Conversation history with persistence (PostgreSQL / H2)
- [ ] ChromaDB vector embeddings for semantic memory
- [ ] LangChain4j integration for agent patterns
- [ ] Spring AI adapter (swap Ollama for any compatible provider)
- [ ] Multi-turn chat sessions with context window management
