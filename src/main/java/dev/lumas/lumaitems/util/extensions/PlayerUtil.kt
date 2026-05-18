@file:JvmName("PlayerUtil")
package dev.lumas.lumaitems.util.extensions

import dev.lumas.core.util.Text
import dev.lumas.lumaitems.enums.TriState
import dev.lumas.lumaitems.model.item.PdcSource
import dev.lumas.lumaitems.hooks.ProtectionHook
import dev.lumas.lumaitems.hooks.TownyHook
import dev.lumas.lumaitems.hooks.WorldGuardHook
import dev.lumas.lumaitems.model.item.Mixable
import dev.lumas.lumaitems.registry.Registry
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockFace
import org.bukkit.command.CommandSender
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import kotlin.math.floor
import org.bukkit.inventory.PlayerInventory


private val PROTECTION_HOOKS by lazy {
    Registry.HOOKS.getOrThrow(WorldGuardHook::class.java, TownyHook::class.java)
        .map { it as ProtectionHook }
}
private val AIR by lazy { ItemStack.of(Material.AIR) }

val Player.itemInMainHand: ItemStack
    get() = inventory.itemInMainHand

val Player.itemInOffHand: ItemStack
    get() = inventory.itemInOffHand

val PlayerInventory.hotbarContents: Array<ItemStack?>
    get() {
        val result = Array<ItemStack?>(9) { null }
        for (i in 0..8) {
            result[i] = getItem(i)
        }
        return result
    }


fun Player.isWearing(identifier: String): Boolean {
    return isWearing(identifier.namespacedKey())
}

fun Player.isWearing(identifier: NamespacedKey): Boolean {
    return inventory.armorContents.any {
        it?.hasPersistentKey(identifier) == true
    }
}

fun Player.isItemInSlot(identifier: NamespacedKey, slot: EquipmentSlot): Boolean {
    val item = equipment.getItem(slot)
    return item.persistentDataContainer.has(identifier) || Mixable.isMixable(item) // TODO: temporary solution
}

fun Player.isItemInSlot(identifier: String, slot: EquipmentSlot): Boolean {
    return isItemInSlot(identifier.namespacedKey(), slot)
}

fun Player.isItemInSlots(identifier: String, vararg slots: EquipmentSlot): Boolean {
    return slots.any { isItemInSlot(identifier, it) }
}

fun Player.isItemInSlots(identifier: NamespacedKey, vararg slots: EquipmentSlot): Boolean {
    return slots.any { isItemInSlot(identifier, it) }
}

fun Player.isInAnySlot(identifier: NamespacedKey): Boolean {
    return inventory.armorContents.any { it?.hasPersistentKey(identifier) == true } || inventory.itemInMainHand.hasPersistentKey(identifier) || inventory.itemInOffHand.hasPersistentKey(identifier)
}


fun Player.isBoundingBoxOnGround(amt: Double): Boolean {
    val bb = boundingBox
    val y = floor(bb.minY - amt).toInt()
    val xs = intArrayOf(floor(bb.minX).toInt(), floor(bb.maxX - 1e-9).toInt())
    val zs = intArrayOf(floor(bb.minZ).toInt(), floor(bb.maxZ - 1e-9).toInt())
    for (x in xs) for (z in zs) {
        if (world.getBlockAt(x, y, z).isSolid) return true
    }
    return false
}

fun Player.isBoundingBoxOnGround(): Boolean {
    return isBoundingBoxOnGround(0.1);
}

fun Player.isLocationOnGround(): Boolean {
    return this.location.add(0.0,-0.1, 0.0).block.isSolid
}

fun Player.isLocationOnGround(amt: Double): Boolean {
    return this.location.add(0.0,-amt, 0.0).block.isSolid
}

fun Player.isLocationOnGround(amt: Double, isAir: Boolean): Boolean {
    val block = this.location.add(0.0,-amt, 0.0).block
    return if (isAir) block.isEmpty else block.isSolid
}

fun Player.isLookingUp(): Boolean {
    return eyeLocation.pitch <= -45
}

fun Player.isLookingDown(): Boolean {
    return eyeLocation.pitch >= 45
}

fun Player.isHoldingTwoRods(): Boolean {
    return inventory.itemInMainHand.type == Material.FISHING_ROD && inventory.itemInOffHand.type == Material.FISHING_ROD
}

