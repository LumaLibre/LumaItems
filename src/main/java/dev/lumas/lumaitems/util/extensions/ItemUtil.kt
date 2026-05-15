@file:JvmName("ItemUtil")
package dev.lumas.lumaitems.util.extensions

import com.destroystokyo.paper.MaterialTags
import com.destroystokyo.paper.profile.ProfileProperty
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.PdcSource
import dev.lumas.lumaitems.relics.RelicCrafting
import dev.lumas.lumaitems.util.SharedContainers
import java.util.UUID
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlotGroup
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

fun ItemMeta.setTexture(base64: String?) {
    if (this !is SkullMeta || base64 == null) return
    val profile = Bukkit.createProfile(STATIC_UUID)
    profile.properties.add(ProfileProperty("textures", base64))
    playerProfile = profile
}

fun ItemMeta.setTexture(player: Player) {
    if (this !is SkullMeta) return
    this.playerProfile = player.playerProfile
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
    return willBreak(test, itemMeta ?: return false)
}

fun ItemStack.willBreak(test: Int, itemMeta: ItemMeta): Boolean {
    val meta = itemMeta as? Damageable ?: return false
    val maxDmg = if (meta.hasMaxDamage()) meta.maxDamage else type.maxDurability.toInt()
    if (maxDmg <= 0) return false
    return meta.damage + test >= maxDmg
}

fun ItemStack.getHealth(): Int {
    val meta = itemMeta as? Damageable ?: return -1
    val maxDmg = if (meta.hasMaxDamage()) meta.maxDamage else type.maxDurability.toInt()
    if (maxDmg <= 0) return -1
    val damage = if (meta.hasDamage()) meta.damage else 0
    return maxDmg - damage
}

fun Material.isDye(): Boolean = MaterialTags.DYES.isTagged(this)

fun Material.toBundleMaterial(): Material? {
    if (!isDye()) return null
    val base = name.removeSuffix("_DYE")
    return Material.matchMaterial("${base}_BUNDLE")
}

fun computeDyedBundleResult(matrix: Array<ItemStack?>, key: String): ItemStack? {
    var bundle: ItemStack? = null
    var dye: Material? = null
    for (it in matrix) {
        if (it == null || it.type == Material.AIR) continue
        when {
            it.isMatchingItem(key) -> {
                if (bundle != null) return null
                bundle = it
            }
            it.type.isDye() -> {
                if (dye != null) return null
                dye = it.type
            }
            else -> return null
        }
    }
    val baseBundle = bundle ?: return null
    val dyeMat = dye ?: return null
    val outMat = dyeMat.toBundleMaterial() ?: return null
    if (baseBundle.type == outMat) return null
    val out = ItemStack(outMat, baseBundle.amount.coerceAtLeast(1))
    out.itemMeta = baseBundle.itemMeta
    return out
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

fun ItemStack.isTagged(tag: Tag<Material>) =
    tag.isTagged(this.type)

fun ItemStack?.asSource(): PdcSource? = this?.let(PdcSource::of)

fun ItemStack.isLumaItem(): Boolean {
    val meta = this.itemMeta ?: return false
    return meta.persistentDataContainer.has(ItemFactory.LUMAITEM)
}

fun ItemStack.isRelic(): Boolean {
    val meta = this.itemMeta ?: return false
    return meta.persistentDataContainer.has(RelicCrafting.RELIC_KEY)
}


fun ItemStack.useNewSharedScaleContainer(takeIfMatching: String, amount: Double) {
    useNewSharedScaleContainer(NamespacedKey(LumaItems.getInstance(), takeIfMatching), amount)
}

fun ItemStack.useNewSharedScaleContainer(takeIfMatching: NamespacedKey, amount: Double) {
    val item = this.takeIf { it.isMatchingItem(takeIfMatching) } ?: return
    item.editMeta { meta ->
        if (meta.getAttributeModifiers(Attribute.SCALE)?.any { it.key == SharedContainers.SCALE.key } == true) return@editMeta

        meta.removeAttributeModifier(Attribute.SCALE)

        val built = SharedContainers.SCALE
            .setAmount(amount)
            .setOperation(AttributeModifier.Operation.ADD_NUMBER)
            .build()
            .modifier()
        meta.addAttributeModifier(Attribute.SCALE, built)
    }
}