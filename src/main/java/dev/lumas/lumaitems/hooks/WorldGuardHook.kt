package dev.lumas.lumaitems.hooks

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.LocalPlayer
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.regions.RegionQuery
import dev.lumas.lumaitems.registry.Identifier
import dev.lumas.lumaitems.registry.StringIdentifier
import dev.lumas.lumaitems.enums.TriState
import org.bukkit.Location
import org.bukkit.entity.Enemy
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

class WorldGuardHook : ProtectionHook {


    override fun canDamage(attacker: Player, entity: LivingEntity): TriState {
        if (!this.isWith()) {
            return TriState.NOT_SET
        }

        val regionContainer= WorldGuard.getInstance().platform.regionContainer

        val location = BukkitAdapter.adapt(entity.location)
        val localPlayer: LocalPlayer = WorldGuardPlugin.inst().wrapPlayer(attacker)
        val query: RegionQuery = regionContainer.createQuery()


        val flag = if (entity is HumanEntity) Flags.PVP else Flags.DAMAGE_ANIMALS
        val state = query.queryState(location, localPlayer, flag)

        if (state == StateFlag.State.DENY) {
            return TriState.FALSE
        }
        return TriState.TRUE
    }

    override fun canBuild(player: Player, location: Location): TriState {
        if (!this.isWith()) {
            return TriState.NOT_SET
        }

        val regionContainer= WorldGuard.getInstance().platform.regionContainer

        val weLocation = BukkitAdapter.adapt(location)
        val localPlayer: LocalPlayer = WorldGuardPlugin.inst().wrapPlayer(player)
        val query: RegionQuery = regionContainer.createQuery()

        return TriState.fromBoolean(query.testState(weLocation, localPlayer, Flags.BUILD))
    }

    override fun identifier(): Identifier {
        return StringIdentifier.of("WorldGuard")
    }
}