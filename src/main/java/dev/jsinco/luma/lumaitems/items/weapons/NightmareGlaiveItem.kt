package dev.jsinco.luma.lumaitems.items.weapons

import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.particles.ParticleDisplay
import dev.jsinco.luma.lumaitems.particles.Particles
import dev.jsinco.luma.lumaitems.util.AbilityUtil
import dev.jsinco.luma.lumaitems.util.BukkitVectors
import dev.jsinco.luma.lumaitems.util.Executors
import dev.jsinco.luma.lumaitems.util.FireAnyways
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.disabling.Disable
import dev.jsinco.luma.lumaitems.util.disabling.WorldName
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import java.awt.Color
import java.util.UUID
import kotlin.random.Random
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Trident
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask


@FireAnyways(Action.ENTITY_DAMAGE)
@Disable(WorldName.PINATA, WorldName.SPAWN, WorldName.EVENT_NEW)
class NightmareGlaiveItem : CustomItemFunctions() {

    companion object {
        private val REFERENCES: MutableMap<UUID, SeizeLoc> = mutableMapOf()
        private val KEY = Util.namespacedKey("nightmare-glaive")
        private val BLINDNESS = PotionEffect(PotionEffectType.BLINDNESS, 200, 0, true, false, false)
        private val COLORS = listOf("#ad7abf", "#d2672d", "#d49662", "#6c3f2e", "#302c2f").map { Color.decode(it) }
    }


    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#ad7abf:#d2672d:#d49662:#6c3f2e:#302c2f>Nightmare Glaive</gradient></b>")
            .customEnchants("<#ad7abf>Seize")
            .persistentData(KEY)
            .material(Material.TRIDENT)
            .tier(Tier.HALLOWEEN_2025)
            .vanillaEnchants(
                Enchantment.CHANNELING to 1,
                Enchantment.MENDING to 1,
                Enchantment.LOYALTY to 4,
                Enchantment.UNBREAKING to 5,
                Enchantment.IMPALING to 8,
                Enchantment.SHARPNESS to 9,
                Enchantment.LOOTING to 6
            )
            .lore(
                "<#ad7abf>Upon landing</#ad7abf>, this",
                "weapon ensnares nearby",
                "entities into a small",
                "radius.",
                "",
                "Seized entities cannot",
                "escape and share damage",
                "taken among each other.",
            )
            .buildPair()
    }


    override fun onProjectileLaunch(player: Player, event: ProjectileLaunchEvent) {
        Util.setPersistentKey(event.entity, KEY, PersistentDataType.SHORT, 1)
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val trident = event.entity as? Trident ?: return

        if (!Util.hasPersistentKey(trident.itemStack, KEY)) {
            return
        }

        REFERENCES[player.uniqueId]?.stopTicking()


        val particleDisplay = ParticleDisplay.of(Particle.DUST)
            .withColor(COLORS.random())
            .withLocation(trident.location)
        Executors.async { _ ->
            Particles.neopaganPentagram(1.5, 0.1, 0.0, 0.1, 300.0, particleDisplay, particleDisplay)
        }

        val seize = SeizeLoc(player, trident.location, particleDisplay)
        seize.startTicking()
    }

    override fun onEntityDamage(player: Player, event: EntityDamageByEntityEvent) {
        val seize = REFERENCES[player.uniqueId] ?: return
        val entity = event.entity
        if (entity in seize.entities) {
            for (entity in seize.entities.filter { it != entity && it != player }) {
                entity.damage(event.damage)
            }
        }
    }


    private class SeizeLoc(
        val player: Player,
        val pin: Location,
        val particleDisplay: ParticleDisplay,
        val radius: Double = 14.0,
        val playersRadius: Double = 7.0,
        val durationTicks: Long = 300L
    ) {


        val entities: MutableList<LivingEntity> = pin.world.getNearbyLivingEntities(pin, radius)
            .filter { it !is Player }
            .toMutableList()
            .apply {
                addAll(pin.world.getNearbyPlayers(pin, playersRadius))
            }
            .filter {
                it == player || !AbilityUtil.noDamagePermission(player, it) || (it is Player && it.isSneaking)
            }
            .toMutableList()

        var task: BukkitTask? = null

        fun startTicking() {
            if (entities.isEmpty()) return
            REFERENCES[player.uniqueId] = this


            var count = 0
            val newDisplay = particleDisplay.clone()
                .withLocation(pin)

            this.entities.filter { it is Player }.forEach {
                it as Player
                it.playSound(pin, Sound.AMBIENT_CAVE, 1.5f, 1.2f)
            }
            pin.world.playSound(pin, Sound.ITEM_LEAD_BREAK, 2.0f, Random.nextDouble(0.5, 0.8).toFloat())

            this.task = Executors.syncTimer(0, 1) { task ->
                this.entities.removeIf { !it.isValid || (it is Player && player.isSneaking) }
                if (++count > durationTicks || this.entities.isEmpty()) {
                    this.stopTicking()
                    return@syncTimer
                } else if (count > durationTicks / 3) {
                    this.entities.filter { it is Player }.forEach {
                        it.removePotionEffect(PotionEffectType.BLINDNESS)
                    }
                    this.entities.removeIf { it is Player }
                }

                Particles.sphere(0.1, 10.0, newDisplay)

                for (entity in entities) {
                    val newVel = BukkitVectors.seizeToAnchor(entity, pin, 3.5)
                    if (newVel != null) {
                        entity.velocity = newVel
                    }

                    entity.addPotionEffect(BLINDNESS)
                    val loc = if (entity.eyeLocation.y < pin.y) entity.eyeLocation else entity.location
                    Particles.line(pin, loc, 0.4, particleDisplay)
                }
            }
        }

        fun stopTicking() {
            this.task?.cancel()
            this.entities.forEach { it.removePotionEffect(PotionEffectType.BLINDNESS) }
            REFERENCES.remove(player.uniqueId)
        }
    }
}