package dev.lumas.lumaitems.model.item

import java.util.UUID

class CustomItemCooldown(
    val customItem: Class<out CustomItem>,
    val players: MutableList<UUID>,
) {

    fun isOnCooldown(player: UUID): Boolean {
        return players.contains(player)
    }

    fun addCooldown(player: UUID) {
        players.add(player)
    }

    fun removeCooldown(player: UUID) {
        players.remove(player)
    }
}