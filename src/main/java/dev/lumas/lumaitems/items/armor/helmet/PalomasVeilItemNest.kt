package dev.lumas.lumaitems.items.armor.helmet

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.PersistentDataRecord
import dev.lumas.lumaitems.model.task.Synchronizable
import dev.lumas.lumaitems.particles.ParticleDisplay
import dev.lumas.lumaitems.particles.Particles
import dev.lumas.lumaitems.util.extensions.asEnum
import dev.lumas.lumaitems.util.extensions.firstEquipmentContainer
import dev.lumas.lumaitems.util.extensions.flag
import dev.lumas.lumaitems.util.extensions.getDrops
import dev.lumas.lumaitems.util.extensions.getPersistentKey
import dev.lumas.lumaitems.util.extensions.isFlagged
import dev.lumas.lumaitems.util.extensions.isItemInSlot
import dev.lumas.lumaitems.util.extensions.itemStack
import dev.lumas.lumaitems.util.extensions.mix
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.removeFlag
import dev.lumas.lumaitems.util.extensions.setPersistentKey
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.extensions.syncDelayed
import dev.lumas.lumaitems.util.extensions.syncTimer
import dev.lumas.lumaitems.util.extensions.toColor
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.addCooldown
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.isOnCooldown
import io.canvasmc.canvas.event.EntityTeleportAsyncEvent
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import java.util.UUID
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import org.bukkit.Color
import org.bukkit.DyeColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.trim.ArmorTrim
import org.bukkit.inventory.meta.trim.TrimMaterial
import org.bukkit.inventory.meta.trim.TrimPattern
import org.bukkit.persistence.PersistentDataType

class PalomasCoreItem : CustomItemFunctions() {

    private val delegate = PalomasVeilItem()

    override fun createItem(): Pair<String, ItemStack> {
        fun c(s: Any) = "<#DD9FC7>$s</#DD9FC7>"

        return ItemFactory.builder()
            .name("<b><gradient:#911025:#c44a58:#f2cbb8:#DD9FC7:#E081CE>Paloma's Core</gradient></b>")
            .customEnchants(c("Kazkan"))
            .material(Material.QUARTZ)
            .tier(Tier.WONDERLAND_2026)
            .persistentData("palomas-core")
            .vanillaEnchants(
                Enchantment.UNBREAKING to 10,
                Enchantment.PROTECTION to 4,
                Enchantment.LOOTING to 7,
                Enchantment.MENDING to 1
            )
            .lore(
                "<gray>Right-click to open.",
                "",
                "With empty hands, press",
                "your ${c("swap key (F)")} to",
                "summon ${c(5)} orbs displays.",
                "",
                "${c("Left-click")} to fire one",
                "at a time or ${c("left-click")}",
                "while ${c("sneaking")} to fire",
                "all orbs at once.",
                "",
                "<red>Cooldown: 4s"
            )
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        val item = player.inventory.itemInMainHand
        if (!item.isMatchingItem("palomas-core")) {
            return
        }
        item.amount -= 1

        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 1f)
        player.give(delegate.createItem().second)
    }
}

class PalomasVeilItem : CustomItemFunctions() {

    companion object {
        private val KEY = "palomas-veil".namespacedKey()
        private val COLOR_TYPE_KEY = "palomas-veil-color-type".namespacedKey()
        private val DISPLAYS = mutableMapOf<UUID, OrbDisplay>()
        private val ALLOWED_INV_TYPES = setOf(InventoryType.CRAFTING, InventoryType.PLAYER, InventoryType.CREATIVE)
        private const val LOOTING_LEVEL = 7
    }


