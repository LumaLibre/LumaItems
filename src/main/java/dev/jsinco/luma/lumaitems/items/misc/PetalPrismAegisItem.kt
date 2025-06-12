package dev.jsinco.luma.lumaitems.items.misc

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.manager.CustomItem
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import java.util.UUID

class PetalPrismAegisItem : CustomItem {

    companion object {
        private val blockedDamages: MutableMap<UUID, BlockedDamage> = mutableMapOf()
    }

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#E899E9&lP&#E399E8&le&#DE98E6&lt&#D998E5&la&#D398E4&ll &#CE98E3&lP&#C997E1&lr&#C497E0&li&#C28EE0&ls&#BF84DF&lm &#BD7BDF&lA&#BB72DE&le&#B969DE&lg&#B65FDD&li&#B456DD&ls",
            mutableListOf("&#C497E0Reflections"),
            mutableListOf("Any damage received while", "blocking will be stored and", "dealt back upon the user's", "next attack.", "", "&cMax storable: 15❤"),
            Material.SHIELD,
            mutableListOf("petalprismaegis"),
            mutableMapOf(Enchantment.UNBREAKING to 6, Enchantment.MENDING to 1, Enchantment.KNOCKBACK to 3)
        )
        item.addQuote("&#E899E9\"&#E699E8W&#E499E8o&#E299E7w&#E099E7, &#DD98E6i&#DB98E6t&#D998E5'&#D798E5s &#D598E4s&#D398E4o &#D198E3s&#CF98E3h&#CC97E2i&#CA97E2n&#C897E1y&#C697E1!&#C497E0\"")
        item.tier = "&#FF9A9A&lE&#FFBAA6&la&#FFD9B2&ls&#FFF9BE&lt&#E5FAD4&le&#CAFCE9&lr &#B0FDFF&l2&#C7E8FF&l0&#DED4FF&l2&#F5BFFF&l4"
        return Pair("petalprismaegis", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.PLAYER_DAMAGED_BY_ENTITY -> {
                if (!player.isBlocking) {
                    return false
                }

                event as EntityDamageByEntityEvent
                val blockedDamage = blockedDamages[player.uniqueId] ?: BlockedDamage()
                blockedDamage.timeStamp = System.currentTimeMillis()

                if (blockedDamage.damage < 25.0) {
                    blockedDamage.damage += event.damage
                }

                blockedDamages[player.uniqueId] = blockedDamage
            }

            /*Ability.ASYNC_RUNNABLE -> { // Swap back to concurrent map if uncommented
                for (blockedDamage in blockedDamages) {
                    if (blockedDamage.value.timeStamp + TIMEOUT_MS < System.currentTimeMillis()) {
                        blockedDamages.remove(blockedDamage.key)
                    }
                }
            }*/

            Action.ENTITY_DAMAGE -> {
                event as EntityDamageByEntityEvent
                if (!blockedDamages.containsKey(player.uniqueId)) {
                    return false
                }


                val blockedDamage = blockedDamages[player.uniqueId] ?: return false
                event.damage = blockedDamage.damage.coerceAtMost(25.0)
                blockedDamages.remove(player.uniqueId)
            }

            else -> return false
        }
        return true
    }
}

// Timestamp in MS, not ticks
private class BlockedDamage {
    var damage: Double = 0.0
    var timeStamp: Long = 0
}
