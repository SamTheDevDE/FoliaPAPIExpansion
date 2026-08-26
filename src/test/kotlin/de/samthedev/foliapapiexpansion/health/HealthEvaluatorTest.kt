// SPDX-License-Identifier: GPL-3.0-or-later

package de.samthedev.foliapapiexpansion.health

import de.samthedev.foliapapiexpansion.config.ExpansionConfig
import de.samthedev.foliapapiexpansion.config.Thresholds
import de.samthedev.foliapapiexpansion.metrics.MetricSample
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HealthEvaluatorTest {
    private val evaluator = HealthEvaluator(ExpansionConfig())

    @Test
    fun `uses inclusive threshold boundaries`() {
        assertEquals(HealthStatus.HEALTHY, evaluator.tpsStatus(19.5))
        assertEquals(HealthStatus.DEGRADED, evaluator.tpsStatus(18.0))
        assertEquals(HealthStatus.OVERLOADED, evaluator.tpsStatus(17.999))
        assertEquals(HealthStatus.HEALTHY, evaluator.msptStatus(40.0))
        assertEquals(HealthStatus.DEGRADED, evaluator.msptStatus(50.0))
        assertEquals(HealthStatus.OVERLOADED, evaluator.msptStatus(50.001))
    }

    @Test
    fun `combined health uses the worst valid metric`() {
        assertEquals(HealthStatus.HEALTHY, evaluator.evaluate(MetricSample(20.0, 20.0, 0.1)))
        assertEquals(HealthStatus.DEGRADED, evaluator.evaluate(MetricSample(19.0, 20.0, 0.1)))
        assertEquals(HealthStatus.OVERLOADED, evaluator.evaluate(MetricSample(20.0, 60.0, 0.1)))
    }

    @Test
    fun `invalid samples have no health`() {
        assertNull(evaluator.evaluate(null))
        assertNull(evaluator.evaluate(MetricSample(Double.NaN, 20.0, 0.1)))
        assertNull(evaluator.evaluate(MetricSample(20.0, Double.NEGATIVE_INFINITY, 0.1)))
    }

    @Test
    fun `invalid configuration thresholds fall back safely`() {
        val validated = ExpansionConfig(
            decimalPlaces = 99,
            unavailable = "",
            tps = Thresholds(10.0, 15.0),
            mspt = Thresholds(60.0, 50.0),
            utilization = Thresholds(Double.NaN, 100.0),
        ).validated()

        assertEquals(6, validated.decimalPlaces)
        assertEquals("N/A", validated.unavailable)
        assertEquals(Thresholds(19.5, 18.0), validated.tps)
        assertEquals(Thresholds(40.0, 50.0), validated.mspt)
        assertEquals(Thresholds(80.0, 100.0), validated.utilization)
    }
}