fun Player.firstEquipmentSource(key: NamespacedKey): PdcSource? {
    val armor = equipment.armorContents

    for (item in armor) {
        if (item != null) {
            val source = PdcSource.of(item)
            if (source != null && source.data.has(key)) return source
        }
    }

    PdcSource.of(inventory.itemInMainHand)?.let { if (it.data.has(key)) return it }
    PdcSource.of(inventory.itemInOffHand)?.let { if (it.data.has(key)) return it }

    return null
}

fun Player.equipmentSources(): List<PdcSource> {
    val result = ArrayList<PdcSource>(6)

    equipment.armorContents.let { armor ->
        for (item in armor) {
            if (item != null) {
                PdcSource.of(item)?.let(result::add)
            }
        }
    }

    PdcSource.of(inventory.itemInMainHand)?.let(result::add)
    PdcSource.of(inventory.itemInOffHand)?.let(result::add)

    return result
}

fun Player.handSources(): List<PdcSource> {
    val inv = inventory
    val main = PdcSource.of(inv.itemInMainHand)
    val off = PdcSource.of(inv.itemInOffHand)

    return when {
        main != null && off != null -> listOf(main, off)
        main != null -> listOf(main)
        off != null -> listOf(off)
        else -> emptyList()
    }
}

fun Player.sources(vararg slots: EquipmentSlot): List<PdcSource> {
    val eq = equipment
    val result = ArrayList<PdcSource>(slots.size)

    for (slot in slots) {
        eq.getItem(slot).let { PdcSource.of(it) }?.let(result::add)
    }

    return result
}

fun Player.mainHandSource(): PdcSource? {
    return PdcSource.of(inventory.itemInMainHand)
}

fun Player.equipmentContainers(): List<PersistentDataContainer> {
    val result = ArrayList<PersistentDataContainer>(6)
    val inv = inventory
    val armor = equipment.armorContents
    for (item in armor) {
        item?.itemMeta?.persistentDataContainer?.let(result::add)
    }

    inv.itemInMainHand.itemMeta?.persistentDataContainer?.let(result::add)
    inv.itemInOffHand.itemMeta?.persistentDataContainer?.let(result::add)
    return result
}

fun Player.firstEquipmentContainer(key: NamespacedKey): ItemStack? {
    val result = ArrayList<ItemStack>(6)
    val inv = inventory

    val armor = equipment.armorContents
    for (item in armor) {
        item?.let(result::add)
    }

    inv.itemInMainHand.let(result::add)
    inv.itemInOffHand.let(result::add)

    return result.firstOrNull { it.hasPersistentKey(key) }
}

fun Player.canDamage(entity: LivingEntity): Boolean {
    var result = true
    for (hook in PROTECTION_HOOKS) {
        val value = hook.canDamage(this, entity)
        if (value == TriState.FALSE) {
            result = false
        }
    }
    return result
}

fun Player.canBuild(location: Location): Boolean {
    var result = true
    for (hook in PROTECTION_HOOKS) {
        val value = hook.canBuild(this, location)
        if (value == TriState.FALSE) {
            result = false
        }
    }
    return result
}


fun Player.canDamageByEvent(victim: LivingEntity): Boolean {
    val damageSource = DamageSource.builder(DamageType.PLAYER_ATTACK)
        .withDamageLocation(victim.location)
        .withDirectEntity(this)
        .withCausingEntity(this)
        .build()
    val event = EntityDamageByEntityEvent(this, victim, EntityDamageEvent.DamageCause.ENTITY_ATTACK, damageSource, emptyMap(), emptyMap(), false)

    return event.callEvent()
}

fun Player.canBuildByEvent(location: Location): Boolean {
    val block = location.block
    val event = BlockPlaceEvent(block, block.state, block.getRelative(BlockFace.DOWN), AIR, this, true, EquipmentSlot.HAND)

    return event.callEvent()
}


fun Player.takeItem(itemStack: ItemStack): Boolean {
    val amount = itemStack.amount
    return takeItem(itemStack, amount)
}

fun Player.takeItem(itemStack: ItemStack, amount: Int): Boolean {
    val inventory = inventory
    if (!inventory.containsAtLeast(itemStack, amount)) {
        return false
    }

    val couldNotRemove: MutableMap<Int, ItemStack> = inventory.removeItemAnySlot(itemStack.asQuantity(amount))
    if (couldNotRemove.isEmpty()) {
        return true
    }
    throw RuntimeException("Failed to remove: $couldNotRemove from $name's inventory!")
}

fun Player.send(msg: String) {
    Text.msg(this, msg)
}

fun CommandSender.send(msg: String) {
    Text.msg(this, msg)
}

fun Player.actionBar(msg: String) {
    sendActionBar(msg.asComponent(italic = true))
}

