// FoliaPAPIExpansion
// Copyright (C) 2026 SamTheDevDE
// SPDX-License-Identifier: GPL-3.0-or-later

package de.samthedev.foliapapiexpansion.health

import de.samthedev.foliapapiexpansion.config.ExpansionConfig
import de.samthedev.foliapapiexpansion.metrics.MetricSample

internal class HealthEvaluator(private val config: ExpansionConfig) {
    fun evaluate(sample: MetricSample?): HealthStatus? {
        val currentSample = sample ?: return null
        val tps = currentSample.tps.validNonNegative() ?: return null
        val mspt = currentSample.mspt.validNonNegative() ?: return null
        return maxOf(tpsStatus(tps), msptStatus(mspt))
    }

    fun tpsStatus(value: Double): HealthStatus = when {
        value >= config.tps.healthy -> HealthStatus.HEALTHY
        value >= config.tps.warning -> HealthStatus.DEGRADED
        else -> HealthStatus.OVERLOADED
    }

    fun msptStatus(value: Double): HealthStatus = when {
        value <= config.mspt.healthy -> HealthStatus.HEALTHY
        value <= config.mspt.warning -> HealthStatus.DEGRADED
        else -> HealthStatus.OVERLOADED
    }

    fun utilizationStatus(percent: Double): HealthStatus = when {
        percent <= config.utilization.healthy -> HealthStatus.HEALTHY
        percent <= config.utilization.warning -> HealthStatus.DEGRADED
        else -> HealthStatus.OVERLOADED
    }

    private fun Double?.validNonNegative(): Double? = this?.takeIf { it.isFinite() && it >= 0.0 }
}
