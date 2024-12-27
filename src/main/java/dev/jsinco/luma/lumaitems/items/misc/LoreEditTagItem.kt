package dev.jsinco.luma.lumaitems.items.misc

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.manager.CustomItem
import dev.jsinco.luma.lumaitems.manager.ItemManager
import dev.jsinco.luma.lumaitems.util.Util
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

class LoreEditTagItem : CustomItem {

    companion object {
        val plugin: LumaItems = LumaItems.getInstance()
        val lores: MutableMap<UUID, MutableList<String>> = mutableMapOf()
    }

    private lateinit var loreEditTag: ItemStack

    override fun createItem(): Pair<String, ItemStack> {
        loreEditTag = Util.createBasicItem(
            "&#a8ff92&lItem Lore Edit &#E2E2E2Tag",
            listOf("&7Right click to use!"),
            Material.NAME_TAG,
            listOf("newloretag"),
            glint = true
        )
        return Pair("newloretag", loreEditTag)
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        //val chatMessageEvent: AsyncPlayerChatEvent? = event as? AsyncPlayerChatEvent

        when (type) {
            Action.RIGHT_CLICK -> {
                //if (player.inventory.itemInMainHand.itemMeta.persistentDataContainer.has(NamespacedKey(RenameTagItem.plugin, "newloretag"), PersistentDataType.SHORT)) {
                //    startEditingLore(player)
                //}
                player.sendMessage(Util.colorcode("${Util.prefix} Redeem by making a ticket on &#a8ff92https://lumamc.net/discord"))
            }
            /*
            Ability.CHAT -> {
                sentMessageForLore(chatMessageEvent!!.message, player)
                chatMessageEvent.isCancelled = true
            }

             */

            else -> return false
        }
        return true
    }

    private fun startEditingLore(player: Player) {
        player.persistentDataContainer.set(NamespacedKey(plugin, "newloretag"), PersistentDataType.SHORT, 1)
        player.inventory.itemInMainHand.amount -= 1

        val msg = Util.colorcode("""
            ${Util.prefix} Hold the item you want to edit the lore of in your hand.
            ${Util.prefix} Type your new lore, one message is equal to one line of lore.
            ${Util.prefix} Use a tilde '&d~&#E2E2E2' to add a blank line.
            ${Util.prefix} Type '&ccancel&#E2E2E2' to cancel or type '&adone&#E2E2E2' to apply to item.
        """.trimIndent())
        player.sendMessage(msg)
    }


    private fun sentMessageForLore(msg: String, player: Player) {
        if (msg == "cancel") {
            cancelLoreEditing(player)
            player.sendMessage(Util.colorcode("${Util.prefix} Cancelled!"))
            return
        } else if (msg == "done") {
            completeLoreEditing(player)
            return
        } else if (msg == "~") {
            msg.replace("~", "").trim()
        }


        if (lores.containsKey(player.uniqueId)) {
            lores[player.uniqueId]!!.add(msg)
        } else {
            lores[player.uniqueId] = mutableListOf(msg)
        }
        player.sendMessage(Util.colorcode("${Util.prefix} Added line to lore!"))
    }


    private fun completeLoreEditing(player: Player) {
        val item = player.inventory.itemInMainHand
        if (item.type == Material.AIR) {
            cancelLoreEditing(player)
            player.sendMessage(Util.colorcode("${Util.prefix} You must be holding an item to edit the lore!"))
            return
        }

        val meta = item.itemMeta!!
        var isCustomItem = false

        if (meta.persistentDataContainer.has(NamespacedKey(plugin, "lumaitem"), PersistentDataType.SHORT) || meta.persistentDataContainer.has(NamespacedKey(plugin, "stellar"), PersistentDataType.SHORT)) {
            isCustomItem = true
        } else { // Secondary check
            for (nbt in ItemManager.customItems) {
                if (meta.persistentDataContainer.has(nbt.key, PersistentDataType.SHORT)) {
                    isCustomItem = true
                }
            }
        }

        val lore = try {
            Util.colorcodeList(lores[player.uniqueId]!!)
        } catch (e: Exception) {
            cancelLoreEditing(player)
            player.sendMessage(Util.colorcode("${Util.prefix} Something went wrong"))
            return
        }
        if (isCustomItem) {
            reloreCustomItem(item, lore)
        } else {
            reloreNormalItem(item, lore)
        }
        player.persistentDataContainer.remove(NamespacedKey(plugin, "newloretag"))
        lores.remove(player.uniqueId)
        player.sendMessage(Util.colorcode("${Util.prefix} Lore edited!"))
    }


    private fun reloreNormalItem (item: ItemStack, newLore: List<String>) {
        val meta = item.itemMeta
        meta.lore = newLore
        item.itemMeta = meta
    }

    private fun reloreCustomItem(item: ItemStack, newLore: List<String>) {
        val meta = item.itemMeta!!
        val lore = meta.lore!!

        val enchants: MutableList<String> = mutableListOf()
        val tier: MutableList<String> = mutableListOf()

        for (line in lore) {
            if (line != "§") {
                enchants.add(line)
            } else {
                break
            }
        }
        for (i in lore.size - 1 downTo (lore.size - 4)) {
            tier.add(lore[i])
        }

        val finalLore: MutableList<String> = mutableListOf()
        finalLore.addAll(enchants)
        finalLore.add("§")
        finalLore.addAll(newLore)
        finalLore.addAll(tier.reversed())

        meta.lore = finalLore
        item.itemMeta = meta
    }

    private fun cancelLoreEditing(player: Player) {
        player.persistentDataContainer.remove(NamespacedKey(plugin, "newloretag"))
        player.inventory.addItem(loreEditTag)
        lores.remove(player.uniqueId)
    }
}