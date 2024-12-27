package dev.jsinco.luma.placeholders

import dev.jsinco.luma.LumaItems
import org.bukkit.OfflinePlayer

interface Placeholder {
    fun onReceivedRequest(plugin: LumaItems, player: OfflinePlayer?, args: List<String>): String?
}