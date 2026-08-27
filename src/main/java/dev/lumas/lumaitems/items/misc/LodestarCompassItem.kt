package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.actionBar
import dev.lumas.lumaitems.util.extensions.addCooldown
import dev.lumas.lumaitems.util.extensions.formatSnakeCase
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.isOnCooldown
import dev.lumas.lumaitems.util.extensions.isTagged
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.extensions.syncTimer
import dev.lumas.lumaitems.util.extensions.toBukkitColor
import net.minecraft.core.Holder
import net.minecraft.core.QuartPos
import net.minecraft.resources.Identifier
import net.minecraft.world.level.biome.Climate
import org.bukkit.HeightMap
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.World
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.CompassMeta
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt
import net.minecraft.world.level.biome.Biome as MinecraftBiome

class LodestarCompassItem : CustomItemFunctions() {

    private companion object {
        private const val KEY = "lodestar-compass"
        private val KEY_NS = KEY.namespacedKey()

        private const val SCAN_RADIUS = 1000
        private const val COARSE_STEP = 16 // quart
        private const val REFINE_RADIUS = COARSE_STEP
        private const val MIN_TARGET_DISTANCE = 300

        private const val MAX_TARGETS = 12

        private const val RESTALE_DISTANCE = 4000.0

        private const val COOLDOWN_TICKS = 20L

        private const val SWEEP_RADIUS = 75
        private const val SWEEP_RINGS_PER_TICK = 1
        private const val SWEEP_INITIAL_RINGS = 6
        private const val SWEEP_PARTICLE_OFFSET = 1.0
        private const val SWEEP_ACCENT_STRIDE = 11
        private val RING_OFFSETS: Array<IntArray> = buildRingOffsets()

        private fun buildRingOffsets(): Array<IntArray> {
            val buckets = Array(SWEEP_RADIUS + 1) { mutableListOf<Int>() }

            for (dx in -SWEEP_RADIUS..SWEEP_RADIUS) {
                for (dz in -SWEEP_RADIUS..SWEEP_RADIUS) {
                    val ring = sqrt((dx * dx + dz * dz).toDouble()).roundToInt()
                    if (ring > SWEEP_RADIUS) continue

                    buckets[ring].add(dx)
                    buckets[ring].add(dz)
                }
            }
            return Array(buckets.size) { buckets[it].toIntArray() }
        }

        private val SWEEP_DUST = Particle.DustTransition(
            "#00E5FF".toBukkitColor(),
            "#0033CC".toBukkitColor(),
            1.5f
        )
        private const val SURFACE_SEARCH_DEPTH = 32
        private const val LIQUID_SURFACE = 1.4
        private const val FULL_FOOTPRINT = 0.99
        private val CANOPY = setOf(
            Material.BROWN_MUSHROOM_BLOCK,
            Material.RED_MUSHROOM_BLOCK,
            Material.MUSHROOM_STEM,
            Material.BEE_NEST,
            Material.BEEHIVE
        )

        private const val ACCENT = "#9be4df"
    }

    private val surveys = ConcurrentHashMap<UUID, Survey>()
    private val surveying = ConcurrentHashMap.newKeySet<UUID>()

    override fun createItem(): Pair<String, ItemStack> {
        return KEY to build(null, 0, 0)
    }

    override fun onLeftClick(player: Player, event: PlayerInteractEvent) {
        event.item?.takeIf { it.isMatchingItem(KEY_NS) } ?: return
        if (event.hand != EquipmentSlot.HAND && event.hand != EquipmentSlot.OFF_HAND) return

        event.isCancelled = true
        if (player.isOnCooldown(this)) return
        player.addCooldown(this, COOLDOWN_TICKS)

        this.beginSurvey(player)
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        event.item?.takeIf { it.isMatchingItem(KEY_NS) } ?: return
        if (event.hand != EquipmentSlot.HAND && event.hand != EquipmentSlot.OFF_HAND) return

        val clicked = event.clickedBlock
        if (clicked != null) {
            if (clicked.type == Material.LODESTONE) {
                event.isCancelled = true
                return
            } else if (@Suppress("DEPRECATION") clicked.type.isInteractable && !player.isSneaking) { // deprecated, no replacement
                return
            }
        }

        event.isCancelled = true
        if (player.isOnCooldown(this)) return

        val survey = this.surveys[player.uniqueId]
        if (survey == null || survey.targets.isEmpty()) {
            player.actionBar("<$ACCENT>Left-click to scan the area first.")
            return
        }

        if (survey.isStale(player.location)) {
            player.actionBar("<$ACCENT>This reading is far behind you. Left-click to scan again.")
            return
        }

        player.addCooldown(this, COOLDOWN_TICKS)
        survey.index = (survey.index + 1) % survey.targets.size
        this.applySurvey(player, survey)
    }

