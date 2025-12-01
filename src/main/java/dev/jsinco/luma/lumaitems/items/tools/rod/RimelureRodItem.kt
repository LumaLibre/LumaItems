package dev.jsinco.luma.lumaitems.items.tools.rod

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.inventory.ItemStack

class RimelureRodItem : CustomItemFunctions() {

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#bba5ff:#d3b0ff:#f6c8ff:#aae8f1:#99abff>Rimelure Rod</gradient></b>")
            .customEnchants("<#99abff>Swiftline", "<#9FBFFA>Coupled")
            .material(Material.FISHING_ROD)
            .persistentData("rimelure-rod")
            .tier(Tier.CHRISTMAS_2025)
            .vanillaEnchants(
                Enchantment.LURE to 5,
                Enchantment.LUCK_OF_THE_SEA to 4,
                Enchantment.UNBREAKING to 9,
                Enchantment.MENDING to 1
            )
            .lore(
                "When <#99abff>fishing</#99abff> with this",
                "rod, fish that appear",
                "will swim significantly",
                "faster towards hook.",
                "",
                "Caught fish may also",
                "occasionally come in",
                "<#9FBFFA>pairs/#9FBFFA>."
            )
            .buildPair()
    }

    override fun onFish(player: Player, event: PlayerFishEvent) {
        val hook = event.hook

        when (event.state) {
            PlayerFishEvent.State.FISHING -> {
                hook.minLureTime = (hook.minLureTime * 0.25).toInt()
                hook.maxLureTime = (hook.maxLureTime * 0.25).toInt()
            }
            PlayerFishEvent.State.CAUGHT_FISH -> {
                if (random().nextInt(101) > 8) return
                val item = event.caught as Item

                if (item.itemStack.maxStackSize > 1) {
                    item.itemStack.amount = 2
                    item.world.spawnParticle(Particle.WITCH, item.location.add(0.0, 0.6, 0.0), 10, 0.3, 0.1, 0.3, 0.1)
                }
            }
            else -> return
        }
    }
}