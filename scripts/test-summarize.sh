#!/usr/bin/env bash
HOST=${HOST:-http://localhost:8080}
MODEL=${MODEL:-phi4-mini}

TEXT="Kotlin is a modern statically typed programming language developed by JetBrains. \
It runs on the JVM and is fully interoperable with Java. Kotlin features null safety built into \
the type system, extension functions, coroutines for asynchronous programming, and data classes \
that eliminate boilerplate. It is the preferred language for Android development and is \
increasingly used in backend services with Spring Boot."

for STYLE in BULLETS TLDR ONE_LINER PARAGRAPH; do
  echo "--- Summarize ($STYLE) ---"
  curl -s -X POST "$HOST/tools/summarize" \
    -H "Content-Type: application/json" \
    -d "{\"text\":\"$TEXT\",\"style\":\"$STYLE\",\"model\":\"$MODEL\"}" \
    | python3 -m json.tool
  echo ""
done
