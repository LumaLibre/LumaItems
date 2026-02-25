package dev.lumas.lumaitems.items.armor.boots

import dev.lumas.lumaitems.annotations.Disable
import dev.lumas.lumaitems.enums.WorldName
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.Executors
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Location
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.abs

@Disable(WorldName.EVENT_NEW)
class WanderersDecanterBoots : CustomItemFunctions() {

    companion object {
        private val KEY = Util.namespacedKey("wanderers-decanter-boots")
        val BOTTLE = ItemStack(Material.EXPERIENCE_BOTTLE, 1)

        private const val XP_COST_PER_BOTTLE = 8
        private const val WALK_BLOCKS_PER_BOTTLE = 32.0
        private const val SPRINT_BLOCKS_PER_BOTTLE = 48.0


        private data class WalkState(
            var lastBlockX: Int,
            var lastBlockZ: Int,
            var accum: Double
        )

        private const val RESYNC_BLOCK_JUMP = 6
        private const val DIAGONAL_STEP = 1.41421356237 // sqrt(2)
        private val STATE: MutableMap<UUID, WalkState> = ConcurrentHashMap()
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#B9E6C9:#A7DCEB:#F3E3B1:#CDE7B5>Wanderer's Decanter</gradient></b>")
            .customEnchants("<#A7DCEB>Distillation")
            .material(Material.NETHERITE_BOOTS)
            .persistentData(KEY)
            .tier(Tier.VALENTIDE_2026)
            .lore(
                "Journeys leave more",
                "than footprints.",
                "",
                "<#A7DCEB>Walk</#A7DCEB> or <#A7DCEB>sprint</#A7DCEB> to",
                "slowly condense your",
                "experience into",
                "XP bottles."
            )
            .vanillaEnchants(
                // Max vanilla-ish enchantments (not all of them)
                Enchantment.PROTECTION to 5,
                Enchantment.FEATHER_FALLING to 3,
                Enchantment.SOUL_SPEED to 3,
                Enchantment.DEPTH_STRIDER to 3,
                Enchantment.UNBREAKING to 3,
                Enchantment.MENDING to 1
            )
            .buildPair()
    }

    override fun onMove(player: Player, event: PlayerMoveEvent) {
        if (!event.hasExplicitlyChangedBlock()) return
        Executors.async {
            val to = event.to
            val from = event.from

            if (player.isGliding || player.isFlying || player.isInsideVehicle || player.isInWater) return@async

            val step = horizontalStepDistance(from, to)
            if (step <= 0.0) return@async

            val st = STATE.getOrPut( player.uniqueId) {
                WalkState(to.blockX, to.blockZ, 0.0)
            }

            val rdx = abs(to.blockX - st.lastBlockX)
            val rdz = abs(to.blockZ - st.lastBlockZ)
            if (rdx > RESYNC_BLOCK_JUMP || rdz > RESYNC_BLOCK_JUMP) {
                st.lastBlockX = to.blockX
                st.lastBlockZ = to.blockZ
                st.accum = 0.0
                return@async
            }

            st.lastBlockX = to.blockX
            st.lastBlockZ = to.blockZ
            st.accum += step

            val blocksPerBottle = if (player.isSprinting) SPRINT_BLOCKS_PER_BOTTLE else WALK_BLOCKS_PER_BOTTLE
            if (st.accum >= blocksPerBottle) {
                if (tryConvertOneBottle(player)) st.accum -= blocksPerBottle
                if (st.accum >= blocksPerBottle) st.accum = blocksPerBottle
            }
        }
    }

    private fun horizontalStepDistance(from: Location, to: Location): Double {
        val dx = abs(to.blockX - from.blockX)
        val dz = abs(to.blockZ - from.blockZ)
        return when {
            dx == 0 && dz == 0 -> 0.0
            dx == 1 && dz == 0 -> 1.0
            dx == 0 && dz == 1 -> 1.0
            dx == 1 && dz == 1 -> DIAGONAL_STEP
            else -> -1.0 // teleport/knockback/lag
        }
    }

    private fun tryConvertOneBottle(player: Player): Boolean {
        if (player.totalExperience < XP_COST_PER_BOTTLE) return false
        player.sync {
            player.giveExp(-XP_COST_PER_BOTTLE)
            player.give(BOTTLE)
        }
        return true
    }

    override fun onPlayerQuit(player: Player, event: PlayerQuitEvent) {
        STATE.remove(player.uniqueId)
    }

    override fun onPluginDisableGlobal() {
        STATE.clear()
    }
}