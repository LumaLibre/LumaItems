package dev.lumas.lumaitems.items.tools.mattock

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.manager.CustomItem
import dev.lumas.lumaitems.util.disabling.Disable
import dev.lumas.lumaitems.util.disabling.WorldName
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.ItemStack

@Disable(WorldName.EVENT_NEW)
class DeepAquaPickaxeItem : CustomItem {

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#026161&lD&#106764&le&#1D6C67&le&#2B7269&lp &#477D6F&lA&#548372&lq&#628875&lu&#708E78&la &#8B997D&lP&#999F80&li&#7C826A&lc&#606655&lk&#43493F&la&#272D2A&lx&#0A1014&le",
            mutableListOf("&#1B7070Smooth Edge"),
            mutableListOf("Grants the user the ability to", "silk touch reinforced deepslate.", "", "Attacking wardens with this", "tool will deal significantly", "more damage."),
            Material.NETHERITE_PICKAXE,
            mutableListOf("deepaquapickaxe"),
            mutableMapOf(Enchantment.EFFICIENCY to 9, Enchantment.UNBREAKING to 10, Enchantment.FORTUNE to 4, Enchantment.MENDING to 1)
        )
        item.tier = "&#F34848&lS&#E36643&lo&#D3843E&ll&#C3A239&ls&#B3C034&lt&#A3DE2F&li&#93FC2A&lc&#7DE548&le&#66CD66&l &#50B684&l2&#399EA1&l0&#2387BF&l2&#0C6FDD&l4"
        return Pair("deepaquapickaxe", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.ENTITY_DAMAGE -> {
                event as EntityDamageEvent
                if (event.entity.type != EntityType.WARDEN) {
                    return true
                }

                event.damage *= 3
            }

            Action.BREAK_BLOCK -> {
                event as BlockBreakEvent
                val b = event.block
                if (b.type != Material.REINFORCED_DEEPSLATE) {
                    return true
                }

                event.isDropItems = false
                b.world.dropItemNaturally(b.location, ItemStack(Material.REINFORCED_DEEPSLATE, 1))
                b.world.spawnParticle(Particle.DUST, b.location.add(0.5, 0.5, 0.5), 20, 0.5, 0.5, 0.5, 0.1,
                    DustOptions(b.blockData.mapColor, 1f))
            }

            else -> return false
        }
        return true
    }
}