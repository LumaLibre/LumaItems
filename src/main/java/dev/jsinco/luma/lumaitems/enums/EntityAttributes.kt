package dev.jsinco.luma.lumaitems.enums

import org.bukkit.attribute.Attribute

enum class EntityAttributes(vararg val values: Pair<Attribute, Double>) {
    // This is all I need for right now.
    PLAYER(
        Attribute.ATTACK_SPEED to 4.0,
        Attribute.ATTACK_DAMAGE to 1.0,
    )
}
