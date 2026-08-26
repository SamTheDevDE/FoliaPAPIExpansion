// SPDX-License-Identifier: GPL-3.0-or-later

package org.dreeam.expansion.folia.placeholder

import org.dreeam.expansion.folia.config.ExpansionConfig
import org.dreeam.expansion.folia.metrics.MetricSample
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.util.Locale
import kotlin.test.assertEquals

class MetricFormatterTest {
    private val originalLocale = Locale.getDefault()

    @AfterEach
    fun restoreLocale() {
        Locale.setDefault(originalLocale)
    }

    @Test
    fun `formats decimals with Locale ROOT`() {
        Locale.setDefault(Locale.GERMANY)
        val formatter = MetricFormatter(ExpansionConfig(decimalPlaces = 2))
        assertEquals("19.99", formatter.metric(MetricKind.TPS, sample(tps = 19.987), false, false))
    }

    @Test
    fun `honors configured decimal precision`() {
        val formatter = MetricFormatter(ExpansionConfig(decimalPlaces = 3))
        assertEquals("12.346", formatter.metric(MetricKind.MSPT, sample(mspt = 12.3456), false, false))
    }

    @Test
    fun `returns unavailable for missing invalid and negative values`() {
        val formatter = MetricFormatter(ExpansionConfig(unavailable = "--"))
        assertEquals("--", formatter.metric(MetricKind.TPS, null, false, false))
        assertEquals("--", formatter.metric(MetricKind.TPS, sample(tps = Double.NaN), false, false))
        assertEquals("--", formatter.metric(MetricKind.MSPT, sample(mspt = Double.POSITIVE_INFINITY), false, false))
        assertEquals("--", formatter.metric(MetricKind.UTILIZATION, sample(utilization = -0.1), false, false))
    }

    @Test
    fun `handles TPS boundaries and clamps TPS percentage`() {
        val formatter = MetricFormatter(ExpansionConfig())
        assertEquals("0.00", formatter.metric(MetricKind.TPS, sample(tps = 0.0), false, false))
        assertEquals("*20.50", formatter.metric(MetricKind.TPS, sample(tps = 20.5), false, false))
        assertEquals("*100.00%", formatter.metric(MetricKind.TPS, sample(tps = 20.5), false, true))
    }

    @Test
    fun `formats MSPT budget percentage without hiding overload`() {
        val formatter = MetricFormatter(ExpansionConfig())
        assertEquals("0.00%", formatter.metric(MetricKind.MSPT, sample(mspt = 0.0), false, true))
        assertEquals("100.00%", formatter.metric(MetricKind.MSPT, sample(mspt = 50.0), false, true))
        assertEquals("160.00%", formatter.metric(MetricKind.MSPT, sample(mspt = 80.0), false, true))
    }

    @Test
    fun `formats utilization as percent and applies configured colors`() {
        val formatter = MetricFormatter(ExpansionConfig())
        assertEquals("50.00", formatter.metric(MetricKind.UTILIZATION, sample(utilization = 0.5), false, false))
        assertEquals("\u00a7e90.00", formatter.metric(MetricKind.UTILIZATION, sample(utilization = 0.9), true, false))
        assertEquals("\u00a7c120.00", formatter.metric(MetricKind.UTILIZATION, sample(utilization = 1.2), true, false))
    }

    private fun sample(
        tps: Double? = 20.0,
        mspt: Double? = 10.0,
        utilization: Double? = 0.2,
    ) = MetricSample(tps, mspt, utilization)
}
