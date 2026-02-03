package dev.lumas.lumaitems.items.weapons.scythe

import dev.lumas.lumaitems.enums.DefaultAttributes
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.manager.GlowManager
import dev.lumas.lumaitems.model.AttributeContainer
import dev.lumas.lumaitems.particles.ParticleDisplay
import dev.lumas.lumaitems.particles.Particles
import dev.lumas.lumaitems.util.AbilityUtil
import dev.lumas.lumaitems.util.Executors
import dev.lumas.lumaitems.model.PersistentDataRecord
import dev.lumas.lumaitems.util.Executors.syncEntity
import dev.lumas.lumaitems.util.Executors.syncEntityTimer
import dev.lumas.lumaitems.util.Executors.syncLocation
import dev.lumas.lumaitems.util.QuickTasks
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.isItemInSlot
import dev.lumas.lumaitems.util.disabling.Disable
import dev.lumas.lumaitems.util.disabling.WorldName
import dev.lumas.lumaitems.util.tiers.Tier
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import java.awt.Color
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.BiConsumer
import java.util.function.Predicate
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.entity.Tameable
import org.bukkit.entity.Villager
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.RayTraceResult
import org.bukkit.util.Vector

class HeavyPrismCoreItem : CustomItemFunctions() {

    private val parent = HeavyPrismSickleItem()

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#FAEDCB>Heavy Prism Core</#FAEDCB></b>")
            //.customEnchants("<#FAEDCB>Glass Cannon")
            .lore(
                "<#FAEDCB>Right-click to open.",
                "",
                "Right-click to summon",
                "six prism orbs that will",
                "seek out and damage",
                "nearby entities.",
                "",
                "Lock on to an entity",
                "by directly looking at",
                "it before firing.",
                "",
                "Taking damage of any",
                "kind will stop your",
                "orbs from firing.",
            )
            .persistentData("heavy-prism-core")
            .material(Material.QUARTZ)
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .tier(Tier.SUMMER_2025)
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        val item = event.item ?: return
        item.amount -= 1

        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 1f)
        Util.giveItem(player, parent.createItem().second)
    }

}

@Disable(value = [WorldName.PINATA, WorldName.SPAWN])
class HeavyPrismSickleItem : CustomItemFunctions() {

    companion object {
        private const val RANGE = 25.0
        private const val AUTO_TARGET_RANGE = 20.0
        private const val CIRCLE_RADIUS = 1.0
        private const val CIRCLE_RATE = 3
        private const val MAX_ORBS = 6
        private const val FIRE_TIME = 340

        private val UP = Vector(0, 1, 0)
        private val STOP = Vector(0, 0, 0)

        private val nameSpace = Util.namespacedKey("heavy-prism-sickle")
        private val processes: MutableSet<Process> = mutableSetOf()
    }


    override fun createItem(): Pair<String, ItemStack> {
        val colorKit = ColorKit.entries.random()
        val rOpen = "<${colorKit.rgb}>"
        val rClose = "</${colorKit.rgb}>"
        return ItemFactory.builder()
            .name("<b><gradient:#ff6666:#ffbd54:#ffff67:#9de24f:#87cefa:#EA87FA:#FAEDCB>Heavy Prism Sickle</gradient></b>")
            .customEnchants("${rOpen}Glass Cannon")
            .material(Material.NETHERITE_HOE)
            .persistentData(nameSpace)
            .lore(
                "${rOpen}Right-click${rClose} to summon",
                "${rOpen}six${rClose} prism orbs that will",
                "seek out and damage",
                "nearby entities.",
                "",
                "Lock on to an entity",
                "by directly looking at",
                "it before firing.",
                "",
                "Taking damage of any",
                "kind will stop your",
                "orbs from firing.",
                "",
                "<red>Cooldown: 35s"
            )
            .vanillaEnchants(
                Enchantment.SHARPNESS to 10,
                Enchantment.LOOTING to 5,
                Enchantment.UNBREAKING to 4,
                Enchantment.MENDING to 1
            )
            .tier(Tier.SUMMER_2025)
            .persistentDataRecords(
                PersistentDataRecord.create(nameSpace, PersistentDataType.STRING, colorKit.name)
            )
            .attributeModifiers(
                DefaultAttributes.NETHERITE_HOE.appendThenGetAttributes(
                    AttributeContainer.of(nameSpace, Attribute.ATTACK_SPEED, AttributeModifier.Operation.ADD_NUMBER,-3.3, EquipmentSlotGroup.MAINHAND)
                )
            )
            .buildPair()
    }


    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (QuickTasks.isOnCooldown(this, player) || !player.isItemInSlot(nameSpace, EquipmentSlot.HAND)) return

