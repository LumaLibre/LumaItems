package dev.jsinco.luma.lumaitems.placeholders

import dev.jsinco.luma.lumaitems.LumaItems
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

class PAPIManager(val plugin: LumaItems) : PlaceholderExpansion() {

    companion object {
        private val placeHolders: MutableMap<String, Placeholder> = mutableMapOf()
    }

    init {
        placeHolders["color"] = TeamColorPlaceholder()
    }


    override fun getIdentifier(): String {
        return "lumaitems"
    }

    override fun getAuthor(): String {
        return plugin.description.authors[0]
    }

    override fun getVersion(): String {
        return plugin.description.version
    }

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        val args: List<String> = params.split("_")

        return placeHolders[args[0]]?.onReceivedRequest(plugin, player, args)
    }
}