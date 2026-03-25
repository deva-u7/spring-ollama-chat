#!/usr/bin/env bash
HOST=${HOST:-http://localhost:8080}

echo "--- Available Models ---"
curl -s "$HOST/models" | python3 -m json.tool
