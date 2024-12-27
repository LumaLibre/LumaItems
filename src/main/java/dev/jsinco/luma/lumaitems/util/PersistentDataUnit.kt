package dev.jsinco.luma.lumaitems.util

import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType

data class PersistentDataUnit(
    val namespacedKey: NamespacedKey,
    val persistentDataType: PersistentDataType<*,*>,
    val value: Any
)
