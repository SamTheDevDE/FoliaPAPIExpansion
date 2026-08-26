/*
 * Folia-Expansion
 * Copyright (C) 2026 SamTheDevDE
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.dreeam.expansion.folia

import me.clip.placeholderapi.expansion.Cacheable
import me.clip.placeholderapi.expansion.Configurable
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.dreeam.expansion.folia.config.ExpansionConfig
import org.dreeam.expansion.folia.metrics.Folia26MetricProvider
import org.dreeam.expansion.folia.metrics.MetricProvider
import org.dreeam.expansion.folia.placeholder.ParsedPlaceholder
import org.dreeam.expansion.folia.placeholder.PlaceholderParser
import org.dreeam.expansion.folia.placeholder.PlaceholderRenderer
import java.util.LinkedHashMap

class FoliaExpansion : PlaceholderExpansion(), Cacheable, Configurable {
    @Volatile
    private var runtimeComponents: RuntimeComponents? = null

    override fun getIdentifier(): String = IDENTIFIER

    override fun getAuthor(): String = "SamTheDevDE"

    override fun getVersion(): String = javaClass.`package`.implementationVersion ?: "unknown"

    override fun canRegister(): Boolean = PlatformDetector.isFolia

    override fun persist(): Boolean = true

    override fun clear() {
        runtimeComponents?.provider?.clear()
        runtimeComponents = null
    }

    override fun getDefaults(): Map<String, Any> = LinkedHashMap<String, Any>().apply {
        put("format.decimal-places", ExpansionConfig.DEFAULT_DECIMAL_PLACES)
        put("format.unavailable", ExpansionConfig.DEFAULT_UNAVAILABLE)
        put("tps.healthy", 19.5)
        put("tps.warning", 18.0)
        put("mspt.healthy", 40.0)
        put("mspt.warning", 50.0)
        put("utilization.healthy", 80.0)
        put("utilization.warning", 100.0)
        put("colors.healthy", "&a")
        put("colors.warning", "&e")
        put("colors.critical", "&c")
        put("health.healthy", "HEALTHY")
        put("health.warning", "DEGRADED")
        put("health.critical", "OVERLOADED")
    }

    override fun onRequest(player: OfflinePlayer?, identifier: String): String? {
        val placeholder = PlaceholderParser.parse(identifier) ?: return null
        if (placeholder == ParsedPlaceholder.ExpansionVersion) return version
        if (!PlatformDetector.isFolia) return null

        val onlinePlayer = player as? Player
        val regionContextAvailable = onlinePlayer != null && Bukkit.isOwnedByCurrentRegion(onlinePlayer)
        return runtime().renderer.render(placeholder, regionContextAvailable)
    }

    private fun runtime(): RuntimeComponents {
        val existing = runtimeComponents
        if (existing != null) return existing
        return synchronized(this) {
            runtimeComponents ?: run {
                val provider = Folia26MetricProvider()
                RuntimeComponents(
                    provider = provider,
                    renderer = PlaceholderRenderer(provider, ExpansionConfig.from(this), version),
                ).also { runtimeComponents = it }
            }
        }
    }

    private data class RuntimeComponents(
        val provider: MetricProvider,
        val renderer: PlaceholderRenderer,
    )

    private companion object {
        const val IDENTIFIER = "folia"
    }
}
