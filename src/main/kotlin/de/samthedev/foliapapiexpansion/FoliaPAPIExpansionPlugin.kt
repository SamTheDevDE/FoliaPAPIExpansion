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

import de.samthedev.foliapapiexpansion.config.ExpansionConfig
import de.samthedev.foliapapiexpansion.metrics.Folia26MetricProvider
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

class FoliaPAPIExpansionPlugin : JavaPlugin() {
    private var expansion: FoliaExpansion? = null

    override fun onEnable() {
        if (!PlatformDetector.isFolia) {
            disable("FoliaPAPIExpansion only supports Folia 26.2; this server is not Folia.")
            return
        }

        val placeholderApi = server.pluginManager.getPlugin(PLACEHOLDER_API)
        if (placeholderApi == null || !placeholderApi.isEnabled) {
            disable("PlaceholderAPI is required but is not installed or enabled.")
            return
        }

        saveDefaultConfig()
        val provider = Folia26MetricProvider()
        val candidate = FoliaExpansion(this, provider, ExpansionConfig.from(config))

        val registered = try {
            candidate.register()
        } catch (exception: RuntimeException) {
            logger.log(Level.SEVERE, "PlaceholderAPI rejected the Folia expansion registration.", exception)
            false
        }

        if (!registered) {
            candidate.clear()
            disable("Could not register the 'folia' PlaceholderAPI expansion. Check for a conflicting expansion.")
            return
        }

        expansion = candidate
        logger.info(
            "Enabled version ${description.version} on Folia; registered PlaceholderAPI identifier 'folia' " +
                "with PlaceholderAPI ${placeholderApi.description.version}.",
        )
    }

    override fun onDisable() {
        val current = expansion ?: return
        expansion = null

        try {
            if (current.isRegistered) {
                current.unregister()
            }
        } catch (exception: RuntimeException) {
            logger.log(Level.WARNING, "Could not unregister the 'folia' PlaceholderAPI expansion cleanly.", exception)
        } finally {
            current.clear()
        }
    }

    private fun disable(message: String) {
        logger.severe(message)
        server.pluginManager.disablePlugin(this)
    }

    private companion object {
        const val PLACEHOLDER_API = "PlaceholderAPI"
    }
}
