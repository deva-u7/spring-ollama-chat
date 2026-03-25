#!/usr/bin/env bash
HOST=${HOST:-http://localhost:8080}
MODEL=${MODEL:-phi4-mini}

echo "--- Commit Message (new feature) ---"
curl -s -X POST "$HOST/tools/commit" \
  -H "Content-Type: application/json" \
  -d "{\"diff\":\"+ fun getUserById(id: UUID): User? = userRepository.findById(id).orElse(null)\",\"model\":\"$MODEL\"}" \
  | python3 -m json.tool

echo ""
echo "--- Commit Message (bug fix) ---"
curl -s -X POST "$HOST/tools/commit" \
  -H "Content-Type: application/json" \
  -d "{\"diff\":\"- if (user.role == \\\"admin\\\") {\\n+ if (user.role == \\\"admin\\\" && user.isActive) {\",\"model\":\"$MODEL\"}" \
  | python3 -m json.tool
