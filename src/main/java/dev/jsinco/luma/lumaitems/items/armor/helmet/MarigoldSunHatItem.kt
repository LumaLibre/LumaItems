package dev.jsinco.luma.lumaitems.items.armor.helmet

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.BukkitVectors
import dev.jsinco.luma.lumaitems.util.Executors
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.ItemStack

class MarigoldSunHatItem : CustomItemFunctions() {

    companion object {
        private const val MAX_FLY_TIME = 200
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#F69A22:#E3A16B>Marig</gradient><gradient:#E3A16B:#A73F2B>old Sun hat</gradient></b>")
            .customEnchants("<#F69A22>Drawing")
            .material(Material.NETHERITE_HELMET)
            .persistentData("marigold-sun-hat")
            .tier(Tier.HALLOWEEN_2025)
            .lore(
                "<#F69A22>Sneak</#F69A22> to pull in",
                "nearby items that",
                "match the item in",
                "your off-hand."
            )
            .vanillaEnchants(
                Enchantment.PROTECTION to 7,
                Enchantment.BLAST_PROTECTION to 6,
                Enchantment.UNBREAKING to 8,
                Enchantment.RESPIRATION to 4,
                Enchantment.AQUA_AFFINITY to 1,
                Enchantment.MENDING to 1
            )
            .buildPair()
    }

    override fun onPlayerCrouch(player: Player, event: PlayerToggleSneakEvent) {
        val offHType = player.inventory.itemInOffHand.type
        val material = if (offHType.isAir) player.inventory.itemInMainHand.type else offHType
        if (material.isAir) return


        val nearbyItems = player.location.getNearbyEntitiesByType(Item::class.java, 25.0)
            .filter { it.itemStack.type == material }
        if (nearbyItems.isNotEmpty()) {
            flyToPlayerExecutor(nearbyItems, player)
        }
    }

    private fun flyToPlayerExecutor(items: List<Item>, player: Player) {
        var count = 0

        Executors.syncTimer(0, 1) { task ->
            if (++count > MAX_FLY_TIME || items.all { isWithinDistance(it, player, 2.5) || !it.isValid }) {
                task.cancel()
                return@syncTimer
            }
            items.forEach { item ->
                item.velocity = BukkitVectors.flyToLivingEntity(player, item, 3.0)
            }
        }
    }

    private fun isWithinDistance(item: Item, player: Player, distance: Double): Boolean {
        return item.location.distanceSquared(player.location) <= distance * distance
    }
}