package dev.lumas.lumaitems.hooks

import dev.lumas.lumaitems.registry.Identifier
import dev.lumas.lumaitems.registry.StringIdentifier
import io.lumine.mythic.bukkit.MythicBukkit
import org.bukkit.entity.Entity

class MythicMobsHook : Hook {
    override fun identifier(): Identifier {
        return StringIdentifier.of("MythicMobs")
    }

    fun isMythicMob(bukkitEntity: Entity): Boolean {
        if (!this.isWith()) {
            return false
        }
        return MythicBukkit.inst().mobManager.isMythicMob(bukkitEntity)
    }
}