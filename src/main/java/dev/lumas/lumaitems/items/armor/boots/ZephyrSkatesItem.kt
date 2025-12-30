package dev.lumas.lumaitems.items.armor.boots

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.util.AbilityUtil.isLocationOnGround
import dev.lumas.lumaitems.util.QuickTasks
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.Util.isItemInSlot
import dev.lumas.lumaitems.util.extensions.ColorUtil.toBukkitColor
import dev.lumas.lumaitems.util.tiers.Tier
import java.util.UUID
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class ZephyrSkatesItem : CustomItemFunctions() {

    companion object {
        private val REFERENCES: MutableMap<UUID, Vector> = HashMap()
        private val KEY = Util.namespacedKey("zephyr-skates")
        private val COLORS = listOf("#8a1c23", "#dc3b44", "#f1f2d9", "#85b79d", "#17341f")
            .map { Particle.DustOptions(it.toBukkitColor(), 1f) }
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.Companion.builder()
            .name("<b><gradient:#8a1c23:#dc3b44:#f1f2d9:#85b79d:#17341f>Zephyr Skates</gradient></b>")
            .customEnchants("<#E37876>Quick Dash")
            .material(Material.NETHERITE_BOOTS)
            .persistentData(KEY)
            .tier(Tier.CHRISTMAS_2025)
            .lore(
                "A pair of skates that",
                "can glide easily along",
                "any surface.",
                "",
                "While grounded, <#E37876>sneak</#E37876>",
                "to dash in the direction",
                "you are moving.",
            )
            .vanillaEnchants(
                Enchantment.PROTECTION to 5,
                Enchantment.BLAST_PROTECTION to 4,
                Enchantment.FIRE_PROTECTION to 4,
                Enchantment.FEATHER_FALLING to 7,
                Enchantment.UNBREAKING to 8,
                Enchantment.MENDING to 1,
                Enchantment.SOUL_SPEED to 3,
                Enchantment.DEPTH_STRIDER to 4
            )
            .buildPair()
    }

    override fun onArmorChange(player: Player, event: PlayerArmorChangeEvent) {
        if (!player.isItemInSlot(KEY, EquipmentSlot.FEET) && REFERENCES.containsKey(player.uniqueId)) {
            REFERENCES.remove(player.uniqueId)
        }
    }

    override fun onMove(player: Player, event: PlayerMoveEvent) {
        if (player.isLocationOnGround(0.5) && !player.isFlying && !player.isGliding && !player.isSneaking) {
            REFERENCES[player.uniqueId] = event.to.clone().subtract(event.from.clone()).toVector()
        } else if (REFERENCES.containsKey(player.uniqueId)) {
            REFERENCES.remove(player.uniqueId)
        }
    }


    override fun onPlayerCrouch(player: Player, event: PlayerToggleSneakEvent) {
        if (!event.isSneaking || !REFERENCES.contains(player.uniqueId) || QuickTasks.isOnCooldown(this, player)) return
        val direction = REFERENCES.remove(player.uniqueId) ?: return

        QuickTasks.addCooldown(this, player, 20)
        player.velocity = direction.normalize().multiply(6.0).setY(-0.1)
        player.world.spawnParticle(Particle.DUST, player.location.add(0.0, 0.5, 0.0), 10, 0.5, 0.5, 0.5, 0.1, COLORS.random())

        if (random().nextBoolean()) {
            return
        }

        if (player.saturation >= 1.0f) {
            player.saturation -= 1.0f
        } else if (player.foodLevel >= 1.0f) {
            player.foodLevel -= 1
        }
    }

}