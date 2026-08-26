// Folia-Expansion
// Copyright (C) 2026 SamTheDevDE
// SPDX-License-Identifier: GPL-3.0-or-later

package org.dreeam.expansion.folia

import io.papermc.paper.ServerBuildInfo
import net.kyori.adventure.key.Key

internal object PlatformDetector {
    val isFolia: Boolean by lazy(LazyThreadSafetyMode.PUBLICATION) {
        runCatching {
            ServerBuildInfo.buildInfo().isBrandCompatible(Key.key("papermc", "folia"))
        }.getOrDefault(false)
    }
}
