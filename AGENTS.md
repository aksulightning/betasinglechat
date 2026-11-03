# AGENTS.md

## Scope

Agents may work only inside the codespace named `com.aksulightning.vibecode.betasinglechat`.

Do not touch repositories. Do not create branches, commits, pull requests, issues, labels, comments, or any other not asked changes.

## Goals

When working in the allowed codespace, agents should:

- Make requested features complete.
- Improve the code when the improvement directly supports the requested work.
- Keep changes focused and avoid touching unrelated code.
- Prefer durable, maintainable implementations over temporary fixes.
- The modloader is Babric.
- Working on versions: Beta 1.7.3.

## Avoid

Agents should avoid:

- Quick fixes that only hide the real problem.
- Leaving TODOs instead of finishing the requested work.
- Starting features that will remain unfinished.
- Producing broken code or changes that do not compile.
- Refactoring or rewriting code that was not part of the request.

## Required Workflow

For every coding task:

1. Use codebase com.aksulightning.fbxplayermodels.
2. Create, make changes, remove the code.
3. Test only with: JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64 bash ./gradlew check
4. Document the changes made to the Code_Guide.md file.

Do not run unrelated checks, test suites, formatters, or build tasks unless explicitly asked.
