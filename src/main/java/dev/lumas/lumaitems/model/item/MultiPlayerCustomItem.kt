package dev.lumas.lumaitems.model.item

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

abstract class MultiPlayerCustomItem(
    val secretKey: NamespacedKey
) : CustomItemFunctions() {

    companion object {
        private const val SECRET_LENGTH = 16
        private val SECRET_CHARS = ('A'..'Z') + ('a'..'z') + ('0'..'9')

    override fun asyncGlobalTask() {
        private val cachedBonds: MutableMap<NamespacedKey, MutableMap<UUID, Set<String>>> = ConcurrentHashMap()

        /**
         * A fresh secret for one bonded group. Every group needs its own: reusing a secret links every
         * holder of it together instead of just the two intended players.
         */
        fun generateSecret(): String = (1..SECRET_LENGTH).map { SECRET_CHARS.random() }.joinToString("")
    }

    private val bonds: MutableMap<UUID, Set<String>>
        get() = cachedBonds.computeIfAbsent(secretKey) { ConcurrentHashMap() }


    override fun asyncGlobalTask() {
        val bonds = this.bonds
        val online = Bukkit.getOnlinePlayers()
        bonds.keys.retainAll(online.mapTo(mutableSetOf()) { it.uniqueId })

        for (player in online) {
            val secrets = findSecrets(player)
            if (secrets.isEmpty()) {
                bonds.remove(player.uniqueId)
            } else {
                bonds[player.uniqueId] = secrets
            }
        }
    }

    protected fun getBondedPlayer(seeker: Player, secret: String): Player? {
        val partner = bondedPartners(seeker, secret).singleOrNull() ?: return null
        return Bukkit.getPlayer(partner)
    }

    protected fun isBondedPlayerOnline(seeker: Player, secret: String): Boolean {
        return bondedPartners(seeker, secret).size == 1
    }

    private fun bondedPartners(seeker: Player, secret: String): List<UUID> {
        val bonds = this.bonds
        return bonds.keys.filter { it != seeker.uniqueId && bonds[it]?.contains(secret) == true }
    }

    private fun findSecrets(player: Player): Set<String> {
        val secrets = mutableSetOf<String>()
        for (item in player.inventory.contents) {
            secrets.add(getSecret(item) ?: continue)
        }
        return secrets
    }

    protected fun getSecret(item: ItemStack?): String? {
        return item?.itemMeta?.persistentDataContainer?.get(secretKey, PersistentDataType.STRING)
    }

}
