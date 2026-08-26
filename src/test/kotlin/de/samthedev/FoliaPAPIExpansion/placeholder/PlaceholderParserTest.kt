// SPDX-License-Identifier: GPL-3.0-or-later

package de.samthedev.FoliaPAPIExpansion.placeholder

import de.samthedev.FoliaPAPIExpansion.metrics.MetricInterval
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlaceholderParserTest {
    @Test
    fun `parses every global and regional interval`() {
        for (interval in MetricInterval.entries) {
            val global = assertIs<ParsedPlaceholder.Metric>(
                PlaceholderParser.parse("global_tps_${interval.token}"),
            )
            assertEquals(MetricScope.GLOBAL, global.scope)
            assertEquals(MetricKind.TPS, global.kind)
            assertEquals(interval, global.interval)

            val region = assertIs<ParsedPlaceholder.Metric>(
                PlaceholderParser.parse("mspt_${interval.token}"),
            )
            assertEquals(MetricScope.REGION, region.scope)
            assertEquals(MetricKind.MSPT, region.kind)
            assertEquals(interval, region.interval)
        }
    }

    @Test
    fun `parses colored and percentage variants in either suffix order`() {
        val conventional = assertIs<ParsedPlaceholder.Metric>(
            PlaceholderParser.parse("global_tps_5s_percent_colored"),
        )
        assertTrue(conventional.percentage)
        assertTrue(conventional.colored)

        val compatible = assertIs<ParsedPlaceholder.Metric>(
            PlaceholderParser.parse("tps_5s_colored_percent"),
        )
        assertTrue(compatible.percentage)
        assertTrue(compatible.colored)
    }

    @Test
    fun `parses legacy aggregate and utilization placeholders`() {
        val globalTps = assertIs<ParsedPlaceholder.Metric>(PlaceholderParser.parse("global_tps"))
        assertNull(globalTps.interval)
        assertEquals(MetricScope.GLOBAL, globalTps.scope)

        val globalUtil = assertIs<ParsedPlaceholder.Metric>(PlaceholderParser.parse("global_util_colored"))
        assertNull(globalUtil.interval)
        assertEquals(MetricKind.UTILIZATION, globalUtil.kind)
        assertTrue(globalUtil.colored)

        val intervalUtil = assertIs<ParsedPlaceholder.Metric>(PlaceholderParser.parse("util_15m_colored"))
        assertEquals(MetricInterval.FIFTEEN_MINUTES, intervalUtil.interval)
    }

    @Test
    fun `parses health server and metadata placeholders`() {
        assertEquals(
            ParsedPlaceholder.Health(MetricScope.GLOBAL, colored = true),
            PlaceholderParser.parse("global_health_colored"),
        )
        assertEquals(
            ParsedPlaceholder.Health(MetricScope.REGION, colored = false),
            PlaceholderParser.parse("health"),
        )
        assertEquals(ParsedPlaceholder.SchedulerThreads, PlaceholderParser.parse("scheduler_threads"))
        assertEquals(ParsedPlaceholder.RegionCount, PlaceholderParser.parse("regions"))
        assertEquals(ParsedPlaceholder.ExpansionVersion, PlaceholderParser.parse("expansion_version"))
    }

    @Test
    fun `rejects malformed and unknown identifiers`() {
        assertNull(PlaceholderParser.parse(""))
        assertNull(PlaceholderParser.parse("global_ram"))
        assertNull(PlaceholderParser.parse("global_tps_30s"))
        assertNull(PlaceholderParser.parse("global_util_5s_percent"))
        assertNull(PlaceholderParser.parse("tps_colored"))
        assertNull(PlaceholderParser.parse("tps_5s_colored_colored"))
    }
}
