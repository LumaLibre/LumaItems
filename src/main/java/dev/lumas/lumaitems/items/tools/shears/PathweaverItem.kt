package dev.lumas.lumaitems.items.tools.shears

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.shapes.Cuboid
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.addCooldown
import dev.lumas.lumaitems.util.extensions.canBuild
import dev.lumas.lumaitems.util.extensions.dustOptions
import dev.lumas.lumaitems.util.extensions.hotbarContents
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.isOnCooldown
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.setBlockDataWithLog
import kotlin.random.Random
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class PathweaverItem : CustomItemFunctions() {

    private companion object {
        private val KEY = "pathweaver".namespacedKey()
        private val RED_DUST = "#FF9595".dustOptions()
        private val GREEN_DUST = "#CEFACF".dustOptions()
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.Companion.builder()
            .name("<b><gradient:#948D27:#CD5278:#EC9A31>Pathweaver</gradient></b>")
            .material(Material.SHEARS)
            .persistentData(KEY)
            .tier(Tier.WONDERLAND_2026)
            .vanillaEnchants(Enchantment.UNBREAKING to 3, Enchantment.MENDING to 1)
            .lore(
                "<#CD5278>Right-click</#CD5278> grass to",
                "replace a <#CD5278>3x1</#CD5278> area",
                "with random full blocks",
                "from your hotbar.",
                "",
                "But beware, it may <#CD5278>steal</#CD5278>",
                "some blocks...",
                "",
                "<red>Cooldown: 1.5s"
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
        player.addCooldown(this, 30)

        val sources = collectHotbarSources(player)

        if (sources.isEmpty()) {
            spawnDust(clickedBlock.location, RED_DUST)
            return
        }

        val loc1 = clickedBlock.location.clone().add( 1.0, 0.0,  1.0)
        val loc2 = clickedBlock.location.clone().add(-1.0, 0.0, -1.0)
        val cuboid = Cuboid(loc1, loc2)

        for (target in cuboid.blockList()) {
            if (target.type != Material.GRASS_BLOCK) continue
            val source = weightedRandomSource(sources) ?: run {
                spawnDust(target.location, RED_DUST)
                return
            }
            source.reduce(1)
            target.setBlockDataWithLog(player, source.material)
            spawnDust(target.location, GREEN_DUST)
        }

        item.damage(7, player)

        // 50% chance to silently steal 1-3 extra blocks from the hotbar
        if (random.nextBoolean()) {
            val stolen = random.nextInt(1, 4)
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
            if (stack.amount <= 0 || !blockType.isOccluding) return@forEachIndexed
            sources.add(HotbarSource(index, stack))
        }

        return sources
    }

    // returns null when all sources are exhausted (totalWeight == 0)
    private fun weightedRandomSource(sources: MutableList<HotbarSource>): HotbarSource? {
        var totalWeight = 0
        for (source in sources) {
            if (source.amount > 0) totalWeight += source.amount
        }
        if (totalWeight <= 0) return null

        var roll = Random.Default.nextInt(totalWeight)
        for (source in sources) {
            if (source.amount <= 0) continue
            roll -= source.amount
            if (roll < 0) return source
        }

        return sources.lastOrNull { it.amount > 0 }
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

    private fun spawnDust(location: Location, dustOptions: Particle.DustOptions) {
        location.world?.spawnParticle(Particle.DUST, location.clone().add(0.5, 1.0, 0.5), 5, 0.2, 0.2, 0.2, 0.0, dustOptions)
    }
}