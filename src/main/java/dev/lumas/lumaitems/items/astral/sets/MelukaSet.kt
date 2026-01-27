package dev.lumas.lumaitems.items.astral.sets

import dev.lumas.lumaitems.items.astral.AstralSet
import dev.lumas.lumaitems.items.astral.AstralSetFactory
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.enums.GenericToolType
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.disabling.Disable
import dev.lumas.lumaitems.util.disabling.WorldName
import dev.lumas.lumaitems.util.extensions.ItemUtil.isWearing
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

@Disable(WorldName.EVENT_NEW)
class MelukaSet : AstralSet {

    override fun setItems(): List<ItemStack> {
        val astralSetFactory = AstralSetFactory("meluka-set", "Meluka", mutableListOf("&#AC87FBMarine"))

        val materials = listOf(
            Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS,
            Material.GOLDEN_BOOTS, Material.GOLDEN_PICKAXE, Material.GOLDEN_SHOVEL
        )

        astralSetFactory.commonEnchants = mutableMapOf(
            Enchantment.PROTECTION to 5, Enchantment.SHARPNESS to 5, Enchantment.UNBREAKING to 5,
            Enchantment.SWEEPING_EDGE to 3, Enchantment.EFFICIENCY to 4, Enchantment.SILK_TOUCH to 1,
            Enchantment.DEPTH_STRIDER to 3, Enchantment.AQUA_AFFINITY to 2, Enchantment.RESPIRATION to 3
        )

        for (material in materials) {
            val lore = if (GenericToolType.getGenericToolType(material) == GenericToolType.TOOL) {
                mutableListOf("Breaking blocks underwater will", "automatically teleport them", "to the user's inventory.")
            } else {
                mutableListOf("Grants the wearer dolphin's", "grace while in water.")
            }

            astralSetFactory.astralSetItemGenericEnchantOnly(
                material,
                lore
            )
        }

        return astralSetFactory.createdAstralItems
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        if (!player.isInWater) return false
        when (type) {
            Action.RUNNABLE -> {
                if (player.isWearing("meluka-set")) {
                    player.addPotionEffect(PotionEffect(PotionEffectType.DOLPHINS_GRACE, 240, 0, false, false, false))
                }
            }
            Action.BREAK_BLOCK -> {
                event as BlockBreakEvent
                val item = player.inventory.itemInMainHand
                if (GenericToolType.getGenericToolType(item.type) != GenericToolType.TOOL) return false

                event.isDropItems = false

                val drops = event.block.getDrops(item)
                for (drop in drops) {
                    Util.giveItem(player, drop)
                }
            }
            else -> return false
        }
        return true
    }

    override fun identifier(): String {
        return "meluka-set"
    }
}