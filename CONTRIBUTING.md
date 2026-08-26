# Contributing

Thank you for improving Folia-Expansion.

## Development setup

- Use Java 25 and the committed Gradle Wrapper.
- Keep all application and test source in Kotlin under `src/main/kotlin` and `src/test/kotlin`.
- Do not add Java source, telemetry, network calls, blocking operations, coroutines, or bundled server dependencies.
- Isolate Folia/NMS/CraftBukkit internals in the `metrics` provider layer.

Before opening a pull request, run:

```bash
./gradlew clean check build --warning-mode=all
find src -type f -name '*.java'
```

The `find` command must print nothing. Add pure unit tests for parser, formatter, configuration, or health changes. Changes to Folia internals should explain the exact upstream API/build tested and why a public API cannot be used.

By contributing, you agree that your changes are licensed under GPL-3.0-or-later.