    override fun onPlayerQuit(player: Player, event: PlayerQuitEvent) {
        this.surveys.remove(player.uniqueId)
        this.surveying.remove(player.uniqueId)
    }

    override fun onPluginDisableGlobal() {
        this.surveys.clear()
        this.surveying.clear()
    }

    private fun beginSurvey(player: Player) {
        if (!this.surveying.add(player.uniqueId)) return

        val origin = player.location.clone()
        player.actionBar("<$ACCENT>Scanning area...")
        player.world.playSound(origin, Sound.ITEM_SPYGLASS_USE, 0.7f, 1.4f)
        val sweep = this.sweepTerrain(player, origin)

        async {
            val targets = try {
                surveyBiomes(origin)
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
                emptyList()
            }

            sweep.thenRun {
                player.sync {
                    surveying.remove(player.uniqueId)
                    if (!player.isValid) return@sync

                    if (targets.isEmpty()) {
                        player.actionBar("<red>Nothing but more of the same.")
                        return@sync
                    }

                    val survey = Survey(origin, targets)
                    surveys[player.uniqueId] = survey
                    applySurvey(player, survey)
                }
            }
        }
    }

    private fun sweepTerrain(player: Player, origin: Location): CompletableFuture<Void> {
        val finished = CompletableFuture<Void>()
        val world = origin.world ?: return finished.also { it.complete(null) }

        val baseX = origin.blockX
        val baseZ = origin.blockZ

        var ring = 0
        val scheduled = player.syncTimer(0, 1) { task ->
            if (!player.isValid || player.world != world) {
                task.cancel()
                finished.complete(null)
                return@syncTimer
            }

            var emitted = 0
            val budget = if (ring == 0) SWEEP_INITIAL_RINGS else SWEEP_RINGS_PER_TICK
            while (emitted < budget && ring <= SWEEP_RADIUS) {
                emitRing(world, ring, baseX, baseZ, player)
                ring++
                emitted++
            }

            if (ring > SWEEP_RADIUS) {
                task.cancel()
                finished.complete(null)
            }
        }

        if (scheduled == null) {
            finished.complete(null)
        }
        return finished
    }

    private fun emitRing(world: World, ring: Int, baseX: Int, baseZ: Int, player: Player) {
        val offsets = RING_OFFSETS[ring]
        var i = 0
        var index = 0

        while (i < offsets.size) {
            val x = baseX + offsets[i]
            val z = baseZ + offsets[i + 1]
            i += 2
            index++

            if (!world.isChunkLoaded(x shr 4, z shr 4)) continue

            val surface = surfaceHeight(world, x, z)
            if (surface.isNaN()) continue

            val px = x + 0.5
            val py = surface + SWEEP_PARTICLE_OFFSET
            val pz = z + 0.5

            player.spawnParticle(
                Particle.DUST_COLOR_TRANSITION, px, py, pz,
                1, 0.0, 0.0, 0.0, 0.0, SWEEP_DUST, true
            )

            if ((index + ring) % SWEEP_ACCENT_STRIDE == 0) {
                player.spawnParticle(
                    Particle.GLOW, px, py + 0.25, pz,
                    1, 0.0, 0.0, 0.0, 0.0, null, true
                )
            }
        }
    }

