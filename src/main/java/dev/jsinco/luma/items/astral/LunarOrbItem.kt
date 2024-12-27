package dev.jsinco.luma.items.astral

import dev.jsinco.luma.LumaItems
import dev.jsinco.luma.enums.Action
import dev.jsinco.luma.manager.CustomItem
import dev.jsinco.luma.manager.FileManager
import dev.jsinco.luma.enums.Rarity
import dev.jsinco.luma.relics.RelicCrafting
import dev.jsinco.luma.relics.RelicCreator
import dev.jsinco.luma.util.Util
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class LunarOrbItem : CustomItem {

    companion object {
        val rarity: Rarity = Rarity.LUNAR
    }

    override fun createItem(): Pair<String, ItemStack> {
        return Pair("lunarorb", RelicCrafting.lunarOrb)
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.RIGHT_CLICK -> {
                if (!player.inventory.itemInMainHand.itemMeta.persistentDataContainer.has(NamespacedKey(LumaItems.getInstance(), "lunarorb"), PersistentDataType.SHORT)) return false

                event as PlayerInteractEvent
                event.isCancelled = true

                val materials = FileManager("relics.yml").getFileYaml().getStringList("relic-materials.lunar")

                val relicCreator = RelicCreator(
                    rarity.algorithmWeight,
                    7,
                    rarity,
                    Material.valueOf(materials.random())
                )
                player.inventory.itemInMainHand.amount -= 1
                Util.giveItem(player, relicCreator.getRelicItem())
                player.playSound(player.location, Sound.ENTITY_EVOKER_CAST_SPELL, 1f, 1f)
            }
            else -> {
                return false
            }
        }
        return true
    }
}