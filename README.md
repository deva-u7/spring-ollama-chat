# spring-ollama-chat

A Kotlin + Spring Boot application that integrates with [Ollama](https://ollama.com) for local LLM inference.

Rather than basic chat forwarding, this app exposes developer tools powered by local models: code review, conventional commit message generation, and multi-style text summarization — all running on your own hardware with no external API calls.

## Architecture

```
HTTP Client
      │
      ▼
RequestLoggingFilter      ← correlation ID, request/response logging
      │
      ▼
Controller                ← maps HTTP ↔ domain types
      │
      ▼
Service                   ← prompt templates, response parsing, fallbacks
      │
      ▼
OllamaClient              ← WebClient, logs model + latency
      │
      ▼
Ollama  (local / LAN)     ← /api/chat  /api/generate  /api/tags
```

Full design → [docs/architecture.md](docs/architecture.md)

---

## Stack

- **Kotlin** + **Spring Boot 3** (WebFlux — reactive, non-blocking)
- **Ollama REST API** — chat, generate, model listing
- **Server-Sent Events** for streaming responses
- **WireMock** + **Spring WebFlux Test** for tests

## Quick Start

```bash
# 1. Pull a model
ollama pull phi4-mini

# 2. Build
make build

# 3. Run (defaults to local profile — see Configuration)
make run

# 4. Smoke test all endpoints
make smoke
```

## Makefile Targets

```
make build              # Compile + unit tests
make run                # Start app  (PROFILE=local)
make test               # Unit tests only
make clean              # Clean build output

make health             # GET  /health
make models             # GET  /models

make chat               # POST /chat
make chat-with-system   # POST /chat  — with system prompt
make chat-stream        # POST /chat/stream  — SSE

make review-bad         # POST /tools/review  — buggy code
make review-good        # POST /tools/review  — clean code
make commit-feat        # POST /tools/commit  — feature diff
make commit-fix         # POST /tools/commit  — bug fix diff
make summarize-bullets  # POST /tools/summarize  — BULLETS
make summarize-tldr     # POST /tools/summarize  — TLDR
make summarize-one-liner # POST /tools/summarize — ONE_LINER
make summarize-paragraph # POST /tools/summarize — PARAGRAPH

make smoke              # Core endpoints — health, models, chat, review, commit, summarize
make smoke-all          # Every endpoint × every payload variant

make tunnel             # Start ngrok tunnel on port 8080
make serve-frontend     # Serve frontend/ on port 8181 (for local dev)
```

Override model or host:
```bash
make chat MODEL=qwen2.5:14b
make smoke HOST=http://YOUR_SERVER_IP:8080
```

## Endpoints

### System

| Method | Path | Description |
|---|---|---|
| GET | `/health` | Ollama connectivity check + latency |
| GET | `/models` | List available Ollama models with size in MB |

**Health response:**
```json
{
  "status": "UP",
  "ollamaReachable": true,
  "latencyMs": 212,
  "defaultModel": "phi4-mini"
}
```

---

### Chat

| Method | Path | Description |
|---|---|---|
| POST | `/chat` | Single-turn chat — returns full response |
| POST | `/chat/stream` | Streaming chat — SSE token-by-token |

```json
{
  "prompt": "What is reactive programming?",
  "model": "phi4-mini",
  "systemPrompt": "You are a concise technical writer."
}
```

`model` and `systemPrompt` are optional.

---

### Developer Tools

| Method | Path | Description |
|---|---|---|
| POST | `/tools/review` | Code review with score, issues, and suggestions |
| POST | `/tools/commit` | Conventional commit message from a git diff |
| POST | `/tools/summarize` | Summarize text in a chosen style |

**Code review:**
```json
{ "code": "fun divide(a: Int, b: Int) = a / b", "language": "Kotlin" }
```
```json
{
  "score": 2,
  "summary": "Function lacks error handling for division by zero.",
  "issues": [
    {
      "severity": "CRITICAL",
      "description": "Division by zero not handled.",
      "suggestion": "Add require(b != 0) before dividing."
    }
  ]
}
```

**Commit message:**
```json
{ "diff": "+ fun getUserById(id: UUID): User? = userRepository.findById(id).orElse(null)" }
```
```json
{
  "type": "feat",
  "scope": "user",
  "description": "add getUserById returning nullable User",
  "message": "feat(user): add getUserById returning nullable User"
}
```

**Summarize** — styles: `PARAGRAPH` · `BULLETS` · `TLDR` · `ONE_LINER`
```json
{ "text": "...", "style": "BULLETS" }
```

---

## Configuration

All config lives in `src/main/resources/application.yml`:

```yaml
ollama:
  host: http://localhost:11434    # Ollama server URL
  default-model: phi4-mini       # Used when model not specified in request
  timeout-seconds: 120           # WebClient read timeout
  cors:
    allowed-origins:
      - "*"
```

For a remote Ollama instance (e.g. a dedicated server on your LAN), create `src/main/resources/application-local.yml` — it is gitignored:

```yaml
ollama:
  host: http://YOUR_SERVER_IP:11434
  timeout-seconds: 30
  cors:
    allowed-origins:
      - "http://localhost:3000"
```

Activate with:
```bash
make run PROFILE=local
# or
./gradlew bootRun --args='--spring.profiles.active=local'
```

## Tests

Tests use WireMock to mock Ollama — no running Ollama required.

```bash
make test
```

## Architecture

```
controller/       ← Thin REST layer (request/response mapping only)
service/          ← Business logic — prompt building, response parsing
client/           ← Ollama HTTP client (WebClient)
config/
  PromptLoader    ← Loads prompt templates from resources/prompts/ at startup
filter/           ← Request logging with correlation ID (X-Correlation-Id)
model/
  ollama/         ← Ollama API types (internal)
  request/        ← Inbound request DTOs
  response/       ← Outbound response DTOs
exception/        ← Global error handling
resources/
  prompts/        ← Prompt templates (code-review.txt, commit-message.txt, summarize.txt)
```

The service layer is the abstraction point. When adding LangChain4j or Spring AI, only the client layer changes — services stay the same.

See [docs/architecture.md](docs/architecture.md) for the full design.

## Frontend

`docs/index.html` is a single-file, no-framework UI for all endpoints.

- **Base URL field** — paste `http://localhost:8080` or an ngrok URL, saved to localStorage
- **Tabs**: Chat · Code Review · Commit · Summarize
- **SSE streaming** — the Chat tab supports real-time token streaming
- **Mobile-friendly** — works from phone when exposed via ngrok

**To use from your phone:**

```bash
# Terminal 1 — run the API
make run

# Terminal 2 — start ngrok tunnel
make tunnel          # prints a public URL like https://abc123.ngrok-free.app

# Terminal 3 — serve the frontend locally
make serve-frontend  # → http://localhost:8181

# Or: open frontend/index.html directly in a browser
# Paste the ngrok URL as the base URL in the page → works from any device
```

The page can also be hosted on [GitHub Pages](https://pages.github.com/) — push `frontend/` to the repo and enable Pages from the `main` branch.

## What's Next

- [ ] Conversation history with persistence (PostgreSQL / H2)
- [ ] ChromaDB vector embeddings for semantic memory
- [ ] LangChain4j integration for agent patterns
- [ ] Spring AI adapter (swap Ollama for any compatible provider)
- [ ] Multi-turn chat sessions with context window management
