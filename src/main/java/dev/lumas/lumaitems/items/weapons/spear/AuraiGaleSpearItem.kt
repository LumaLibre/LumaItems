package dev.lumas.lumaitems.items.weapons.spear

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.AttributeContainer
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.PersistentDataRecord
import dev.lumas.lumaitems.util.extensions.isItemInSlot
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack

class AuraiGaleSpearItem : CustomItemFunctions() {

    private companion object {
        private val KEY = "aurai-gale-spear".namespacedKey()
    }

    private val damageGuard = ThreadLocal.withInitial { false }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#FAC3C3:#F8C2B4:#FAEBB3:#B8E2FC:#9EB4EC>Aurai Gale Spear</gradient></b>")
            .customEnchants("<#9EB4EC>Surge")
            .material(Material.DIAMOND_SPEAR)
            .persistentData(KEY)
            .persistentDataRecords(PersistentDataRecord.PREVENT_NETHERITE_SMITHING)
            .tagline("#9EB4EC", "Let the wind rise...")
            .tier(Tier.VALENTIDE_2026)
            .lore(
                "While held, <#9EB4EC>jump</#9EB4EC> to",
                "launch yourself into",
                "the air.",
                "",
                "<#9EB4EC>Attack</#9EB4EC> any entity to",
                "damage all nearby",
                "entities based on",
                "your speed.",
            )
            .vanillaEnchants(
                Enchantment.SHARPNESS to 6,
                Enchantment.KNOCKBACK to 3,
                Enchantment.LOOTING to 4,
                Enchantment.UNBREAKING to 3,
                Enchantment.MENDING to 1,
                Enchantment.LUNGE to 5
            )
            .attributeModifiers(
                AttributeContainer.builder()
                    .setKey(KEY)
                    .setAttribute(Attribute.MOVEMENT_SPEED)
                    .setOperation(AttributeModifier.Operation.ADD_NUMBER)
                    .setAmount(0.075)
                    .setSlot(EquipmentSlotGroup.MAINHAND)
                    .build()
            )
            .buildPair()
    }



    override fun onJump(player: Player, event: PlayerJumpEvent) {
        if (!player.isGliding && !player.isInWater && player.isItemInSlot(KEY, EquipmentSlot.HAND)) {
            player.inventory.itemInMainHand.damage(14, player)
            player.gustEffect(true)
            val current = player.velocity
            player.velocity = current.setY(current.y + 1.0)
        }
    }

    override fun onEntityDamage(player: Player, event: EntityDamageByEntityEvent) {
        if (damageGuard.get() || !player.isItemInSlot(KEY, EquipmentSlot.HAND)) return

        damageGuard.set(true)

        try {
            val entity = event.entity as? LivingEntity ?: return
            val nearby = entity.location.getNearbyLivingEntities(5.0)
                .filter { it != player && it != entity && it.type == entity.type }
            if (nearby.isEmpty()) return

            val item = player.inventory.itemInMainHand

            event.entity.gustEffect(false)
            val dir = player.eyeLocation.direction
            for (entity in nearby) {
                entity.damage(event.damage.times(0.7), player)
                entity.knockback(3.5, -dir.x, -dir.z)
                item.damage(4, player)
            }
        } finally {
            damageGuard.set(false)
        }
    }

    private fun Entity.gustEffect(big: Boolean) {
        world.playSound(location, Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1f, 0.8f)
        world.spawnParticle(Particle.GUST_EMITTER_SMALL, location, 1, 0.05, 0.05, 0.05, 0.01)
        if (big) world.spawnParticle(Particle.GUST_EMITTER_LARGE, location, 1, 0.03, 0.03, 0.03, 0.01)
    }
}