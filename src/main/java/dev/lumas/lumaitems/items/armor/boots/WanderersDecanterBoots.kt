package dev.lumas.lumaitems.items.armor.boots

import dev.lumas.lumaitems.annotations.Disable
import dev.lumas.lumaitems.enums.WorldName
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.GameMode
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.sqrt

@Disable(WorldName.EVENT_NEW)
class WanderersDecanterBoots : CustomItemFunctions() {

    companion object {
        private val KEY = Util.namespacedKey("wanderers-decanter-boots")

        private const val XP_COST_PER_BOTTLE = 8
        private const val WALK_BLOCKS_PER_BOTTLE = 32.0
        private const val SPRINT_BLOCKS_PER_BOTTLE = 24.0

        private data class WalkState(
            var lastX: Double,
            var lastY: Double,
            var lastZ: Double,
            var accum: Double
        )

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
                "Walk or sprint to",
                "slowly condense your",
                "<#F3E3B1>experience</#F3E3B1> into",
                "<#A7DCEB>XP bottles</#A7DCEB>."
            )
            .vanillaEnchants(
                // Max vanilla-ish enchantments (not all of them)
                Enchantment.PROTECTION to 4,
                Enchantment.FEATHER_FALLING to 3,
                Enchantment.SOUL_SPEED to 3,
                Enchantment.DEPTH_STRIDER to 3,
                Enchantment.UNBREAKING to 3,
                Enchantment.MENDING to 1
            )
            .buildPair()
    }

    override fun onMove(player: Player, event: PlayerMoveEvent) {
        val to = event.to
        val from = event.from

        // Don't process movement unless the player has moved from one block to another
        if (to.blockX == from.blockX && to.blockZ == from.blockZ) return

        val boots = player.inventory.boots
        if (boots == null || !boots.isMatchingItem(KEY)) {
            STATE.remove(player.uniqueId)
            return
        }

        if (!player.isOnline) return
        if (player.gameMode == GameMode.CREATIVE || player.gameMode == GameMode.SPECTATOR) {
            STATE.remove(player.uniqueId)
            return
        }

        if (player.isGliding) return
        if (player.allowFlight && player.isFlying) return
        if (player.isInsideVehicle) return
        if (to.block.isLiquid) return

        val dx = to.x - from.x
        val dz = to.z - from.z
        val hDist = sqrt(dx * dx + dz * dz)
        if (hDist < 0.02) return
        if (hDist > 1.2) return

        val id = player.uniqueId
        val st = STATE.getOrPut(id) { WalkState(to.x, to.y, to.z, 0.0) }

        val rx = to.x - st.lastX
        val rz = to.z - st.lastZ
        val resyncDist = sqrt(rx * rx + rz * rz)
        if (resyncDist > 6.0) {
            st.lastX = to.x
            st.lastY = to.y
            st.lastZ = to.z
            st.accum = 0.0
            return
        }

        st.lastX = to.x
        st.lastY = to.y
        st.lastZ = to.z

        st.accum += hDist

        val blocksPerBottle = if (player.isSprinting) SPRINT_BLOCKS_PER_BOTTLE else WALK_BLOCKS_PER_BOTTLE

        while (st.accum >= blocksPerBottle) {
            if (!tryConvertOneBottle(player)) break
            st.accum -= blocksPerBottle
        }
    }

    private fun tryConvertOneBottle(player: Player): Boolean {
        if (player.totalExperience < XP_COST_PER_BOTTLE) return false

        player.giveExp(-XP_COST_PER_BOTTLE)

        val bottle = ItemStack(Material.EXPERIENCE_BOTTLE, 1)
        val leftover = player.inventory.addItem(bottle)
        if (leftover.isNotEmpty()) {
            player.world.dropItemNaturally(player.location, bottle)
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