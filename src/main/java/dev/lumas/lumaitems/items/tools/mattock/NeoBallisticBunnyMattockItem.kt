package dev.lumas.lumaitems.items.tools.mattock

import dev.lumas.lumaitems.configuration.files.HeadsYml
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.util.BukkitVectors
import dev.lumas.lumaitems.util.extensions.addCooldown
import dev.lumas.lumaitems.util.extensions.dustOptions
import dev.lumas.lumaitems.util.extensions.isOnCooldown
import dev.lumas.lumaitems.util.extensions.itemStack
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.setPersistentKey
import dev.lumas.lumaitems.util.extensions.setTexture
import dev.lumas.lumaitems.util.Tier
import java.util.UUID
import kotlin.random.Random
import kotlin.random.asJavaRandom
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.jetbrains.annotations.Nullable
import org.joml.AxisAngle4f
import org.joml.Vector3f

class NeoBallisticBunnyMattockItem : CustomItemFunctions() {

    companion object {
        private val KEY = "neo-ballistic-bunny-mattock".namespacedKey()
        private val DUST_OPTIONS = listOf("#CF8EF3", "#FF8BB7", "#F593B5", "#9DD2FF", "#73BBFD")
            .map { it.dustOptions() }
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.Companion.builder()
            .name("<b><gradient:#CF8EF3:#FF8BB7:#F593B5:#9DD2FF:#73BBFD>Neo Ballistic Bunny Mattock</gradient></b>")
            .customEnchants("<#FF8BB7>Eggs Away!")
            .persistentData(KEY)
            .material(Material.NETHERITE_PICKAXE)
            .tier(Tier.WONDERLAND_2026)
            .vanillaEnchants(
                Enchantment.EFFICIENCY to 9,
                Enchantment.UNBREAKING to 10,
                Enchantment.SILK_TOUCH to 1,
                Enchantment.MENDING to 1
            )
            .lore(
                "The third generation of ballistic",
                "bunny tools. Primed and ready",
                "for its next owner.",
                "",
                "<#FF8BB7>Right-click</#FF8BB7> to place an explosive",
                "egg. <#FF8BB7>Hit</#FF8BB7> this egg enough times to",
                "create an explosion based upon",
                "the size of the egg.",
                "",
                "<red>Cooldown: 25s"
            )
            .buildPair()
    }


    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (player.isOnCooldown(this)) return
        player.addCooldown(this, 500)

        val clickedBlock = event.clickedBlock ?: return
        val clickedBlockFace = event.blockFace
        val item = player.inventory.itemInMainHand

        // add 1.0 in the direction of the clicked face to prevent spawning inside the block
        val spawnLocation = clickedBlock.location
            .toCenterLocation()
            .add(clickedBlockFace.direction.multiply(1.0))
            .setDirection(BukkitVectors.WEST)

