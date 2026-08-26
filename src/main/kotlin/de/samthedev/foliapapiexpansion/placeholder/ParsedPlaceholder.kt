// FoliaPAPIExpansion
// Copyright (C) 2026 SamTheDevDE
// SPDX-License-Identifier: GPL-3.0-or-later

package de.samthedev.foliapapiexpansion.placeholder

import de.samthedev.foliapapiexpansion.metrics.MetricInterval

internal enum class MetricScope {
    GLOBAL,
    REGION,
}

internal enum class MetricKind {
    TPS,
    MSPT,
    UTILIZATION,
}

internal sealed interface ParsedPlaceholder {
    data class Metric(
        val scope: MetricScope,
        val kind: MetricKind,
        val interval: MetricInterval?,
        val colored: Boolean,
        val percentage: Boolean,
    ) : ParsedPlaceholder

    data class Health(val scope: MetricScope, val colored: Boolean) : ParsedPlaceholder

    data object SchedulerThreads : ParsedPlaceholder

    data object RegionCount : ParsedPlaceholder

    data object ExpansionVersion : ParsedPlaceholder
}
