package dev.jsinco.luma.items.astral

import dev.jsinco.luma.LumaItems
import dev.jsinco.luma.enums.Action
import dev.jsinco.luma.manager.CustomItem
import dev.jsinco.luma.manager.FileManager
import dev.jsinco.luma.relics.RelicCrafting
import dev.jsinco.luma.util.Util
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import kotlin.random.Random

class AstralOrbItem : CustomItem {

    override fun createItem(): Pair<String, ItemStack> {
        return Pair("astralorb", RelicCrafting.astralOrb)
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.RIGHT_CLICK -> {
                event as PlayerInteractEvent
                event.isCancelled = true

                if (!player.inventory.itemInMainHand.itemMeta.persistentDataContainer.has(NamespacedKey(LumaItems.getInstance(), "astralorb"), PersistentDataType.SHORT)) return false

                player.inventory.itemInMainHand.amount -= 1
                player.playSound(player.location, Sound.ENTITY_EVOKER_CAST_SPELL, 1f, 1f)
                for (globalPlayer in Bukkit.getOnlinePlayers()) {
                    globalPlayer.playSound(globalPlayer.location, Sound.ENTITY_EVOKER_CAST_SPELL, 0.9f, 1f)
                    globalPlayer.sendMessage(Util.colorcode("${Util.prefix} &#F7FFC9${player.name}&#E2E2E2 has revealed a relic inside of a &#AC87FB&lAstral &#F7FFC9Orb&#E2E2E2!"))
                }

                Util.giveItem(player, getAstralItem() ?: return false)
            }

            else -> return false
        }
        return true
    }
    private fun getAstralItem(): ItemStack? {
        val file = FileManager("astral.yml").generateYamlFile()

        val itemClasses = file.getConfigurationSection("astral-orb-rarities")?.getKeys(true)
        val setsAndWeight: MutableMap<List<ItemStack>, Int> = mutableMapOf()

        if (itemClasses == null) {
            return null
        }

        for (itemClass in itemClasses) {
            val items = RelicCrafting.getItemsFromClass(itemClass)
            val weight = file.getInt("astral-orb-rarities.$itemClass")
            setsAndWeight[items] = weight
        }

        var selectedSet = setsAndWeight.keys.toList().random()
        while (setsAndWeight[selectedSet]!! < Random.nextInt(1, 100)) {
            selectedSet = setsAndWeight.keys.toList().random()
        }
        return selectedSet.random()
    }
}