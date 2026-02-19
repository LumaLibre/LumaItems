package dev.lumas.lumaitems.hooks

import com.palmergames.bukkit.towny.`object`.TownyPermission
import com.palmergames.bukkit.towny.utils.CombatUtil
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil
import dev.lumas.lumaitems.enums.TriState
import dev.lumas.lumaitems.registry.Identifier
import dev.lumas.lumaitems.registry.StringIdentifier
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent.DamageCause


class TownyHook : ProtectionHook {
    override fun canDamage(attacker: Player, entity: LivingEntity): TriState {
        if (!this.isWith())  {
            return TriState.NOT_SET
        }

        return TriState.fromBoolean(!CombatUtil.preventDamageCall(attacker, entity, DamageCause.ENTITY_ATTACK))
    }

    override fun canBuild(player: Player, location: Location): TriState {
        if (!this.isWith())  {
            return TriState.NOT_SET
        }

        val canBuild = PlayerCacheUtil.getCachePermission(player, location, location.block.type, TownyPermission.ActionType.BUILD)
        return TriState.fromBoolean(canBuild)
    }

    override fun identifier(): Identifier {
        return StringIdentifier.of("Towny")
    }
}