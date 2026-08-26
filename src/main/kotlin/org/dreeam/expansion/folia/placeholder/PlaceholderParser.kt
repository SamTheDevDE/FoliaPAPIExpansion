// Folia-Expansion
// Copyright (C) 2026 SamTheDevDE
// SPDX-License-Identifier: GPL-3.0-or-later

package org.dreeam.expansion.folia.placeholder

import org.dreeam.expansion.folia.metrics.MetricInterval

internal object PlaceholderParser {
    fun parse(identifier: String): ParsedPlaceholder? {
        if (identifier.isEmpty()) return null
        return when (identifier) {
            "scheduler_threads" -> ParsedPlaceholder.SchedulerThreads
            "regions" -> ParsedPlaceholder.RegionCount
            "expansion_version" -> ParsedPlaceholder.ExpansionVersion
            "health" -> ParsedPlaceholder.Health(MetricScope.REGION, colored = false)
            "health_colored" -> ParsedPlaceholder.Health(MetricScope.REGION, colored = true)
            "global_health" -> ParsedPlaceholder.Health(MetricScope.GLOBAL, colored = false)
            "global_health_colored" -> ParsedPlaceholder.Health(MetricScope.GLOBAL, colored = true)
            else -> parseMetric(identifier)
        }
    }

    private fun parseMetric(identifier: String): ParsedPlaceholder.Metric? {
        val global = identifier.startsWith(GLOBAL_PREFIX)
        val body = if (global) identifier.substring(GLOBAL_PREFIX.length) else identifier
        val separator = body.indexOf('_')
        val metricToken = if (separator < 0) body else body.substring(0, separator)
        val kind = when (metricToken) {
            "tps" -> MetricKind.TPS
            "mspt" -> MetricKind.MSPT
            "util" -> MetricKind.UTILIZATION
            else -> return null
        }

        if (separator < 0) {
            return ParsedPlaceholder.Metric(
                scope = if (global) MetricScope.GLOBAL else MetricScope.REGION,
                kind = kind,
                interval = null,
                colored = false,
                percentage = false,
            )
        }

        var interval: MetricInterval? = null
        var colored = false
        var percentage = false
        var start = separator + 1
        while (start < body.length) {
            val end = body.indexOf('_', start).let { if (it < 0) body.length else it }
            val token = body.substring(start, end)
            val parsedInterval = MetricInterval.fromToken(token)
            when {
                interval == null && parsedInterval != null -> interval = parsedInterval
                token == "colored" && !colored -> colored = true
                token == "percent" && !percentage && kind != MetricKind.UTILIZATION -> percentage = true
                else -> return null
            }
            start = end + 1
        }

        if (interval == null) {
            if (kind == MetricKind.UTILIZATION && colored && !percentage) {
                return ParsedPlaceholder.Metric(
                    scope = if (global) MetricScope.GLOBAL else MetricScope.REGION,
                    kind = kind,
                    interval = null,
                    colored = true,
                    percentage = false,
                )
            }
            return null
        }

        return ParsedPlaceholder.Metric(
            scope = if (global) MetricScope.GLOBAL else MetricScope.REGION,
            kind = kind,
            interval = interval,
            colored = colored,
            percentage = percentage,
        )
    }

    private const val GLOBAL_PREFIX = "global_"
}
