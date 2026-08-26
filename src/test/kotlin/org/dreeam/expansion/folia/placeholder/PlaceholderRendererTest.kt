// SPDX-License-Identifier: GPL-3.0-or-later

package org.dreeam.expansion.folia.placeholder

import org.dreeam.expansion.folia.config.ExpansionConfig
import org.dreeam.expansion.folia.metrics.MetricInterval
import org.dreeam.expansion.folia.metrics.MetricProvider
import org.dreeam.expansion.folia.metrics.MetricSample
import org.dreeam.expansion.folia.metrics.MetricSnapshot
import org.dreeam.expansion.folia.metrics.ServerUtilizationSnapshot
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PlaceholderRendererTest {
    private val sample = MetricSample(tps = 19.75, mspt = 42.0, utilization = 0.75)
    private val metricSnapshot = MetricSnapshot(
        samples = MetricInterval.entries.associateWith { sample },
        capturedAtNanos = 1L,
    )
    private val provider = FakeProvider(metricSnapshot)
    private val renderer = PlaceholderRenderer(provider, ExpansionConfig(), "2.0.0")

    @Test
    fun `global placeholders work without a player region context`() {
        val placeholder = requireNotNull(PlaceholderParser.parse("global_tps_5s"))
        assertEquals("19.75", renderer.render(placeholder, regionContextAvailable = false))
    }

    @Test
    fun `regional placeholders require a player-owned region context`() {
        val placeholder = requireNotNull(PlaceholderParser.parse("tps_5s"))
        assertEquals("N/A", renderer.render(placeholder, regionContextAvailable = false))
        assertEquals("19.75", renderer.render(placeholder, regionContextAvailable = true))
    }

    @Test
    fun `renders health utilization scheduler and version placeholders`() {
        assertEquals(
            "DEGRADED",
            renderer.render(requireNotNull(PlaceholderParser.parse("global_health")), false),
        )
        assertEquals(
            "80.00",
            renderer.render(requireNotNull(PlaceholderParser.parse("global_util_5s")), false),
        )
        assertEquals("4", renderer.render(ParsedPlaceholder.SchedulerThreads, false))
        assertEquals("7", renderer.render(ParsedPlaceholder.RegionCount, false))
        assertEquals("2.0.0", renderer.render(ParsedPlaceholder.ExpansionVersion, false))
    }

    private class FakeProvider(private val snapshot: MetricSnapshot) : MetricProvider {
        override fun globalSnapshot(): MetricSnapshot = snapshot

        override fun currentRegionSnapshot(): MetricSnapshot = snapshot

        override fun serverUtilizationSnapshot() = ServerUtilizationSnapshot(
            utilization = MetricInterval.entries.associateWith { 0.8 },
            regionCount = 7,
            schedulerThreads = 4,
            capturedAtNanos = 1L,
        )

        override fun schedulerThreads(): Int = 4

        override fun regionCount(): Int = 7

        override fun clear() = Unit
    }
}
