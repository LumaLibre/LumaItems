package dev.jsinco.luma.lumaitems.items.misc

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.inventory.ItemStack

class FrostedAlluringItem : CustomItemFunctions() {

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#CCEAD5>F<#C9E5DF>r<#C6DFE9>o<#C3DAF3>s<#C0D4FD>t<#CDD2F1>e<#DAD0E5>d <#F4CCCC>A<#F7DDD4>l<#F9EEDC>l<#FCFFE4>u<#EFFAE4>r<#E2F5E4>i<#D4EFE3>n<#C7EAE3>g")
            .customEnchants("<#fcffe4>Worker")
            .lore("When fishing, bites will", "instantly be reeled in.")
            .material(Material.FISHING_ROD)
            .persistentData("frosted-alluring")
            .vanillaEnchants(Enchantment.LURE to 4, Enchantment.LUCK_OF_THE_SEA to 5, Enchantment.UNBREAKING to 7, Enchantment.MENDING to 1)
            .tier(Tier.WINTER_2024)
            .buildPair()
    }


    override fun onFish(player: Player, event: PlayerFishEvent) {
        if (event.state == PlayerFishEvent.State.BITE) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(instance(), Runnable {
                LumaItems.getProtocolManager()?.receiveClientPacket(player, PacketContainer(PacketType.Play.Client.USE_ITEM))
            }, 1L)
        }
    }

}