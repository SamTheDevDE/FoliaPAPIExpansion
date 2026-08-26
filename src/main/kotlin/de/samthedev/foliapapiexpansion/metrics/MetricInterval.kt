// FoliaPAPIExpansion
// Copyright (C) 2026 SamTheDevDE
// SPDX-License-Identifier: GPL-3.0-or-later

package de.samthedev.foliapapiexpansion.metrics

internal enum class MetricInterval(val token: String) {
    FIVE_SECONDS("5s"),
    FIFTEEN_SECONDS("15s"),
    ONE_MINUTE("1m"),
    FIVE_MINUTES("5m"),
    FIFTEEN_MINUTES("15m");

    companion object {
        private val byToken = entries.associateBy(MetricInterval::token)

        fun fromToken(token: String): MetricInterval? = byToken[token]
    }
}
