#!/usr/bin/env bash
HOST=${HOST:-http://localhost:8080}
MODEL=${MODEL:-phi4-mini}

echo "--- Chat (single turn) ---"
curl -s -X POST "$HOST/chat" \
  -H "Content-Type: application/json" \
  -d "{\"prompt\":\"What is reactive programming? Answer in one sentence.\",\"model\":\"$MODEL\"}" \
  | python3 -m json.tool

echo ""
echo "--- Chat (with system prompt) ---"
curl -s -X POST "$HOST/chat" \
  -H "Content-Type: application/json" \
  -d "{\"prompt\":\"Explain coroutines\",\"model\":\"$MODEL\",\"systemPrompt\":\"You are a Kotlin expert. Be concise and use bullet points.\"}" \
  | python3 -m json.tool

echo ""
echo "--- Chat Stream (SSE) ---"
curl -s -X POST "$HOST/chat/stream" \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d "{\"prompt\":\"Count from 1 to 5, one number per line.\",\"model\":\"$MODEL\"}"
echo ""
