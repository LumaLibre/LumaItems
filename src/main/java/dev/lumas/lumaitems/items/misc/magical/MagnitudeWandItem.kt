package dev.lumas.lumaitems.items.misc.magical

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.AbilityUtil
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.toBukkitColor
import dev.lumas.lumaitems.util.tiers.Tier
import java.awt.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class MagnitudeWandItem : CustomItemFunctions() {

    companion object {
        private const val MIN_BOUND = -0.7
        private const val MAX_BOUND = 1.2

        private val KEY = Util.namespacedKey("magnitude-wand")
        private val BLACKLISTED = listOf(EntityType.ENDER_DRAGON, EntityType.WITHER, EntityType.PLAYER)
        private val COLORS = listOf("#26372B", "#AED4DE", "#263D41").map { Color.decode(it) }
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#26372B:#AED4DE:#263D41>Magnitude Wand</gradient></b>")
            .customEnchants("<#AED4DE>Scalar")
            .material(Material.BLAZE_ROD)
            .persistentData(KEY)
            .tier(Tier.HALLOWEEN_2025)
            .vanillaEnchants(
                Enchantment.KNOCKBACK  to 2
            )
            .lore(
                "<#AED4DE>Left-click</#AED4DE> to cast a",
                "spell that alters the",
                "scale of the first",
                "entity it hits.",
                "",
                "<red>13 lapis per spell"
            )
            .buildPair()
    }


    override fun onLeftClick(player: Player, event: PlayerInteractEvent) {
        if (!player.inventory.containsAtLeast(ItemStack(Material.LAPIS_LAZULI), 13)) {
            return
        }

        AbilityUtil.spawnSpell(player, null, KEY, 100) {
            it.world.spawnParticle(Particle.DUST, it.location, 4, 0.1, 0.1, 0.1, 0.0, Particle.DustOptions(COLORS.random().toBukkitColor(), 1.0f))
        }
    }


    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val hitEntity = event.hitEntity as? LivingEntity ?: return
        if (BLACKLISTED.contains(hitEntity.type) || AbilityUtil.noDamagePermission(player, hitEntity) || !AbilityUtil.takeSpellLapisCost(player, 13)) {
            return
        }

        val attribute = hitEntity.getAttribute(Attribute.SCALE) ?: return
        if (attribute.getModifier(KEY) != null) {
            attribute.removeModifier(KEY)
            return
        }

        val amt = random().nextDouble(MIN_BOUND, MAX_BOUND)
        val modifier = AttributeModifier(KEY, amt, AttributeModifier.Operation.ADD_SCALAR)


        attribute.addModifier(modifier)
        hitEntity.world.playSound(hitEntity.location, Sound.ENTITY_EVOKER_CAST_SPELL, 1f, 1f)
    }
}