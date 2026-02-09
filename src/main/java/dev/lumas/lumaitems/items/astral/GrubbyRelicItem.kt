package dev.lumas.lumaitems.items.astral

import dev.lumas.lumaitems.enums.Rarity
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.relics.RelicCreator
import dev.lumas.lumaitems.util.extensions.isItemInSlot
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

class GrubbyRelicItem : CustomItemFunctions() {

    private companion object {
        val KEY = "grubby-relic".namespacedKey()
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><dark_gray>Grubby Relic</dark_gray></b>")
            .material(Material.CHARCOAL)
            .tier(Tier.BLANK)
            .hideEnchants(true)
            .addSpace(false)
            .vanillaEnchants(Enchantment.UNBREAKING to 1)
            .lore("<gray>Right-click to open!")
            .persistentData(KEY)
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (!player.isItemInSlot(KEY, EquipmentSlot.HAND)) return
        player.inventory.itemInMainHand.amount -= 1

        val rarity = GrubbyWeights.randomByTotalWeight().delegate
        val material = rarity.materials.random()

        val relic = RelicCreator(rarity.algorithmWeight, -1, rarity, material).getRelicItem()

        val spawned = player.world.dropItem(player.eyeLocation, relic)
        spawned.pickupDelay = 0
    }

    private enum class GrubbyWeights(val weight: Int, val delegate: Rarity) {
        LUNAR(1, Rarity.LUNAR),
        NOVA(10, Rarity.NOVA),
        PULSAR(200, Rarity.PULSAR),
        SOLAR(200, Rarity.SOLAR),
        DELTA(200, Rarity.DELTA);

        companion object {
            val TOTAL_WEIGHT = entries.toTypedArray().sumOf { it.weight }

            fun randomByTotalWeight(): GrubbyWeights {
                val randomValue = (1..TOTAL_WEIGHT).random()
                var cumulativeWeight = 0

                for (percentChance in entries) {
                    cumulativeWeight += percentChance.weight
                    if (randomValue <= cumulativeWeight) {
                        return percentChance
                    }
                }
                return SOLAR // fallback
            }
        }
    }

}