package dev.jsinco.luma.lumaitems.placeholders

import dev.jsinco.luma.lumaitems.LumaItems
import org.bukkit.OfflinePlayer

interface Placeholder {
    fun onReceivedRequest(plugin: LumaItems, player: OfflinePlayer?, args: List<String>): String?
}