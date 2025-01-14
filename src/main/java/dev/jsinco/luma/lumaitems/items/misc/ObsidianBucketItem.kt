package dev.jsinco.luma.lumaitems.items.misc

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerBucketFillEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.util.*

class ObsidianBucketItem : CustomItemFunctions() {

    companion object {
        private val cooldown: MutableSet<UUID> = mutableSetOf()
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#C18DF5>G<#C784E9>l<#CD7BDE>a<#D372D2>c<#D969C7>i<#DF5FBB>e<#E556AF>r <#DC6F8B>M<#D28485>a<#C89A7E>g<#BDAF78>i<#B3C471>c <#A8DA6B>W<#9EEF64>a<#F24195>n<#F33B89>d</b>")
            .customEnchants("<#F24195>Obsidian I")
            .lore("LORE/INFO TO DO")
            .material(Material.BUCKET)
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .tier("<b><#F24195>K<#F33B89>a<#F3367C>t<#F43070>a<#F52A64>r<#F52557>a<#F61F4B>y <#F42D69>2<#F43377>0<#F33A86>2<#F24195>5</b>")
            .persistentData("obsidianbucket")
            .buildPair()
    }

    private fun onPlayerFillBucket(player: Player, event: PlayerBucketFillEvent) {
        if (cooldown.contains(player.uniqueId)) return

        if (event.blockClicked.type == Material.LAVA ) {
            event.isCancelled = true;

            val newItem = ItemStack(Material.OBSIDIAN)
            player.world.dropItemNaturally(player.location, newItem)

            cooldown.add(player.uniqueId)
            Bukkit.getServer().scheduler.scheduleSyncDelayedTask(instance(), {
                cooldown.remove(player.uniqueId)
            }, 160L)
        }

    }

}
