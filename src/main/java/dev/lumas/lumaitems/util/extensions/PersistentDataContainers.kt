@file:JvmName("PersistentDataContainers")
package dev.lumas.lumaitems.util.extensions

import dev.lumas.lumaitems.LumaItems
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType


fun String.namespacedKey(): NamespacedKey {
    return NamespacedKey(LumaItems.getInstance(), this)
}

fun PersistentDataHolder.removePersistentKey(key: NamespacedKey) {
    persistentDataContainer.remove(key)
}

fun PersistentDataHolder.removePersistentKey(key: String) {
    persistentDataContainer.remove(key.namespacedKey())
}

fun <P, C : Any> PersistentDataHolder.setPersistentKey(key: NamespacedKey, dataType: PersistentDataType<P, C>, value: C) {
    persistentDataContainer.set(key, dataType, value)
}

fun <P : Any, C : Any> PersistentDataHolder.getPersistentKey(key: String, dataType: PersistentDataType<P, C>): C? {
    return persistentDataContainer.get(key.namespacedKey(), dataType)
}

fun <P : Any, C : Any> PersistentDataHolder.getPersistentKey(key: NamespacedKey, dataType: PersistentDataType<P, C>): C? {
    return persistentDataContainer.get(key, dataType)
}

fun <P, C : Any> PersistentDataHolder.setPersistentKey(key: String, dataType: PersistentDataType<P, C>, value: C) {
    persistentDataContainer.set(key.namespacedKey(), dataType, value)
}

fun <P, C : Any> ItemStack.setPersistentKey(key: NamespacedKey, dataType: PersistentDataType<P, C>, value: C) {
    val meta = itemMeta ?: return
    meta.persistentDataContainer.set(key, dataType, value)
    itemMeta = meta
}

fun <P : Any, C : Any> ItemStack.getPersistentKey(key: NamespacedKey, dataType: PersistentDataType<P, C>): C? {
    return itemMeta?.persistentDataContainer?.get(key, dataType)
}

fun ItemStack.hasPersistentKey(key: NamespacedKey): Boolean {
    return itemMeta?.persistentDataContainer?.has(key) == true
}

fun PersistentDataHolder?.hasPersistentKey(key: NamespacedKey): Boolean {
    if (this == null) return false
    return persistentDataContainer.has(key)
}

fun PersistentDataHolder?.hasPersistentKey(key: String): Boolean {
    if (this == null) return false
    return persistentDataContainer.has(key.namespacedKey())
}