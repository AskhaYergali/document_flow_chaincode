#!/usr/bin/env bash
set -euo pipefail
if [ ! -d ".gradle-wrapper" ]; then
  mkdir -p .gradle-wrapper
fi
exec docker run --rm -u "$(id -u):$(id -g)" \
  -v "$PWD":/workspace -w /workspace \
  gradle:8.7-jdk17 gradle "$@"
