package dev.jsinco.luma.lumaitems.items.weapons.cutlass

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.obj.AttributeContainer
import dev.jsinco.luma.lumaitems.util.AbilityUtil
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.Util.isItemInSlot
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack

class MidnightClaymoreItem : CustomItemFunctions() {

    companion object {
        private val KEY = Util.namespacedKey("midnight-claymore")
    }

    private val damageGuard = ThreadLocal.withInitial { false }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#342f6c:#7b75aa:#b58fc9>Midnight Claymore</gradient></b>")
            .customEnchants("<#7b75aa>Heavyweight")
            .material(Material.NETHERITE_SWORD)
            .persistentData(KEY)
            .tier(Tier.HALLOWEEN_2025)
            .attributeModifiers(
                AttributeContainer.of(KEY, Attribute.ATTACK_SPEED, AttributeModifier.Operation.ADD_NUMBER, -3.5, EquipmentSlotGroup.ANY),
                AttributeContainer.of(KEY, Attribute.MOVEMENT_SPEED, AttributeModifier.Operation.ADD_NUMBER, -0.010, EquipmentSlotGroup.MAINHAND)
            )
            .vanillaEnchants(
                Enchantment.SHARPNESS to 10,
                Enchantment.SMITE to 6,
                Enchantment.BANE_OF_ARTHROPODS to 6,
                Enchantment.UNBREAKING to 9,
                Enchantment.LOOTING to 5,
                Enchantment.MENDING to 1
            )
            .lore(
                "A massive claymore that",
                "takes two hands to wield.",
                "",
                "Its heavy blade deals",
                "exponentially more damage",
                "the more enemies it hits",
                "at once."
            )
            .buildPair()
    }


    override fun onEntityDamage(player: Player, event: EntityDamageByEntityEvent) {
        if (damageGuard.get() || !player.isItemInSlot(KEY, EquipmentSlot.HAND)) return
        damageGuard.set(true)

        try {
            val hitEntity = event.entity as? LivingEntity ?: return
            val nearbyEntities = player.location.getNearbyLivingEntities(3.0)
                .apply { addAll(
                    hitEntity.location.getNearbyLivingEntities(5.0)
                        .sortedBy { it.location.distanceSquared(hitEntity.location) }
                ) }
                .filter { (it != player || !it.isValid) && it is LivingEntity }

            // get block under entity
            val material = hitEntity.location.subtract(0.0, 1.0, 0.0).block.type

            var factor = nearbyEntities.size.toDouble()
            nearbyEntities.forEach { entity ->
                if (!AbilityUtil.noDamagePermission(player, entity) || entity.type == EntityType.CREAKING) {
                    entity.damage(event.damage * (1 + factor * 0.19), player)
                    entity.world.playSound(entity.location, Sound.ITEM_AXE_STRIP, 1.5f, 0.9f)
                    entity.world.spawnParticle(Particle.SWEEP_ATTACK, entity.boundingBox.center.toLocation(entity.world),1, 0.7, 0.7, 0.7, 0.1)
                    if (!material.isAir) {
                        entity.world.spawnParticle(Particle.BLOCK, entity.boundingBox.center.toLocation(entity.world),30, 0.7, 0.7, 0.7, material.createBlockData())
                    }
                    factor -= 0.5
                }
            }
        } finally {
            damageGuard.set(false)
        }
    }
}