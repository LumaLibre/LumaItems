package dev.lumas.lumaitems.items.tools.mattock

import dev.lumas.lumaitems.annotations.Disable
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.enums.WorldName
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItem
import dev.lumas.lumaitems.shapes.Cuboid
import dev.lumas.lumaitems.util.tags.Kind
import java.util.Random
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue

@Disable(WorldName.EVENT_NEW)
class SpellboundShattererItem : CustomItem {

    companion object {
        private val dustOptions: List<DustOptions> = listOf(
            DustOptions(Color.fromRGB(118, 0, 117), 1f),
            DustOptions(Color.fromRGB(245, 96, 1), 1f),
            DustOptions(Color.fromRGB(61, 143, 0), 1f)
        )
    }

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#760075&lS&#840b68&lp&#92155b&le&#a0204e&ll&#ae2b41&ll&#bd3535&lb&#cb4028&lo&#d94b1b&lu&#e7550e&ln&#f56001&ld &#e16501&lS&#cc6a01&lh&#b87001&la&#a37501&lt&#8f7a00&lt&#7a7f00&le&#668500&lr&#518a00&le&#3d8f00&lr",
            mutableListOf("&#f56001B&#e9570cu&#de4f16r&#d24621s&#c73d2bt&#bb3436i&#b02c40n&#a4234bg &#991a55S&#8d1160p&#82096ae&#760075l&#760075l"),
            mutableListOf("&#760075\"&#80086cM&#8a0f62o&#941759u&#9f1f50n&#a92647t&#b32e3da&#bd3634i&#c73d2bn&#d14521s &#dc4d18t&#e6540fr&#f05c06e&#ee6201m&#df6601b&#d06901l&#c16d01e &#b37101b&#a47501e&#957800l&#877c00o&#788000w&#698400.&#5a8700.&#4c8b00.&#3d8f00\"","","Breaking blocks with this pickaxe","grants the chance to shatter" ,"nearby blocks in a 5x5 radius"),
            Material.NETHERITE_PICKAXE,
            mutableListOf("spellboundshatterer"),
            mutableMapOf(Enchantment.EFFICIENCY to 8, Enchantment.SILK_TOUCH to 1, Enchantment.UNBREAKING to 10, Enchantment.MENDING to 1)
        )
        item.tier = "&#c46bfb&lH&#c86eee&la&#cd71e2&ll&#d174d5&ll&#d677c8&lo&#da7abc&lm&#de7daf&la&#e380a2&lr&#e78395&le&#eb8689&ls &#f0897c&l2&#f48c6f&l0&#f98f63&l2&#fd9256&l3"
        return Pair("spellboundshatterer", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        if (Random().nextInt(200) >= 3) return false

        when (type) {
            Action.BREAK_BLOCK -> {
                event as BlockBreakEvent
                shatterNearbyBlocks(event.block, player)

            }
            else -> return false
        }
        return true
    }

    private fun shatterNearbyBlocks(block: Block, player: Player) {
        if (player.hasMetadata("shattering")) return // Prevents infinite recursion

        val cuboid = Cuboid(
            block.location.add(2.0, 2.0, 2.0),
            block.location.add(-2.0, -2.0, -2.0)
        )
        block.world.playSound(block.location, Sound.ENTITY_WITCH_AMBIENT, 0.5f, 1f)
        block.world.playSound(block.location, Sound.ENTITY_GENERIC_EXPLODE, 0.2f, 1f)

        player.setMetadata("shattering", FixedMetadataValue(instance(), true))
        for (b in cuboid.blockList()) {
            if (Kind.BLACKLIST.isTagged(b.type)) continue

            if (Random().nextInt(50) <= 5) {
                b.world.spawnParticle(Particle.DUST, block.location, 10, 0.5, 0.5, 0.5, 0.1, dustOptions.random())
            }
            player.breakBlock(b)
            b.world.spawnParticle(Particle.BLOCK, b.location.add(0.5, 0.5, 0.5), 10, 0.5, 0.5, 0.5, 0.1, b.blockData)
        }
        player.removeMetadata("shattering", instance())
    }
}