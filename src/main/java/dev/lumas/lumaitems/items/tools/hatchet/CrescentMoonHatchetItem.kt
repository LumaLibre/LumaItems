package dev.lumas.lumaitems.items.tools.hatchet

import dev.lumas.lumaitems.annotations.Ignore
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItem
import dev.lumas.lumaitems.shapes.Cuboid
import dev.lumas.lumaitems.util.extensions.syncDelayed
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue

@Ignore // This disaster should never be used again
class CrescentMoonHatchetItem : CustomItem {

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#8bf6f6&lC&#92e9f6&lr&#98dcf6&le&#9fcff6&ls&#a6c2f6&lc&#acb5f6&le&#b3a8f6&ln&#9fa0f6&lt &#8b99f6&lM&#7791f6&lo&#6289f6&lo&#4e82f6&ln &#3a7af6&lH&#427df6&la&#4a7ff6&lt&#5382f6&lc&#5b84f6&lh&#6387f6&le&#6b89f6&lt",
            mutableListOf("&#6b89f6F&#7099f6r&#74a8f6a&#79b8f6c&#7dc7f6t&#82d7f6u&#86e6f6r&#8bf6f6e"),
            mutableListOf("&#89f3f3\"&#8aeff3T&#8beaf3h&#8ce6f3e &#8de1f3h&#8eddf3e&#8fd9f3a&#90d4f3d &#91d0f3o&#92cbf3f &#93c7f3t&#94c3f3h&#95bef3e &#96baf3a&#97b5f3x&#99b1f3e &#9aadf3w&#9ba8f3a&#9ca4f3s &#9d9ff3m&#9a9df3a&#949bf3d&#8f99f3e &#8a97f3f&#8595f3r&#8093f3o&#7b91f3m","&#758ff3a &#708df3r&#6b8cf3a&#668af3r&#6188f3e &#5b86f3m&#5684f3a&#5182f3t&#4c80f3e&#477ef3r&#427cf3i&#3c7af3a&#3a79f3l&#3c7af3, &#3f7bf3t&#417bf3a&#447cf3k&#477df3e &#497ef3g&#4c7ef3o&#4e7ff3o&#5180f3d &#5380f3c&#5681f3a&#5882f3r&#5b83f3e &#5d83f3o&#6084f3f &#6285f3i&#6586f3t&#6786f3.&#6a87f3\"","","§fBreaks trees in one hit", "","§cCooldown: 1.6 secs"),
            Material.NETHERITE_AXE,
            mutableListOf("crescentmoonhatchet","cuboid"),
            mutableMapOf(Enchantment.EFFICIENCY to 8, Enchantment.UNBREAKING to 9, Enchantment.FORTUNE to 6, Enchantment.MENDING to 1)
        )
        return Pair("crescentmoonhatchet", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        val blockBreakEvent: BlockBreakEvent? = event as? BlockBreakEvent

        when (type) {
            Action.BREAK_BLOCK -> {
                treeFeller(blockBreakEvent!!.block, player)
            }
            else -> return false
        }
        return true
    }

    private fun treeFeller(block: Block, player: Player) {
        if (!block.type.toString().contains("LOG") || player.hasMetadata("BlockTreeFeller")) return
        val cuboid = Cuboid(
            block.location.add(-1.0, -5.0, -1.0),
            block.location.add(1.0, 25.0, 1.0)
        )
        player.setMetadata("BlockTreeFeller", FixedMetadataValue(instance(), true))
        for (i in 0 until cuboid.blockList().size) {
            val b: Block = cuboid.blockList().get(i)
            if (b.type.toString().contains("LOG")) {
                player.breakBlock(b)
                //breakRelativeBlock(b, player, null, "leaves", 0)
            }
        }

        player.syncDelayed(35) {
            player.removeMetadata("BlockTreeFeller", instance())
        }
    }
}