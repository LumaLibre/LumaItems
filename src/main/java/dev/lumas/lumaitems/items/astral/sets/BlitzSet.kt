package dev.lumas.lumaitems.items.astral.sets

import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.enums.ToolType
import dev.lumas.lumaitems.items.astral.AstralSet
import dev.lumas.lumaitems.items.astral.AstralSetFactory
import dev.lumas.lumaitems.util.Util
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class BlitzSet : AstralSet {

    override fun setItems(): List<ItemStack> {
        val factory = AstralSetFactory("blitz-set", "Blitz", mutableListOf("&#AC87FBSwift"))

        factory.commonEnchants = mutableMapOf(
            Enchantment.UNBREAKING to 6
        )

        factory.astralSetItem(
            Material.DIAMOND_AXE,
            mutableMapOf(Enchantment.EFFICIENCY to 5, Enchantment.FORTUNE to 3),
            mutableListOf("Grants haste while", "being held.")
        )

        factory.astralSetItem(
            Material.ELYTRA,
            mutableMapOf(Enchantment.FEATHER_FALLING to 4, Enchantment.BLAST_PROTECTION to 3),
            mutableListOf("Grants extra speed", "while boosting midair."),
            includeCommonEnchants = true,
            customName = "&#AC87FB&lBlitz &fWings",
            attributeModifiers = null,
            customEnchants = null
        )

        return factory.createdAstralItems
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        val genericMCToolType = ToolType.getToolType(player.inventory.itemInMainHand)

        when (type) {
            Action.RUNNABLE -> {
                if (genericMCToolType == ToolType.AXE) {
                    player.addPotionEffect(PotionEffect(PotionEffectType.HASTE, 220, 0, false, false, true))
                }
            }
            Action.ELYTRA_BOOST -> {
                if (Util.isItemInSlot("blitz-set", EquipmentSlot.CHEST, player)) {
                    player.velocity = player.location.getDirection().multiply(1.5)
                    player.world.playSound(player.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1f)
                }

            }
            else -> return false
        }
        return true
    }

    override fun setIdentifier(): String {
        return "blitz-set"
    }
}

/*factory.astralSetItem(
    Material.DIAMOND_SWORD,
    mutableMapOf(Enchantment.SHARPNESS to 6, Enchantment.LOOTING to 3, Enchantment.SILK_TOUCH to 1),
    mutableListOf("When killing mobs, dropped items", "will automatically be placed", "in your inventory")
)

factory.astralSetItem(
    Material.DIAMOND_AXE,
    mutableMapOf(Enchantment.EFFICIENCY to 7, Enchantment.FORTUNE to 5),
    mutableListOf("When breaking blocks, dropped items", "will automatically be placed", "in your inventory")
)*/