package dev.lumas.lumaitems.items.weapons.scythe

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.AttributeContainer
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.AbilityUtil
import dev.lumas.lumaitems.util.BukkitVectors
import dev.lumas.lumaitems.util.extensions.QuickTasks
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.Executors
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.extensions.toBukkitColor
import dev.lumas.lumaitems.util.tiers.Tier
import kotlin.random.asJavaRandom
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.bukkit.loot.LootContext
import org.bukkit.loot.LootTable
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector

class HeavyBloblobScytheItem : CustomItemFunctions() {

    companion object {
        private val KEY = Util.namespacedKey("heavy-bloblob-scythe")
        private fun itemStackOf(type: Material): ItemStack {
            return ItemStack.of(type).apply {
                addUnsafeEnchantment(Enchantment.UNBREAKING, 1)
            }
        }
    }


    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.Companion.builder()
            .name("<b><gradient:#90d5ff:#b0ffff:#e4dcff:#b0abff:#7dbfff>Heavy Bloblob Scythe</gradient></b>")
            .customEnchants("<#B0ABFF>Frozen Blobs", "<#8EB8FF>Naturality")
            .material(Material.NETHERITE_HOE)
            .persistentData(KEY)
            .tier(Tier.CHRISTMAS_2025)
            .attributeModifiers(
                AttributeContainer.of(KEY, Attribute.ATTACK_SPEED, AttributeModifier.Operation.ADD_NUMBER, -3.45, EquipmentSlotGroup.ANY)
            )
            .lore(
//                "A frozen scythe that",
//                "launches icy blobs",
//                "that shatter on impact.",
//                "",
                "<#B0ABFF>Left-click</#B0ABFF> with a full",
                "charge to launch a large",
                "spread of icy blobs",
                "that deal damage to any",
                "entity they hit.",
                "",
                "<#8EB8FF>Naturally</#8EB8FF> spawned mobs",
                "will drop more items",
                "upon death when killed",
                "with this scythe."
            )
            .vanillaEnchants(
                Enchantment.SHARPNESS to 9,
                Enchantment.SMITE to 5,
                Enchantment.BANE_OF_ARTHROPODS to 7,
                Enchantment.UNBREAKING to 10,
                Enchantment.LOOTING to 6,
                Enchantment.MENDING to 1
            )
            .buildPair()
    }


    override fun onLeftClick(player: Player, event: PlayerInteractEvent) {
        start(player)
    }

    override fun onEntityDamage(player: Player, event: EntityDamageByEntityEvent) {
        if (event.damageSource.directEntity != player) return
        start(player)
    }

    override fun onEntityDeath(player: Player, event: EntityDeathEvent) {
        val entity = event.entity
        if (entity.fromMobSpawner() || random().nextInt(100) < 10 || entity !is LootTable || !player.inventory.itemInMainHand.isMatchingItem(KEY)) {
            return
        }

        val lootContext = LootContext.Builder(entity.location)
            .killer(player)
            .lootedEntity(entity)
            .build()

        entity.populateLoot(random().asJavaRandom(), lootContext).forEach { itemStack ->
            if (itemStack.maxStackSize > 1 && random().nextBoolean()) {
                itemStack.amount += 1
            }
        }
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val snowball = event.entity as? Snowball ?: return

        (event.hitEntity as? LivingEntity)?.let { hitEntity ->
            if (hitEntity == snowball.shooter || hitEntity.isDead) {
                return@let
            }

            snowball.world.playSound(snowball.location, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 0.45f, 1.5f)
            snowball.world.playSound(snowball.location, Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, 0.7f, 1.2f)
            snowball.world.spawnParticle(Particle.ITEM, snowball.location, 10, 0.5, 0.25, 0.25, 0.15, snowball.item)
            snowball.world.spawnParticle(Particle.SOUL_FIRE_FLAME, snowball.location, 10, 0.5, 0.5, 0.5, 0.15)
            if (!AbilityUtil.noDamagePermission(snowball.shooter as Player, hitEntity)) {
                val damageSource = DamageSource.builder(DamageType.GENERIC)
                    .withDirectEntity(snowball)
                    .withCausingEntity(player)
                    .withDamageLocation(snowball.location)
                    .build()
                hitEntity.damage(18.0, damageSource)
                hitEntity.noDamageTicks = 0
            }
            snowball.remove()
            return
        }

        if (snowball.world != player.world || snowball.location.distance(player.location) > 30 || snowball.velocity.length() <= 0.2) {
            snowball.remove()
            return
        }
        val surface = event.hitBlockFace ?: return
        val newSnowball = cloneSnowball(snowball, BukkitVectors.bounceWithBlockFace(snowball, surface, 0.75))
        newSnowball.spawnAt(snowball.location)
        trackSnowball(newSnowball)

        snowball.world.playSound(snowball.location, Sound.BLOCK_AMETHYST_BLOCK_STEP, 0.15f, 0.8f)
        snowball.world.playSound(snowball.location, Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, 0.4f, 0.8f)
    }

    private fun start(player: Player) {
        // This could be written better
        if (player.attackCooldown < 0.83f || QuickTasks.isOnCooldown(this, player)) return
        QuickTasks.addCooldown(this, player, 25)

        val snowballs = mutableListOf<Snowball>()

        val snowball = player.launchProjectile(Snowball::class.java)
        val originalVelocity = snowball.velocity

        val originalDir = originalVelocity.clone().normalize()
        val newDir = BukkitVectors.rotateVectorY(originalDir, Math.toRadians(random().nextDouble(-5.0, 5.0)))
        snowball.velocity = newDir.multiply(originalVelocity.length()).multiply(0.6)

        snowball.isPersistent = false
        snowball.item = BallAttribute.CENTER.itemStack
        Util.setPersistentKey(snowball, KEY, PersistentDataType.SHORT, 1)
        snowballs.add(snowball)

        for (ballAttr in BallAttribute.entries) {
            if (ballAttr == BallAttribute.CENTER) continue
            val angle = ballAttr.radian
            val newSnowball = cloneSnowball(snowball, BukkitVectors.rotateVectorY(newDir, Math.toRadians(angle)), ballAttr.itemStack)
            newSnowball.spawnAt(snowball.location)
            snowballs.add(newSnowball)
        }

        trackSnowballs(snowballs)
        snowball.world.playSound(snowball.location, Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, 0.4f, 0.8f)
    }

    private fun trackSnowballs(snowballs: MutableCollection<Snowball>) {
        var count = 0
        Executors.asyncTimer(1, 1) { task ->
            val removal = ++count >= 200

            snowballs.removeIf { snowball ->
                if (snowball.isDead || snowball.location.block.isLiquid || removal) {
                    snowball.sync { snowball.remove() }
                    return@removeIf true
                } else {
                    val dustOptions = BallAttribute.dustOptionsOf(snowball.item.type) ?: run {
                        snowball.sync { snowball.remove() }
                        task.cancel()
                        return@removeIf true
                    }
                    snowball.world.spawnParticle(Particle.DUST, snowball.location, 1, 0.05, 0.05, 0.05, 0.1, dustOptions)
                    return@removeIf false
                }
            }

            if (snowballs.isEmpty() || removal) {
                task.cancel()
            }
        }
    }

    private fun trackSnowball(snowball: Snowball) {
        var count = 0
        Executors.asyncTimer(0, 1) { task ->
            if (snowball.isDead || snowball.location.block.isLiquid || ++count >= 200) {
                task.cancel()
                snowball.sync { snowball.remove() }
                return@asyncTimer
            }

            val dustOptions = BallAttribute.dustOptionsOf(snowball.item.type) ?: run {
                task.cancel()
                snowball.sync { snowball.remove() }
                return@asyncTimer
            }
            snowball.world.spawnParticle(Particle.DUST, snowball.location, 1, 0.05, 0.05, 0.05, 0.1, dustOptions)
        }
    }

    private fun cloneSnowball(snowball: Snowball, newVelocity: Vector, newItem: ItemStack? = null): Snowball {
        val newSnowball = snowball.world.createEntity(snowball.location, Snowball::class.java)
        newSnowball.velocity = newVelocity
        newSnowball.shooter = snowball.shooter
        if (newItem != null) {
            newSnowball.item = newItem
        } else {
            newSnowball.item = snowball.item
        }
        newSnowball.setGravity(snowball.hasGravity())
        newSnowball.isPersistent = false
        Util.setPersistentKey(newSnowball, KEY, PersistentDataType.SHORT, 1)
        return newSnowball
    }


    private enum class BallAttribute(
        val radian: Double,
        val itemStack: ItemStack,
        val color: Particle.DustOptions
    ) {
        FAR_LEFT(-10.0, itemStackOf(Material.LIGHT_BLUE_DYE), Particle.DustOptions("#90d5ff".toBukkitColor(), 0.8f)),
        LEFT(-5.0, itemStackOf(Material.WHITE_DYE), Particle.DustOptions(Color.WHITE, 0.8f)),
        CENTER(0.0, itemStackOf(Material.LIGHT_BLUE_DYE), Particle.DustOptions("#90d5ff".toBukkitColor(), 0.8f)),
        RIGHT(5.0, itemStackOf(Material.WHITE_DYE), Particle.DustOptions(Color.WHITE, 0.8f)),
        FAR_RIGHT(10.0, itemStackOf(Material.LIGHT_BLUE_DYE), Particle.DustOptions("#90d5ff".toBukkitColor(), 0.8f))
        ;

        companion object {
            fun dustOptionsOf(material: Material): Particle.DustOptions? {
                return BallAttribute.entries.firstOrNull { it.itemStack.type == material }?.color
            }
        }
    }
}