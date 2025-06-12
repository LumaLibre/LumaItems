package dev.jsinco.luma.lumaitems.items.weapons

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.particles.ParticleDisplay
import dev.jsinco.luma.lumaitems.particles.Particles
import dev.jsinco.luma.lumaitems.util.AbilityUtil
import dev.jsinco.luma.lumaitems.util.Executors
import dev.jsinco.luma.lumaitems.util.PersistentDataRecord
import dev.jsinco.luma.lumaitems.util.QuickTasks
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.disabling.Disable
import dev.jsinco.luma.lumaitems.util.disabling.WorldName
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import java.awt.Color
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.BiConsumer
import java.util.function.Predicate
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector

// TODO: Needs to ignore friendlies unless targeted
@Disable(value = [WorldName.PINATA, WorldName.SPAWN])
class UnnamedWeaponItem : CustomItemFunctions() {

    companion object {
        private const val RANGE = 45.0
        private const val CIRCLE_RADIUS = 1.0
        private const val CIRCLE_RATE = 3
        private const val MAX_ORBS = 6

        private val UP = Vector(0, 1, 0)
        private val STOP = Vector(0, 0, 0)

        private val nameSpace = Util.namespacedKey("unnamed-weapon2")
        private val glowLib = LumaItems.getGlowingEntities()
        private val processes: MutableSet<Process> = mutableSetOf()
    }


    override fun createItem(): Pair<String, ItemStack> {
        val colorKit = ColorKit.entries.random()
        val c = colorKit.rgb
        return ItemFactory.builder()
            .name("<b>${colorKit.gradient}Unnamed Weapon2")
            .customEnchants("${colorKit.rgb}Glass Cannon")
            .material(Material.NETHERITE_HOE)
            .persistentData(nameSpace)
            .lore(
                "${c}Right-click <white>to summon ${c}6",
                "glass cannon orbs that will",
                "track and kill nearby entities.",
            )
            .vanillaEnchants(Enchantment.MENDING to 1, Enchantment.UNBREAKING to 10, Enchantment.SHARPNESS to 6)
            .tier(Tier.DEBUG)
            .persistentDataRecords(
                PersistentDataRecord.create(nameSpace, PersistentDataType.STRING, colorKit.name)
            )
            .buildPair()
    }


    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (QuickTasks.isOnCooldown(this, player)) return

        val item = event.item ?: return
        val colorKitString = item.persistentDataContainer.get(nameSpace, PersistentDataType.STRING)
        val colorKit = colorKitString?.let { Util.enumValueOfOrNull(ColorKit::class.java, it) } ?: ColorKit.RED

        val process = Process(player)
        val anyOrbsFired = process.start(colorKit)

