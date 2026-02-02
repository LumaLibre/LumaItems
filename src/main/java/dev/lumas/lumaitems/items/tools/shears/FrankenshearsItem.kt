package dev.lumas.lumaitems.items.tools.shears

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.manager.CustomItem
import dev.lumas.lumaitems.util.extensions.breakNaturallyWithLog
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class FrankenshearsItem : CustomItem {
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#79a617&lF&#749d18&lr&#709319&la&#6b8a1a&ln&#66811b&lk&#61771c&le&#5d6e1e&ln&#58651f&ls&#535b20&lh&#4e5221&le&#4a4922&la&#453f23&lr&#403624&ls",
            mutableListOf("&#79a617S&#6a9918k&#5b8b19u&#4c7e1al&#3d701bl &#2e631cC&#31651dr&#46771eu&#5c891fs&#729b20h&#87ad20e&#9dbf21r"),
            mutableListOf("&#79a617\"&#77a217B&#759e18o&#739a18r&#719519n &#6e9119f&#6c8d1ar&#6a891ao&#68851bm &#66811ba &#647d1cm&#62781co&#60741dn&#5e701ds&#5b6c1et&#59681ee&#57641fr&#555f1f'&#535b20s &#515720a&#4f5321m&#4d4f21b&#4b4b22i&#484722t&#464223i&#443e23o&#423a24n&#403624\"", "", "This tool allows the user", "to break any head instantly"),
            Material.SHEARS,
            mutableListOf("frakenshears"),
            mutableMapOf(Enchantment.EFFICIENCY to 6, Enchantment.UNBREAKING to 7, Enchantment.MENDING to 1)
        )
        item.tier = "&#c46bfb&lH&#c86eee&la&#cd71e2&ll&#d174d5&ll&#d677c8&lo&#da7abc&lm&#de7daf&la&#e380a2&lr&#e78395&le&#eb8689&ls &#f0897c&l2&#f48c6f&l0&#f98f63&l2&#fd9256&l3"
        return Pair("frakenshears", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        val interact: PlayerInteractEvent? = event as? PlayerInteractEvent

        when (type) {
            Action.LEFT_CLICK -> {
                val block = interact!!.clickedBlock ?: return false
                if (block.toString().contains("HEAD")) {
                    block.breakNaturallyWithLog(player)
                    block.world.spawnParticle(Particle.BLOCK, block.location.add(0.5, 0.5, 0.5), 10, 0.5, 0.5, 0.5, 0.1, block.blockData)
                }
            }
            else -> return false
        }
        return true
    }

}