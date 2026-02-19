@file:JvmName("ItemUtil")
package dev.lumas.lumaitems.util.extensions

import com.destroystokyo.paper.profile.ProfileProperty
import dev.lumas.lumaitems.LumaItems
import java.util.UUID
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta

private val STATIC_UUID by lazy { UUID.fromString("e9378e48-0e8e-42a9-9df1-7074b00df6a9") }

fun ItemStack.isMatchingItem(key: String): Boolean {
    return isMatchingItem(NamespacedKey(LumaItems.getInstance(), key))
}

fun ItemStack.isMatchingItem(key: NamespacedKey): Boolean {
    val meta = this.itemMeta ?: return false
    return meta.persistentDataContainer.has(key)
}

fun ItemMeta.setBase64Texture(base64: String?) {
    if (this !is SkullMeta || base64 == null) return
    val profile = Bukkit.createProfile(STATIC_UUID)
    profile.properties.add(ProfileProperty("textures", base64))
    playerProfile = profile
}

fun ItemStack.withMeta(editMeta: (ItemMeta) -> Unit): ItemStack {
    val meta = itemMeta ?: return this
    editMeta(meta)
    itemMeta = meta
    return this
}

fun Collection<ItemStack>.determineMostCommon(): ItemStack {
    return this.groupingBy { it }
        .eachCount()
        .maxBy { it.value }
        .key
}

fun Material.itemStack(amount: Int = 1, editMeta: ((ItemMeta) -> Unit)? = null): ItemStack {
    val itemStack = ItemStack(this, amount)
    if (editMeta != null) {
        val itemMeta = itemStack.itemMeta ?: return itemStack
        editMeta(itemMeta)
        itemStack.itemMeta = itemMeta
    }
    return itemStack
}

fun ItemStack.willBreak(test: Int): Boolean {
    val meta = this.itemMeta as? Damageable ?: return false
    val maxDmg = if (meta.hasMaxDamage()) meta.maxDamage else type.maxDurability.toInt()
    if (maxDmg <= 0) return false
    return meta.damage + test >= maxDmg
}

fun ItemStack.setRemainingHealth(health: Int) {
    val meta = this.itemMeta as? Damageable ?: return
    val maxDmg = if (meta.hasMaxDamage()) meta.maxDamage else type.maxDurability.toInt()
    if (maxDmg <= 0) return
    val newDamage = (maxDmg - health).coerceAtLeast(1)
    if (meta.damage != newDamage) {
        meta.damage = newDamage
        this.itemMeta = meta
    }
}