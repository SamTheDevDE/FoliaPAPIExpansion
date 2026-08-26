// Folia-Expansion
// Copyright (C) 2026 SamTheDevDE
// SPDX-License-Identifier: GPL-3.0-or-later

package org.dreeam.expansion.folia.metrics

internal interface MetricProvider {
    fun globalSnapshot(): MetricSnapshot?

    fun currentRegionSnapshot(): MetricSnapshot?

    fun serverUtilizationSnapshot(): ServerUtilizationSnapshot?

    fun schedulerThreads(): Int?

    fun regionCount(): Int?

    fun clear()
}