        val egg = Egg(player.uniqueId, spawnLocation, Egg.Rarity.entries.random())
        egg.place()
        item.damage(40, player)
    }

    override fun onEntityDamagedByPlayer(player: Player, event: EntityDamageByEntityEvent) {
        val interaction = event.entity as? Interaction ?: return

        val egg = Egg.getEgg(interaction) ?: return
        val shell = egg.eggShell

        egg.addCrack()
        if (player.fallDistance > 0.0) {
            egg.addCrack()
            egg.addCrack()
            shell.world.spawnParticle(Particle.CRIT, shell.location, 5, 0.5, 0.5, 0.5, 0.1)
        }
        egg.adjustEggByCracks()

        if (egg.cracks > egg.breakLevel) {
            for (eggi in Egg.getEggs(player.uniqueId)) {
                val loc = eggi.eggShell.location
                loc.world.playSound(loc, Sound.ENTITY_TURTLE_EGG_CRACK, 0.6f, 1.7f)
                loc.world.playSound(loc, Sound.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, 0.4f, 1f)
                loc.world.spawnParticle(Particle.DUST, loc, 20, 0.3, 0.3,  0.3, DUST_OPTIONS.random())

                loc.world.createExplosion(loc, eggi.rarity.power, false, true, player)

                eggi.remove()
            }
        } else {
            shell.world.playSound(shell.location, Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.6f, 9.7f)
        }
    }

    private class Egg {

        companion object {
            private val EASTER_EGGS: MutableSet<Egg> = mutableSetOf()

            private const val EGG_SCALE_FACTOR = 0.1
            private const val EGG_BASE_CRACKS = 1.0
            private const val EGG_BREAK_LEVEL_FACTOR = 0.6

            @Nullable
            fun getEgg(interaction: Interaction): Egg? {
                for (egg in EASTER_EGGS) {
                    if (egg.eggYolk == interaction) {
                        return egg
                    }
                }
                return null
            }

            fun getEggs(owner: UUID): List<Egg> = EASTER_EGGS.filter { it.owner == owner }
        }

        val owner: UUID
        val eggShell: ItemDisplay // ItemDisplay showing egg design (player head)
        val eggYolk: Interaction // Interaction Entity so I can know when a player hits an egg because ItemDisplay's don't have hitboxes

        var cracks: Float = EGG_BASE_CRACKS.toFloat()
            private set
        var breakLevel: Float = EGG_BASE_CRACKS.toFloat() + EGG_BREAK_LEVEL_FACTOR.toFloat()
            private set
        var rarity: Rarity = Rarity.NORMAL
            private set

        constructor(owner: UUID, location: Location, rarity: Rarity) : this(
            owner,
            Material.PLAYER_HEAD.itemStack { meta ->
                val heads = Registry.CONFIGS.getOrThrow(HeadsYml::class).colorfulEasterEggs
                meta.setTexture(heads.random())
            },
            location,
            rarity
        )

        constructor(owner: UUID, itemStack: ItemStack, location: Location, rarity: Rarity) {
            this.owner = owner
            this.eggShell = location.world.createEntity(location, ItemDisplay::class.java)
            this.eggShell.setItemStack(itemStack)

            this.eggYolk = location.world.createEntity(location.add(0.0, -0.41, 0.0), Interaction::class.java)
            this.eggYolk.interactionHeight = 0.5f
            this.eggYolk.interactionWidth = 0.5f
            this.eggYolk.isResponsive = true

            // Prevent saving to disk
            this.eggShell.isPersistent = false
            this.eggYolk.isPersistent = false

            // Identifiers
            this.eggYolk.setPersistentKey(KEY, PersistentDataType.SHORT, 1)
            this.eggShell.setPersistentKey(KEY, PersistentDataType.SHORT, 1)

            this.rarity = rarity;
        }

        fun place() {
            this.eggShell.spawnAt(this.eggShell.location)
            this.eggYolk.spawnAt(this.eggYolk.location)

            EASTER_EGGS.add(this)

            if (rarity == Rarity.NORMAL) {
                return
            }

            // Adjust egg size

            val defaultCracksRange = this.rarity.defaultCracksRange
            val newEggSize = Random.Default.asJavaRandom().nextFloat(defaultCracksRange.first, defaultCracksRange.second)
            this.breakLevel = newEggSize + EGG_BREAK_LEVEL_FACTOR.toFloat()

            val difference: Float = if (newEggSize < EGG_BASE_CRACKS) {
                cracks - newEggSize
            } else {
                newEggSize - cracks
            }

            var i = 0.0
            while (i < difference) {
                if (newEggSize < EGG_BASE_CRACKS) {
                    removeCrack()
                } else {
                    addCrack()
                }
                adjustEggByCracks()
                i += EGG_SCALE_FACTOR
            }
        }


        fun removeCrack() {
            this.cracks -= EGG_SCALE_FACTOR.toFloat()
        }

        fun addCrack() {
            cracks += EGG_SCALE_FACTOR.toFloat()
        }

        fun remove() {
            eggYolk.remove()
            eggShell.remove()
            EASTER_EGGS.remove(this)
        }

        fun transformationRight(axis: Vector, angle: Float) {
            eggShell.interpolationDuration = 40
            eggShell.interpolationDelay = -1
            val transformation: Transformation = eggShell.transformation
            transformation.rightRotation
                .set(AxisAngle4f(angle, axis.x.toFloat(), axis.y.toFloat(), axis.z.toFloat()))
            eggShell.transformation = transformation
        }

        fun transformationLeft(axis: Vector, angle: Float) {
            eggShell.interpolationDuration = 0
            eggShell.interpolationDelay = -1
            val transformation: Transformation = eggShell.transformation
            transformation.leftRotation
                .set(AxisAngle4f(angle, axis.x.toFloat(), axis.y.toFloat(), axis.z.toFloat()))
            eggShell.transformation = transformation
        }

        fun transformationScale(scale: Vector) {
            val isShrinking = scale.x < eggShell.transformation.scale.x

            eggShell.interpolationDuration = 0
            eggShell.interpolationDelay = -1
            val transformation: Transformation = eggShell.transformation
            transformation.scale.set(Vector3f(scale.x.toFloat(), scale.y.toFloat(), scale.z.toFloat()))
            eggShell.transformation = transformation

            var yFactor = 0.05
            if (isShrinking) {
                yFactor *= -1
            }

            eggShell.teleportAsync(eggShell.location.add(0.0, yFactor, 0.0))
            eggYolk.teleportAsync(eggYolk.location.add(0.0, yFactor / 2, 0.0))

            modifyYolkSize(scale)
        }

        fun modifyYolkSize(scale: Vector) { // This is always getting bigger
            eggYolk.interactionWidth = scale.x.toFloat() / 2
            eggYolk.interactionHeight = scale.y.toFloat() / 2
        }

        fun adjustEggByCracks() {
            transformationScale(Vector(cracks.toDouble(), cracks.toDouble(), cracks.toDouble()))
        }

        enum class Rarity(val power: Float, val defaultCracksRange: Pair<Float, Float>) {
            SMALL(3.0f, Pair(0.57f, 0.8f)),
            NORMAL(4.0f, Pair(1.0f, 1.0f)),
            BIG(5.0f, Pair(1.3f, 1.5f))
        }
    }
}