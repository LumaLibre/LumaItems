package dev.lumas.lumaitems.items.armor.chestplate

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.AttributeContainer
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack

class LiquoriceLoricaItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        val k = "liquorice-lorica"
        return ItemFactory.builder()
            .name("<b><gradient:#251a2b:#51588b:#c46fb0:#b8c7f9:#e1c2eb>Liquorice Lorica</gradient></b>")
            .customEnchants("<#e1c2eb>Sugar Rush")
            .tagline("<gradient:#b8c7f9:#c46fb0>\"A sweet and sticky lorica!\"</gradient>")
            .material(Material.NETHERITE_CHESTPLATE)
            .persistentData(k)
            .tier(Tier.EASTER_2025)
            .attributeModifiers(
                    AttributeContainer.of(k, Attribute.MOVEMENT_SPEED, AttributeModifier.Operation.ADD_NUMBER, 0.025, EquipmentSlotGroup.CHEST)
            )
            .vanillaEnchants(
                Enchantment.PROTECTION to 7,
                Enchantment.UNBREAKING to 8,
                Enchantment.BLAST_PROTECTION to 6,
                Enchantment.MENDING to 1
            )
            .lore(
                "Increases movement and",
                "flight speed when worn."
            )
            .buildPair()
    }

    override fun onArmorChange(player: Player, event: PlayerArmorChangeEvent) {
        if (Util.isItemInSlot("liquorice-lorica", EquipmentSlot.CHEST, player)) {
            player.flySpeed = 0.125f
        } else {
            player.flySpeed = 0.1f // default flySpeed
        }
    }

}