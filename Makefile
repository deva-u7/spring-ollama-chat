HOST    ?= http://localhost:8080
MODEL   ?= phi4-mini
PROFILE ?= local
NGROK_PORT ?= 8080

# ─── Build & Run ─────────────────────────────────────────────────────────────

.PHONY: build
build:
	./gradlew build --no-daemon -x bootJar

.PHONY: run
run:
	./gradlew bootRun --args='--spring.profiles.active=$(PROFILE)' --no-daemon

.PHONY: test
test:
	./gradlew test --no-daemon

.PHONY: clean
clean:
	./gradlew clean --no-daemon

# ─── Individual Endpoint Tests ───────────────────────────────────────────────

.PHONY: health
health:
	@HOST=$(HOST) bash scripts/test-health.sh

.PHONY: models
models:
	@HOST=$(HOST) bash scripts/test-models.sh

.PHONY: chat
chat:
	@HOST=$(HOST) MODEL=$(MODEL) bash scripts/test-chat.sh

.PHONY: review
review:
	@HOST=$(HOST) MODEL=$(MODEL) bash scripts/test-review.sh

.PHONY: commit
commit:
	@HOST=$(HOST) MODEL=$(MODEL) bash scripts/test-commit.sh

.PHONY: summarize
summarize:
	@HOST=$(HOST) MODEL=$(MODEL) bash scripts/test-summarize.sh

# ─── Smoke Tests ─────────────────────────────────────────────────────────────

.PHONY: smoke
smoke:
	@HOST=$(HOST) MODEL=$(MODEL) bash scripts/smoke.sh

.PHONY: smoke-all
smoke-all:
	@HOST=$(HOST) MODEL=$(MODEL) bash scripts/smoke-all.sh

# ─── Frontend / Tunnel ───────────────────────────────────────────────────────

.PHONY: tunnel
tunnel:
	ngrok http $(NGROK_PORT)

.PHONY: serve-frontend
serve-frontend:
	@echo "Open http://localhost:8181 and paste your ngrok URL as base URL"
	@python3 -m http.server 8181 --directory docs

# ─── Help ────────────────────────────────────────────────────────────────────

.PHONY: help
help:
	@echo ""
	@echo "Usage: make <target> [MODEL=phi4-mini] [HOST=http://localhost:8080]"
	@echo ""
	@echo "  Build"
	@echo "    build       Compile and run unit tests"
	@echo "    run         Start app (PROFILE=local)"
	@echo "    test        Unit tests only"
	@echo "    clean       Clean build output"
	@echo ""
	@echo "  Endpoint Tests  (scripts/)"
	@echo "    health      GET  /health"
	@echo "    models      GET  /models"
	@echo "    chat        POST /chat  /chat/stream"
	@echo "    review      POST /tools/review"
	@echo "    commit      POST /tools/commit"
	@echo "    summarize   POST /tools/summarize  (all 4 styles)"
	@echo ""
	@echo "  Smoke"
	@echo "    smoke       Core endpoints — quick pass"
	@echo "    smoke-all   Every endpoint × every payload variant"
	@echo ""
	@echo "  Overrides"
	@echo "    make chat MODEL=qwen2.5:14b"
	@echo "    make smoke HOST=http://192.168.1.11:8080"
	@echo ""