    private fun surfaceHeight(world: World, x: Int, z: Int): Double {
        val top = world.getHighestBlockYAt(x, z, HeightMap.WORLD_SURFACE) - 1
        if (top < world.minHeight) return Double.NaN

        val limit = max(world.minHeight, top - SURFACE_SEARCH_DEPTH)
        var y = top
        var covered = false

        while (y >= limit) {
            val block = world.getBlockAt(x, y, z)

            if (!covered) {
                if (block.isLiquid) return y + LIQUID_SURFACE
                if (isGround(block)) return block.boundingBox.maxY
            }

            covered = isFullBlock(block)
            y--
        }
        return Double.NaN
    }

    private fun isGround(block: Block): Boolean {
        return isFullBlock(block)
                && !block.isTagged(Tag.LEAVES, Tag.LOGS)
                && block.type !in CANOPY
    }

    private fun isFullBlock(block: Block): Boolean {
        val box = block.boundingBox
        return box.maxY > box.minY
                && box.widthX >= FULL_FOOTPRINT
                && box.widthZ >= FULL_FOOTPRINT
    }

    private fun surveyBiomes(origin: Location): List<BiomeTarget> {
        val world = origin.world ?: return emptyList()
        val level = (world as CraftWorld).handle
        val source = level.chunkSource.generator.biomeSource
        val sampler = level.chunkSource.randomState().sampler()

        val originX = origin.blockX
        val originZ = origin.blockZ
        val sampleY = sampleHeight(world, origin.blockY)
        val quartY = QuartPos.fromBlock(sampleY)

        val standingIn = source.getNoiseBiome(
            QuartPos.fromBlock(originX), quartY, QuartPos.fromBlock(originZ), sampler
        )

        val radiusSquared = (SCAN_RADIUS.toLong() * SCAN_RADIUS).toDouble()
        val minSquared = (MIN_TARGET_DISTANCE.toLong() * MIN_TARGET_DISTANCE).toDouble()
        val nearest = HashMap<Holder<MinecraftBiome>, BiomeTarget>()

        var x = originX - SCAN_RADIUS
        while (x <= originX + SCAN_RADIUS) {
            val dx = (x - originX).toDouble()
            val quartX = QuartPos.fromBlock(x)

            var z = originZ - SCAN_RADIUS
            while (z <= originZ + SCAN_RADIUS) {
                val dz = (z - originZ).toDouble()
                val distanceSquared = (dx * dx) + (dz * dz)

                if (distanceSquared in minSquared..radiusSquared) {
                    val biome = source.getNoiseBiome(quartX, quartY, QuartPos.fromBlock(z), sampler)

                    if (biome != standingIn) {
                        val current = nearest[biome]
                        if (current == null || distanceSquared < current.distanceSquared) {
                            nearest[biome] = BiomeTarget(biome, x, sampleY, z, distanceSquared)
                        }
                    }
                }
                z += COARSE_STEP
            }
            x += COARSE_STEP
        }

        return nearest.values
            .map { refine(it, source, sampler, quartY, originX, originZ, minSquared) }
            .sortedBy { it.distanceSquared }
            .take(MAX_TARGETS)
    }

    private fun refine(target: BiomeTarget, source: net.minecraft.world.level.biome.BiomeSource, sampler: Climate.Sampler, quartY: Int, originX: Int, originZ: Int, minSquared: Double): BiomeTarget {
        var best = target

        var x = target.x - REFINE_RADIUS
        while (x <= target.x + REFINE_RADIUS) {
            val dx = (x - originX).toDouble()
            val quartX = QuartPos.fromBlock(x)

            var z = target.z - REFINE_RADIUS
            while (z <= target.z + REFINE_RADIUS) {
                val dz = (z - originZ).toDouble()
                val distanceSquared = (dx * dx) + (dz * dz)

                if (distanceSquared >= minSquared && distanceSquared < best.distanceSquared) {
                    if (source.getNoiseBiome(quartX, quartY, QuartPos.fromBlock(z), sampler) == target.biome) {
                        best = BiomeTarget(target.biome, x, target.y, z, distanceSquared)
                    }
                }
                z += 4
            }
            x += 4
        }
        return best
    }
    