    override fun createItem(): Pair<String, ItemStack> {
        // <b><gradient:#911025:#c44a58:#f2cbb8:#ffbd9a:#b18e57:#2b1b15>Paloma's Veil</gradient></b>
        val colorType = ColorType.entries.random()

        fun c(s: Any) = "<${colorType.altColor}>$s</${colorType.altColor}>"

        return ItemFactory.builder()
            .name(colorType.title)
            .customEnchants("<${colorType.altColor}>Kazkan")
            .material(Material.NETHERITE_HELMET)
            .tier(Tier.WONDERLAND_2026)
            .persistentData(KEY)
            .armorTrim(ArmorTrim(colorType.trimMaterial, TrimPattern.DUNE))
            .persistentDataRecords(
                PersistentDataRecord.create(COLOR_TYPE_KEY, PersistentDataType.STRING, colorType.name)
            )
            .vanillaEnchants(
                Enchantment.UNBREAKING to 10,
                Enchantment.PROTECTION to 4,
                Enchantment.LOOTING to LOOTING_LEVEL,
                Enchantment.MENDING to 1
            )
            .lore(
                //"",
                "With empty hands, press",
                "your ${c("swap key (F)")} to",
                "summon ${c(5)} orbs displays.",
                "",
                "${c("Left-click")} to fire one",
                "at a time or ${c("left-click")}",
                "while ${c("sneaking")} to fire",
                "all orbs at once.",
                "",
                "<red>Cooldown: 4s"
            )
            .buildPair()
    }


    fun summonOrbDisplay(player: Player, riseParticles: Boolean = false) {
        // clean up existing
        DISPLAYS.remove(player.uniqueId)?.stop()

        val item = player.firstEquipmentContainer(COLOR_TYPE_KEY) ?: return
        val colorType = item.getPersistentKey(COLOR_TYPE_KEY, PersistentDataType.STRING)?.asEnum(ColorType::class) ?: ColorType.RED

        val orbDisplay = OrbDisplay(player, colorType).also { DISPLAYS[player.uniqueId] = it }
        orbDisplay.create(riseParticles)
        orbDisplay.startTracking { p -> !p.isOnline || p.inventory.itemInMainHand.type != Material.AIR }
    }

    fun removeOrbDisplay(player: Player): Boolean {
        val orbDisplay = DISPLAYS.remove(player.uniqueId)

        if (orbDisplay != null) {
            orbDisplay.stop()
            return true
        }
        return false
    }

    fun doFire(player: Player) {
        val orbDisplay = DISPLAYS[player.uniqueId] ?: return

        orbDisplay.fire(player)
        if (player.isSneaking) {
            player.removeNow()
            // fire the rest
            for (i in 0 until orbDisplay.tracked.size) {
                player.syncDelayed(1L * i) {
                    orbDisplay.fire(player)
                }
            }
            return
        }

        // move to object?
        if (orbDisplay.tracked.isEmpty() && player.inventory.itemInMainHand.type == Material.AIR) {
            summonOrbDisplay(player)
        }
    }

    override fun onLeftClick(player: Player, event: PlayerInteractEvent) {
       doFire(player)
    }

    override fun onEntityDamage(player: Player, event: EntityDamageByEntityEvent) {
        if (event.damager.type == EntityType.PLAYER) {
            val livingEntity = event.entity as? LivingEntity ?: return
            livingEntity.noDamageTicks = 0
            doFire(player)
        }
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val snowball = event.entity as? Snowball ?: return

        val knifeDisplay = DISPLAYS[player.uniqueId] ?: return
        knifeDisplay.handleLand(snowball, event)
    }

    override fun onPlayerItemHeld(player: Player, event: PlayerItemHeldEvent) {
        val newItem = player.inventory.getItem(event.newSlot)
        val oldItem = player.inventory.getItem(event.previousSlot)

        if (oldItem == null && newItem == null) {
            // No item in either hand, do nothing
            return
        }

        if (newItem == null && player.isFlagged(this)) {
            summonOrbDisplay(player)
        } else {
            removeOrbDisplay(player)
        }
    }

    override fun onFastAsyncRunnable(player: Player) {
        if (!player.isFlagged(this)) return
        player.sync {
            if (!ALLOWED_INV_TYPES.contains(player.openInventory.type)) {
                removeOrbDisplay(player)
            } else if (DISPLAYS[player.uniqueId]?.tracked == null && player.inventory.itemInMainHand.type == Material.AIR) {
                summonOrbDisplay(player) // todo
            }
        }
    }

    override fun onInventoryOpen(player: Player, event: InventoryOpenEvent) {
        removeOrbDisplay(player)
    }

