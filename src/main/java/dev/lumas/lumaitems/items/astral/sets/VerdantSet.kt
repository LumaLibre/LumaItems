package dev.lumas.lumaitems.items.astral.sets

import dev.lumas.lumaitems.enums.GenericToolType
import dev.lumas.lumaitems.items.astral.AstralSetFunctions
import dev.lumas.lumaitems.items.astral.ModernAstralSetFactory
import dev.lumas.lumaitems.util.extensions.ItemUtil.isWearing
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class VerdantSet : AstralSetFunctions("verdant-set") {

    companion object {
        private val REGENERATION = PotionEffect(PotionEffectType.REGENERATION, 240, 0, false, false, false)
    }

    override fun setItems(): List<ItemStack> {
        val factory = ModernAstralSetFactory(this)
            .setName("Verdant")
            .withCustomEnchants("Nature's Grasp")
            .withCommonVanillaEnchants(Enchantment.UNBREAKING to 6)

        factory.itemBuilder()
            .lore("${"Breaking blocks".astralColor()} with this",
                "tool will restore hunger",
                "and saturation over time.")
            .vanillaEnchants(Enchantment.EFFICIENCY to 5, Enchantment.FORTUNE to 2)
            .material(Material.DIAMOND_PICKAXE)
            .add()
        factory.itemBuilder()
            .lore("${"Breaking blocks".astralColor()} with this",
                "tool will restore hunger",
                "and saturation over time.")
            .vanillaEnchants(Enchantment.EFFICIENCY to 5, Enchantment.FORTUNE to 2)
            .material(Material.DIAMOND_AXE)
            .add()
        factory.itemBuilder()
            .lore("${"Breaking blocks".astralColor()} with this",
                "tool will restore hunger",
                "and saturation over time.")
            .vanillaEnchants(Enchantment.EFFICIENCY to 5, Enchantment.FORTUNE to 2)
            .material(Material.DIAMOND_HOE)
            .add()
        factory.itemBuilder()
            .lore("${"Breaking blocks".astralColor()} with this",
                "tool will restore hunger",
                "and saturation over time.")
            .vanillaEnchants(Enchantment.EFFICIENCY to 5, Enchantment.FORTUNE to 2)
            .material(Material.DIAMOND_SHOVEL)
            .add()

        factory.itemBuilder()
            .lore(
                "Grants regeneration",
                "while ${"worn".astralColor()}."
            )
            .vanillaEnchants(
                Enchantment.PROTECTION to 4,
                Enchantment.FIRE_PROTECTION to 3,
                Enchantment.BLAST_PROTECTION to 3,
                Enchantment.AQUA_AFFINITY to 1,
                Enchantment.RESPIRATION to 2
            )
            .material(Material.DIAMOND_HELMET)
            .add()
        factory.itemBuilder()
            .lore(
                "Grants regeneration",
                "while ${"worn".astralColor()}."
            )
            .vanillaEnchants(
                Enchantment.PROTECTION to 4,
                Enchantment.FIRE_PROTECTION to 3,
                Enchantment.BLAST_PROTECTION to 3
            )
            .material(Material.DIAMOND_CHESTPLATE)
            .add()
        factory.itemBuilder()
            .lore(
                "Grants regeneration",
                "while ${"worn".astralColor()}."
            )
            .vanillaEnchants(
                Enchantment.PROTECTION to 4,
                Enchantment.FIRE_PROTECTION to 3,
                Enchantment.BLAST_PROTECTION to 3
            )
            .material(Material.DIAMOND_LEGGINGS)
            .add()
        factory.itemBuilder()
            .lore(
                "Grants regeneration",
                "while ${"worn".astralColor()}."
            )
            .vanillaEnchants(
                Enchantment.PROTECTION to 4,
                Enchantment.FIRE_PROTECTION to 3,
                Enchantment.BLAST_PROTECTION to 3,
                Enchantment.FEATHER_FALLING to 4
            )
            .material(Material.DIAMOND_BOOTS)
            .add()

        return factory.items()
    }

    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        val item = player.inventory.itemInMainHand
        if (!item.isToolType(GenericToolType.TOOL)) {
            return
        }

        val food = player.foodLevel
        val saturation = player.saturation
        if (food < 20) {
            player.foodLevel = (food + 1).coerceAtMost(20)
        } else if (saturation < 20f) {
            player.saturation = (saturation + 0.5f).coerceAtMost(20f)
        }
    }

    override fun onRunnable(player: Player) {
        if (player.isWearing(key)) {
            player.addPotionEffect(REGENERATION)
        }
    }
}