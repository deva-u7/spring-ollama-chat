#!/usr/bin/env bash
set -e
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

export HOST=${HOST:-http://localhost:8080}
export MODEL=${MODEL:-phi4-mini}

echo "======================================"
echo " Smoke Test  |  $HOST  |  $MODEL"
echo "======================================"
echo ""

bash "$DIR/test-health.sh"
echo ""
bash "$DIR/test-models.sh"
echo ""

echo "--- Chat (single turn) ---"
curl -s -X POST "$HOST/chat" \
  -H "Content-Type: application/json" \
  -d "{\"prompt\":\"What is reactive programming? Answer in one sentence.\",\"model\":\"$MODEL\"}" \
  | python3 -m json.tool
echo ""

echo "--- Code Review ---"
curl -s -X POST "$HOST/tools/review" \
  -H "Content-Type: application/json" \
  -d "{\"code\":\"fun divide(a: Int, b: Int) = a / b\",\"language\":\"Kotlin\",\"model\":\"$MODEL\"}" \
  | python3 -m json.tool
echo ""

echo "--- Commit Message ---"
curl -s -X POST "$HOST/tools/commit" \
  -H "Content-Type: application/json" \
  -d "{\"diff\":\"+ fun getUserById(id: UUID): User? = userRepository.findById(id).orElse(null)\",\"model\":\"$MODEL\"}" \
  | python3 -m json.tool
echo ""

echo "--- Summarize (BULLETS) ---"
curl -s -X POST "$HOST/tools/summarize" \
  -H "Content-Type: application/json" \
  -d "{\"text\":\"Kotlin is a JVM language by JetBrains with null safety, coroutines, and full Java interop.\",\"style\":\"BULLETS\",\"model\":\"$MODEL\"}" \
  | python3 -m json.tool

echo ""
echo "======================================"
echo " ✓ Smoke tests passed"
echo "======================================"
