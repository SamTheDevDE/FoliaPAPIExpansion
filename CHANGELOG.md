# Changelog

All notable changes are documented here. This project follows [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [2.0.2] - 2026-08-26

### Added

- Complete idiomatic Kotlin rewrite targeting JVM/Java 25 and Folia 26.2.
- Interval-specific utilization, TPS percentage, MSPT tick-budget percentage, health, scheduler-thread, region-count, and expansion-version placeholders.
- Configurable formatting, thresholds, colors, health labels, and unavailable text.
- Short-lived global and per-region snapshot caching.
- Kotlin/JUnit 5 unit tests, secure CI, tag-driven releases, and Dependabot.
- Self-contained runtime JAR with relocated Kotlin stdlib.

### Changed

- Global placeholders now work without an `OfflinePlayer` context.
- Folia internals are isolated in a version-specific provider.
- Number formatting is locale-independent and rejects invalid metrics.
- Expansion and artifact versions now share Gradle's project version.
- Color configuration uses the clearer `colors.healthy`, `colors.warning`, and `colors.critical` keys; version 1 custom colors require a one-time migration.

### Removed

- All Java source, Guava caching, Adventure formatting, and the internal Folia `CommandUtil` dependency.

[Unreleased]: https://github.com/SamTheDevDE/FoliaPAPIExpansion/compare/v2.0.2...HEAD
[2.0.2]: https://github.com/SamTheDevDE/FoliaPAPIExpansion/compare/400fbee...v2.0.2
