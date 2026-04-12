package dev.lumas.lumaitems.model.spell

import java.util.UUID

/**
 * Class for easy cooldown of magic items
 */
data class MagicItemCooldown(
    val playerUUID: UUID,
    val spellEnum: Enum<*>,
    val cooldown: Long
)