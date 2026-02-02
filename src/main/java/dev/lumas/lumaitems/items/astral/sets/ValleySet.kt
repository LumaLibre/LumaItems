package dev.lumas.lumaitems.items.astral.sets

import dev.lumas.lumaitems.items.astral.AstralSet
import dev.lumas.lumaitems.items.astral.AstralSetFactory
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.enums.ToolType
import dev.lumas.lumaitems.util.extensions.setAirWithLog
import org.bukkit.FluidCollisionMode
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.random.Random

class ValleySet : AstralSet {

    override fun setItems(): List<ItemStack> {
        val astralSetFactory = AstralSetFactory("valley-set", "Valley", mutableListOf("&#AC87FBIlk"))
        astralSetFactory.commonEnchants = mutableMapOf(
            Enchantment.MENDING to 1,
            Enchantment.UNBREAKING to 5,
        )

        astralSetFactory.astralSetItem(
            Material.GOLDEN_SHOVEL,
            mutableMapOf(Enchantment.EFFICIENCY to 6, Enchantment.FORTUNE to 5),
            mutableListOf("Has the ability to remove", "water from the direction the", "user is looking.")
        )

        astralSetFactory.astralSetItem(
            Material.GOLDEN_SWORD,
            mutableMapOf(Enchantment.SHARPNESS to 6, Enchantment.SMITE to 6, Enchantment.SWEEPING_EDGE to 4),
            mutableListOf("Grants the user potion buffs", "upon damaging an enemy.")
        )

        astralSetFactory.astralSetItem(
            Material.GOLDEN_AXE,
            mutableMapOf(Enchantment.EFFICIENCY to 6, Enchantment.FORTUNE to 5),
            mutableListOf()
        )

        astralSetFactory.astralSetItem(
            Material.GOLDEN_HOE,
            mutableMapOf(Enchantment.EFFICIENCY to 6, Enchantment.FORTUNE to 5),
            mutableListOf("Has a chance to drop rare", "crops when breaking blocks.")
        )

        astralSetFactory.astralSetItem(
            Material.FISHING_ROD,
            mutableMapOf(Enchantment.LURE to 4, Enchantment.LUCK_OF_THE_SEA to 4),
            mutableListOf()
        )

        return astralSetFactory.createdAstralItems
    }

    override fun setIdentifier(): String {
        return "valley-set"
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        val tool = ToolType.getToolType(player.inventory.itemInMainHand)

        when (type) {
            Action.LEFT_CLICK, Action.RIGHT_CLICK -> {
                if (tool != ToolType.SHOVEL) return false
                val targetBlock = player.getTargetBlockExact(45, FluidCollisionMode.ALWAYS) ?: return false

                // .name.contains("WATER")
                if (targetBlock.type == Material.WATER) {
                    targetBlock.setAirWithLog(player)
                }
            }

            Action.ENTITY_DAMAGE -> {
                if (tool != ToolType.SWORD) return false
                event as EntityDamageByEntityEvent
                if (event.entity !is Monster) return false
                player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 100, 0, false, false, false))
                player.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, 100, 0, false, false, false))
            }

            Action.BREAK_BLOCK -> {
                if (tool == ToolType.HOE && Random.nextInt(100) < 3) {
                    event as BlockBreakEvent
                    event.block.world.dropItem(event.block.location,
                        ItemStack(if (Random.nextBoolean()) Material.WHEAT else Material.GOLDEN_CARROT))
                }
            }

            else -> {
                return false
            }
        }
        return true
    }
}