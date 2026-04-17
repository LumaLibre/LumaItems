@file:JvmName("TagExtensions")
package dev.lumas.lumaitems.util.extensions

import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Block


fun <T : Keyed> Tag<T>.random(): T {
    return this.values.random()
}

fun <T : Keyed> Tag<T>.randomOrNull(): T? {
    return this.values.randomOrNull()
}

fun <T : Keyed> Tag<T>.excluding(vararg items: T): Collection<T> {
    return this.values.filter { !items.contains(it) }
}

fun Material.isTagged(tag: Tag<Material>): Boolean {
    return tag.isTagged(this)
}

fun Block.isTagged(tag: Tag<Material>): Boolean {
    return tag.isTagged(type)
}
