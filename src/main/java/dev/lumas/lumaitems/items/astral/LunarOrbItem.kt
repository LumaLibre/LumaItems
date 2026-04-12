package dev.lumas.lumaitems.items.astral

import dev.lumas.lumaitems.configuration.files.RelicsYml
import dev.lumas.lumaitems.enums.Rarity
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.relics.RelicCrafting
import dev.lumas.lumaitems.relics.RelicCreator
import dev.lumas.lumaitems.util.Util
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class LunarOrbItem : CustomItemFunctions() {

    companion object {
        val LUNAR_ORB_KEY = Util.namespacedKey("lunarorb")
    }

    override fun createItem(): Pair<String, ItemStack> {
        return Pair("lunarorb", RelicCrafting.lunarOrb)
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (!player.inventory.itemInMainHand.itemMeta.persistentDataContainer.has(LUNAR_ORB_KEY, PersistentDataType.SHORT)) return

        event.isCancelled = true

        val materials = Registry.CONFIGS.getOrThrow(RelicsYml::class).relicMaterials.lunar

        val relicCreator = RelicCreator(
            Rarity.LUNAR.algorithmWeight,
            7,
            Rarity.LUNAR,
            materials.random()
        )
        player.inventory.itemInMainHand.amount -= 1
        Util.giveItem(player, relicCreator.getRelicItem())
        player.playSound(player.location, Sound.ENTITY_EVOKER_CAST_SPELL, 1f, 1f)
    }

}