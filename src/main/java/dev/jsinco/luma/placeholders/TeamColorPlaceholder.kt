package dev.jsinco.luma.placeholders

import dev.jsinco.luma.LumaItems
import dev.jsinco.luma.manager.GlowManager
import dev.jsinco.luma.util.Util
import org.bukkit.OfflinePlayer

class TeamColorPlaceholder : Placeholder {

    override fun onReceivedRequest(plugin: LumaItems, player: OfflinePlayer?, args: List<String>): String {
        return Util.getColorCodeByChatColor(player?.player?.let { GlowManager.getGlowColorLegacy(it) } ?: return "§f")
    }
}