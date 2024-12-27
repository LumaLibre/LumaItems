package dev.jsinco.luma.lumaitems.placeholders

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.manager.GlowManager
import dev.jsinco.luma.lumaitems.util.Util
import org.bukkit.OfflinePlayer

class TeamColorPlaceholder : Placeholder {

    override fun onReceivedRequest(plugin: LumaItems, player: OfflinePlayer?, args: List<String>): String {
        return Util.getColorCodeByChatColor(player?.player?.let { GlowManager.getGlowColorLegacy(it) } ?: return "§f")
    }
}