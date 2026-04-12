package dev.lumas.lumaitems.items.armor.chestplate

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.extensions.QuickTasks
import dev.lumas.lumaitems.util.extensions.flag
import dev.lumas.lumaitems.util.extensions.hasPersistentKey
import dev.lumas.lumaitems.util.extensions.isFlagged
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack

class ParadoxCuirassItem : CustomItemFunctions() {

    private companion object {
        private val KEY = "paradox-cuirass".namespacedKey()
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.Companion.builder()
            .name("<b><gradient:#503B78:#695a9d:#8672b7:#703763>Paradox Cuirass</gradient></b>")
            .customEnchants("<#695a9d>Glass Vase")
            .material(Material.NETHERITE_CHESTPLATE)
            .persistentData(KEY)
            .tier(Tier.VALENTIDE_2026)
            .lore(
                "An article of armor",
                "that may swap to any",
                "other type of <#695a9d>netherite</#695a9d>",
                "armor. So long as the",
                "wearer avoids taking",
                "any damage.",
                "",
                "While holding, press the",
                "<#695a9d>swap key (F)</#695a9d> to change",
                "armor pieces."
            )
            .vanillaEnchants(
                Enchantment.PROTECTION to 10,
                Enchantment.AQUA_AFFINITY to 1,
                Enchantment.SOUL_SPEED to 3,
                Enchantment.SWIFT_SNEAK to 3,
                Enchantment.UNBREAKING to 10,
                Enchantment.RESPIRATION to 3,
                Enchantment.THORNS to 4,
                Enchantment.MENDING to 1
            )
            .buildPair()
    }


    override fun onPlayerDamaged(player: Player, event: EntityDamageEvent) {
        if (!player.isFlagged(this) && !event.isCancelled) {
            QuickTasks.flag(this, player.uniqueId)
            player.world.playSound(player.location, Sound.PARTICLE_SOUL_ESCAPE,2.0f, 1.0f)
            player.world.spawnParticle(Particle.SOUL, player.eyeLocation, 10, 0.5, 0.5, 0.5, 0.01)
            player.world.spawnParticle(Particle.COPPER_FIRE_FLAME, player.eyeLocation, 10, 0.5, 0.5, 0.5, 0.01)
        }
    }

    override fun onPlayerDeath(player: Player, event: PlayerDeathEvent) {
        if (event.isCancelled || player.isFlagged(this)) return
        player.flag(this)
    }


    override fun onPlayerSwapHands(player: Player, event: PlayerSwapHandItemsEvent) {
        val item = event.offHandItem.takeIf { it.hasPersistentKey(KEY) } ?: return
        if (player.isFlagged(this)) return

        event.isCancelled = true

        if (QuickTasks.isOnCooldown(this, player)) return
        item.swapArmor()
        QuickTasks.addCooldown(this, player, 10)
        player.inventory.setItemInMainHand(item)
    }

    @Suppress("DEPRECATION")
    private fun ItemStack.swapArmor() {
        type = when (type) {
            Material.NETHERITE_CHESTPLATE -> Material.NETHERITE_LEGGINGS
            Material.NETHERITE_LEGGINGS -> Material.NETHERITE_BOOTS
            Material.NETHERITE_BOOTS -> Material.NETHERITE_HELMET
            Material.NETHERITE_HELMET -> Material.NETHERITE_CHESTPLATE
            Material.LEATHER_CHESTPLATE -> Material.LEATHER_LEGGINGS
            Material.LEATHER_LEGGINGS -> Material.LEATHER_BOOTS
            Material.LEATHER_BOOTS -> Material.LEATHER_HELMET
            Material.LEATHER_HELMET -> Material.LEATHER_CHESTPLATE
            else -> Material.NETHERITE_CHESTPLATE
        }
    }
}