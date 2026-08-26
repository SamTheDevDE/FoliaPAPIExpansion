// FoliaPAPIExpansion
// Copyright (C) 2026 SamTheDevDE
// SPDX-License-Identifier: GPL-3.0-or-later

package de.samthedev.FoliaPAPIExpansion.placeholder

import de.samthedev.FoliaPAPIExpansion.config.ExpansionConfig
import de.samthedev.FoliaPAPIExpansion.health.HealthEvaluator
import de.samthedev.FoliaPAPIExpansion.health.HealthStatus
import de.samthedev.FoliaPAPIExpansion.metrics.MetricInterval
import de.samthedev.FoliaPAPIExpansion.metrics.MetricSample
import de.samthedev.FoliaPAPIExpansion.metrics.MetricSnapshot
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

internal class MetricFormatter(private val config: ExpansionConfig) {
    private val healthEvaluator = HealthEvaluator(config)
    private val numberFormats = ThreadLocal.withInitial {
        DecimalFormat(pattern(config.decimalPlaces), DecimalFormatSymbols.getInstance(Locale.ROOT)).apply {
            roundingMode = RoundingMode.HALF_UP
            isGroupingUsed = false
        }
    }

    fun metric(
        kind: MetricKind,
        sample: MetricSample?,
        colored: Boolean,
        percentage: Boolean,
    ): String {
        val rawValue = when (kind) {
            MetricKind.TPS -> sample?.tps
            MetricKind.MSPT -> sample?.mspt
            MetricKind.UTILIZATION -> sample?.utilization?.times(100.0)
        }.validNonNegative() ?: return config.unavailable

        return format(kind, rawValue, colored, percentage)
    }

    fun utilization(value: Double?, colored: Boolean): String {
        val percent = value?.times(100.0).validNonNegative() ?: return config.unavailable
        return format(MetricKind.UTILIZATION, percent, colored, percentage = false)
    }

    private fun format(kind: MetricKind, rawValue: Double, colored: Boolean, percentage: Boolean): String {
        val displayValue = when {
            percentage && kind == MetricKind.TPS -> (rawValue / TARGET_TPS * 100.0).coerceIn(0.0, 100.0)
            percentage && kind == MetricKind.MSPT -> rawValue / TICK_BUDGET_MILLIS * 100.0
            else -> rawValue
        }
        val overTargetMarker = if (kind == MetricKind.TPS && rawValue > TARGET_TPS) "*" else ""
        val suffix = if (percentage) "%" else ""
        val formatted = overTargetMarker + numberFormats.get().format(displayValue) + suffix
        if (!colored) return formatted

        val status = when (kind) {
            MetricKind.TPS -> healthEvaluator.tpsStatus(rawValue)
            MetricKind.MSPT -> healthEvaluator.msptStatus(rawValue)
            MetricKind.UTILIZATION -> healthEvaluator.utilizationStatus(rawValue)
        }
        return color(status) + formatted
    }

    fun metricList(kind: MetricKind, snapshot: MetricSnapshot?): String {
        if (snapshot == null) return config.unavailable
        val result = StringBuilder()
        for (interval in MetricInterval.entries) {
            if (result.isNotEmpty()) result.append(LEGACY_GRAY_SEPARATOR)
            result.append(metric(kind, snapshot[interval], colored = true, percentage = false))
        }
        return result.toString()
    }

    fun health(sample: MetricSample?, colored: Boolean): String {
        val status = healthEvaluator.evaluate(sample) ?: return config.unavailable
        val label = when (status) {
            HealthStatus.HEALTHY -> config.healthyLabel
            HealthStatus.DEGRADED -> config.warningLabel
            HealthStatus.OVERLOADED -> config.criticalLabel
        }
        return if (colored) color(status) + label else label
    }

    private fun color(status: HealthStatus): String = when (status) {
        HealthStatus.HEALTHY -> config.healthyColor
        HealthStatus.DEGRADED -> config.warningColor
        HealthStatus.OVERLOADED -> config.criticalColor
    }.replace('&', LEGACY_COLOR_CHARACTER)

    private fun Double?.validNonNegative(): Double? = this?.takeIf { it.isFinite() && it >= 0.0 }

    private companion object {
        const val TARGET_TPS = 20.0
        const val TICK_BUDGET_MILLIS = 50.0
        const val LEGACY_COLOR_CHARACTER = '\u00a7'
        const val LEGACY_GRAY_SEPARATOR = "\u00a77, "

        fun pattern(decimalPlaces: Int): String = if (decimalPlaces == 0) {
            "0"
        } else {
            "0." + "0".repeat(decimalPlaces)
        }
    }
}
