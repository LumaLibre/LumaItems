package dev.lumas.lumaitems.items.misc;

import dev.lumas.lumaitems.items.ItemFactory;
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.extensions.isItemInSlot
import dev.lumas.lumaitems.util.extensions.syncDelayed
import dev.lumas.lumaitems.util.tiers.Tier;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack

class SuperchargedBreezeRod : CustomItemFunctions() {

    companion object {
        private const val KEY = "supercharged-breeze-rod"

        private const val KNOCKBACK_POWER = 14.0
        private const val UPWARD_BOOST = 1.25
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#1AC9CC:#5d85dc:#CA51CB>Supercharged Breeze Rod</gradient></b>")
            .customEnchants(
                "<gray>Knockback VIII", // intentional - custom knockback is applied
                "<gradient:#1AC9CC:#5d85dc>Gust Strike</gradient>"
            )
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .hideEnchants(true)
            .lore(
                "Stolen from a Breeze that",
                "was struck down by lightning.",
                "",
                "<#3CA7D4>Strike</#3CA7D4> enemies to send them",
                "soaring with a burst of wind."
            )
            .material(Material.BREEZE_ROD)
            .persistentData(KEY)
            .tier(Tier.WONDERLAND_2026)
            .buildPair()
    }

    override fun onPrepareCraft(player: Player, event: PrepareItemCraftEvent) {
        event.inventory.result = null
    }

    override fun onEntityDamage(player: Player, event: EntityDamageByEntityEvent) {
        if (!player.isItemInSlot(KEY, EquipmentSlot.HAND)) return
        val entity = event.entity as? LivingEntity ?: return
        val dir = player.location.direction

        entity.syncDelayed(1) { _ ->
            val vel = entity.velocity
            entity.velocity = vel
                .setX(dir.x * KNOCKBACK_POWER)
                .setZ(dir.z * KNOCKBACK_POWER)
                .setY(vel.y + UPWARD_BOOST)
        }

        val loc = entity.location.add(0.0, 0.5, 0.0)
        entity.world.playSound(loc, Sound.ENTITY_WIND_CHARGE_WIND_BURST, 0.9f, 0.9f)
        entity.world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0f, 1.1f)
        entity.world.spawnParticle(Particle.GUST_EMITTER_SMALL, loc, 1, 0.05, 0.05, 0.05, 0.01)
        entity.world.spawnParticle(Particle.DUST_PLUME, loc, 25, 0.3, 0.5, 0.3, 0.15)
    }
}
