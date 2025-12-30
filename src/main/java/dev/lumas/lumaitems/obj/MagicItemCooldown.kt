package dev.lumas.lumaitems.obj

import java.util.UUID

/**
 * Class for easy cooldown of magic items
 */
data class MagicItemCooldown(
    val playerUUID: UUID,
    val spellEnum: Enum<*>,
    val cooldown: Long
)