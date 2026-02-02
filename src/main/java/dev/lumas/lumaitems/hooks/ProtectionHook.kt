package dev.lumas.lumaitems.hooks

import dev.lumas.lumaitems.enums.TriState
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

interface ProtectionHook : Hook {

    fun canDamage(attacker: Player, entity: LivingEntity): TriState

    fun canBuild(player: Player, location: Location): TriState


}