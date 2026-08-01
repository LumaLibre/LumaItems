package dev.lumas.lumaitems.items.armor.elytra

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.spell
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class PeachPlumApronItem : CustomItemFunctions() {

    private companion object {
        val PARTICLE_DATA = "#FFDAC1".spell()
    }

    override fun createItem() = ItemFactory.builder()
        .name("<b><gradient:#FFE5B4:#FFD1BA:#FFC6A5:#FFB7B2:#FFDAC1>Peach Plum Apron</gradient></b>")
        .customEnchants("<#FFB7B2>Delight")
        .material(Material.ELYTRA)
        .persistentData("peach-plum-apron")
        .tier(Tier.LUMARINE_2026)
        .vanillaEnchants(
            Enchantment.PROTECTION to 6,
            Enchantment.BLAST_PROTECTION to 5,
            Enchantment.UNBREAKING to 10,
            Enchantment.THORNS to 4,
            Enchantment.MENDING to 1
        )
        .lore(
            "<#FFB7B2>While worn</#FFB7B2>, boosts",
            "the effects received",
            "from beacons by <#FFDAC1>50%</#FFDAC1>.",
            "",
            "<#FFB7B2>Damaged</#FFB7B2> entities will",
            "passively reheal <#FFDAC1>25%</#FFDAC1>",
            "of the damage dealt",
            "to them."
        )
        .buildPair()

    override fun onPotionEffect(player: Player, event: EntityPotionEffectEvent) {
        if (event.cause != EntityPotionEffectEvent.Cause.BEACON) {
            return
        }

        val potionEffect = event.newEffect ?: return
        val oldEffect = event.oldEffect

        val newAmplifier = (potionEffect.amplifier + 1) * 2 - 1 // reminder: amplifiers are zero indexed
        val newEffect = potionEffect.withAmplifier(newAmplifier)

        if (oldEffect == null || !newEffect.similarEnough(oldEffect)) {
            val loc = player.boundingBox.center.toLocation(player.world)
            player.playSound(loc, Sound.ITEM_BOTTLE_FILL, 1.0f, 1.0f)
            player.spawnParticle(Particle.INSTANT_EFFECT, loc, 20, 0.4, 0.3, 0.4, PARTICLE_DATA)
        }

        event.isCancelled = true
        player.addPotionEffect(newEffect)

        if (random.nextBoolean()) {
            player.damageItemStack(EquipmentSlot.CHEST, 1)
        }
    }

    override fun onEntityDamage(player: Player, event: EntityDamageByEntityEvent) {
        val entity = event.entity as? LivingEntity ?: return
        val healAmount = event.damage * 0.25
        val amplifier = (healAmount / 4).toInt() // amplifier is 1 = 4 health

        val instantHealth = PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, amplifier, false, false, false)
        entity.addPotionEffect(instantHealth)

        val loc = entity.boundingBox.center.toLocation(entity.world)
        entity.world.playSound(loc, Sound.ITEM_BOTTLE_FILL, 1f, 1.4f)
        entity.world.spawnParticle(Particle.INSTANT_EFFECT, loc, 5, 0.3, 0.2, 0.3, PARTICLE_DATA)
    }

    private fun PotionEffect.similarEnough(other: PotionEffect) = this.amplifier == other.amplifier && this.type == other.type
}