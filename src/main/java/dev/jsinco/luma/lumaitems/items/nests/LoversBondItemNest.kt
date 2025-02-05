package dev.jsinco.luma.lumaitems.items.nests

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.enums.BlockConstants
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.obj.Cooldowns
import dev.jsinco.luma.lumaitems.shapes.Sphere
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.UUID


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


class LoversBondMattockItem : CustomItemFunctions() {


    companion object {
        val key = NamespacedKey(LumaItems.getInstance(), "lovers-bond-mattock")
        val secretKey = NamespacedKey(LumaItems.getInstance(), "lovers-bond-secret")
        val cachedBonds: MutableMap<UUID, String> = mutableMapOf()
    }

    var secret: () -> String = {
        val chars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        (1..7).map { chars.random() }.joinToString("")
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#D4668E>L<#DF7F9A>o<#EA98A6>v<#F4B1B2>e<#FFCABE>r<#FFD5C9>'<#FFDFD4>s <#FEF4EA>B<#FEE0E1>o<#FFCCD9>n<#FFB8D0>d <#FDD2C6>M<#FCDEC1>a<#FBEBBC>t<#F0E3CC>t<#E5DCDD>o<#D9D4ED>c<#CECCFD>k")
            .customEnchants("<#D4668E>Better Together")
            .lore(
                "Each mattock is intended to",
                "be held onto by another player.",
                "",
                "Whilst your partner has the other",
                "mattock in their inventory, mined",
                "ore veins will be broken instantly.",
                "",
                "While holding this mattock,",
                "<#D4668E>sneak <white>& press your <#D4668E>swap key (F)",
                "to teleport to your partner."
            )
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

    override fun onPlayerSwapHands(player: Player, event: PlayerSwapHandItemsEvent) {
        if (!player.isSneaking || Cooldowns.isOnCooldown(this, player.uniqueId)) {
            return
        }
        val bondedPlayer = getBondedPlayer(player) ?: return run {
            player.sendActionBar(MiniMessageUtil.mm("<red>\uD83D\uDC94 <gray>Couldn't find your partner."))
        }
        event.isCancelled = true
        player.teleportAsync(bondedPlayer.location)
        Cooldowns.addCooldown(this, player.uniqueId, 100L)
    }


    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        val type = event.block.type
        if (!BlockConstants.ORES.contains(type) || !cachedBonds.hasSpecificValueMoreThanTwice(cachedBonds[player.uniqueId])) {
            return // Fast checks
        }
        val blocks = Sphere(event.block.location, 9.0, 20.0).sphere
        val item = player.inventory.itemInMainHand

        for (block in blocks) {
            if (block.type != type) continue
            block.breakNaturally(item, true)
        }
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
            cachedBonds[player.uniqueId] = secret
        }
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

    // Util

    private fun <K, V> Map<K, V>.hasSpecificValueMoreThanTwice(value: V): Boolean {
        return this.values.count { it == value } > 1
    }

}

/*
rip my poor idea of having drops going into the other player's inventory...
"NOOO DON'T HAVE THE DROPS GO INTO THE OTHER PLAYERS INV ITS SO STUPID" - kat

val partner = getBondedPlayer(player) ?: return
        val blocks = Sphere(event.block.location, 9.0, 20.0).sphere
        val allDrops: MutableMap<ItemStack, Int> = mutableMapOf()
        val item = player.inventory.itemInMainHand
        for (block in blocks) {
            if (block.type != type) continue
            val craftBlock = block as CraftBlock
            val nmsBlock = craftBlock.nms.block
            val nmsItemStack = (item as CraftItemStack).handle
            val exp = nmsBlock.getExpDrop(nmsBlock.defaultBlockState(), craftBlock.handle.minecraftWorld, craftBlock.position, nmsItemStack, false)

            allDrops[ItemStack(type)] = exp
            block.type = Material.AIR

            Util.giveItem(partner, block.getDrops(item).first())
            partner.giveExp(exp, true)
            // TODO: Add particles + Sound
        }

        allDrops.forEach { (item, exp) ->

        }
 */