    override fun onEntityDeath(player: Player, event: EntityDeathEvent) {
        if (event.drops.isEmpty()) {
            return
        }

        val entity = event.entity

        val simulatedDrops = entity.getDrops(player, event.damageSource, LOOTING_LEVEL)

        event.drops.clear() // clear this, we handle it manually

        simulatedDrops.forEach {
            entity.world.dropItem(entity.location, it)
        }
    }

    override fun onPlayerSwapHands(player: Player, event: PlayerSwapHandItemsEvent) {
        if (event.offHandItem.type != Material.AIR || event.mainHandItem.type != Material.AIR || player.isOnCooldown(this) || !player.isItemInSlot(KEY, EquipmentSlot.HEAD)) {
            return
        }

        val snowballs = DISPLAYS[player.uniqueId]?.tracked

        if (snowballs == null) {
            summonOrbDisplay(player, true)
            player.flag(this)
        } else {
            player.removeFlag(this) // no cooldown
            player.syncDelayed(1L) {
                removeOrbDisplay(player)
            }
        }
    }


    override fun onArmorChange(player: Player, event: PlayerArmorChangeEvent) {
        if (!player.isItemInSlot(KEY, EquipmentSlot.HEAD)) {
            player.removeNow()
            removeOrbDisplay(player)
        }
    }

    override fun onPlayerTeleport(player: Player, event: PlayerTeleportEvent) {
        if (removeOrbDisplay(player)) {
            player.removeNow()
        }
    }

    override fun onCanvasAsyncPlayerTeleport(player: Player, event: EntityTeleportAsyncEvent) {
        val orbDisplay = DISPLAYS[player.uniqueId] ?: return
        orbDisplay.sync {
            if (removeOrbDisplay(player)) {
                player.removeNow()
            }
        }
    }

    override fun onPlayerQuit(player: Player, event: PlayerQuitEvent) {
        removeOrbDisplay(player)
    }

    override fun onPluginDisable(player: Player) {
        removeOrbDisplay(player)
    }

    override fun onPlayerDeath(player: Player, event: PlayerDeathEvent) {
        player.removeNow()
        removeOrbDisplay(player)
    }

    private fun Player.removeNow() {
        removeFlag(this@PalomasVeilItem)
        addCooldown(this@PalomasVeilItem, 20 * 4L)
    }

    data class OrbitConfig(
        val forward: Double,
        val right: Double,
        val up: Double,
        val material: Material? = Material.NETHER_STAR
    ) {
        val itemStack = material?.itemStack { meta ->
            meta.addEnchant(Enchantment.UNBREAKING, 1, true)
        }
    }

    class OrbDisplay(val player: Player, val colorType: ColorType) : Synchronizable.Entity {

        val tracked = mutableListOf<Snowball>()
        var lastYaw = player.eyeLocation.yaw
        var lastPitch = player.eyeLocation.pitch
        var tick = 0
        val riseTime = 10
        var lastPos = player.location.clone()

        var task: ScheduledTask? = null

        val instantEffect = ParticleDisplay.of(Particle.INSTANT_EFFECT)
            .withExtra(0.0)
            .withColor(colorType.primaryColor.toColor())

        val dust = ParticleDisplay.of(Particle.DUST)
            .withExtra(0.0)
            .withColor(colorType.primaryColor.toColor())

        override val entity: Entity get() = tracked.firstOrNull() ?: player


        fun create(riseParticles: Boolean) {
            for (config in Formation.resolve(player).configs) {
                val spawnLoc = getOffsetLocation(player.eyeLocation, config.forward, config.right - Random.Default.nextDouble(-1.0, 1.0), config.up - Random.Default.nextDouble(-1.0, 1.0))
                val snowball = player.world.spawn(spawnLoc, Snowball::class.java).apply {
                    item = config.itemStack ?: colorType.itemStack
                    setGravity(false)
                    isPersistent = false
                    setPersistentKey(KEY, PersistentDataType.SHORT, 1)
                    shooter = player
                }
                tracked.add(snowball)
            }

            if (riseParticles) {
                val particleDisplay = ParticleDisplay.of(Particle.DUST)
                    .withExtra(0.0)
                    .withColor(colorType.primaryColor.toColor())
                var count = 0
                tracked.syncTimer(0, 1) { task ->
                    if (++count > riseTime) {
                        task.cancel()
                        return@syncTimer
                    }

                    tracked.forEach {
                        particleDisplay.spawn(it.location)
                    }
                }
            }
        }


