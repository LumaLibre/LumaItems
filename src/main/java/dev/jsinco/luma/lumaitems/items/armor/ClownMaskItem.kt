package dev.jsinco.luma.lumaitems.items.armor

import dev.jsinco.luma.lumaitems.enums.DefaultAttributes
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.manager.FileManager
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class ClownMaskItem : CustomItemFunctions() {

    companion object {
        private val headTextures: List<String> = FileManager("heads.yml").generateYamlFile().getStringList("clown-masks")
        private const val KEY = "clownmask"
    }

    private val namespacedKey = NamespacedKey(instance(), KEY)

    override fun createItem(): Pair<String, ItemStack> {

        return ItemFactory.builder()
            .name("<b><#FA909D>C<#F8C2AE>l<#F6F4BE>o<#F7CED4>w<#F7A8E9>n <#EBC1C9>M<#DDFF92>a<#C2ECC9>s<#A7D9FF>k</b>")
            .material(Material.PLAYER_HEAD)
            .customEnchants("<gray>Unbreakable", "<dark_gray>Fortitude")
            .tier(Tier.CARNIVAL_2024)
            .b64PHead(headTextures.random())
            .persistentData(KEY)
            .attributeModifiers(DefaultAttributes.NETHERITE_HELMET.appendThenGetAttributes(Attribute.MAX_HEALTH, AttributeModifier(NamespacedKey(instance(), KEY), 6.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HEAD)))
            .vanillaEnchants(mutableMapOf(Enchantment.PROTECTION to 7, Enchantment.RESPIRATION to 4, Enchantment.AQUA_AFFINITY to 2))
            .lore("Disguise yourself in this", "neat mask!", "", "Wearing this mask grants", "an extra <#FA909D>3</#FA909D> hearts.", "", "<red>Same stats as Netherite</red>")
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (event.item?.itemMeta?.persistentDataContainer?.has(namespacedKey, PersistentDataType.SHORT) == true) {
            event.isCancelled = true
            if (player.equipment.helmet == null) {
                player.equipment.helmet = event.item
                event.item!!.amount -= 1
            }
        }
    }
}