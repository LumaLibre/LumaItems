package dev.lumas.lumaitems.items.weapons

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.AttributeContainer
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.BukkitVectors
import dev.lumas.lumaitems.util.extensions.isItemInSlot
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.tiers.Tier
import io.papermc.paper.event.entity.EntityAttemptSmashAttackEvent
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack

class AuraiGaleMaceItem : CustomItemFunctions() {

    private companion object {
        private val KEY = "aurai-gale-mace".namespacedKey()
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#FAC3C3:#F8C2B4:#FAEBB3:#B8E2FC:#9EB4EC>Aurai Gale Mace</gradient></b>")
            .customEnchants("<#9EB4EC>Updraft")
            .material(Material.MACE)
            .persistentData(KEY)
            .tier(Tier.VALENTIDE_2026)
            .tagline("#9EB4EC", "I'll blow the clouds away.")
            .lore(
                "While held, <#9EB4EC>jump</#9EB4EC> to",
                "launch yourself into",
                "the air.",
                "",
                "Perform a <#9EB4EC>smash attack</#9EB4EC>",
                "to damage all nearby",
                "surrounding entities",
                "based on your falling",
                "distance."
            )
            .vanillaEnchants(
                Enchantment.DENSITY to 8,
                Enchantment.WIND_BURST to 3,
                Enchantment.LOOTING to 5,
                Enchantment.UNBREAKING to 5,
                Enchantment.BREACH to 2,
                //Enchantment.MENDING to 1
            )
            .attributeModifiers(
                AttributeContainer.builder(KEY)
                    .setAttribute(Attribute.MOVEMENT_SPEED)
                    .setOperation(AttributeModifier.Operation.ADD_NUMBER)
                    .setAmount(0.045)
                    .setSlot(EquipmentSlotGroup.MAINHAND)
                    .build()
            )
            .buildPair()
    }


    override fun onJump(player: Player, event: PlayerJumpEvent) {
        if (!player.isGliding && !player.isInWater && player.isItemInSlot(KEY, EquipmentSlot.HAND)) {
            player.gustUp(true)
            player.inventory.itemInMainHand.damage(2, player)
        }
    }

    override fun onSmashAttack(player: Player, event: EntityAttemptSmashAttackEvent) {
        if (!player.isItemInSlot(KEY, EquipmentSlot.HAND) || player.fallDistance < 4.0) {
            return
        }

        val nearbyEntities = event.target
            .getNearbyEntities(5.0, 5.0, 5.0)
            .asSequence()
            .filter { it != player && it != event.target }
            .sortedBy { it.location.distanceSquared(event.target.location) }
            .take(10)


        val fallDistance = player.fallDistance.toDouble()
            .coerceAtMost(30.0)

        val smashMultiplier = 3.0
        val impliedDamage = fallDistance * smashMultiplier
        val item = player.inventory.itemInMainHand

        nearbyEntities.forEach { entity ->
            entity.gustUp(false)
            if (entity is LivingEntity) {
                entity.damage(impliedDamage, player)
                item.damage(1, player)
            }
        }
    }

    private fun Entity.gustUp(big: Boolean) {
        val boostStrength = 1.5
        val current = velocity

        velocity = if (this is LivingEntity) {
            world.playSound(location, Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1f, 0.8f)
            world.spawnParticle(Particle.GUST_EMITTER_SMALL, location, 1, 0.05, 0.05, 0.05, 0.01)
            if (big) {
                world.spawnParticle(Particle.GUST_EMITTER_LARGE, location, 1, 0.03, 0.03, 0.03, 0.01)
            }
            current.setY(current.y + boostStrength)
        } else {
            BukkitVectors.UP
        }
    }
}