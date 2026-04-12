package dev.lumas.lumaitems.items.weapons.cutlass

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.BukkitVectors
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.Executors
import dev.lumas.lumaitems.util.extensions.isItemInSlot
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.extensions.syncTimer
import dev.lumas.lumaitems.util.extensions.toBukkitColor
import dev.lumas.lumaitems.util.Tier
import kotlin.random.asJavaRandom
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustTransition
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Animals
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.loot.LootContext
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector

class YukonCutlassItem : CustomItemFunctions() {

    companion object {
        private const val MAX_HITS = 3
        private val KEY = Util.namespacedKey("yukon-cutlass")
        private val ENTITY_TYPE_BLACKLIST: Set<EntityType> = setOf(
            EntityType.ENDER_DRAGON, EntityType.WITHER,
            EntityType.ELDER_GUARDIAN, EntityType.WARDEN
        )
        private val DUST_TRANSITIONS = listOf(
            DustTransition("#7284D7".toBukkitColor(), "#7D7FE2".toBukkitColor(), 0.5f),
            DustTransition("#AB5BAC".toBukkitColor(), "#814FA5".toBukkitColor(), 0.5f)
        )
    }

    private val randomContext = random().asJavaRandom()

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#7284D7:#AB5BAC:#814FA5:#7D7FE2>Yukon Cutlass</gradient></b>")
            .customEnchants("<#7D7FE2>Pinata")
            .material(Material.NETHERITE_SWORD)
            .persistentData(KEY)
            .tier(Tier.CHRISTMAS_2025)
            .vanillaEnchants(
                Enchantment.SHARPNESS to 10,
                Enchantment.SWEEPING_EDGE to 5,
                Enchantment.LOOTING to 3,
                Enchantment.MENDING to 1
            )
            .lore(
                "<#7D7FE2>Attack</#7D7FE2> mobs to force",
                "them to drop their",
                "loot table with each",
                "hit from this cutlass.",
                "",
                "Mobs killed by this",
                "cutlass will not drop",
                "any items upon death.",
            )
            .buildPair()
    }


    override fun onEntityDamage(player: Player, event: EntityDamageByEntityEvent) {
        val entity = event.entity as? Mob ?: return
        if (entity.type in ENTITY_TYPE_BLACKLIST) return

        val lootTable = entity.lootTable ?: return
        val hits = Util.getPersistentKey(entity, KEY, PersistentDataType.INTEGER) ?: 0

        if (hits >= MAX_HITS || (entity is Animals && !entity.isAdult)) {
            return
        }

        if (!player.isItemInSlot(KEY, EquipmentSlot.HAND)) {
            return
        }

        Util.setPersistentKey(entity, KEY, PersistentDataType.INTEGER, hits + 1)

        val lootContext = LootContext.Builder(entity.location)
            .killer(player)
            .lootedEntity(entity)
            .build()
        val loots = lootTable.populateLoot(randomContext, lootContext)
        val loc = entity.eyeLocation

        val snowballs: MutableList<Snowball> = mutableListOf()

        for (itemStack in loots) {
            if (itemStack.maxStackSize == 1) {
                continue
            }

            for (i in 0 until itemStack.amount) {
                val goal = BukkitVectors.randomGoalLocation(loc, 0.1, 0.3)

                val vector = BukkitVectors.propelAway(loc, goal,0.1, 0.1)
                snowballs.add(loc.snowball(itemStack, vector, player))
            }
        }
        snowballs.trace()
    }

    override fun onEntityDeath(player: Player, event: EntityDeathEvent) {
        if (event.entityType in ENTITY_TYPE_BLACKLIST) return
        event.drops.removeIf { itemStack ->
            itemStack.maxStackSize > 1
        }
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val snowball = event.entity as? Snowball ?: return
        val surface = event.hitBlockFace

        if (event.hitEntity != null || (snowball.velocity.length() <= 0.2 && surface == BlockFace.UP)) {
            snowball.dropAndRemove()
            event.isCancelled = true
            return
        }

        val vector = BukkitVectors.bounceWithBlockFace(snowball, surface ?: return, 0.6)

        val newBall = snowball.location.snowball(snowball.item, vector, player)
        newBall.trace()
        //snowball.world.playSound(snowball.location, Sound.BLOCK_AMETHYST_BLOCK_STEP, 0.15f, 0.8f)
    }

    private fun Location.snowball(itemStack: ItemStack, velocity: Vector, shooter: Player): Snowball {
        val snowball = this.world.createEntity(this, Snowball::class.java)
        snowball.item = itemStack
        snowball.velocity = velocity
        Util.setPersistentKey(snowball, KEY, PersistentDataType.SHORT, 1)
        snowball.shooter = shooter
        snowball.spawnAt(this)
        return snowball
    }

    private fun MutableCollection<Snowball>.trace() {
        this.syncTimer(0, 1) { task ->
            if (this.isEmpty()) {
                task.cancel()
                return@syncTimer
            }

            val iterator = this.iterator()
            while (iterator.hasNext()) {
                val snowball = iterator.next()
                if (snowball.isDead || !snowball.isValid) {
                    iterator.remove()
                } else if (snowball.isInWater || snowball.isInLava) {
                    snowball.sync { snowball.dropAndRemove() }
                    iterator.remove()
                    continue
                }
                snowball.world.spawnParticle(Particle.DUST, snowball.location, 1,  DUST_TRANSITIONS.random())
            }
        }
    }

    private fun Snowball.trace() {
        Executors.asyncTimer(0, 1) { task ->
            if (this.isDead || !this.isValid) {
                task.cancel()
                return@asyncTimer
            }

            this.world.spawnParticle(Particle.DUST, this.location, 1, DUST_TRANSITIONS.random())
        }
    }

    private fun Snowball.dropAndRemove() {
        this.world.dropItem(this.location, this.item.asOne())
        this.remove()
    }
}