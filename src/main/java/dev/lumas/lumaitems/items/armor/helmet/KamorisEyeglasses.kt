package dev.lumas.lumaitems.items.armor.helmet

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.spell
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.extensions.syncDelayed
import java.time.LocalDateTime
import java.util.EnumSet
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.data.Ageable
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Sapling
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack

class KamorisEyeglasses : CustomItemFunctions() {

    private companion object {
        private const val KEY = "kamoris-eyeglasses"

        private const val RADIUS = 5
        private const val HEIGHT = 3
        private const val MAX_PER_PASS = 8
        private const val GROW_DELAY_TICKS = 3L
        private const val CHIME_INTERVAL_MS = 900L

        private val SPELLS = listOf("#D8F3DC", "#B7E4C7", "#95D5B2", "#A9DEF9", "#CDB4DB")
            .map { it.spell() }

        // ageables
        private val NOT_PLANTS = setOf(
            Material.FIRE,
            Material.SOUL_FIRE,
            Material.FROSTED_ICE,
            Material.CHORUS_FLOWER,
        )

        private val GROWABLE: Set<Material> = Material.entries
            .filterTo(EnumSet.noneOf(Material::class.java)) { material ->
                if (!material.isBlock || material.isLegacy || material in NOT_PLANTS) return@filterTo false
                val data = material.createBlockData()
                data is Sapling || (data is Ageable && data !is Bisected)
            }
    }

    private val lastChime = ConcurrentHashMap<UUID, Long>()

    override fun createItem() = ItemFactory.builder()
        .name("<b><gradient:#D8F3DC:#B7E4C7:#95D5B2:#A9DEF9:#CDB4DB>Kamori's Glasses</gradient></b>")
        .customEnchants("<#CDB4DB>Blessing")
        .material(Material.NETHERITE_HELMET)
        .persistentData(KEY)
        .tier(Tier.LUMARINE_2026)
        .vanillaEnchants(
            Enchantment.UNBREAKING to 10,
            Enchantment.PROTECTION to 6,
            Enchantment.MENDING to 1,
        )
        .lore(
            "<#CDB4DB>While worn</#CDB4DB>, nearby",
            "plants will passively",
            "grow up to half of",
            "their full stage.",
            "",
            "For <#CDB4DB>1 hour</#CDB4DB> each day,",
            "they'll grow the way",
            "up instead.",
        )
        .buildPair()

    override fun onAsyncRunnable(player: Player) {
        player.sync {
            val fully = isBloomHour()
            val origin = player.location
            val world = origin.world
            val found = ArrayList<Block>(MAX_PER_PASS)

            scan@ for (x in -RADIUS..RADIUS) {
                for (y in -HEIGHT..HEIGHT) {
                    for (z in -RADIUS..RADIUS) {
                        val block = world.getBlockAt(origin.blockX + x, origin.blockY + y, origin.blockZ + z)
                        if (block.type !in GROWABLE) continue

                        if (!grow(block.blockData, fully)) {
                            continue
                        }

                        found.add(block)
                        if (found.size >= MAX_PER_PASS) break@scan
                    }
                }
            }

            for ((index, block) in found.withIndex()) {
                block.syncDelayed(index * GROW_DELAY_TICKS) {
                    val data = block.blockData
                    if (!grow(data, fully)) return@syncDelayed

                    block.blockData = data
                    sparkle(block)
                    chime(player, fully)
                }
            }
        }
    }

    override fun onPlayerQuit(player: Player, event: PlayerQuitEvent) {
        lastChime.remove(player.uniqueId)
    }

    private fun sparkle(block: Block) {
        block.world.spawnParticle(
            Particle.INSTANT_EFFECT,
            block.location.toCenterLocation(),
            3, 0.25, 0.3, 0.25, 0.0,
            SPELLS.random()
        )
    }

    private fun chime(player: Player, fully: Boolean) {
        val now = System.currentTimeMillis()
        if (now - (lastChime[player.uniqueId] ?: 0L) < CHIME_INTERVAL_MS) return

        lastChime[player.uniqueId] = now
        player.playSound(player.location, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.6f, if (fully) 1.8f else 1.4f)
    }

    private fun grow(data: BlockData, fully: Boolean): Boolean {
        when (data) {
            is Sapling -> {
                val target = if (fully) data.maximumStage else (data.maximumStage + 1) / 2
                if (data.stage >= target) return false
                data.stage = target
            }
            is Ageable -> {
                val target = if (fully) data.maximumAge else (data.maximumAge + 1) / 2
                if (data.age >= target) return false
                data.age = target
            }
            //is Bisected -> return false // pitcher plant
            else -> return false
        }
        return true
    }


    private fun isBloomHour(): Boolean {
        val now = LocalDateTime.now()
        return bloomHour(now.toLocalDate().toEpochDay()) == now.hour
    }

    private fun bloomHour(epochDay: Long): Int {
        var z = epochDay * -0x61c8864680b583ebL + KEY.hashCode()
        z = (z xor (z ushr 30)) * -0x40a7b892e31b1a47L
        z = (z xor (z ushr 27)) * -0x6b2fb644ecceee15L
        z = z xor (z ushr 31)
        return ((z ushr 1) % 24).toInt()
    }
}
