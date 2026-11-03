#!/usr/bin/env bash
set -euo pipefail

cd -- "$(dirname -- "${BASH_SOURCE[0]}")"

# Respect an explicit JAVA_HOME; otherwise find a JDK suitable for Gradle 9.
if [[ -z "${JAVA_HOME:-}" ]]; then
    for jdk_candidate in /usr/lib/jvm/*; do
        [[ -x "$jdk_candidate/bin/java" && -x "$jdk_candidate/bin/javac" ]] || continue
        jdk_version="$("$jdk_candidate/bin/javac" -version 2>&1)" || continue
        jdk_version="${jdk_version#javac }"
        if [[ "$jdk_version" =~ ^([0-9]+) ]] && (( BASH_REMATCH[1] >= 21 )); then
            export JAVA_HOME="$jdk_candidate"
            break
        fi
    done
fi

exec bash ./gradlew build "$@"