    private fun sampleHeight(world: World, playerY: Int): Int {
        val y = if (world.environment == World.Environment.NORMAL) {
            max(playerY, world.seaLevel)
        } else {
            playerY
        }
        return y.coerceIn(world.minHeight, world.maxHeight - 1)
    }

    private fun applySurvey(player: Player, survey: Survey) {
        val target = survey.targets.getOrNull(survey.index) ?: return
        val world = survey.origin.world ?: return
        val replaced = this.replaceHeldItem(player, this.build(target, survey.index, survey.targets.size)) { item ->
            (item.itemMeta as? CompassMeta)?.let { meta ->
                meta.lodestone = Location(world, target.x + 0.5, target.y.toDouble(), target.z + 0.5)
                meta.isLodestoneTracked = false
                item.itemMeta = meta
            }
        }

        if (!replaced) return

        val origin = target.origin()?.let { " <dark_gray>[$it]</dark_gray>" } ?: ""
        player.actionBar(
            "<$ACCENT>${target.displayName()}</$ACCENT>$origin <gray>•</gray> ${target.distance()} blocks " +
                    "<gray>(${survey.index + 1}/${survey.targets.size})</gray>"
        )
        player.playSound(player.location, Sound.UI_BUTTON_CLICK, 0.5f, 1.6f)
    }

    private fun replaceHeldItem(player: Player, newItem: ItemStack, edit: (ItemStack) -> Unit): Boolean {
        val inventory = player.inventory

        if (inventory.itemInMainHand.isMatchingItem(KEY_NS)) {
            edit(newItem)
            inventory.setItemInMainHand(newItem)
            return true
        }
        if (inventory.itemInOffHand.isMatchingItem(KEY_NS)) {
            edit(newItem)
            inventory.setItemInOffHand(newItem)
            return true
        }
        return false
    }

    private fun build(target: BiomeTarget?, index: Int, total: Int): ItemStack {
        val status = if (target == null) {
            listOf("", "<dark_gray>No biomes scanned yet.")
        } else {
            listOfNotNull(
                "",
                "<gray>Watching <$ACCENT>${target.displayName()}",
                target.origin()?.let { "<gray>Charted by <dark_gray>$it" },
                //"<gray>Distance <$ACCENT>${target.distance()}</$ACCENT> <gray>blocks",
                "<dark_gray>${index + 1} of $total scanned"
            )
        }

        return ItemFactory.builder()
            .name("<b><gradient:#FFBCE2:#FAEDCB:#DBCDF0>Lodestar Compass</gradient></b>")
            .customEnchants("<#DBCDF0>Climate Point")
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            //.hideEnchants(true)
            .material(Material.COMPASS)
            .persistentData(KEY)
            .maxStackSize(1)
            .tier(Tier.LUMARINE_2026)
            .lore(
                mutableListOf(
                    "<#DBCDF0>Left-click</#DBCDF0> to scan biomes",
                    "within <#DBCDF0>${String.format("%,d", SCAN_RADIUS)}</#DBCDF0> blocks.",
                    "",
                    "<#DBCDF0>Right-click</#DBCDF0> to read the",
                    "next biome found.",
                ).apply {
                    addAll(status)
                }
            )
            .buildItem()
    }

    private class Survey(val origin: Location, val targets: List<BiomeTarget>) {
        var index: Int = 0

        fun isStale(now: Location): Boolean {
            return now.world != origin.world || now.distanceSquared(origin) > RESTALE_DISTANCE * RESTALE_DISTANCE
        }
    }

    private class BiomeTarget(
        val biome: Holder<MinecraftBiome>,
        val x: Int,
        val y: Int,
        val z: Int,
        val distanceSquared: Double
    ) {

        fun displayName(): String {
            val key = biome.unwrapKey().orElse(null) ?: return "Unknown Biome"
            return key.identifier().path.formatSnakeCase()
        }

        fun origin(): String? {
            val namespace = biome.unwrapKey().orElse(null)?.identifier()?.namespace ?: return null
            return namespace.takeUnless { it == Identifier.DEFAULT_NAMESPACE }
        }

        fun distance(): Int = sqrt(distanceSquared).roundToInt()
    }
}
