package dev.jsinco.lumaitems.items.astral

import dev.jsinco.lumaitems.LumaItems
import dev.jsinco.lumaitems.enums.Action
import dev.jsinco.lumaitems.manager.CustomItem
import dev.jsinco.lumaitems.manager.FileManager
import dev.jsinco.lumaitems.enums.Rarity
import dev.jsinco.lumaitems.relics.RelicCrafting
import dev.jsinco.lumaitems.relics.RelicCreator
import dev.jsinco.lumaitems.util.Util
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