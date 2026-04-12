package dev.lumas.lumaitems.items.weapons.spear

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.extensions.QuickTasks
import dev.lumas.lumaitems.util.extensions.isItemInSlot
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.Tier
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack


class ParadoxSpearItem : CustomItemFunctions() {

    private companion object {
        private val KEY = "paradox-spear".namespacedKey()
        private const val BASE_DAMAGE_MULTIPLIER = 6.0
        private const val DAMAGE_REDUCTION_PER_HIT = 2.0
        private const val PLAYER_DAMAGE_MULTIPLIER = 0.5
        private const val MAX_STARS = 3
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#503B78:#695a9d:#8672b7:#703763>Paradox Spear</gradient></b>")
            .customEnchants("<#695a9d>Glass Cannon")
            .material(Material.NETHERITE_SPEAR)
            .persistentData(KEY)
            .tier(Tier.VALENTIDE_2026)
            .lore(
                "An extremely powerful",
                "weapon only limited by",
                "the wielder's ability",
                "to avoid taking damage.",
                "",
                "This spear will deal <#695a9d>6x</#695a9d>",
                "damage to all enemies",
                "but will lose <#695a9d>2x</#695a9d> damage",
                "for every hit the wielder",
                "receives.",
            )
            .vanillaEnchants(
                Enchantment.SHARPNESS to 10,
                Enchantment.SMITE to 9,
                Enchantment.LOOTING to 6,
                Enchantment.KNOCKBACK to 2,
                Enchantment.UNBREAKING to 3,
                Enchantment.LUNGE to 4,
                Enchantment.MENDING to 1
            )
            .buildPair()
    }

    override fun onEntityDamage(player: Player, event: EntityDamageByEntityEvent) {
        val entity = event.entity
        if (entity !is LivingEntity || entity is Player || !player.isItemInSlot(KEY, EquipmentSlot.HAND)) return

        val value = QuickTasks.getFlag(this, player.uniqueId, Double::class.java) ?: BASE_DAMAGE_MULTIPLIER
        val item = player.inventory.itemInMainHand
        event.damage *= value.coerceAtLeast(1.0)
        if (value > 0) {
            item.damage(30, player)
        }
    }


    override fun onPlayerDamaged(player: Player, event: EntityDamageEvent) {
        if (event.isCancelled) return
        event.damage *= PLAYER_DAMAGE_MULTIPLIER

        val value = QuickTasks.getFlag(this, player.uniqueId, Double::class.java) ?: BASE_DAMAGE_MULTIPLIER
        val newValue = (value - DAMAGE_REDUCTION_PER_HIT).coerceAtLeast(0.0)

        if (value > 0) {
            QuickTasks.flag(this, player.uniqueId, newValue)
        }

        if (QuickTasks.isOnCooldown(this, player)) return
        QuickTasks.addCooldown(this, player, 10L)
        player.showLife(newValue)
    }

    override fun onPlayerDeath(player: Player, event: PlayerDeathEvent) {
        if (event.isCancelled) return
        QuickTasks.flag(this, player.uniqueId, 0.0)
        player.showLife(0.0)
    }


    private fun Player.showLife(value: Double) {
        val filledStars = if (value <= 0.0) {
            0
        } else {
            (value / DAMAGE_REDUCTION_PER_HIT).toInt()
        }.coerceIn(0, MAX_STARS)

        val emptyStars = MAX_STARS - filledStars
        val stars = "★".repeat(filledStars) + "☆".repeat(emptyStars)

        sendActionBar(Component.text(stars).color(TextColor.fromHexString("#703763")))

        world.playSound(location, Sound.PARTICLE_SOUL_ESCAPE,2.0f, 1.0f)
        world.spawnParticle(Particle.SOUL, eyeLocation, 5, 0.5, 0.5, 0.5, 0.01)
        world.spawnParticle(Particle.COPPER_FIRE_FLAME, eyeLocation, 5, 0.5, 0.5, 0.5, 0.01)
    }
}