        fun startTracking(stopIf: (Player) -> Boolean) {
            task = player.syncTimer(0, 1) { task ->
                val tracked = DISPLAYS[player.uniqueId]?.tracked
                if (tracked == null || stopIf(player)) {
                    task.cancel()
                    tracked?.forEach { it.remove() }
                    DISPLAYS.remove(player.uniqueId)
                    return@syncTimer
                }

                tracked.removeAll { !it.isValid }
                if (tracked.isEmpty()) {
                    task.cancel()
                    DISPLAYS.remove(player.uniqueId)
                    return@syncTimer
                }

                val movementSpeed = player.location.toVector().subtract(lastPos.toVector()).lengthSquared()
                lastPos = player.location.clone()

                val eye = player.eyeLocation
                val yawDelta = eye.yaw - lastYaw
                val pitchDelta = eye.pitch - lastPitch
                lastYaw = eye.yaw
                lastPitch = eye.pitch

                val speed = sqrt((yawDelta * yawDelta + pitchDelta * pitchDelta).toDouble())
                val predictionStrength = (1.5 - (speed * 0.1)).coerceIn(0.0, 1.5).toFloat()

                val predicted = eye.clone()
                predicted.yaw += yawDelta * predictionStrength
                predicted.pitch = (predicted.pitch + pitchDelta * predictionStrength).coerceIn(-90f, 90f)

                val riseProgress = if (tick < riseTime) {
                    val t = tick.toDouble() / riseTime
                    1.0 - (1.0 - t) * (1.0 - t) // ease-out quadratic
                } else 1.0

                tick++

                for ((i, snowball) in tracked.withIndex()) {
                    val config = Formation.resolve(player).configs[i]
                    val upOffset = config.up - (1.5 * (1.0 - riseProgress))
                    val target = getOffsetLocation(predicted, config.forward, config.right, upOffset)
                    val diff = target.toVector().subtract(snowball.location.toVector())

                    if (speed > 0.5 || movementSpeed > 0.01) {
                        val distance = diff.lengthSquared()
                        val strength = (distance * 0.2).coerceIn(0.15, 0.95)
                        val correction = diff.multiply(strength)
                        val blended = snowball.velocity.multiply(0.2).add(correction.multiply(0.9))
                        snowball.velocity = blended//.multiply(0.55)
                    } else {
                        snowball.velocity = diff.multiply(0.45)
                    }
                }
            }
        }

        fun stop() {
            task?.cancel()
            tracked.forEach { it.remove() }
            tracked.clear()
        }

        fun handleLand(snowball: Snowball, event: ProjectileHitEvent) {
            if (snowball in tracked) { // still being tracked, just respawn
                val index = tracked.indexOf(snowball)
                snowball.remove()
                tracked.removeAt(index)

                val config = Formation.resolve(player).configs[index]
                val spawnLoc = getOffsetLocation(player.eyeLocation, config.forward, config.right, config.up)
                val newSnowball = player.world.spawn(spawnLoc, Snowball::class.java).apply {
                    item = snowball.item
                    setGravity(false)
                    isPersistent = false
                    setPersistentKey(KEY, PersistentDataType.SHORT, 1)
                    shooter = player
                }
                tracked.add(index, newSnowball)
                event.isCancelled = true
                return
            }

            val hitEntity = event.hitEntity as? LivingEntity
            val nearby = snowball.location.getNearbyLivingEntities(1.0)
                .filter { it != event.hitEntity && it != player }

            val damageSource = DamageSource.builder(DamageType.ARROW)
                .withCausingEntity(player)
                .withDirectEntity(snowball)
                .withDamageLocation(snowball.location)
                .build()

            nearby.forEach { entity ->
                entity.damage(Random.Default.nextDouble(3.0, 6.0), damageSource)
                entity.noDamageTicks = 0
            }

            if (nearby.isNotEmpty() || hitEntity != null) {
                snowball.world.spawnParticle(Particle.WHITE_SMOKE, snowball.location, 2, 0.1, 0.1, 0.1, 0.3)
                snowball.world.spawnParticle(Particle.COPPER_FIRE_FLAME, snowball.location, 3, 0.5, 0.5, 0.5, 0.0)
            }

            val withLocation = instantEffect.withLocation(snowball.location)

            Particles.circle(0.3, 5.0, withLocation)

            if (hitEntity != null) {
                snowball.world.playSound(snowball.location, Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.8f, 2.0f)
                hitEntity.damage(8.0, damageSource)
                hitEntity.noDamageTicks = 0
            } else {
                snowball.world.playSound(snowball.location, Sound.BLOCK_CANDLE_HIT, 0.5f, 1.9f)
            }
        }

