// FoliaPAPIExpansion
// Copyright (C) 2026 SamTheDevDE
// SPDX-License-Identifier: GPL-3.0-or-later

package de.samthedev.FoliaPAPIExpansion.config

import me.clip.placeholderapi.expansion.PlaceholderExpansion

internal data class Thresholds(
    val healthy: Double,
    val warning: Double,
)

internal data class ExpansionConfig(
    val decimalPlaces: Int = DEFAULT_DECIMAL_PLACES,
    val unavailable: String = DEFAULT_UNAVAILABLE,
    val tps: Thresholds = Thresholds(healthy = 19.5, warning = 18.0),
    val mspt: Thresholds = Thresholds(healthy = 40.0, warning = 50.0),
    val utilization: Thresholds = Thresholds(healthy = 80.0, warning = 100.0),
    val healthyColor: String = "&a",
    val warningColor: String = "&e",
    val criticalColor: String = "&c",
    val healthyLabel: String = "HEALTHY",
    val warningLabel: String = "DEGRADED",
    val criticalLabel: String = "OVERLOADED",
) {
    fun validated(): ExpansionConfig {
        val validTps = tps.takeIf {
            it.healthy.isFinite() && it.warning.isFinite() && it.healthy >= it.warning && it.warning >= 0.0
        } ?: Thresholds(19.5, 18.0)
        val validMspt = mspt.takeIf {
            it.healthy.isFinite() && it.warning.isFinite() && it.healthy >= 0.0 && it.warning >= it.healthy
        } ?: Thresholds(40.0, 50.0)
        val validUtilization = utilization.takeIf {
            it.healthy.isFinite() && it.warning.isFinite() && it.healthy >= 0.0 && it.warning >= it.healthy
        } ?: Thresholds(80.0, 100.0)

        return copy(
            decimalPlaces = decimalPlaces.coerceIn(0, MAX_DECIMAL_PLACES),
            unavailable = unavailable.ifBlank { DEFAULT_UNAVAILABLE },
            tps = validTps,
            mspt = validMspt,
            utilization = validUtilization,
            healthyColor = healthyColor.ifBlank { "&a" },
            warningColor = warningColor.ifBlank { "&e" },
            criticalColor = criticalColor.ifBlank { "&c" },
            healthyLabel = healthyLabel.ifBlank { "HEALTHY" },
            warningLabel = warningLabel.ifBlank { "DEGRADED" },
            criticalLabel = criticalLabel.ifBlank { "OVERLOADED" },
        )
    }

    companion object {
        const val DEFAULT_DECIMAL_PLACES = 2
        const val DEFAULT_UNAVAILABLE = "N/A"
        private const val MAX_DECIMAL_PLACES = 6

        fun from(expansion: PlaceholderExpansion): ExpansionConfig = ExpansionConfig(
            decimalPlaces = expansion.getInt("format.decimal-places", DEFAULT_DECIMAL_PLACES),
            unavailable = expansion.getString("format.unavailable", DEFAULT_UNAVAILABLE) ?: DEFAULT_UNAVAILABLE,
            tps = Thresholds(
                healthy = expansion.getDouble("tps.healthy", 19.5),
                warning = expansion.getDouble("tps.warning", 18.0),
            ),
            mspt = Thresholds(
                healthy = expansion.getDouble("mspt.healthy", 40.0),
                warning = expansion.getDouble("mspt.warning", 50.0),
            ),
            utilization = Thresholds(
                healthy = expansion.getDouble("utilization.healthy", 80.0),
                warning = expansion.getDouble("utilization.warning", 100.0),
            ),
            healthyColor = expansion.getString("colors.healthy", "&a") ?: "&a",
            warningColor = expansion.getString("colors.warning", "&e") ?: "&e",
            criticalColor = expansion.getString("colors.critical", "&c") ?: "&c",
            healthyLabel = expansion.getString("health.healthy", "HEALTHY") ?: "HEALTHY",
            warningLabel = expansion.getString("health.warning", "DEGRADED") ?: "DEGRADED",
            criticalLabel = expansion.getString("health.critical", "OVERLOADED") ?: "OVERLOADED",
        ).validated()
    }
}