        QuickTasks.addCooldown(this, player, 100) {
            if (anyOrbsFired.get()) {
                player.playSound(player.location, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f)
                QuickTasks.addCooldown(this, player, 600)
            }
        }
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val hitEntity = event.hitEntity as? LivingEntity ?: return
        hitEntity.damage(30.0, player)
    }

    override fun onPluginDisable() {
        processes.forEach {
            it.cleanupAndStop()
        }
    }


    class Process(val player: Player) {

        private var scheduledTask: ScheduledTask? = null
        private var orbGroup: OrbGroup? = null

        fun start(colorKit: ColorKit): AtomicBoolean {
            if (scheduledTask != null) {
                throw IllegalStateException("Process already started or not initialized properly.")
            }
            val targetEntity = player.getTargetEntity(RANGE.toInt()) as? LivingEntity

            val orbGroup = OrbGroup(player, colorKit, CIRCLE_RADIUS, CIRCLE_RATE, targetEntity)
            this.orbGroup = orbGroup
            orbGroup.create()

            var count: Long = 0
            val anyOrbsFired: AtomicBoolean = AtomicBoolean(false)
            this.scheduledTask = Executors.asyncTimer(0, 1) { task ->
                if (count >= 500) {
                    task.cancel()
                    Executors.sync { orbGroup.destroy() }
                    processes.remove(this)
                    return@asyncTimer
                }


                orbGroup.moveGroup(count)
                var anyFiring = false

                orbGroup.orbs.forEach { orb ->
                    if (orb.fireTime < count) {
                        anyFiring = true
                        if (!anyOrbsFired.get()) {
                            anyOrbsFired.set(true)
                        }
                        if (orb.target == null || orb.target!!.isDead) {
                            orb.autoTarget(orbGroup.activeTargets)
                        }

                        orb.drawLine()
                    }
                }

                if (anyFiring) {
                    orbGroup.pruneTargets()
                }
                count++
            }
            processes.add(this)
            return anyOrbsFired
        }

        fun cleanupAndStop() {
            orbGroup?.destroy()
            scheduledTask?.cancel()
            processes.remove(this)
        }
    }


    class OrbGroup(
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


        fun create() {
            val location = player.eyeLocation.clone().add(player.eyeLocation.direction.clone().multiply(DIRECTION_FACTOR))
            // Groups of two, so two orbs at a time should have the same fireTime, then add 50
            calculateLocations(location) { index, loc ->
                if (orbs.size >= MAX_ORBS) return@calculateLocations // limit to 6 orbs
                val fireTime = if (initialTarget == null) {
                    50L + (index / 2) * 15 // every two orbs have the same fireTime
                } else {
                    15L
                }
                val orb = Orb(initialTarget, player, colorKit, loc, fireTime, DIRECTION_FACTOR)
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
                val baseLoc = player.eyeLocation.clone().add(player.eyeLocation.direction.clone().multiply(DIRECTION_FACTOR))
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

        private fun calculateLocations(point: Location, consumer: BiConsumer<Int, Location>) {
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
            }
        }
    }


    class Orb(
        var target: LivingEntity?,
        val player: Player,
        val colorKit: ColorKit,
        var position: Location,
        val fireTime: Long = 50,
        val directionFactor: Double
    ) {
        companion object {
            private const val SPEED = 0.45
            private const val SPEED_MULTIPLIER = 2.5
            private const val SNAP = 0.5

            private fun getPredicate(player: Player): Predicate<Entity> {
                return Predicate { entity ->
                    entity is LivingEntity && entity != player
                            && player.canSee(entity) && !AbilityUtil.noDamagePermission(player, entity)
                }
            }
        }

        private val rayTraceParticle = ParticleDisplay.of(colorKit.particleType).withColor(colorKit.color)
        private val itemStack = ItemStack.of(colorKit.material)

        private val spawnLocation = player.eyeLocation.clone().add(player.eyeLocation.direction.clone().multiply(directionFactor))

        lateinit var snowball: Snowball
        private var valid = true
        private var lineCurrentEnd: Location? = null

        init {
            if (target != null) {
                glowLib.setGlowing(target!!, player, colorKit.chatColor)
            }
        }

        private fun createSnowball(location: Location) {
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
            Executors.sync {
                createSnowball(spawnLocation)
            }
        }

        fun despawn() {
            this.valid = false
            if (!snowball.isDead) {
                snowball.setGravity(true)
            }
            if (target != null && target?.isDead == false) {
                glowLib.unsetGlowing(target!!, player)
            }
        }

        fun move() {
            var smallCount = 40
            Executors.syncTimer(0, 1) { task ->
                if (snowball.isDead) {
                    if (valid) {
                        createSnowball(snowball.location)
                    } else {
                        task.cancel()
                        return@syncTimer
                    }
                } else if (snowball.location.distance(position) <= 0.1) {
                    snowball.velocity = STOP
                    task.cancel()
                    return@syncTimer
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

        fun autoTarget(activeTargets: LinkedHashMap<LivingEntity, Int>) {
            if (!valid) return
            fun setTarget(entity: LivingEntity) {
                this.target = entity
                if (activeTargets.containsKey(entity)) {
                    activeTargets[entity] = activeTargets.getOrDefault(entity, 0) + 1
                } else {
                    activeTargets[entity] = 1
                }
                glowLib.setGlowing(entity, player, colorKit.chatColor)
            }

            Executors.sync {
                val nearbyEntities = position.getNearbyLivingEntities(RANGE.minus(20.0))
                    .filter { entity -> getPredicate(player).test(entity) }
                    .filter { entity -> activeTargets.getOrDefault(entity, 0) < 2 }
                    .sortedBy { it.location.distance(player.location) }
                if (nearbyEntities.isEmpty()) {
                    if (activeTargets.isEmpty()) {
                        despawn()
                        return@sync
                    }
                    setTarget(activeTargets.keys.first())
                } else {
                    setTarget(nearbyEntities.first())
                }

            }

        }

        fun drawLine() {
            if (!valid || target == null) return

            val targetLoc = target!!.boundingBox.center.toLocation(target!!.world).clone()
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

            Executors.sync {
                val direction = lineCurrentEnd!!.clone().subtract(start).toVector().normalize()
                val distanceToEnd = start.distance(lineCurrentEnd!!)
                raytrace(start, direction, distanceToEnd, getPredicate(player))
            }
        }

        private fun raytrace(start: Location, direction: Vector, reach: Double, predicate: Predicate<Entity>) {
            val raytraceResult = position.world.rayTraceEntities(start, direction, reach, predicate)
            val hitEntity = raytraceResult?.hitEntity ?: return
            val position = raytraceResult.hitPosition.toLocation(hitEntity.world)
            val newReach = start.distance(position)
            if (newReach > 1) {
                // recursively raytrace until we have no more entities in the way
                raytrace(position, direction, newReach, predicate.and { it != hitEntity })
            }

            if (hitEntity is LivingEntity) {
                hitEntity.damage(8.0, player)
            }
        }

    }


    enum class ColorKit(
        val gradient: String,
        val rgb: String,
        val material: Material,
        val color: Color,
        val chatColor: ChatColor,
        val particleType: Particle = Particle.DUST
    ) {
        RED(
            gradient = "<gradient:#E94A4A:#FF6868:#F54EB3>",
            rgb = "<#FF6868>",
            material = Material.RED_DYE,
            color = Util.hex2AwtColor("#E94A4A"),
            chatColor = ChatColor.RED,
        ),
        WHITE(
            gradient = "<gradient:#FFFFFF:#F0F0F0:#E0E0E0>",
            rgb = "<#FFFFFF>",
            material = Material.NETHER_STAR,
            color = Color.WHITE /*Util.hex2AwtColor("#FFFFFF")*/,
            chatColor = ChatColor.WHITE,
        ),
        PURPLE(
            gradient = "<gradient:#AD76E3:#BF9BDD:#F56868>",
            rgb = "<#BF9BDD>",
            material = Material.LARGE_AMETHYST_BUD,
            color = Util.hex2AwtColor("#AD76E3"),
            chatColor = ChatColor.LIGHT_PURPLE,
        )
    }
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