        fun fire(player: Player) {
            val direction = player.location.direction.normalize()

            val snowball = tracked.firstOrNull { it.isValid }?.also { tracked.remove(it) } ?: return

            snowball.setGravity(true)
            snowball.velocity = direction.multiply(4.2)

            snowball.syncTimer(0, 1) {
                if (snowball.isDead) {
                    it.cancel()
                } else {
                    dust.spawn(snowball.location)
                }
            }
        }

        /**
         * Calculates a target location offset from the player's eye using
         * the player's look direction as a basis.
         *
         * @param eye The player's eye location (provides position + direction)
         * @param forward Distance in front of the player's face
         * @param right Distance to the right (negative = left)
         * @param up Distance upward (negative = down)
         */
        private fun getOffsetLocation(eye: Location, forward: Double, right: Double, up: Double): Location {
            val yawRad = Math.toRadians(eye.yaw.toDouble())
            val pitchRad = Math.toRadians(eye.pitch.toDouble())

            // Forward vector from yaw + pitch
            val dx = -sin(yawRad) * cos(pitchRad)
            val dy = -sin(pitchRad)
            val dz = cos(yawRad) * cos(pitchRad)

            // Right vector (perpendicular on horizontal plane)
            val rx = cos(yawRad)
            val rz = sin(yawRad)

            return eye.clone().add(
                dx * forward + rx * right,
                dy * forward + up,
                dz * forward + rz * right
            )
        }
    }

    enum class Formation(val configs: List<OrbitConfig>, val predicate: (Player, Double) -> Boolean) {

        WATER(
            listOf(
                OrbitConfig(forward = 3.0, right = 0.0, up = 0.0, material = Material.NETHER_STAR),     // tip
                OrbitConfig(forward = 2.0, right = -0.7, up = 0.0, material = null),         // left wing inner
                OrbitConfig(forward = 2.0, right = 0.7, up = 0.0, material = null),          // right wing inner
                OrbitConfig(forward = 2.5, right = -0.6, up = 0.3, material = Material.NETHER_STAR),     // left wing outer
                OrbitConfig(forward = 2.5, right = 0.6, up = 0.3, material = Material.NETHER_STAR),      // right wing outer
            ),
            { p, _ -> p.isInWater }
        ),

        GLIDE_SQUARE_CROUCH(
            listOf(
                OrbitConfig(forward = 3.0, right = -0.8, up = 2.5, material = Material.NETHER_STAR),    // top-left
                OrbitConfig(forward = 3.0, right = 0.8, up = 2.5, material = Material.NETHER_STAR),     // top-right
                OrbitConfig(forward = 3.0, right = -0.8, up = 1.0, material = null),        // bottom-left
                OrbitConfig(forward = 3.0, right = 0.8, up = 1.0, material = null),         // bottom-right
                OrbitConfig(forward = 3.0, right = 0.0, up = 1.75, material = Material.NETHER_STAR),    // center
            ),
            { player, _ -> player.isGliding && player.isSneaking }
        ),

