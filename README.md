# Folia-Expansion

[![CI](https://github.com/SamTheDevDE/Folia-Expansion/actions/workflows/build.yml/badge.svg)](https://github.com/SamTheDevDE/Folia-Expansion/actions/workflows/build.yml)
[![Latest release](https://img.shields.io/github/v/release/SamTheDevDE/Folia-Expansion?display_name=tag)](https://github.com/SamTheDevDE/Folia-Expansion/releases/latest)
[![License](https://img.shields.io/github/license/SamTheDevDE/Folia-Expansion)](LICENSE)
![Java 25](https://img.shields.io/badge/Java-25-orange)
![Folia 26.2](https://img.shields.io/badge/Folia-26.2-blue)

A lightweight Kotlin PlaceholderAPI expansion exposing Folia scheduler and server-health metrics.

## About

Folia-Expansion exposes global-region and current-player-region tick statistics without scheduling tasks, blocking a region thread, performing I/O, or contacting external services. Version 2 is a ground-up Kotlin rewrite designed for modern Folia servers.

The expansion is designed for frequently refreshed displays such as TAB. Folia-specific, non-public metric access is contained in one provider so the parser, formatter, health logic, and configuration remain independent and testable.

## Features

- Global and player-region TPS, MSPT, and utilization for five reporting intervals
- TPS and 50 ms tick-budget percentages
- Configurable colored health output
- Scheduler thread and active-region counts
- Null-player support for every global placeholder
- Locale-independent number formatting and safe handling of unavailable/invalid reports
- Short, lock-free publication caches; no coroutines, telemetry, networking, or filesystem reads
- Self-contained release JAR with a relocated Kotlin runtime

## Requirements

- Minecraft 26.2
- Folia 26.2 (`26.2.build.7-beta` is the pinned compile target)
- Java 25
- PlaceholderAPI 2.12.3 or newer compatible 2.12.x release

Folia 26.2 itself is still published as a beta at the pinned build. That qualifier comes from the upstream server; all other direct build dependencies use stable releases except paperweight, whose current supported line is named `2.0.0-beta`.

## Installation

1. Install Folia 26.2 and PlaceholderAPI 2.12.3.
2. Download `Folia-Expansion-<version>.jar` from this repository's Releases page.
3. Put the JAR in `plugins/PlaceholderAPI/expansions/`.
4. Restart the backend or run `/papi reload`.
5. Verify with `/papi parse me %folia_global_tps_5s%`.

Do not install the `-thin` or `-sources` artifacts. The release JAR contains the relocated Kotlin runtime needed on a clean server, but does not contain Folia, Paper, Bukkit, Minecraft, or PlaceholderAPI classes.

## Placeholders

Intervals are `5s`, `15s`, `1m`, `5m`, and `15m`. Placeholder identifiers are case-sensitive.

### Existing placeholders

All version 1 names remain accepted:

| Scope | Placeholders | Output |
|---|---|---|
| Global | `%folia_global_tps%`, `%folia_global_mspt%` | Colored values for all five intervals |
| Global | `%folia_global_tps_<interval>%`, `%folia_global_mspt_<interval>%` | One numeric value |
| Global | `%folia_global_tps_<interval>_colored%`, `%folia_global_mspt_<interval>_colored%` | One legacy-colored value |
| Global | `%folia_global_util%`, `%folia_global_util_colored%` | 15-second scheduler-capacity utilization |
| Region | `%folia_tps%`, `%folia_mspt%` | Colored values for all five intervals |
| Region | `%folia_tps_<interval>%`, `%folia_mspt_<interval>%` | One numeric value |
| Region | `%folia_tps_<interval>_colored%`, `%folia_mspt_<interval>_colored%` | One legacy-colored value |
| Region | `%folia_util%`, `%folia_util_colored%` | Current region's 5-second utilization |

For example, `<interval>` expands to `%folia_global_tps_5s%`, `%folia_global_tps_15s%`, `%folia_global_tps_1m%`, `%folia_global_tps_5m%`, and `%folia_global_tps_15m%` (and the equivalent MSPT/region/colored names).

### New placeholders

| Placeholders | Output |
|---|---|
| `%folia_global_tps_<interval>_percent%` | Global TPS as a percentage of 20 TPS, clamped to 100% |
| `%folia_global_tps_<interval>_percent_colored%` | Colored global TPS percentage |
| `%folia_tps_<interval>_percent%` | Current-region TPS percentage |
| `%folia_tps_<interval>_percent_colored%` | Colored current-region TPS percentage |
| `%folia_global_mspt_<interval>_percent%` | Global MSPT as utilization of the 50 ms tick budget |
| `%folia_global_mspt_<interval>_percent_colored%` | Colored global MSPT percentage |
| `%folia_mspt_<interval>_percent%` | Current-region MSPT tick-budget percentage |
| `%folia_mspt_<interval>_percent_colored%` | Colored current-region MSPT percentage |
| `%folia_global_util_<interval>%` | Aggregate Folia scheduler-capacity utilization |
| `%folia_global_util_<interval>_colored%` | Colored aggregate scheduler utilization |
| `%folia_util_<interval>%` | Current-region utilization |
| `%folia_util_<interval>_colored%` | Colored current-region utilization |
| `%folia_global_health%`, `%folia_global_health_colored%` | 5-second global health label |
| `%folia_health%`, `%folia_health_colored%` | 5-second current-region health label |
| `%folia_scheduler_threads%` | Live Folia scheduler thread count |
| `%folia_regions%` | Active region count across loaded worlds |
| `%folia_expansion_version%` | Exact expansion build version |

Both `_percent_colored` and `_colored_percent` suffix orders are accepted. MSPT percentage is tick-time budget utilization, not CPU usage; values above 100% intentionally remain visible.

## Examples

```text
TPS: %folia_global_tps_5s_colored%
MSPT: %folia_global_mspt_5s_colored% ms
Health: %folia_global_health_colored%
Regions: %folia_regions% / Threads: %folia_scheduler_threads%
```

TPS values above 20 retain the version 1 `*` marker. Invalid, negative, non-finite, or unavailable values render as the configured unavailable text.

## TAB / TAB-Bridge

A common proxy layout is:

```text
Velocity proxy: TAB
Folia backend: PlaceholderAPI + TAB-Bridge + Folia-Expansion
```

Use the placeholders in the backend/server section of TAB configuration, for example:

```yaml
footer:
  - "&7TPS: %folia_global_tps_5s_colored%"
  - "&7MSPT: %folia_global_mspt_5s_colored%"
```

TAB and TAB-Bridge are optional; any PlaceholderAPI consumer can use the expansion.

## Configuration

PlaceholderAPI writes configurable expansion defaults beneath `expansions.folia` in its configuration. Defaults work without editing:

```yaml
expansions:
  folia:
    format:
      decimal-places: 2
      unavailable: "N/A"
    tps:
      healthy: 19.5
      warning: 18.0
    mspt:
      healthy: 40.0
      warning: 50.0
    utilization:
      healthy: 80.0
      warning: 100.0
    colors:
      healthy: "&a"
      warning: "&e"
      critical: "&c"
    health:
      healthy: "HEALTHY"
      warning: "DEGRADED"
      critical: "OVERLOADED"
```

TPS at or above `healthy` is healthy; TPS at or above `warning` is degraded. MSPT/utilization at or below `healthy` is healthy and at or below `warning` is degraded. Reload PlaceholderAPI after changing values. Decimal places are safely clamped from 0 through 6, and numbers always use `Locale.ROOT` (`19.98`, never locale-dependent `19,98`).

Version 2 replaces the old `tps_color.high`, `tps_color.medium`, and `tps_color.low` configuration keys with `colors.healthy`, `colors.warning`, and `colors.critical`. Placeholder names did not change, but custom version 1 colors must be copied to the new keys once.

## Compatibility and thread safety

Global placeholders do not require a player. Region placeholders only read the current tick region when Folia reports that the current thread owns the supplied online player; otherwise they return the unavailable value. This prevents an asynchronous or unrelated-region PlaceholderAPI call from silently reporting the wrong region.

Metrics use Folia internals because no public API exposes these tick reports. The provider is pinned to Folia 26.2 and may require an update for a later Minecraft/Folia release. See [SECURITY.md](SECURITY.md) for reporting problems.

## Building

Install Java 25, then run:

```bash
./gradlew clean check build --warning-mode=all
```

The installable artifact is `build/libs/Folia-Expansion-<version>.jar`. The build also creates a thin development JAR and a sources JAR; neither is the server install artifact. The Gradle build is Kotlin DSL, compiles Kotlin to JVM 25 bytecode, and packages/relocates Kotlin stdlib while leaving server dependencies provided.

## Releases

Semantic version tags trigger the release workflow. It verifies the tag against Gradle's version, runs tests, checks the runtime JAR contents, creates a SHA-256 checksum, and publishes both files:

```bash
git tag vX.Y.Z
git push origin vX.Y.Z
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Please keep parsing and formatting independent of Bukkit/Folia so they remain unit-testable.

## Credits

This project is a modernized Kotlin rewrite of [VanillaAdventures/Folia-Expansion](https://github.com/VanillaAdventures/Folia-Expansion).

## License

Folia-Expansion remains licensed under [GPL-3.0-or-later](LICENSE). Kotlin stdlib is distributed under Apache-2.0 inside the runtime JAR; its license notice is preserved in the artifact.
