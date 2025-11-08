package dev.jsinco.luma.lumaitems.items.astral.sets

import dev.jsinco.luma.lumaitems.items.astral.AstralSet
import dev.jsinco.luma.lumaitems.items.astral.AstralSetFactory
import dev.jsinco.luma.lumaitems.enums.Action
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ArchaelSet : AstralSet {

    override fun setItems(): List<ItemStack> {
        val astralSetFactory = AstralSetFactory("archael-set", "Archael")

        astralSetFactory.astralSetItem(
            Material.ELYTRA,
            mutableMapOf(Enchantment.PROTECTION to 6, Enchantment.PROJECTILE_PROTECTION to 5, Enchantment.UNBREAKING to 5,
                Enchantment.FEATHER_FALLING to 4),
            mutableListOf(),
            true,
            null,
            "&#AC87FB&lArchael &fWings",
            null
        )

        return astralSetFactory.createdAstralItems

    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        return false
    }

    override fun identifier(): String {
        return "archael-set"
    }
}