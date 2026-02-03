package dev.lumas.lumaitems.items.tools.mattock

import dev.lumas.lumaitems.enums.BlockConstants
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.util.Executors.syncDelayed
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.disabling.Disable
import dev.lumas.lumaitems.util.disabling.WorldName
import dev.lumas.lumaitems.util.extensions.getOreColor
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.ItemMergeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

@Disable(WorldName.EVENT_NEW)
class AutumnsMattockItem : CustomItemFunctions() {

    companion object {
        private val KEY = Util.namespacedKey("autumns-mattock")
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#cd3c33:#e16120:#eb7316:#f38d0e:#fbb708>Autumn's Mattock</gradient></b>")
            .customEnchants("<#eb7316>Full Harvest")
            .material(Material.NETHERITE_PICKAXE)
            .persistentData(KEY)
            .tier(Tier.HALLOWEEN_2025)
            .vanillaEnchants(
                Enchantment.UNBREAKING to 10,
                Enchantment.FORTUNE to 5,
                Enchantment.EFFICIENCY to 7,
                Enchantment.MENDING to 1
            )
            .lore(
                "<#eb7316>Breaking ores</#eb7316> with this",
                "tool will on occasion,",
                "yield a surplus amount",
                "of drops. Or none at all.",
            )
            .buildPair()
    }


    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        val block = event.block
        if (!BlockConstants.ORES.contains(block.type)) return

        val chance = random().nextInt(100)
        if (chance >= 25) return // 75% chance to not trigger

        val loc = block.location.toCenterLocation()

        event.isDropItems = false
        if (chance <= 12) {
            // 12% chance to drop nothing
            block.world.playSound(block.location, Sound.BLOCK_END_PORTAL_SPAWN, 0.1f, 1f)
        } else {

            block.world.playSound(block.location, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR, 0.5f, 1f)
            block.world.spawnParticle(Particle.DUST, loc, 20, 0.5, 0.5, 0.5, 0.1, Particle.DustOptions(block.getOreColor() ?: Color.RED, 1f))

            val drops = block.getDrops(player.inventory.itemInMainHand)

            for (itemStack in drops) {
                itemStack.amount *= random().nextInt(2, 6)
                for (i in 0 until itemStack.amount.coerceAtMost(72)) {
                    dropItem(loc, itemStack.asOne(), player)
                }
            }
        }
    }

    override fun onItemMerge(player: Player, event: ItemMergeEvent) {
        event.isCancelled = true
    }

    private fun dropItem(location: Location, itemStack: ItemStack, player: Player) {
        val item = location.world.dropItem(location, itemStack)
        item.thrower = player.uniqueId
        Util.setPersistentKey(item, KEY, PersistentDataType.SHORT, 1)
        item.syncDelayed(20) {
            Util.removePersistentKey(item, KEY)
        }
    }


}