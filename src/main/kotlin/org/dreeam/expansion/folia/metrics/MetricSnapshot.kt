// Folia-Expansion
// Copyright (C) 2026 SamTheDevDE
// SPDX-License-Identifier: GPL-3.0-or-later

package org.dreeam.expansion.folia.metrics

internal data class MetricSample(
    val tps: Double?,
    val mspt: Double?,
    val utilization: Double?,
)

internal data class MetricSnapshot(
    val samples: Map<MetricInterval, MetricSample>,
    val capturedAtNanos: Long,
) {
    operator fun get(interval: MetricInterval): MetricSample? = samples[interval]
}

internal data class ServerUtilizationSnapshot(
    val utilization: Map<MetricInterval, Double?>,
    val regionCount: Int,
    val schedulerThreads: Int,
    val capturedAtNanos: Long,
) {
    operator fun get(interval: MetricInterval): Double? = utilization[interval]
}
