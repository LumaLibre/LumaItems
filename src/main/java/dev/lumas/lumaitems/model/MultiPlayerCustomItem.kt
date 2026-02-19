package dev.lumas.lumaitems.model

import java.util.UUID
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

abstract class MultiPlayerCustomItem(
    val secretKey: NamespacedKey
) : CustomItemFunctions() {
    
    protected companion object {
        val cachedBonds: MutableMap<UUID, String> = mutableMapOf()
    }


    override fun asyncGlobalTask() {
        if (cachedBonds.isNotEmpty()) {
            cachedBonds.entries.retainAll { entry ->
                val player = Bukkit.getPlayer(entry.key) ?: return@retainAll false
                findSecret(player) != null
            }
        }

        for (player in Bukkit.getOnlinePlayers()) {
            if (cachedBonds.containsKey(player.uniqueId)) continue // already cached
            val secret = findSecret(player) ?: continue
            cachedBonds[player.uniqueId] = secret
        }
    }

    protected fun getBondedPlayer(seeker: Player): Player? {
        val secret = cachedBonds[seeker.uniqueId] ?: return null
        for (bond in cachedBonds) {
            if (bond.value == secret && bond.key != seeker.uniqueId) {
                return Bukkit.getPlayer(bond.key)
            }
        }
        return null
    }

    protected fun isBondedPlayerOnline(seeker: Player): Boolean {
        return cachedBonds.hasSpecificValueMoreThanTwice(cachedBonds[seeker.uniqueId])
    }

    private fun findSecret(player: Player): String? {
        for (item in player.inventory.contents.filterNotNull()) {
            val secret = getSecret(item)
            if (secret != null) {
                return secret
            }
        }
        return null
    }

    private fun getSecret(item: ItemStack): String? {
        return item.itemMeta?.persistentDataContainer?.get(secretKey, PersistentDataType.STRING)
    }

    // Util

    private fun <K, V> Map<K, V>.hasSpecificValueMoreThanTwice(value: V): Boolean {
        return this.values.count { it == value } > 1
    }


    protected class SecretGenerator(val maxLength: Int = 7) {

        val chars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        lateinit var secret: String
        init {
            reGenerate()
        }

        fun reGenerate() {
            secret = (1..maxLength).map { chars.random() }.joinToString("")
        }

    }
}