        GLIDE_SQUARE(
            listOf(
                OrbitConfig(forward = 0.0, right = -0.8, up = 2.5, material = Material.NETHER_STAR),    // top-left
                OrbitConfig(forward = 0.0, right = 0.8, up = 2.5, material = Material.NETHER_STAR),     // top-right
                OrbitConfig(forward = 0.0, right = -0.8, up = 1.0, material = null),        // bottom-left
                OrbitConfig(forward = 0.0, right = 0.8, up = 1.0, material = null),         // bottom-right
                OrbitConfig(forward = 0.0, right = 0.0, up = 1.75, material = Material.NETHER_STAR),    // center
            ),
            { player, _ -> player.isGliding }
        ),

        DIAMOND(
            listOf(
                OrbitConfig(forward = 3.0, right = 0.0, up = 0.8, material = Material.NETHER_STAR),
                OrbitConfig(forward = 3.0, right = -0.8, up = 0.0, material = null),
                OrbitConfig(forward = 3.0, right = 0.8, up = 0.0, material = null),
                OrbitConfig(forward = 3.0, right = -0.4, up = -0.8, material = Material.NETHER_STAR),
                OrbitConfig(forward = 3.0, right = 0.4, up = -0.8, material = Material.NETHER_STAR),
            ),
            { player, _ -> player.isSneaking }
        ),

        FLYING(
            listOf(
                OrbitConfig(forward = 3.0, right = 0.0, up = 0.4, material = Material.NETHER_STAR),
                OrbitConfig(forward = 3.0, right = -0.7, up = 0.2, material = null),
                OrbitConfig(forward = 3.0, right = 0.7, up = 0.2, material = null),
                OrbitConfig(forward = 3.0, right = -1.4, up = 0.0, material = Material.NETHER_STAR),
                OrbitConfig(forward = 3.0, right = 1.4, up = 0.0, material = Material.NETHER_STAR),
            ),
            { player, _ ->
                @Suppress("DEPRECATION") // for decoration, client reporting is fine
                !player.isOnGround || player.isFlying
            }
        ),

        DEFAULT(
            listOf(
                OrbitConfig(forward = 3.0, right = 0.0, up = 0.0, material = Material.NETHER_STAR),
                OrbitConfig(forward = 3.0, right = -0.7, up = 0.2, material = null),
                OrbitConfig(forward = 3.0, right = 0.7, up = 0.2, material = null),
                OrbitConfig(forward = 3.0, right = -1.4, up = 0.4, material = Material.NETHER_STAR),
                OrbitConfig(forward = 3.0, right = 1.4, up = 0.4, material = Material.NETHER_STAR),
            ),
            { _, _ -> true } // fallback, always matches
        );

        companion object {
            fun resolve(player: Player, movementSpeed: Double = 0.0): Formation {
                return entries.first { it.predicate(player, movementSpeed) }
            }
        }
    }

    enum class ColorType(val material: Material, val trimMaterial: TrimMaterial, val dyeColor: DyeColor, val altColor: String, val title: String) {
        RED(Material.RED_DYE, TrimMaterial.REDSTONE, DyeColor.RED, "#DD9FC7", "<b><gradient:#911025:#c44a58:#f2cbb8:#DD9FC7:#E081CE>Paloma's Veil</gradient></b>"),
        LIME(Material.LIME_DYE, TrimMaterial.EMERALD, DyeColor.LIME, "#CF9CFA", "<b><gradient:#86FF54:#B9F3A1:#EFE9F3:#CF9CFA>Paloma's Veil</gradient></b>"),
        LIGHT_BLUE(Material.LIGHT_BLUE_DYE, TrimMaterial.DIAMOND, DyeColor.LIGHT_BLUE, "#F3C7F6", "<b><gradient:#547BFF:#A1A4F3:#DEDFF3:#F3C7F6:#E99CFA>Paloma's Veil</gradient></b>"),
        YELLOW(Material.YELLOW_DYE, TrimMaterial.GOLD, DyeColor.YELLOW, "#F6C7EB", "<b><gradient:#FFFA54:#F3F1A1:#F3F2DE:#F6C7EB:#F19CFA>Paloma's Veil</gradient></b>")
        ;

        val primaryColor = dyeColor.color.mix(Color.WHITE, 0.1f)
        val itemStack = material.itemStack { meta ->
            meta.addEnchant(Enchantment.UNBREAKING, 1, true)
        }
    }

}