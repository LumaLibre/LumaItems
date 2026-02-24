@file:JvmName("ParticleUtil")
package dev.lumas.lumaitems.util.extensions

import org.bukkit.Color
import org.bukkit.Particle

fun String.dustOptions(size: Float): Particle.DustOptions {
    return Particle.DustOptions(this.toBukkitColor(), size)
}

fun String.dustOptions(): Particle.DustOptions {
    return this.dustOptions(1f)
}

fun Color.dustOptions(size: Float): Particle.DustOptions {
    return Particle.DustOptions(this, size)
}

fun Color.dustOptions(): Particle.DustOptions {
    return Particle.DustOptions(this, 1f)
}

fun String.spell(power: Float): Particle.Spell {
    return Particle.Spell(this.toBukkitColor(), power)
}

fun String.spell(): Particle.Spell {
    return this.spell(1f)
}

fun Color.spell(power: Float): Particle.Spell {
    return Particle.Spell(this, power)
}

fun Color.spell(): Particle.Spell {
    return Particle.Spell(this, 1f)
}

