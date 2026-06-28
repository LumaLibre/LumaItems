package dev.lumas.lumaitems.items.tools.hatchet

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.tags.Kind
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

class SpringHatchetItem : CustomItemFunctions() {

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#fab26d:#f57b61:#e15c64:#aabd83:#839b88>Spring Hatchet</gradient></b>")
            .customEnchants("<#e15c64>Glasscutter")
            .material(Material.NETHERITE_AXE)
            .tier(Tier.EASTER_2025)
            .persistentData("spring-hatchet")
            .vanillaEnchants(
                Enchantment.EFFICIENCY to 10,
                Enchantment.SILK_TOUCH to 1,
                Enchantment.UNBREAKING to 5,
                Enchantment.MENDING to 1,
            )
            .lore(
                "Breaks all types of",
                "glass instantly."
            )
            .buildPair()
    }

    override fun onLeftClick(player: Player, event: PlayerInteractEvent) {
        if (!Util.isItemInSlot("spring-hatchet", EquipmentSlot.HAND, player)) {
            return
        }
        val block = event.clickedBlock ?: return
        if (!Kind.GLASS.isTagged(block.type)) {
            return
        }
        player.breakBlock(block)
    }

}

/*
    companion object {
        private val persistentKey = Util.namespacedKey("color-crystal-hatchet-color")
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#feaeac:#fed5a6:#fcffb6:#cafebf:#9bf6ff:#a0c3ff:#bdb1fe:#fec7fe>Color Crystal Hatchet</gradient></b>")
            .customEnchants("<#bdb1fe>Glasscutter", "<#ec9be>Style")
            .material(Material.NETHERITE_SHOVEL)
            .tier(Tier.EASTER_2025)
            .persistentData("color-crystal-hatchet")
            .vanillaEnchants()
            .lore()
            .build()
            .addPersistentData(persistentKey, PersistentDataType.STRING, "")
            .toReturnablePair()
    }

    override fun onPlayerSwapHands(player: Player, event: PlayerSwapHandItemsEvent) {
        val item = player.inventory.itemInMainHand
        var color = Util.getPersistentKey(item, persistentKey, PersistentDataType.STRING) ?: return

        if (player.isSneaking) {
            color = ""
        } else {
            val coloredGlassBlocks = BlockConstants.COLORED_GLASS.materials
            val currentGlassColor = Util.enumValueOfOrNull(Material::class.java, color)
            if (currentGlassColor != null) {
                val nextColorIndex = (coloredGlassBlocks.indexOf(currentGlassColor) + 1) % coloredGlassBlocks.size
                color = coloredGlassBlocks[nextColorIndex].name
            }
        }
    }
*/