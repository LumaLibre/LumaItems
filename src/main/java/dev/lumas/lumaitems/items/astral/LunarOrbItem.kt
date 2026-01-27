package dev.lumas.lumaitems.items.astral

import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.manager.CustomItem
import dev.lumas.lumaitems.manager.FileManager
import dev.lumas.lumaitems.enums.Rarity
import dev.lumas.lumaitems.relics.RelicCrafting
import dev.lumas.lumaitems.relics.RelicCreator
import dev.lumas.lumaitems.util.Util
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class LunarOrbItem : CustomItem {

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
                    Rarity.LUNAR.algorithmWeight,
                    7,
                    Rarity.LUNAR,
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