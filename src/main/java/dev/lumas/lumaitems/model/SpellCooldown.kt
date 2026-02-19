package dev.lumas.lumaitems.model

import java.util.UUID

data class SpellCooldown<T>(val uuid: UUID, val spell: T) where T : Enum<T>, T : AbstractSpell {
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is SpellCooldown<*>) return false
        return uuid == other.uuid && spell == other.spell
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + spell.hashCode()
        return result
    }

    companion object {
        fun <T> of(player: UUID, spell: T): SpellCooldown<T> where T : Enum<T>, T : AbstractSpell {
            return SpellCooldown(player, spell)
        }
    }
}