        val item = event.item ?: return
        val colorKitString = item.persistentDataContainer.get(nameSpace, PersistentDataType.STRING)
        val colorKit = colorKitString?.let { Util.enumValueOfOrNull(ColorKit::class.java, it) } ?: ColorKit.RED

        val process = Process(player)
        processes.add(process)
        val anyOrbsFired = process.start(colorKit) {
            processes.remove(process)
        }
        player.world.playSound(player.location, Sound.ENTITY_BREEZE_LAND, 1f, 1f)

        QuickTasks.addCooldown(this, player, 100) {
            if (anyOrbsFired.get()) {
                //player.playSound(player.location, Sound.ENTITY_ARROW_HIT_PLAYER, 0.2f, 0.6f)
                QuickTasks.addCooldown(this, player, 600)
            }
        }
    }


    override fun onPlayerDamaged(player: Player, event: EntityDamageEvent) {
        val process = processes.firstOrNull { it.player == player } ?: return
        process.cleanupAndStop()
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val hitEntity = event.hitEntity as? LivingEntity ?: return
        hitEntity.damage(30.0, player)
    }

    override fun onPluginDisableGlobal() {
        if (processes.isEmpty()) return
        processes.forEach {
            it.cleanupAndStop()
        }
        processes.clear()
    }


    private class Process(val player: Player) {

        private var scheduledTask: ScheduledTask? = null
        var orbGroup: OrbGroup? = null

        fun start(colorKit: ColorKit, whenDone: () -> Unit): AtomicBoolean {
            if (scheduledTask != null) {
                throw IllegalStateException("Process already started or not initialized properly.")
            }
            val targetEntity = player.getTargetEntity(RANGE.toInt()) as? LivingEntity

            val orbGroup = OrbGroup(player, colorKit, CIRCLE_RADIUS, CIRCLE_RATE, targetEntity)
            this.orbGroup = orbGroup
            val anyOrbsFired: AtomicBoolean = AtomicBoolean(false)
            orbGroup.create {
                var count: Long = 0
                this.scheduledTask = Executors.asyncTimer(0, 1) { task ->
                    // Prune orbs that are no longer valid
                    if (count >= FIRE_TIME || orbGroup.orbs.all { !it.valid }) {
                        task.cancel()
                        orbGroup.destroy()
                        whenDone()
                        return@asyncTimer
                    }


                    orbGroup.moveGroup(count)

                    player.syncEntity {
                        orbGroup.orbs.forEach { orb ->
                            if (orb.fireTime < count) {
                                if (!orb.autoTarget(orbGroup.activeTargets)) {
                                    return@forEach
                                }


                                if (!orb.startedFiring) {
                                    orb.startedFiring = true
                                    val snowball = orb.snowball
                                    snowball.world.playSound(
                                        snowball.location,
                                        Sound.ENTITY_BREEZE_IDLE_GROUND,
                                        0.2f,
                                        0.8f
                                    )
                                }

                                if (!anyOrbsFired.get()) {
                                    anyOrbsFired.set(true)
                                }


                                Executors.async {
                                    orb.drawLine()
                                }
                            }
                        }
                    }

                    if (anyOrbsFired.get()) {
                        orbGroup.pruneTargets()
                    }
                    count++
                }
            }

            return anyOrbsFired
        }

        fun cleanupAndStop() {
            orbGroup?.destroy()
            scheduledTask?.cancel()
        }
    }


    private class OrbGroup(
        val player: Player,
        val colorKit: ColorKit,
        val radius: Double,
        val rate: Int,
        val initialTarget: LivingEntity? = null
    ) {

        companion object {
            private const val DIRECTION_FACTOR = -1.0
        }

        val orbs: MutableSet<Orb> = mutableSetOf()
        val activeTargets: LinkedHashMap<LivingEntity, Int> = linkedMapOf()

        fun pruneTargets() {
            activeTargets.entries.removeIf { it.key.isDead }
        }


        fun create(whenFinished: () -> Unit = {}) {
            val location = player.eyeLocation.clone().add(player.eyeLocation.direction.clone().multiply(DIRECTION_FACTOR))
            // Groups of two, so two orbs at a time should have the same fireTime, then add 50

            val orbDecalList = colorKit.getOrbDecalList()
            calculateLocations(location, whenFinished) { index, loc ->
                if (orbs.size >= MAX_ORBS) return@calculateLocations // limit to 6 orbs
                val fireTime = if (initialTarget == null) {
                    50L + (index / 2) * 15 // every two orbs have the same fireTime
                } else {
                    15L
                }
                val orb = Orb(initialTarget, player, orbDecalList[index], loc, fireTime, DIRECTION_FACTOR)
                orb.spawn()
                //orb.autoTarget(activeTargets) // auto-target entities around the orb
                orb.move()
                orbs.add(orb)
            }
        }

        fun destroy() {
            orbs.forEach { it.despawn() }
            orbs.clear()
            activeTargets.clear()
        }

        fun moveGroup(count: Long): Boolean {
            // check if ANY orbs still need to be moved. if not, we can skip this
            var anyToMove = false
            orbs.forEach { orb ->
                if (orb.fireTime > count) {
                    anyToMove = true
                }
            }
            if (!anyToMove) return false

            synchronized(orbs) {
                val orbsIterator = orbs.iterator()
                val baseLoc = player.eyeLocation.clone().add(player.eyeLocation.direction.clone().multiply(
                    DIRECTION_FACTOR
                ))
                calculateLocations(baseLoc) { _, loc ->
                    if (!orbsIterator.hasNext()) return@calculateLocations
                    val orb = orbsIterator.next()
                    if (orb.fireTime > count) {
                        orb.position = loc
                        orb.move()
                    }
                }
            }
            return true
        }

        private fun calculateLocations(point: Location, whenFinished: () -> Unit = {}, consumer: BiConsumer<Int, Location>) {
            Executors.async {
                // basic circle around a point in 3D space, but flipped on the Y axis
                val rateDiv: Double = Math.PI / abs(this.rate)
                val direction = point.direction.normalize()

                val right = direction.clone().crossProduct(UP).normalize()
                val newUp = right.clone().crossProduct(direction).normalize()

                var theta = 0.0
                var index = 0
                // double pi
                while (theta <= Math.TAU) {
                    val x = this.radius * cos(theta)
                    val y = this.radius * sin(theta)

                    // transform our local (x, y) circle point to 3D in world space
                    val offset = right.clone().multiply(x).add(newUp.clone().multiply(y))
                    val newLoc = point.clone().add(offset)

                    // operation on where an orb should be
                    consumer.accept(index, newLoc)

                    theta += rateDiv
                    index++
                }
                whenFinished.invoke()
            }
        }
    }


    private class Orb(
        var target: LivingEntity?,
        val player: Player,
        val orbDecals: OrbDecals,
        var position: Location,
        val fireTime: Long = 50,
        val directionFactor: Double
    ) {
        companion object {
            private const val SPEED = 0.45
            private const val SPEED_MULTIPLIER = 2.5
            private const val SNAP = 0.5
            private const val MAX_RECURSION = 8

            private fun getPredicate(player: Player, autoTargetting: Boolean = false): Predicate<Entity> {
                return Predicate<Entity> { entity ->
                    entity is LivingEntity &&
                            entity.type != EntityType.ARMOR_STAND &&
                            entity.type != EntityType.GLOW_ITEM_FRAME &&
                            entity.type != EntityType.ITEM_FRAME &&
                            entity != player &&
                            player.canSee(entity) &&
                            (!autoTargetting || (
                                    entity !is Villager &&
                                            (entity as? Tameable)?.isTamed != true
                                            && entity !is Player
                                    ))
                }
            }
        }

        private val rayTraceParticle = ParticleDisplay.of(orbDecals.particleType).withColor(orbDecals.color)
        private val itemStack = ItemStack.of(orbDecals.material)

        private val spawnLocation = player.eyeLocation.clone().add(player.eyeLocation.direction.clone().multiply(directionFactor))

        lateinit var snowball: Snowball
        var startedFiring = false
        var valid = true

        private var timesRespawned = 0
        private var lineCurrentEnd: Location? = null

        init {
            target?.let {
                if (it !is Player) {
                    GlowManager.setGlowColor(it, orbDecals.chatColor)
                }
                GlowManager.setProtocolGlowPacket(player, it, true)
            }
        }

        private fun createSnowball(location: Location) {
            if (timesRespawned >= 50) {
                return // If we've respawned too many times, we stop trying to create a new snowball
            }
            timesRespawned++

            this.snowball = player.world.spawn(location, Snowball::class.java)
            snowball.isPersistent = false
            snowball.setGravity(false)
            snowball.item = itemStack
            snowball.persistentDataContainer.set(nameSpace, PersistentDataType.SHORT, 0)
        }

        fun spawn() {
            if (!valid) {
                this.valid = true
            }

            spawnLocation.syncLocation {
                createSnowball(spawnLocation)
            }
        }

        fun despawn() {
            this.valid = false
            snowball.syncEntity {
                if (!snowball.isDead) {
                    snowball.setGravity(true)
                }
                if (target != null && target?.isDead == false) {
                    if (target !is Player) {
                        GlowManager.removeGlowColor(target!!)
                    }
                    GlowManager.setProtocolGlowPacket(player, target!!, false)
                }
            }
        }

        fun move() { //
            var smallCount = 40
            snowball.syncEntityTimer(0, 1) { task ->
                if (snowball.isDead) {
                    if (valid) {
                        createSnowball(snowball.location)
                    } else {
                        task.cancel()
                        return@syncEntityTimer
                    }
                } else if (snowball.location.world != position.world || snowball.location.distance(position) <= 0.1) {
                    snowball.velocity = STOP
                    task.cancel()
                    return@syncEntityTimer
                }
                val direction = position.clone().subtract(snowball.location).toVector().normalize()
                snowball.velocity = direction.multiply(0.2)
                if (smallCount >= 40) {
                    rayTraceParticle.spawn(snowball.location)
                    smallCount = 0
                } else {
                    smallCount++
                }
            }
        }

        fun autoTarget(activeTargets: LinkedHashMap<LivingEntity, Int>): Boolean {
            if (!valid) return false
            if (this.target != null && !target!!.isDead) {
                return true // already have a target
            }
            fun setTarget(entity: LivingEntity) {
                this.target = entity
                if (activeTargets.containsKey(entity)) {
                    activeTargets[entity] = activeTargets.getOrDefault(entity, 0) + 1
                } else {
                    activeTargets[entity] = 1
                }
                if (entity !is Player) {
                    GlowManager.setGlowColor(entity, orbDecals.chatColor)
                }
                GlowManager.setProtocolGlowPacket(player, entity, true)
            }

            val nearbyEntities = position.getNearbyLivingEntities(AUTO_TARGET_RANGE)
                .filter { entity -> getPredicate(player, true).test(entity) }
                .filter { !AbilityUtil.noDamagePermission(player, it) }
                .filter { entity -> activeTargets.getOrDefault(entity, 0) < 2 }
                .sortedBy { it.location.distanceSquared(player.location) }
            if (nearbyEntities.isEmpty()) {
                if (activeTargets.isEmpty()) {
                    despawn()
                    return false
                }
                setTarget(activeTargets.keys.first())
            } else {
                setTarget(nearbyEntities.first())
            }

            return true
        }

        fun drawLine() {
            if (!valid || target == null) return

            val targetLoc = target!!.boundingBox.center.toLocation(target!!.world).clone()
            if (targetLoc.world != position.world) {
                this.target = null // Probably a player going to another world
                return
            }
            val directionToTarget = targetLoc.clone().subtract(position).toVector().normalize()
            val destination = position.clone().add(directionToTarget.multiply(RANGE))

            if (lineCurrentEnd == null) {
                lineCurrentEnd = destination.clone()
            }

            val toDestination = destination.clone().subtract(lineCurrentEnd!!)
            val distance = toDestination.length()

            if (distance > SNAP) {
                // Move currentEnd 'SPEED' blocks toward destination
                // If we're really far away from our target, we'll increase our speed a bit more than normal
                val directionStep = toDestination.toVector().normalize().multiply(
                    if (distance > 18.0) {
                        SPEED * SPEED_MULTIPLIER // increase speed if we're far away
                    } else {
                        SPEED
                    }
                )
                lineCurrentEnd!!.add(directionStep)
            } else {
                // Snap to destination if we're close enough
                lineCurrentEnd = destination.clone()
            }

            // Draw line from snowball to the current extended endpoint
            val start = snowball.location.clone()
            Particles.line(start, lineCurrentEnd!!, 0.45, rayTraceParticle)

            start.syncLocation {
                val direction = lineCurrentEnd!!.clone().subtract(start).toVector().normalize()
                val distanceToEnd = start.distance(lineCurrentEnd!!)
                raytrace(start, direction, distanceToEnd, getPredicate(player))
            }
        }

        private fun raytrace(start: Location, direction: Vector, reach: Double, predicate: Predicate<Entity>, recursionDepth: Int = 0) {
            if (recursionDepth >= MAX_RECURSION) return

            val raytraceResult: RayTraceResult? = position.world.rayTraceEntities(start, direction, reach, predicate)
            val hitEntity = raytraceResult?.hitEntity ?: return
            val position = raytraceResult.hitPosition.toLocation(hitEntity.world)
            val newReach = start.distance(position)


            if (hitEntity is LivingEntity) {
                if (!AbilityUtil.noDamagePermission(player, hitEntity)) {
                    this.despawn()
                    return
                }
                hitEntity.damage(8.0, player)
                if (Random.nextInt(10) == 0) {
                    hitEntity.world.playSound(hitEntity.location, Sound.BLOCK_AMETHYST_BLOCK_HIT, 0.5f, 0.1f)
                }
            }

            if (newReach > 1) {
                // recursively raytrace until we have no more entities in the way
                raytrace(position, direction, newReach, predicate.and { it != hitEntity }, recursionDepth + 1)
            }
        }

    }



    private enum class ColorKit(
        val rgb: String,
        val materials: List<Material>,
        val colors: List<Color>,
        val chatColors:  List<NamedTextColor>,
        val particleType: Particle = Particle.DUST
    ) {
        RED(
            rgb = "#ff6666",
            materials = listOf(Material.RED_DYE),
            colors = listOf(Color.RED),
            chatColors =  listOf(NamedTextColor.RED),
        ),
        ORANGE(
            rgb = "#ffbd54",
            materials = listOf(Material.ORANGE_DYE),
            colors = listOf(Util.hex2AwtColor("#ff6666")),
            chatColors =  listOf(NamedTextColor.GOLD),
        ),
        YELLOW(
            rgb = "#ffff67",
            materials = listOf(Material.YELLOW_DYE),
            colors = listOf(Util.hex2AwtColor("#ffbd54")),
            chatColors =  listOf(NamedTextColor.YELLOW),
        ),
        GREEN(
            rgb = "#9de24f",
            materials = listOf(Material.LIME_DYE),
            colors = listOf(Util.hex2AwtColor("#9de24f")),
            chatColors =  listOf(NamedTextColor.GREEN),
        ),
        BLUE(
            rgb = "#87cefa",
            materials = listOf(Material.LIGHT_BLUE_DYE),
            colors = listOf(Util.hex2AwtColor("#87cefa")),
            chatColors =  listOf(NamedTextColor.BLUE),
        ),
        PURPLE(
            rgb = "#EA87FA",
            materials = listOf(Material.PURPLE_DYE),
            colors = listOf(Util.hex2AwtColor("#EA87FA")),
            chatColors =  listOf(NamedTextColor.LIGHT_PURPLE),
        ),
        PRISMATIC(
            rgb = "#FAEDCB",
            materials = listOf(Material.RED_DYE, Material.ORANGE_DYE, Material.YELLOW_DYE, Material.LIME_DYE, Material.LIGHT_BLUE_DYE, Material.PURPLE_DYE),
            colors = listOf("#ff6666", "#ffbd54", "#ffff67", "#9de24f", "#87cefa", "#EA87FA").map { Util.hex2AwtColor(it) },
            chatColors =  listOf(NamedTextColor.RED, NamedTextColor.GOLD, NamedTextColor.YELLOW, NamedTextColor.GREEN, NamedTextColor.BLUE, NamedTextColor.LIGHT_PURPLE),
        );

        fun getOrbDecalList(): List<OrbDecals> {
            // Check if the max number of orbs is divisible by the number of materials, colors, and chatColors
            val maxOrbs = MAX_ORBS
            val materialCount = materials.size
            if (maxOrbs % materialCount != 0 || maxOrbs % colors.size != 0 || maxOrbs % chatColors.size != 0) {
                throw IllegalStateException("Max orbs must be divisible by the number of materials, colors, and chatColors.")
            }

            // next, check to see if the number of materials, colors, and chatColors are equal
            if (materials.size != colors.size || materials.size != chatColors.size) {
                throw IllegalStateException("Materials, colors, and chatColors must have the same size.")
            }

            // If it's 1, we return a list of 6 OrbColor objects all with the same values
            // if it's 2, we return a list of 6 OrbColor objects, each with each half having the same values
            // if it's 3, we return a list of 6 OrbColor objects, each with each third having the same values
            // if it's 6, we return a list of 6 OrbColor objects, each with different values

            return (0 until maxOrbs).map { index ->
                val materialIndex = index % materialCount
                val colorIndex = index % colors.size
                val chatColorIndex = index % chatColors.size

                OrbDecals(
                    color = colors[colorIndex],
                    chatColor = chatColors[chatColorIndex],
                    material = materials[materialIndex],
                    particleType = particleType
                )
            }

        }
    }

    private class OrbDecals(
        val color: Color,
        val chatColor:  NamedTextColor,
        val material: Material,
        val particleType: Particle = Particle.DUST
    )
}

