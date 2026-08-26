// FoliaPAPIExpansion
// Copyright (C) 2026 SamTheDevDE
// SPDX-License-Identifier: GPL-3.0-or-later

package de.samthedev.foliapapiexpansion.placeholder

import de.samthedev.foliapapiexpansion.config.ExpansionConfig
import de.samthedev.foliapapiexpansion.metrics.MetricInterval
import de.samthedev.foliapapiexpansion.metrics.MetricProvider
import de.samthedev.foliapapiexpansion.metrics.MetricSnapshot

internal class PlaceholderRenderer(
    private val provider: MetricProvider,
    config: ExpansionConfig,
    private val expansionVersion: String,
) {
    private val formatter = MetricFormatter(config)
    private val unavailable = config.unavailable

    fun render(placeholder: ParsedPlaceholder, regionContextAvailable: Boolean): String = when (placeholder) {
        ParsedPlaceholder.ExpansionVersion -> expansionVersion
        ParsedPlaceholder.SchedulerThreads -> provider.schedulerThreads()?.toString() ?: unavailable
        ParsedPlaceholder.RegionCount -> provider.regionCount()?.toString() ?: unavailable
        is ParsedPlaceholder.Health -> renderHealth(placeholder, regionContextAvailable)
        is ParsedPlaceholder.Metric -> renderMetric(placeholder, regionContextAvailable)
    }

    private fun renderHealth(
        placeholder: ParsedPlaceholder.Health,
        regionContextAvailable: Boolean,
    ): String {
        val snapshot = snapshotFor(placeholder.scope, regionContextAvailable)
        return formatter.health(snapshot?.get(MetricInterval.FIVE_SECONDS), placeholder.colored)
    }

    private fun renderMetric(
        placeholder: ParsedPlaceholder.Metric,
        regionContextAvailable: Boolean,
    ): String {
        if (placeholder.kind == MetricKind.UTILIZATION && placeholder.scope == MetricScope.GLOBAL) {
            val interval = placeholder.interval ?: MetricInterval.FIFTEEN_SECONDS
            val utilization = provider.serverUtilizationSnapshot()?.get(interval)
            return formatter.utilization(utilization, placeholder.colored)
        }

        val snapshot = snapshotFor(placeholder.scope, regionContextAvailable)
        val interval = placeholder.interval
        if (interval == null && placeholder.kind != MetricKind.UTILIZATION) {
            return formatter.metricList(placeholder.kind, snapshot)
        }

        val effectiveInterval = interval ?: MetricInterval.FIVE_SECONDS
        return formatter.metric(
            placeholder.kind,
            snapshot?.get(effectiveInterval),
            placeholder.colored,
            placeholder.percentage,
        )
    }

    private fun snapshotFor(scope: MetricScope, regionContextAvailable: Boolean): MetricSnapshot? = when (scope) {
        MetricScope.GLOBAL -> provider.globalSnapshot()
        MetricScope.REGION -> if (regionContextAvailable) provider.currentRegionSnapshot() else null
    }
}
