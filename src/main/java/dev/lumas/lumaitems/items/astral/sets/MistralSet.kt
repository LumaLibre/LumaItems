package dev.lumas.lumaitems.items.astral.sets

import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.enums.GenericToolType
import dev.lumas.lumaitems.enums.ToolType
import dev.lumas.lumaitems.items.astral.AstralSet
import dev.lumas.lumaitems.items.astral.AstralSetFactory
import dev.lumas.lumaitems.model.AttributeContainer
import dev.lumas.lumaitems.relics.RelicCrafting
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class MistralSet : AstralSet {

    override fun setItems(): List<ItemStack> {
        val astralSetFactory = AstralSetFactory("mistral-set", "Mistral", mutableListOf("&#AC87FBSwift"))

        astralSetFactory.commonEnchants = mutableMapOf(
            Enchantment.PROTECTION to 4, Enchantment.PROJECTILE_PROTECTION to 5, Enchantment.FEATHER_FALLING to 5,
            Enchantment.SHARPNESS to 6, Enchantment.UNBREAKING to 7, Enchantment.SWEEPING_EDGE to 4,
            Enchantment.EFFICIENCY to 6, Enchantment.SILK_TOUCH to 1, //Enchantment.MENDING to 1,
            Enchantment.LURE to 4, Enchantment.LUCK_OF_THE_SEA to 4
        )

        val materials: List<Material> = listOf(
            Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS,
            Material.IRON_BOOTS, Material.IRON_SWORD, Material.IRON_PICKAXE,
            Material.FISHING_ROD
        )

        for (material in materials) {
            val genericToolType = GenericToolType.getGenericToolType(material)
            val genericMCToolType = ToolType.getToolType(material)

            astralSetFactory.astralSetItemGenericEnchantOnly(
                material,

                if (genericToolType == GenericToolType.ARMOR) {
                    mutableListOf("&6Set Bonus:&7 Speed I")
                } else {
                    mutableListOf("Grants extra speed", "while being held.")
                },
                AttributeContainer.ofMap(Attribute.MOVEMENT_SPEED, setIdentifier(), 0.025, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ANY))
        }

        return astralSetFactory.createdAstralItems
    }

    override fun setIdentifier(): String {
        return "mistral-set"
    }
    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.RUNNABLE -> {
                if (RelicCrafting.hasFullSet("mistral-set", player)) {
                    player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 240, 0, false, false, false))
                }
            }

            else -> {
                return false
            }
        }
        return true
    }
}