// Other raytracing methods suggested/provided to me by nice users on PaperMC:

/*
 * NO RECURSION ALTERNATIVE
 *
 * fun raytraceManual(start: Location, direction: Vector, reach: Double, predicate: Predicate<Entity>) {
 *             val normalizedDirection = direction.clone().normalize()
 *             val stepSize = 0.2 // 0.2 is about as high as i'm willing to go
 *             val stepVector = normalizedDirection.multiply(stepSize)
 *
 *             val currentLocation = start.clone()
 *
 *             val alreadyHit = mutableSetOf<UUID>()
 *
 *             var distanceTraveled = 0.0
 *             while (distanceTraveled < reach) {
 *                 // this is the part that i feel would be much slower without raytracing
 *                 val nearby = start.world.getNearbyEntities(currentLocation, stepSize, stepSize, stepSize)
 *                 for (entity in nearby) {
 *                     if (entity.uniqueId !in alreadyHit && predicate.test(entity)) {
 *                         if (entity is LivingEntity) {
 *                             entity.damage(10.0)
 *                             alreadyHit.add(entity.uniqueId)
 *                         }
 *                     }
 *                 }
 *
 *                 currentLocation.add(stepVector)
 *                 distanceTraveled += stepSize
 *             }
 *         }
 */

/*
 * NO RECURSION/NO LOOP ALTERNATIVE (Java)
 *
 *   public static List<Entity> getIntersecting(Location start, Vector direction, double dist, Predicate<Entity> filter) {
 *     Vector span = direction.normalize().multiply(dist);
 *     World world = start.getWorld();
 *
 *     Vector startVec = start.toVector();
 *     Vector endVec = startVec.clone().add(span);
 *
 *     BoundingBox boundingBox = BoundingBox.of(startVec, endVec);
 *     // Get all entities within the AABB, so we only need to check those
 *     Collection<Entity> entitiesInArea = world.getNearbyEntities(boundingBox, filter);
 *
 *     List<Entity> hitEntities = new ArrayList<>();
 *     for (Entity entity : entitiesInArea) {
 *       BoundingBox entityBox = entity.getBoundingBox();
 *       // Check if the AABB of the entity intersects with the ray in any way
 *       if (intersects(entityBox, startVec, endVec)) {
 *         hitEntities.add(entity);
 *       }
 *     }
 *     return hitEntities;
 *   }
 *
 *   private static final List<Function<Vector, Double>> COMPONENTS = List.of(
 *       Vector::getX,
 *       Vector::getY,
 *       Vector::getZ
 *   );
 *
 *   private static boolean intersects(BoundingBox box, Vector start, Vector end) {
 *     Vector direction = end.clone().subtract(start);
 *     double tMin = 0.0;
 *     double tMax = 1.0;
 *
 *     for (int i = 0; i < 3; i++) {
 *       double startVal = COMPONENTS.get(i).apply(start);
 *       double dir = COMPONENTS.get(i).apply(direction);
 *       double boxMin = COMPONENTS.get(i).apply(box.getMin());
 *       double boxMax = COMPONENTS.get(i).apply(box.getMax());
 *
 *       // Check for parallel vec
 *       if (Math.abs(dir) < 1e-8) {
 *         if (startVal < boxMin || startVal > boxMax) {
 *           return false;
 *         }
 *       } else {
 *         double invDir = 1.0 / dir;
 *         double t1 = (boxMin - startVal) * invDir;
 *         double t2 = (boxMax - startVal) * invDir;
 *
 *         if (t1 > t2) {
 *           double temp = t1;
 *           t1 = t2;
 *           t2 = temp;
 *         }
 *
 *         tMin = Math.max(tMin, t1);
 *         tMax = Math.min(tMax, t2);
 *
 *         if (tMax < tMin) {
 *           return false;
 *         }
 *       }
 *     }
 *
 *     return true;
 *   }
 */