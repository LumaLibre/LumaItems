package dev.jsinco.luma.lumaitems.items.nests

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.disabling.Ignore
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

@Ignore
class LoversBondItemCapsule : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#D4668E>L<#DF7F9A>o<#EA98A6>v<#F4B1B2>e<#FFCABE>r<#FFD5C9>'<#FFDFD4>s <#FEF4EA>B<#FEE0E1>o<#FFCCD9>n<#FFB8D0>d <#FDD2C6>I<#FCDEC1>t<#FBEBBC>e<#F0E3CC>m<#E5DCDD> C<#D9D4ED>a<#CECCFD>p<#CECCFD>s<#CECCFD>u<#CECCFD>l<#CECCFD>e")
            .material(Material.RED_DYE)
            .tier(Tier.VALENTIDE_2025)
            .persistentData("lovers-bond-capsule")
            .buildPair()
    }


    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        player.sendMessage("You have right-clicked the lovers bond capsule")

        val loversBondMattockItem = LoversBondMattockItem().createItem().second
        Util.giveItem(player, loversBondMattockItem.asQuantity(2))
    }
}

@Ignore
class LoversBondMattockItem : CustomItemFunctions() {


    companion object {
        val key = NamespacedKey(LumaItems.getInstance(), "lovers-bond-mattock")
        val secretKey = NamespacedKey(LumaItems.getInstance(), "lovers-bond-secret")
        val cachedBonds: MutableMap<UUID, String> = mutableMapOf()
    }

    var secret: () -> String = {
        val chars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        (1..7)
            .map { chars.random() }
            .joinToString("")
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#D4668E>L<#DF7F9A>o<#EA98A6>v<#F4B1B2>e<#FFCABE>r<#FFD5C9>'<#FFDFD4>s <#FEF4EA>B<#FEE0E1>o<#FFCCD9>n<#FFB8D0>d <#FDD2C6>M<#FCDEC1>a<#FBEBBC>t<#F0E3CC>t<#E5DCDD>o<#D9D4ED>c<#CECCFD>k")
            .customEnchants("<#D4668E>Better Together")
            .lore("TODO")
            .material(Material.NETHERITE_PICKAXE)
            .tier(Tier.VALENTIDE_2025)
            .persistentData(key.key)
            .stringPersistentDatas(mutableMapOf(secretKey to secret()))
            .buildPair()
    }


    // Functionality

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        val bondedPlayer = getBondedPlayer(player) ?: return
        player.sendMessage("You are bonded with ${bondedPlayer.name}")
    }



    // Secrets

    override fun asyncGlobalTask() {
        if (cachedBonds.isNotEmpty()) {
            cachedBonds.entries.retainAll { entry ->
                val player = Bukkit.getPlayer(entry.key) ?: return@retainAll false
                findSecret(player) != null
            }
        }

        for (player in Bukkit.getOnlinePlayers()) {
            if (cachedBonds.containsKey(player.uniqueId)) continue // already cached
            val secret = findSecret(player) ?: continue
            println("Found secret for ${player.name}: $secret")
            cachedBonds[player.uniqueId] = secret
        }
        println("Cached bonds: $cachedBonds")
    }

    private fun getBondedPlayer(seeker: Player): Player? {
        val secret = cachedBonds[seeker.uniqueId] ?: return null
        for (bond in cachedBonds) {
            if (bond.value == secret && bond.key != seeker.uniqueId) {
                return Bukkit.getPlayer(bond.key)
            }
        }
        return null
    }

    private fun findSecret(player: Player): String? {
        for (item in player.inventory.contents.filterNotNull()) {
            val secret = getSecret(item)
            if (secret != null) {
                return secret
            }
        }
        return null
    }

    private fun getSecret(item: ItemStack): String? {
        return item.itemMeta?.persistentDataContainer?.get(secretKey, PersistentDataType.STRING)
    }

}