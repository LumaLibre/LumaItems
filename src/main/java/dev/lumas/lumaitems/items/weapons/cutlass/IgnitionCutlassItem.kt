package dev.lumas.lumaitems.items.weapons.cutlass

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.canDamage
import dev.lumas.lumaitems.util.extensions.namespacedKey
import io.papermc.paper.entity.Leashable
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.entity.Tameable
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class IgnitionCutlassItem : CustomItemFunctions() {

    private companion object {
        val KEY = "ignition-cutlass".namespacedKey()
        val EFFECTS = listOf(
            PotionEffect(PotionEffectType.SPEED, 160, 0, false, false, false),
            PotionEffect(PotionEffectType.REGENERATION, 160, 0, false, false, false),
            PotionEffect(PotionEffectType.HASTE, 160, 0, false, false, false),
        )
    }

    override fun createItem() = ItemFactory.builder()
        .name("<b><gradient:#2E2F30:#8F2736:#ECCD64>Ignition Cutlass</gradient></b>")
        .customEnchants("<#8F2736>Firestarter")
        .persistentData(KEY)
        .material(Material.NETHERITE_SWORD)
        .tier(Tier.LUMARINE_2026)
        .vanillaEnchants(
            Enchantment.UNBREAKING to 5,
            Enchantment.FIRE_ASPECT to 8,
            Enchantment.SHARPNESS to 10,
            Enchantment.SWEEPING_EDGE to 5,
            Enchantment.LOOTING to 4,
            Enchantment.MENDING to 1,
        )
        .lore(
            "A cutlass that grants",
            "extra speed and haste",
            "when <#8F2736>damaging</#8F2736> entities.",
            "",
            "While <#8F2736>held</#8F2736>, this sword",
            "will cause nearby",
            "entities to ignite.",
        )
        .buildPair()

    override fun onRunnable(player: Player) {
        for (entity in player.location.getNearbyLivingEntities(10.0)) {
            if (entity is Player
                || entity.customName() != null
                || (entity is Tameable && entity.isTamed)
                || (entity is Leashable && entity.isLeashed)
                || !player.canDamage(entity)) {
                continue
            }

            entity.damage(2.0)
            entity.fireTicks = 400
            entity.world.spawnParticle(Particle.FLAME, entity.eyeLocation, 15, 0.3, 0.3, 0.3, 0.1)
            entity.world.playSound(entity.location, Sound.ITEM_FIRECHARGE_USE, 0.3f, 1f)
        }
    }

    override fun onEntityDamage(player: Player, event: EntityDamageByEntityEvent) {
        var doSound = false
        for (effect in EFFECTS) {
            if (!player.hasPotionEffect(effect.type)) {
                doSound = true
            }
            player.addPotionEffect(effect)
        }
        if (doSound) {
            player.world.playSound(player.location, Sound.ITEM_BOTTLE_FILL, 0.3f, 1f)
        }
    }
}