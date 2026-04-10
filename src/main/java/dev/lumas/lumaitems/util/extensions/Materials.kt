package dev.lumas.lumaitems.util.extensions

import org.bukkit.Material
import org.bukkit.Tag

fun Material.isTagged(tag: Tag<Material>): Boolean {
    return tag.isTagged(this)
}