package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.shapes.Cuboid
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.addCooldown
import dev.lumas.lumaitems.util.extensions.canBuild
import dev.lumas.lumaitems.util.extensions.hotbarContents
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.isOnCooldown
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.setBlockDataWithLog
import kotlin.random.Random
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class PathmakerItem : CustomItemFunctions() {

    private companion object {
        private val KEY = "pathmaker".namespacedKey() }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#7D9FFC:#CDA9FF:#E28BDC:#F56868>Pathmaker</gradient></b>")
            .material(Material.SHEARS)
            .persistentData(KEY)
            .tier(Tier.WONDERLAND_2026)
            .vanillaEnchants(Enchantment.UNBREAKING to 10, Enchantment.KNOCKBACK to 2)
            .lore(
                "<#CDA9FF>Right-click</#CDA9FF> grass to",
                "replace a <#CDA9FF>3x1</#CDA9FF> area",
                "with random full blocks",
                "from your hotbar.",
                "",
                "Only <#CDA9FF>full blocks</#CDA9FF> are used.",
                "<red>Works on grass blocks only.",
                "",
                "<red>Cooldown: 1s"
            )
            .buildPair()
    }


    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        val item = event.item ?: return
        val clickedBlock = event.clickedBlock ?: return

        if (event.action != Action.RIGHT_CLICK_BLOCK || player.isOnCooldown(this) || !item.isMatchingItem(KEY)) {
            return
        }
        if (clickedBlock.type != Material.GRASS_BLOCK || !player.canBuild(clickedBlock.location)) {
            return
        }

        event.isCancelled = true
        player.addCooldown(this, 20)

        val sources = collectHotbarSources(player)
        if (sources.isEmpty()) return

        val facing = player.facing
        val (dx, dz) = when (facing) {
            BlockFace.NORTH, BlockFace.SOUTH -> Pair(1, 0)
            BlockFace.EAST, BlockFace.WEST   -> Pair(0, 1)
            else -> Pair(1, 0)
        }

        val loc1 = clickedBlock.location.clone().add( 1.0, 0.0,  1.0)
        val loc2 = clickedBlock.location.clone().add(-1.0, 0.0, -1.0)
        val cuboid = Cuboid(loc1, loc2)

        for (target in cuboid.blockList()) {
            if (target.type != Material.GRASS_BLOCK) continue
            val source = weightedRandomSource(sources) ?: break
            source.reduce(1)
            target.setBlockDataWithLog(player, source.material)
        }

        // 50% chance to silently steal 1-5 extra blocks from the hotbar
        if (Random.nextBoolean()) {
            val stolen = Random.nextInt(1, 6)
            repeat(stolen) {
                val source = weightedRandomSource(sources) ?: return
                source.reduce(1)
            }
        }
    }

    private fun collectHotbarSources(player: Player): MutableList<HotbarSource> {
        val sources = ArrayList<HotbarSource>(9)

        player.inventory.hotbarContents.forEachIndexed { index, stack ->
            if (stack == null) return@forEachIndexed
            val blockType = stack.type.asBlockType() ?: return@forEachIndexed

            if (stack.amount <= 0 || !blockType.isOccluding) {
                return@forEachIndexed
            }
            sources.add(HotbarSource(index, stack))
        }
        return sources
    }

    // returns null when all sources are exhausted (totalWeight == 0)
    private fun weightedRandomSource(sources: MutableList<HotbarSource>): HotbarSource? {
        var totalWeight = 0
        for (source in sources) {
            if (source.amount > 0) {
                totalWeight += source.amount
            }
        }
        if (totalWeight <= 0) return null

        var roll = Random.nextInt(totalWeight)
        for (source in sources) {
            if (source.amount <= 0) continue
            roll -= source.amount
            if (roll < 0) return source
        }

        return null
    }


    private data class HotbarSource(
        val slot: Int,
        val reference: ItemStack
    ) {
        val material = reference.type
        var amount = reference.amount

        fun reduce(amount: Int) {
            this.amount -= amount
            this.reference.amount -= amount
        }
    }
}