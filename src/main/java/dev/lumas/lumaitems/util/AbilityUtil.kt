package dev.lumas.lumaitems.util

import dev.lumas.lumaitems.model.spell.SpellCaster
import dev.lumas.lumaitems.model.entity.TickDamager
import dev.lumas.lumaitems.util.extensions.canBuild
import dev.lumas.lumaitems.util.extensions.canDamage
import dev.lumas.lumaitems.util.extensions.isBoundingBoxOnGround
import dev.lumas.lumaitems.util.extensions.isLocationOnGround
import dev.lumas.lumaitems.util.extensions.takeItem
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.inventory.ItemStack


object AbilityUtil {

    private val LAPIS_LAZULI by lazy { ItemStack.of(Material.LAPIS_LAZULI) }


    fun noDamagePermission(attacker: Player, victim: Entity) = (victim as? LivingEntity)?.let { !attacker.canDamage(it) } ?: false
    fun noBuildPermission(player: Player, block: Block) = !player.canBuild(block.location)
    fun isOnGround(entity: Entity, amt: Double) = if (entity is Player) entity.isBoundingBoxOnGround(amt) else entity.isOnGround
    fun isOnGround(entity: Entity) = isOnGround(entity, 0.1);

    fun takeSpellLapisCost(player: Player, amount: Int) = player.takeItem(LAPIS_LAZULI, amount)


    fun spawnSpell(player: Player, particle: Particle?, key: String, ticksAlive: Long) = spawnSpell(player, particle, Util.namespacedKey(key), ticksAlive, null)
    fun spawnSpell(player: Player, particle: Particle?, key: String, ticksAlive: Long, runnableCallback: ((Snowball) -> Unit)?) = spawnSpell(player, particle, Util.namespacedKey(key), ticksAlive, runnableCallback)

    fun spawnSpell(player: Player, particle: Particle?, key: NamespacedKey, ticksAlive: Long, runnableCallback: ((Snowball) -> Unit)? = null): Snowball {
        val builder = SpellCaster.builder()
            .player(player)
            .particle(particle)
            .key(key)
            .ticks(ticksAlive)

        runnableCallback?.let { builder.onTick(it) }

        val spellCaster = builder.build()
        spellCaster.cast()
        return spellCaster.snowballOrThrow()
    }


    fun damageOverTicks(victim: LivingEntity, attacker: Player?, damage: Double, hitAmount: Int, runnableCallback: ((LivingEntity) -> Unit)? = null, whenFinishedCallback: ((LivingEntity) -> Unit)? = null): Int {
        val builder = TickDamager.builder()
            .victims(victim)
            .attacker(attacker)
            .damage(damage)
            .hitAmount(hitAmount)

        runnableCallback?.let { builder.onTick(it) }
        whenFinishedCallback?.let { builder.onFinish(it) }

        val damager = builder.build()
        damager.start()
        return damager.ticksToComplete().toInt()
    }

}