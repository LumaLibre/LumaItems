package dev.lumas.lumaitems.items.misc.scale

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import dev.lumas.lumaitems.annotations.Disable
import dev.lumas.lumaitems.enums.WorldName
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.SharedContainers
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.itemInMainHand
import dev.lumas.lumaitems.util.extensions.useNewSharedScaleContainer
import io.papermc.paper.event.entity.EntityCompostItemEvent
import org.bukkit.Material
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack

@Disable(WorldName.EVENT_NEW, hard = true)
class MiniPoppyItem : CustomItemFunctions() {

    companion object {
        private const val KEY = "mini-poppy"
        private const val AMT = -0.5
    }

    override fun createItem(): Pair<String, ItemStack> {

        return ItemFactory.builder()
            .name("<b><#FF5959>M<#FF6259>i<#FF6B58>n<#FF765F>i <#FE8D6C>P<#FD8A84>o<#FB869C>p<#FC7890>p<#FC6984>y")
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .customEnchants("<#ff5959>Flower Extract")
            .lore(
                "A wonderful smelling poppy,",
                "It's so small and cute!",
                "",
                "I wonder if holding it",
                "does anything special?"
            )
            .tier(Tier.VALENTIDE_2025)
            .persistentData(KEY)
            .material(Material.POPPY)
            .attributeModifiers(
                SharedContainers.SCALE.setOperation(AttributeModifier.Operation.ADD_NUMBER).setAmount(AMT).build()
            )
            .autoHat(true)
            .buildPair()
    }


    override fun onPlaceBlock(player: Player, event: BlockPlaceEvent) {
        val item = event.itemInHand
        if (!item.isMatchingItem(KEY)) {
            return
        }
        event.isCancelled = true
    }

    override fun onEntityCompostItem(event: EntityCompostItemEvent) {
        if (!event.item.isMatchingItem(KEY)) return
        event.isCancelled = true
    }

    // TODO: Temporary VV

    override fun onArmorChange(player: Player, event: PlayerArmorChangeEvent) {
        event.newItem.useNewSharedScaleContainer(KEY, AMT)
    }

    override fun onPlayerSwapHands(player: Player, event: PlayerSwapHandItemsEvent) {
        event.player.itemInMainHand.useNewSharedScaleContainer(KEY, AMT)
    }

    override fun onPlayerItemHeld(player: Player, event: PlayerItemHeldEvent) {
        player.itemInMainHand.useNewSharedScaleContainer(KEY, AMT)
    }
}