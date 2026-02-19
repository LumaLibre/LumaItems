package dev.lumas.lumaitems.items.armor.elytra

import dev.lumas.lumaitems.enums.GenericToolType
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.particles.ParticleDisplay
import dev.lumas.lumaitems.particles.Particles
import dev.lumas.lumaitems.util.AbilityUtil
import dev.lumas.lumaitems.util.BukkitVectors
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.QuickTasks
import dev.lumas.lumaitems.util.extensions.toColor
import dev.lumas.lumaitems.util.tiers.Tier
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType


class MagiciansCloakItem : CustomItemFunctions() {

    companion object {
        private const val STAR_WEIGHT = 120
        private const val MAX_STARS = 5
        private const val DAMAGE_PER_STAR = 20
        private val COLORS = listOf("#8ec4f7", "#ff9ccb", "#d7f58d", "#fffe8a", "#ffd365")
            .map { it.toColor() }
        private val KEY = Util.namespacedKey("magicianscloak")
    }



    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#a9bd8b:#d5db9b:#fbf1b2:#f0ae97:#a87f85>Magician's Cloak</gradient></b>")
            .customEnchants("<#a9bd8b>Pizzazz!")
            .material(Material.ELYTRA)
            .persistentData(KEY)
            .tier(Tier.CHRISTMAS_2025)
            .lore(
                "<#a9bd8b>Damage</#a9bd8b> entities to charge",
                "up your cloak.",
                "",
                "While holding a tool, <#a9bd8b>click</#a9bd8b>",
                "an entity to release a",
                "powerful attack spell."
            )
            .vanillaEnchants(
                Enchantment.PROTECTION to 6,
                Enchantment.FEATHER_FALLING to 5,
                Enchantment.UNBREAKING to 7,
                Enchantment.MENDING to 1
            )
            .buildPair()
    }

    override fun onEntityDamage(player: Player, event: EntityDamageByEntityEvent) {
        if (QuickTasks.isOnCooldown(this, player)) return
        val damage = event.damage.toInt().toShort()
        if (damage < 1) return

        appendCloakDamage(player, damage)
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        val target = player.getTargetEntity(50) as? LivingEntity ?: return
        val totalDamage = getTotalDamage(player)

        dealDamage(player, target, totalDamage)
    }

    private fun appendCloakDamage(player: Player, amt: Short) {
        val item = player.equipment?.chestplate ?: return
        val meta = item.itemMeta ?: return

        val currentDamage = meta.persistentDataContainer.get(KEY, PersistentDataType.SHORT) ?: 0
        if (currentDamage >= STAR_WEIGHT * MAX_STARS) return
        meta.persistentDataContainer.set(KEY, PersistentDataType.SHORT, (currentDamage + amt).toShort())
        item.itemMeta = meta
        showDamageStars(player)
    }

    private fun updateCloakDamage(player: Player, amt: Short) {
        val item = player.equipment?.chestplate ?: return
        val meta = item.itemMeta ?: return

        meta.persistentDataContainer.set(KEY, PersistentDataType.SHORT, amt)
        item.itemMeta = meta
    }

    private fun getTotalDamage(player: Player): Short {
        val item = player.equipment?.chestplate ?: return 0
        val meta = item.itemMeta ?: return 0
        return meta.persistentDataContainer.get(KEY, PersistentDataType.SHORT) ?: 0
    }

    private fun showDamageStars(player: Player) {
        val damage = getTotalDamage(player)
        val stars = "★".repeat((damage / STAR_WEIGHT).coerceAtLeast(0)) +
                "☆".repeat((MAX_STARS - damage / STAR_WEIGHT)
                    .coerceAtLeast(0))

        val color = COLORS.random()


        player.sendActionBar(Component.text(stars).color(TextColor.color(color.red, color.green, color.blue)))
    }

    private fun dealDamage(player: Player, target: LivingEntity, totalDamageStored: Short) {
        val itemInHand = GenericToolType.getGenericToolType(player.inventory.itemInMainHand.type)

        if (totalDamageStored < STAR_WEIGHT || AbilityUtil.noDamagePermission(player, target) || (itemInHand != GenericToolType.TOOL && itemInHand != GenericToolType.WEAPON)) {
            return
        }
        QuickTasks.addCooldown(this, player, 100)

        val particleDisplay = ParticleDisplay.of(Particle.DUST).withLocation(target.location).withColor(COLORS.random())
        val factor = totalDamageStored / STAR_WEIGHT

        Particles.meguminExplosion(instance(), factor.toDouble(), particleDisplay)
        showDamageStars(player)

        val damageToDeal = (factor * DAMAGE_PER_STAR).toDouble()

        updateCloakDamage(player, 0)

        val ticks = AbilityUtil.damageOverTicks(target, player, damageToDeal, 5, {
            target.velocity = BukkitVectors.ZERO
            target.world.playSound(target.location, Sound.ITEM_TOTEM_USE, 1.0f, 0.6f)
        }, {
            target.world.createExplosion(target.location, factor.toFloat(), false, false, player)
        })
        target.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, ticks, 3, false, false, false))
    }
}