package dev.lumas.lumaitems.items.tools.spade

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockDropItemEvent

class UndertowSpadeItem : CustomItemFunctions() {


    override fun createItem() = ItemFactory.builder()
        .name("<b><gradient:#1e8abf:#9be4df:#ffe494>Undertow Spade</gradient></b>")
        .customEnchants("<#9be4df>Tidepull")
        .material(Material.NETHERITE_SHOVEL)
        .persistentData("undertow-spade")
        .tier(Tier.LUMARINE_2026)
        .vanillaEnchants(
            Enchantment.EFFICIENCY to 5,
            Enchantment.UNBREAKING to 5,
            Enchantment.MENDING to 1,
        )
        .lore(
            "Blocks <#9be4df>broken</#9be4df> with this",
            "spade are swept straight",
            "into your inventory."
        )
        .buildPair()

    override fun onBlockDropItem(player: Player, event: BlockDropItemEvent) {
        val items = event.items
        if (items.isEmpty()) return

        val block = event.block
        val drops = items.map { it.itemStack }
        items.clear()

        val result = player.give(drops, false)

        if (result.leftovers().isEmpty()) {
            block.world.spawnParticle(Particle.PORTAL, block.location.toCenterLocation(), 3, 0.25, 0.25, 0.25, 0.05)
            block.world.playSound(block.location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.1f, 1.6f)
        } else {
            for (leftover in result.leftovers()) {
                block.world.dropItem(block.location.toCenterLocation(), leftover)
            }
        }
    }
}
