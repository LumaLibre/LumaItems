package dev.lumas.lumaitems.items.astral.sets

import dev.lumas.lumaitems.items.astral.AstralSetFunctions
import dev.lumas.lumaitems.items.astral.ModernAstralSetFactory
import dev.lumas.lumaitems.util.extensions.ItemUtil.isWearing
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class HarbingerSet : AstralSetFunctions("harbinger-set") {

    companion object {
        private val NIGHT_VISION = PotionEffect(PotionEffectType.NIGHT_VISION, 300, 0, false, false, false)
    }

    override fun setItems(): List<ItemStack> {
        val factory = ModernAstralSetFactory(this)
            .setName("Harbinger")
            .withCustomEnchants("Night Vision I")
            .withLore("Allows the ${"wearer".astralColor()} to", "see in the dark.")
            .withCommonVanillaEnchants(
                Enchantment.UNBREAKING to 6,
                        Enchantment.PROTECTION to 6,
            )

        factory.itemBuilder()
            .material(Material.COPPER_HELMET)
            .vanillaEnchants(Enchantment.RESPIRATION to 1)
            .add()
        factory.itemBuilder()
            .material(Material.COPPER_CHESTPLATE)
            .add()
        factory.itemBuilder()
            .material(Material.COPPER_LEGGINGS)
            .vanillaEnchants(Enchantment.SWIFT_SNEAK to 1)
            .add()
        factory.itemBuilder()
            .material(Material.COPPER_BOOTS)
            .vanillaEnchants(Enchantment.FEATHER_FALLING to 2)
            .add()
        factory.itemBuilder()
            .material(Material.ELYTRA)
            .vanillaEnchants(Enchantment.FEATHER_FALLING to 2)
            .add()

        return factory.items()
    }

    override fun onRunnable(player: Player) {
        if (player.isWearing(key)) {
            player.addPotionEffect(NIGHT_VISION)
        }
    }
}