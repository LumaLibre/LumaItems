package dev.lumas.lumaitems.items.weapons.incursion

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.ItemExpiration
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.itemInMainHand
import dev.lumas.lumaitems.util.extensions.sync
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.Event
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

abstract class IncursionEggItem : CustomItemFunctions() {

    companion object {
        private const val COOLDOWN_TICKS = 70
        private const val RADIUS = 4.5
        private const val DAMAGE_FALLOFF = 0.85
        private const val THROW_SPEED = 1.5
    }

    protected abstract val key: String
    protected abstract val displayName: String
    protected abstract val customEnchant: String
    protected abstract val loreLines: List<String>
    protected abstract val material: Material
    protected abstract val burstColor: Color
    protected abstract val damage: Double

    private val burstDust: Particle.DustOptions by lazy { Particle.DustOptions(burstColor, 1.4f) }

    protected open fun applyEffect(target: LivingEntity) {}

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name(displayName)
            .vanillaEnchants(Enchantment.INFINITY to 10)
            .customEnchants(customEnchant)
            .lore(loreLines)
            .material(material)
            .maxStackSize(1)
            .persistentData(key)
            .tier(Tier.LUMARINE_2026)
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (event.item?.isMatchingItem(key) != true) return

        event.setUseItemInHand(Event.Result.DENY)
        event.isCancelled = true

        if (player.hasCooldown(material)) {
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_HAT, 0.7f, 0.5f)
            return
        }
        player.setCooldown(material, COOLDOWN_TICKS)

        val handle = (player as CraftPlayer).handle
        val carried = handle.knownMovement
        val velocity = player.eyeLocation.direction.normalize().multiply(THROW_SPEED)
            .add(Vector(carried.x, if (handle.onGround()) 0.0 else carried.y, carried.z))

        val thrown = player.launchProjectile(Snowball::class.java, velocity)
        thrown.item = ItemStack(material)
        Util.setPersistentKey(thrown, key, PersistentDataType.SHORT, 1)

        player.world.playSound(player.location, Sound.ENTITY_EGG_THROW, 0.8f, 1.1f)
    }

    override fun onPlayerSwapHands(player: Player, event: PlayerSwapHandItemsEvent) {
        val hand = when {
            player.inventory.itemInMainHand.isMatchingItem(key) -> EquipmentSlot.HAND
            player.inventory.itemInOffHand.isMatchingItem(key) -> EquipmentSlot.OFF_HAND
            else -> return
        }

        event.isCancelled = true
        val current = player.inventory.getItem(hand)
        player.inventory.setItem(hand, nextVariant(current) ?: return)
        player.playSound(player, Sound.ENTITY_CHICKEN_EGG, 0.8f, 1.0f)
    }

    private fun nextVariant(item: ItemStack): ItemStack? {
        val next = when (item.type) {
            Material.EGG -> CryoEggItem().createItem().second
            Material.BLUE_EGG -> IncendiaryEggItem().createItem().second
            Material.BROWN_EGG -> UnstableEggItem().createItem().second
            else -> null
        } ?: return null

        ItemExpiration.transfer(item, next)
        return next
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val projectile = event.entity
        val at = when {
            event.hitBlock != null -> projectile.location
            event.hitEntity != null -> event.hitEntity!!.location
            else -> projectile.location
        }.clone()

        projectile.remove()
        detonate(player, at)
    }

    private fun detonate(thrower: Player, at: Location) {
        val world = at.world

        IncursionArsenal.forced(world, Particle.EXPLOSION_EMITTER, at, 1, 0.0, 0.0)
        IncursionArsenal.forced(world, Particle.DUST, at, 45, 0.7, 0.1, burstDust)
        world.playSound(at, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 1.1f)
        world.playSound(at, Sound.ENTITY_CHICKEN_HURT, 1.4f, 0.9f)

        var connected = false
        for (target in IncursionArsenal.targetsAround(thrower, at, RADIUS)) {
            val toTarget: Vector = target.hitbox.center.subtract(at.toVector())
            val distance = toTarget.length()
            if (distance > RADIUS) continue

            //if (distance > 1.0E-4 && !IncursionArsenal.hasClearShot(at, toTarget.clone().multiply(1.0 / distance), distance)) continue

            IncursionArsenal.hurt(target.entity, thrower, damage * (1.0 - (DAMAGE_FALLOFF * (distance / RADIUS)))) {
                applyEffect(it)
            }
            connected = true
        }

        if (connected) thrower.sync { IncursionArsenal.hitFeedback(thrower) }
    }

    override fun onPrepareCraft(player: Player, event: PrepareItemCraftEvent) {
        event.inventory.result = null
    }

    override fun onBlockDispenseItem(event: BlockDispenseEvent) {
        if (event.block.type != Material.DISPENSER) return
        event.isCancelled = true
    }
}
