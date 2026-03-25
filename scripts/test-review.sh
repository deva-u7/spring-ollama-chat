#!/usr/bin/env bash
HOST=${HOST:-http://localhost:8080}
MODEL=${MODEL:-phi4-mini}

echo "--- Code Review (bad code — no error handling) ---"
curl -s -X POST "$HOST/tools/review" \
  -H "Content-Type: application/json" \
  -d "{\"code\":\"fun divide(a: Int, b: Int) = a / b\",\"language\":\"Kotlin\",\"model\":\"$MODEL\"}" \
  | python3 -m json.tool

echo ""
echo "--- Code Review (good code — with guard) ---"
curl -s -X POST "$HOST/tools/review" \
  -H "Content-Type: application/json" \
  -d "{\"code\":\"fun divide(a: Int, b: Int): Result<Int> {\\n    require(b != 0) { \\\"Divisor must not be zero\\\" }\\n    return Result.success(a / b)\\n}\",\"language\":\"Kotlin\",\"model\":\"$MODEL\"}" \
  | python3 -m json.tool
