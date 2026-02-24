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
        val BOTTLE = ItemStack(Material.EXPERIENCE_BOTTLE, 1)

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
                "<#A7DCEB>Walk</#A7DCEB> or <#A7DCEB>sprint</#A7DCEB> to",
                "slowly condense your",
                "experience into",
                "XP bottles."
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

    // TODO: This is doing too many operations in onMove. onMove is pretty hot and
    //   sqrt() shouldnt be called in it
    override fun onMove(player: Player, event: PlayerMoveEvent) {
        if (true) return // TODO temporarily disabled because of the sqrt() in this hot method

        if (!event.hasExplicitlyChangedBlock()) return

        val to = event.to
        val from = event.from

        // TODO unnecessary overhead, we're in onMove()
        //  -> If you want to do these checks this should/can be moved to another thread
        val boots = player.inventory.boots
        if (boots == null || !boots.isMatchingItem(KEY)) {
            STATE.remove(player.uniqueId)
            return
        }

        // same as comment above
        if (player.gameMode == GameMode.CREATIVE || player.gameMode == GameMode.SPECTATOR) {
            STATE.remove(player.uniqueId)
            return
        }

        if (player.isGliding || player.isFlying || player.isInsideVehicle || player.isInWater) return


        val dx = to.x - from.x
        val dz = to.z - from.z
        val hDist = sqrt(dx * dx + dz * dz) // TODO expensive
        if (hDist < 0.02) return // TODO can be constant because this feels like a magic number (i dont know what this means)
        if (hDist > 1.2) return // TODO can be constant

        val id = player.uniqueId
        // TODO maybe unnecessary overhead
        val st = STATE.getOrPut(id) { WalkState(to.x, to.y, to.z, 0.0) }

        val rx = to.x - st.lastX
        val rz = to.z - st.lastZ
        val resyncDist = sqrt(rx * rx + rz * rz) // TODO expensive
        if (resyncDist > 6.0) { // TODO can be constant
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

        while (st.accum >= blocksPerBottle) { // TODO can this be a for loop?
            if (!tryConvertOneBottle(player)) break
            st.accum -= blocksPerBottle
        }
    }

    private fun tryConvertOneBottle(player: Player): Boolean {
        if (player.totalExperience < XP_COST_PER_BOTTLE) return false

        player.giveExp(-XP_COST_PER_BOTTLE)
        player.give(BOTTLE)
        return true
    }

    override fun onPlayerQuit(player: Player, event: PlayerQuitEvent) {
        STATE.remove(player.uniqueId)
    }

    override fun onPluginDisableGlobal() {
        STATE.clear()
    }
}