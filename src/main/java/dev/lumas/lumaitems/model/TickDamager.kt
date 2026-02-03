package dev.lumas.lumaitems.model

import dev.lumas.lumaitems.util.Executors
import dev.lumas.lumaitems.util.Executors.sync
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

/**
 * A simple builder for damaging entities over a period of ticks
 */
class TickDamager private constructor(
    val victims: Set<LivingEntity>,
    val attacker: Player?,
    val damage: Double,
    val hitAmount: Int,
    val tickInterval: Long,
    val onTickCallback: ((LivingEntity) -> Unit)?,
    val onFinishCallback: ((LivingEntity) -> Unit)?
) {

    var task: ScheduledTask? = null

    fun ticksToComplete(): Long = hitAmount * tickInterval
    fun damagePerTick(): Double = damage / hitAmount
    fun isRunning(): Boolean = task != null && !task!!.isCancelled
    fun isComplete(): Boolean = !isRunning()


    fun start() {
        if (isRunning()) {
            throw IllegalStateException("TickDamager is already running")
        }

        val damageToDealOverTicks = damage / hitAmount
        var count = 0

        this.task = Executors.asyncTimer(0, tickInterval) { task ->
            for (victim in victims) {
                victim.sync {
                    if (count >= hitAmount || victim.isDead) {
                        task.cancel()
                        onFinishCallback?.invoke(victim)
                        return@sync
                    }
                    victim.damage(damageToDealOverTicks, attacker)
                    onTickCallback?.invoke(victim)
                }
            }
            ++count
        }
    }


    companion object {
        fun builder() = Builder()
    }

    class Builder {
        private val victims: MutableSet<LivingEntity> = mutableSetOf()
        private var attacker: Player? = null
        private var damage: Double = 0.0
        private var hitAmount: Int = 1
        private var tickInterval: Long = 10L
        private var onTickCallback: ((LivingEntity) -> Unit)? = null
        private var onFinishCallback: ((LivingEntity) -> Unit)? = null


        fun victims(vararg entities: LivingEntity) = apply { victims.addAll(entities) }
        fun attacker(player: Player?) = apply { this.attacker = player }
        fun damage(amount: Double) = apply { this.damage = amount }
        fun hitAmount(amount: Int) = apply { this.hitAmount = amount }
        fun tickInterval(ticks: Long) = apply { this.tickInterval = ticks }
        fun onTick(callback: (LivingEntity) -> Unit) = apply { this.onTickCallback = callback }
        fun onFinish(callback: (LivingEntity) -> Unit) = apply { this.onFinishCallback = callback }

        fun build(): TickDamager {
            return TickDamager(
                victims = victims,
                attacker = attacker,
                damage = damage,
                hitAmount = hitAmount,
                tickInterval = tickInterval,
                onTickCallback = onTickCallback,
                onFinishCallback = onFinishCallback
            )
        }

        fun runNow(): TickDamager {
            return build().also { it.start() }
        }
    }


}