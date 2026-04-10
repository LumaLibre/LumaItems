@file:JvmName("LivingEntityUtil")
package dev.lumas.lumaitems.util.extensions

import dev.lumas.lumaitems.util.FakeLootTable
import kotlin.random.Random
import kotlin.random.asJavaRandom
import org.bukkit.Material
import org.bukkit.damage.DamageSource
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.loot.LootContext

/**
 * Drops all equipment of the living entity (armor, main hand item, and offhand item) at the entity's eye location.
 * After dropping the items, it clears the entity's equipment.
 */
fun LivingEntity.dropEquipment() {
    val entityEquipment = equipment ?: return
    entityEquipment.let { equip ->
        equip.armorContents.forEach { it?.let { world.dropItemNaturally(eyeLocation, it) } }
        equip.itemInMainHand.let { world.dropItemNaturally(eyeLocation, it) }
        equip.itemInOffHand.let { world.dropItemNaturally(eyeLocation, it) }
    }
    entityEquipment.clear()
}

/**
 * Checks if the entity has any equipment (armor, main hand item, or off hand item).
 */
fun LivingEntity.hasEquipment(): Boolean {
    val entityEquipment = equipment ?: return false
    return entityEquipment.armorContents.any { it != null } ||
            entityEquipment.itemInMainHand.type != Material.AIR ||
            entityEquipment.itemInOffHand.type != Material.AIR
}

fun Mob.getDrops(killer: Player, luck: Float): Collection<ItemStack> {
    val lootTable = lootTable ?: return emptyList()
    val context = LootContext.Builder(location)
        .killer(killer)
        .lootedEntity(this)
        .luck(luck)
        .build()
    return lootTable.populateLoot(Random.asJavaRandom(), context)
}


fun LivingEntity.getDrops(killer: Player, damageSource: DamageSource, looting: Int): Collection<ItemStack> {
    return FakeLootTable.builder()
        .world(this.world)
        .entity(this)
        .player(killer)
        .damageSource(damageSource)
        .looting(looting)
        .simulate()
}

fun LivingEntity.getDrops(damageSource: DamageSource, looting: Int): Collection<ItemStack> {
    return FakeLootTable.builder()
        .world(this.world)
        .entity(this)
        .damageSource(damageSource)
        .looting(looting)
        .simulate()
}

