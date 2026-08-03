package dev.lumas.lumaitems.items.misc

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import dev.lumas.lumaitems.model.item.AttributeContainer
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.PaperDataComponent
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.isItemInSlot
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.extensions.syncTimer
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.Equippable
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.phys.Vec3
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Registry
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Directional
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.block.data.CraftBlockData
import org.bukkit.craftbukkit.entity.CraftCreeper
import org.bukkit.craftbukkit.entity.CraftMob
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Creeper
import org.bukkit.entity.Entity
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.entity.ShulkerBullet
import org.bukkit.entity.Warden
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class SmugglersCaskItem : CustomItemFunctions() {

    private companion object {
        val KEY = "smugglers-cask".namespacedKey()

        val DISGUISE_ATTRIBUTES = listOf(
            Attribute.CAMERA_DISTANCE,
            Attribute.MOVEMENT_SPEED,
            Attribute.JUMP_STRENGTH,
            Attribute.SCALE,
        )

        const val MOB_TARGET_FORGET_RADIUS = 32.0
        val TARGET_MEMORIES = listOf(
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.ROAR_TARGET,
        )
        const val DISGUISED_SCALE = 0.35

        val BARREL_DATA: BlockData = (Material.BARREL.createBlockData() as Directional).apply { facing = BlockFace.UP }
        const val FAKE_BARREL_VIEW_RADIUS = 64.0
        const val VIEWER_REFRESH_TICKS = 20L

        val INVISIBILITY = PotionEffect(
            PotionEffectType.INVISIBILITY,
            (VIEWER_REFRESH_TICKS * 5).toInt(),
            0,
            true,
            false,
            false
        )
    }

    private val disguises: MutableMap<UUID, Disguise> = ConcurrentHashMap()
    private val armed: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#8c5a2b:#b07d43:#d9a566:#b07d43:#6f4520>Smuggler's Cask</gradient></b>")
            .customEnchants("<gray>Unbreaking X", "<#d9a566>Stowaway")
            .material(Material.BARREL)
            .maxStackSize(1)
            .persistentData(KEY)
            .tier(Tier.LUMARINE_2026)
            .lore(
                "Half the contraband in",
                "the harbour crossed the",
                "docks inside one of these.",
                "",
                "Wear it on your head and",
                "<#d9a566>sneak</#d9a566> to hide from hostile",
                "mobs and angry guards.",
            )
            .paperDataComponents(PaperDataComponent.valued(
                DataComponentTypes.EQUIPPABLE, Equippable.equippable(EquipmentSlot.HEAD)
                    .apply { Registry.SOUNDS.getKey(Sound.BLOCK_BARREL_CLOSE)?.let { equipSound(it) } }
                    .build()
            ))
            .buildPair()
    }

    override fun onPlayerCrouch(player: Player, event: PlayerToggleSneakEvent) {
        if (event.isSneaking) {
            if (!player.isItemInSlot(KEY, EquipmentSlot.HEAD)) return
            if (!disguise(player)) armed.add(player.uniqueId)
        } else {
            undisguise(player)
        }
    }

    override fun onRunnable(player: Player) {
        val active = disguises[player.uniqueId] ?: run {
            tryClosingLid(player, player.location)
            return
        }
        if (!player.isSneaking || !player.isItemInSlot(KEY, EquipmentSlot.HEAD) || player.location.block != active.block) {
            undisguise(player)
        }
    }

    override fun onMove(player: Player, event: PlayerMoveEvent) {
        val active = disguises[player.uniqueId] ?: run {
            if (!event.hasChangedBlock()) return
            tryClosingLid(player, event.to) ?: return
        }
        event.to = active.anchor.clone().apply {
            yaw = event.to.yaw
            pitch = event.to.pitch
        }
    }

    override fun onEntityTargetPlayer(player: Player, event: EntityTargetLivingEntityEvent) {
        if (!disguises.containsKey(player.uniqueId)) return
        event.isCancelled = true
    }

    override fun onArmorChange(player: Player, event: PlayerArmorChangeEvent) {
        if (event.newItem.isMatchingItem(KEY)) return
        undisguise(player)
    }

    override fun onPlayerTeleport(player: Player, event: PlayerTeleportEvent) {
        val disguise = disguises[player.uniqueId] ?: return
        if (event.to.block == disguise.block) return
        undisguise(player)
    }

    override fun onPlayerDeath(player: Player, event: PlayerDeathEvent) {
        undisguise(player, effects = false)
    }

    override fun onPlayerQuit(player: Player, event: PlayerQuitEvent) {
        undisguise(player, effects = false)
    }

    override fun onPluginDisableGlobal() {
        for (uuid in disguises.keys) {
            val player = Bukkit.getPlayer(uuid) ?: continue
            undisguise(player, effects = false)
        }
        disguises.values.forEach {
            it.task.cancel()
            it.barrel.despawn()
        }
        disguises.clear()
        armed.clear()
    }

    override fun onPlaceBlock(player: Player, event: BlockPlaceEvent) {
        if (!event.itemInHand.isMatchingItem(KEY)) return
        event.isCancelled = true
    }

    // Returns false when there is nothing solid for the barrel to stand on yet
    private fun disguise(player: Player, at: Location = player.location): Boolean {
        if (disguises.containsKey(player.uniqueId)) return true

        val block = at.block
        if (!block.getRelative(BlockFace.DOWN).isSolid) return false

        val centered = block.location.add(0.5, 0.0, 0.5).apply {
            yaw = player.location.yaw
            pitch = player.location.pitch
        }
        player.teleportAsync(centered)

        val barrel = ClientSideBarrelEntity(block.location)

        clearModifiers(player)
        applyModifier(player, Attribute.CAMERA_DISTANCE, AttributeModifier.Operation.ADD_NUMBER, 32.0) // max
        applyModifier(player, Attribute.MOVEMENT_SPEED, AttributeModifier.Operation.MULTIPLY_SCALAR_1, -1.0)
        applyModifier(player, Attribute.JUMP_STRENGTH, AttributeModifier.Operation.MULTIPLY_SCALAR_1, -1.0)
        applyScaleModifier(player)

        val task = block.location.syncTimer(VIEWER_REFRESH_TICKS, VIEWER_REFRESH_TICKS) {
            refresh(player, barrel)
        }

        disguises[player.uniqueId] = Disguise(block, centered, barrel, task)

        refresh(player, barrel)
        playEffects(centered, Sound.BLOCK_BARREL_CLOSE, 0.8f)
        return true
    }

    // Closes an armed barrel when [at] has something solid below
    private fun tryClosingLid(player: Player, at: Location): Disguise? {
        if (!armed.contains(player.uniqueId)) return null
        if (!disguise(player, at)) return null

        armed.remove(player.uniqueId)
        return disguises[player.uniqueId]
    }

    private fun undisguise(player: Player, effects: Boolean = true) {
        armed.remove(player.uniqueId)
        val disguise = disguises.remove(player.uniqueId) ?: return

        disguise.task.cancel()
        disguise.barrel.despawn()
        clearModifiers(player)
        player.removePotionEffect(PotionEffectType.INVISIBILITY)

        if (effects) {
            playEffects(disguise.block.location.toCenterLocation(), Sound.BLOCK_BARREL_OPEN, 1.0f)
        }
    }

    private fun refresh(player: Player, barrel: ClientSideBarrelEntity) {
        if (!player.isOnline) return
        barrel.refreshViewers()
        player.addPotionEffect(INVISIBILITY)
        forgetPlayer(player)
    }

    private fun forgetPlayer(player: Player) {
        val at = player.location
        at.getNearbyEntitiesByType(Mob::class.java, MOB_TARGET_FORGET_RADIUS).forEach { mob ->
            if (mob !is Warden && mob.target?.uniqueId != player.uniqueId) return@forEach
            mob.now { forget(mob, player) }
        }
        at.getNearbyEntitiesByType(ShulkerBullet::class.java, MOB_TARGET_FORGET_RADIUS).forEach { bullet ->
            if (bullet.target?.uniqueId != player.uniqueId) return@forEach
            bullet.now { bullet.target = null }
        }
    }

    private fun Entity.now(runnable: Runnable) {
        if (Bukkit.isOwnedByCurrentRegion(this)) runnable.run() else this.sync(runnable)
    }

    private fun forget(mob: Mob, player: Player) {
        mob.target = null
        eraseTargetMemories(mob, player)
        defuse(mob)
        if (mob is Warden) mob.clearAnger(player)
    }

    private fun eraseTargetMemories(mob: Mob, player: Player) {
        val brain = (mob as CraftMob).handle.brain
        for (memory in TARGET_MEMORIES) {
            if (brain.getMemoryInternal(memory)?.orElse(null)?.uuid != player.uniqueId) continue
            brain.eraseMemory(memory)
        }
    }

    private fun defuse(mob: Mob) {
        if (mob !is Creeper) return
        val handle = (mob as CraftCreeper).handle
        handle.swellDir = -1
        handle.swell = 0
    }

    private fun applyScaleModifier(player: Player) {
        val current = player.getAttribute(Attribute.SCALE)?.value ?: return
        if (current <= 0.0) return
        applyModifier(
            player,
            Attribute.SCALE,
            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
            DISGUISED_SCALE / current - 1.0
        )
    }

    private fun applyModifier(
        player: Player,
        attribute: Attribute,
        operation: AttributeModifier.Operation,
        amount: Double
    ) {
        if (amount == 0.0) return
        val instance = player.getAttribute(attribute) ?: return
        instance.addTransientModifier(
            AttributeContainer.builder(KEY)
                .setAttribute(attribute)
                .setOperation(operation)
                .setAmount(amount)
                .build()
                .modifier()
        )
    }

    private fun clearModifiers(player: Player) {
        for (attribute in DISGUISE_ATTRIBUTES) {
            val instance = player.getAttribute(attribute) ?: continue
            instance.modifiers
                .filter { it.key == KEY }
                .forEach { instance.removeModifier(it) }
        }
    }

    private fun playEffects(location: Location, sound: Sound, pitch: Float) {
        val world = location.world ?: return
        world.playSound(location, sound, 1f, pitch)
        world.playSound(location, Sound.BLOCK_WOOD_PLACE, 0.7f, 0.7f)
        world.spawnParticle(Particle.BLOCK, location, 30, 0.35, 0.45, 0.35, 0.05, BARREL_DATA)
    }

    private class Disguise(
        val block: Block,
        val anchor: Location,
        val barrel: ClientSideBarrelEntity,
        val task: ScheduledTask
    )

    // A barrel block display entity that only exists as packets
    private class ClientSideBarrelEntity(private val location: Location) {

        private val display = Display.BlockDisplay(EntityTypes.BLOCK_DISPLAY, (location.world as CraftWorld).handle).apply {
            setPos(location.x, location.y, location.z)
            blockState = (BARREL_DATA as CraftBlockData).state
        }

        // Concurrent because refresh and disguise/undisguise might run on different threads
        private val viewers: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

        fun refreshViewers() {
            val inRange = location.getNearbyPlayers(FAKE_BARREL_VIEW_RADIUS)
            val stillHere = HashSet<UUID>(inRange.size)

            for (player in inRange) {
                stillHere.add(player.uniqueId)
                if (viewers.add(player.uniqueId)) show(player)
            }

            val iterator = viewers.iterator()
            while (iterator.hasNext()) {
                val uuid = iterator.next()
                if (stillHere.contains(uuid)) continue
                iterator.remove()
                Bukkit.getPlayer(uuid)?.let(::hide)
            }
        }

        fun despawn() {
            viewers.mapNotNull(Bukkit::getPlayer).forEach(::hide)
            viewers.clear()
        }

        private fun show(player: Player) {
            val connection = (player as CraftPlayer).handle.connection
            connection.send(
                ClientboundAddEntityPacket(
                    display.id,
                    display.uuid,
                    location.x,
                    location.y,
                    location.z,
                    0f,
                    0f,
                    EntityTypes.BLOCK_DISPLAY,
                    0,
                    Vec3.ZERO,
                    0.0
                )
            )
            connection.send(ClientboundSetEntityDataPacket(display.id, display.entityData.packAll()))
        }

        private fun hide(player: Player) {
            (player as CraftPlayer).handle.connection.send(ClientboundRemoveEntitiesPacket(display.id))
        }
    }
}
