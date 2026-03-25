#!/usr/bin/env bash
HOST=${HOST:-http://localhost:8080}

echo "--- Health ---"
curl -s "$HOST/health" | python3 -m json.tool
