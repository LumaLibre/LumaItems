package dev.lumas.lumaitems.items.astral.sets

import com.gamingmesh.jobs.Jobs
import dev.lumas.lumaitems.items.astral.AstralSet
import dev.lumas.lumaitems.items.astral.AstralSetFactory
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.enums.ToolType
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack


class FalterSet : AstralSet {

    override fun setItems(): List<ItemStack> {
        val astralSetFactory = AstralSetFactory("falter-set", "Falter", mutableListOf("&#AC87FBFoster"))
        val commonLore = mutableListOf("Damage dealt to enemies scales", "with %s Job level.")

        astralSetFactory.commonEnchants = mutableMapOf(
            Enchantment.SHARPNESS to 7,
            Enchantment.SMITE to 7,
            Enchantment.UNBREAKING to 8,
            Enchantment.MENDING to 1
        )

        astralSetFactory.astralSetItem(
            Material.DIAMOND_PICKAXE,
            mutableMapOf(Enchantment.EFFICIENCY to 8, Enchantment.FORTUNE to 5),
            commonLore.map { it.format("Miner") }
        )
        astralSetFactory.astralSetItem(
            Material.DIAMOND_HOE,
            mutableMapOf(Enchantment.EFFICIENCY to 8,Enchantment.FORTUNE to 5),
            commonLore.map { it.format("Farmer") }
        )
        astralSetFactory.astralSetItem(
            Material.DIAMOND_AXE,
            mutableMapOf(Enchantment.EFFICIENCY to 8, Enchantment.FORTUNE to 4, Enchantment.LOOTING to 6),
            commonLore.map { it.format("Lumberjack") }
        )
        astralSetFactory.astralSetItem(
            Material.FISHING_ROD,
            mutableMapOf(Enchantment.LURE to 5, Enchantment.LUCK_OF_THE_SEA to 6),
            commonLore.map { it.format("Fisherman") }
        )

        return astralSetFactory.createdAstralItems
    }

    override fun identifier(): String {
        return "falter-set"
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.ENTITY_DAMAGE -> {
                event as EntityDamageByEntityEvent
                event.damage += (getJobLevel(player.inventory.itemInMainHand.type, player) / 4.0)
            }
            else -> return false
        }
        return true
    }


    private fun getJobLevel(material: Material, player: Player): Int {
        val jobsPlayer = Jobs.getPlayerManager().getJobsPlayer(player)

        val genericMCToolType = ToolType.getToolType(material)
        val job = when (genericMCToolType) {
            ToolType.PICKAXE -> Jobs.getJob("Miner")
            ToolType.HOE -> Jobs.getJob("Farmer")
            ToolType.AXE -> Jobs.getJob("Lumberjack")
            ToolType.FISHING_ROD -> Jobs.getJob("Fisherman")
            else -> return 0
        }
        return if (jobsPlayer.isInJob(job)) jobsPlayer.getJobProgression(job).level else 0
    }

}