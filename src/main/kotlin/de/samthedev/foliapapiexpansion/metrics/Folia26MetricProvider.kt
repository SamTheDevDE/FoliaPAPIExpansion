// FoliaPAPIExpansion
// Copyright (C) 2026 SamTheDevDE
// SPDX-License-Identifier: GPL-3.0-or-later

package de.samthedev.foliapapiexpansion.metrics

import ca.spottedleaf.common.time.TickData
import io.papermc.paper.threadedregions.RegionizedServer
import io.papermc.paper.threadedregions.TickRegionScheduler
import io.papermc.paper.threadedregions.TickRegions
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.CraftWorld
import java.util.Collections
import java.util.EnumMap
import java.util.concurrent.atomic.AtomicReference

internal class Folia26MetricProvider(
    private val clock: () -> Long = System::nanoTime,
) : MetricProvider {
    private val globalCache = AtomicReference<Cached<MetricSnapshot>?>()
    private val serverUtilizationCache = AtomicReference<Cached<ServerUtilizationSnapshot>?>()
    private val regionCountCache = AtomicReference<Cached<Int>?>()
    private val regionalCache = ThreadLocal<RegionalCache?>()

    override fun globalSnapshot(): MetricSnapshot? {
        val now = clock()
        globalCache.get()?.takeIf { now - it.createdAtNanos < GLOBAL_CACHE_NANOS }?.let { return it.value }

        return snapshot(RegionizedServer.getGlobalTickData(), now)?.also {
            globalCache.set(Cached(now, it))
        }
    }

    override fun currentRegionSnapshot(): MetricSnapshot? {
        val region = TickRegionScheduler.getCurrentRegion() ?: return null
        val handle = region.data.regionSchedulingHandle
        val now = clock()
        regionalCache.get()?.takeIf {
            it.handle === handle && now - it.createdAtNanos < REGIONAL_CACHE_NANOS
        }?.let { return it.snapshot }

        return snapshot(handle, now)?.also {
            regionalCache.set(RegionalCache(handle, now, it))
        }
    }

    override fun serverUtilizationSnapshot(): ServerUtilizationSnapshot? {
        val now = clock()
        serverUtilizationCache.get()?.takeIf {
            now - it.createdAtNanos < GLOBAL_CACHE_NANOS
        }?.let { return it.value }

        val schedulerThreads = schedulerThreads() ?: return null
        if (schedulerThreads <= 0) return null

        val handles = regionHandles()
        regionCountCache.set(Cached(now, handles.size))

        val sums = DoubleArray(MetricInterval.entries.size)
        val validCounts = IntArray(MetricInterval.entries.size)
        handles.add(RegionizedServer.getGlobalTickData())
        for (handle in handles) {
            for (interval in MetricInterval.entries) {
                val utilization = report(handle, interval, now)?.utilisation()
                if (utilization != null && utilization.isFinite() && utilization >= 0.0) {
                    sums[interval.ordinal] += utilization
                    validCounts[interval.ordinal]++
                }
            }
        }

        val utilization = EnumMap<MetricInterval, Double?>(MetricInterval::class.java)
        for (interval in MetricInterval.entries) {
            utilization[interval] = if (validCounts[interval.ordinal] == 0) {
                null
            } else {
                sums[interval.ordinal] / schedulerThreads.toDouble()
            }
        }

        return ServerUtilizationSnapshot(
            utilization = Collections.unmodifiableMap(utilization),
            regionCount = handles.size - 1,
            schedulerThreads = schedulerThreads,
            capturedAtNanos = now,
        ).also {
            serverUtilizationCache.set(Cached(now, it))
        }
    }

    override fun schedulerThreads(): Int? = runCatching {
        TickRegions.getScheduler().totalThreadCount
    }.getOrNull()?.takeIf { it > 0 }

    override fun regionCount(): Int? {
        val now = clock()
        regionCountCache.get()?.takeIf { now - it.createdAtNanos < GLOBAL_CACHE_NANOS }?.let {
            return it.value
        }
        return runCatching { regionHandles().size }.getOrNull()?.also {
            regionCountCache.set(Cached(now, it))
        }
    }

    override fun clear() {
        globalCache.set(null)
        serverUtilizationCache.set(null)
        regionCountCache.set(null)
        regionalCache.remove()
    }

    private fun regionHandles(): ArrayList<TickRegionScheduler.RegionScheduleHandle> {
        val handles = ArrayList<TickRegionScheduler.RegionScheduleHandle>()
        for (world in Bukkit.getWorlds()) {
            val craftWorld = world as? CraftWorld ?: continue
            craftWorld.handle.regioniser.computeForAllRegions { region ->
                handles.add(region.data.regionSchedulingHandle)
            }
        }
        return handles
    }

    private fun snapshot(
        handle: TickRegionScheduler.RegionScheduleHandle,
        now: Long,
    ): MetricSnapshot? = runCatching {
        val samples = EnumMap<MetricInterval, MetricSample>(MetricInterval::class.java)
        for (interval in MetricInterval.entries) {
            samples[interval] = sample(report(handle, interval, now))
        }
        MetricSnapshot(Collections.unmodifiableMap(samples), now)
    }.getOrNull()

    private fun sample(report: TickData.TickReportData?): MetricSample {
        if (report == null) return MetricSample(null, null, null)
        return MetricSample(
            tps = report.tpsData().segmentAll().average(),
            mspt = report.timePerTickData().segmentAll().average() / NANOSECONDS_PER_MILLISECOND,
            utilization = report.utilisation(),
        )
    }

    private fun report(
        handle: TickRegionScheduler.RegionScheduleHandle,
        interval: MetricInterval,
        now: Long,
    ): TickData.TickReportData? = when (interval) {
        MetricInterval.FIVE_SECONDS -> handle.getTickReport5s(now)
        MetricInterval.FIFTEEN_SECONDS -> handle.getTickReport15s(now)
        MetricInterval.ONE_MINUTE -> handle.getTickReport1m(now)
        MetricInterval.FIVE_MINUTES -> handle.getTickReport5m(now)
        MetricInterval.FIFTEEN_MINUTES -> handle.getTickReport15m(now)
    }

    private data class Cached<T>(val createdAtNanos: Long, val value: T)

    private data class RegionalCache(
        val handle: TickRegionScheduler.RegionScheduleHandle,
        val createdAtNanos: Long,
        val snapshot: MetricSnapshot,
    )

    private companion object {
        const val NANOSECONDS_PER_MILLISECOND = 1_000_000.0
        const val GLOBAL_CACHE_NANOS = 500_000_000L
        const val REGIONAL_CACHE_NANOS = 250_000_000L
    }
}
