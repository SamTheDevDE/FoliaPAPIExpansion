/*
 * FoliaPAPIExpansion
 * Copyright (C) 2026 SamTheDevDE
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.samthedev.foliapapiexpansion

import me.clip.placeholderapi.expansion.Cacheable
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import de.samthedev.foliapapiexpansion.config.ExpansionConfig
import de.samthedev.foliapapiexpansion.metrics.MetricProvider
import de.samthedev.foliapapiexpansion.placeholder.ParsedPlaceholder
import de.samthedev.foliapapiexpansion.placeholder.PlaceholderParser
import de.samthedev.foliapapiexpansion.placeholder.PlaceholderRenderer

internal class FoliaExpansion(
    private val plugin: FoliaPAPIExpansionPlugin,
    private val provider: MetricProvider,
    config: ExpansionConfig,
) : PlaceholderExpansion(), Cacheable {
    private val renderer = PlaceholderRenderer(provider, config, plugin.description.version)

    override fun getIdentifier(): String = IDENTIFIER

    override fun getAuthor(): String = "SamTheDevDE"

    override fun getVersion(): String = plugin.description.version

    override fun getRequiredPlugin(): String = plugin.name

    override fun canRegister(): Boolean = plugin.isEnabled && PlatformDetector.isFolia

    override fun persist(): Boolean = true

    override fun clear() = provider.clear()

    override fun onRequest(player: OfflinePlayer?, identifier: String): String? {
        val placeholder = PlaceholderParser.parse(identifier) ?: return null
        if (placeholder == ParsedPlaceholder.ExpansionVersion) return version

        val onlinePlayer = player as? Player
        val regionContextAvailable = onlinePlayer != null && Bukkit.isOwnedByCurrentRegion(onlinePlayer)
        return renderer.render(placeholder, regionContextAvailable)
    }

    private companion object {
        const val IDENTIFIER = "folia"
    }
}
