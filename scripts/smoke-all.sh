#!/usr/bin/env bash
set -e
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

export HOST=${HOST:-http://localhost:8080}
export MODEL=${MODEL:-phi4-mini}

echo "======================================"
echo " Full Smoke  |  $HOST  |  $MODEL"
echo "======================================"
echo ""

bash "$DIR/test-health.sh"
echo ""
bash "$DIR/test-models.sh"
echo ""
bash "$DIR/test-chat.sh"
echo ""
bash "$DIR/test-review.sh"
echo ""
bash "$DIR/test-commit.sh"
echo ""
bash "$DIR/test-summarize.sh"

echo "======================================"
echo " ✓ Full smoke suite passed"
echo "======================================"
