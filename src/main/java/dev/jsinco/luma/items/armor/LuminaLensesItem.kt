package dev.jsinco.luma.items.armor

import dev.jsinco.luma.LumaItems
import dev.jsinco.luma.items.ItemFactory
import dev.jsinco.luma.enums.Action
import dev.jsinco.luma.manager.CustomItem
import dev.jsinco.luma.util.Util
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.UUID

class LuminaLensesItem : CustomItem {

    companion object {
        val plugin: LumaItems = LumaItems.getInstance()
        val ticker: MutableMap<UUID, Int> = mutableMapOf()
    }

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#2ca4fb&lL&#2fb1f9&lu&#31bef7&lm&#34cbf4&li&#36d8f2&ln&#39e5f0&la &#3fdef0&lL&#4ac3f2&le&#55a7f4&ln&#608bf7&ls&#6a70f9&le&#7554fb&ls",
            mutableListOf("&#3efeecB&#47e2efa&#50c5f1t&#5aa9f4t&#638df6e&#6c70f9r&#7554fby 100%"),
            mutableListOf("&#7554fb\"&#7358fbH&#725cfaa&#7060far&#6e65fan&#6c69f9e&#6b6df9s&#6971f9s &#6775f8t&#6679f8h&#647df8e &#6282f8e&#6086f7t&#5f8af7e&#5d8ef7r&#5b92f6n&#5a96f6a&#589af6l &#569ff5b&#54a3f5r&#53a7f5i&#51abf4g&#4faff4h&#4eb3f4t&#4cb7f3n&#4abcf3e&#48c0f3s&#47c4f3s &#45c8f2o&#43ccf2f &#42d0f2a &#40d4f1s&#3ed9f1t&#3cddf1a&#3be1f0r&#39e5f0\"","","&fGrants multiple potion buffs while","&fsufficiently charged","","&fThis helmet relies on a charge to function","&fRight-click any amount of raw copper to","&frecharge your helmet"),
            Material.NETHERITE_HELMET,
            mutableListOf("luminalenses"),
            mutableMapOf(Enchantment.PROTECTION to 6, Enchantment.BLAST_PROTECTION to 5, Enchantment.AQUA_AFFINITY to 1, Enchantment.RESPIRATION to 3, Enchantment.UNBREAKING to 8, Enchantment.MENDING to 1)
        )
        return Pair("luminalenses", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.RUNNABLE -> {
                if (tickPlayer(player)) {
                    addNoctalEffects(player)
                }
            }

            Action.RIGHT_CLICK -> {
                batteryCharge(player.inventory.helmet!!, player.inventory.itemInMainHand, player)
            }
            else -> return false
        }
        return true
    }


    fun tickPlayer(player: Player): Boolean {
        val helmet = player.inventory.helmet ?: return false
        val meta = helmet.itemMeta ?: return false
        val currentCharge = meta.persistentDataContainer.get(NamespacedKey(plugin, "LuminaLenses"), PersistentDataType.SHORT) ?: return false
        if (currentCharge <= 0) return false
        ticker.set(player.uniqueId, ticker.get(player.uniqueId)?.plus(1) ?: 0)

        if (ticker.get(player.uniqueId)!! >= 30) {
            ticker.set(player.uniqueId, 0)
            deductBatteryCharges(helmet)
        }
        return true
    }


    private fun batteryCharge(eyeGlasses: ItemStack, copper: ItemStack, player: Player) {
        if (copper.type != Material.RAW_COPPER) return
        val amount = copper.amount.toShort()
        val meta = eyeGlasses.itemMeta
        val currentCharge = meta.persistentDataContainer.get(NamespacedKey(plugin, "LuminaLenses"), PersistentDataType.SHORT)!!
        if (currentCharge + amount > 100) { // smartest thing I've seen copilot write
            meta.persistentDataContainer.set(NamespacedKey(plugin, "LuminaLenses"), PersistentDataType.SHORT, 100.toShort())
            copper.amount = amount - (100 - currentCharge)
        } else {
            meta.persistentDataContainer.set(NamespacedKey(plugin, "LuminaLenses"), PersistentDataType.SHORT, (currentCharge + amount).toShort())
            copper.amount = 0
        }
        val lore = meta.lore
        for (i in lore!!.indices) {
            if (lore[i] != null && ChatColor.stripColor(lore[i])!!.contains("Battery")) {
                lore[i] = Util.colorcode("&#3efeecB&#47e2efa&#50c5f1t&#5aa9f4t&#638df6e&#6c70f9r&#7554fby " + meta.persistentDataContainer.get(NamespacedKey(plugin, "LuminaLenses"), PersistentDataType.SHORT) + "%")
            }
        }
        meta.lore = lore
        eyeGlasses.setItemMeta(meta)
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(ChatColor.BLUE.toString() + "Battery: " + meta.persistentDataContainer.get<Short, Short>(NamespacedKey(plugin, "LuminaLenses"), PersistentDataType.SHORT) + "%"))
        addNoctalEffects(player)
    }

    private fun deductBatteryCharges(helmet: ItemStack) {
        val meta = helmet.itemMeta ?: return
        if (!meta.persistentDataContainer.has(NamespacedKey(plugin, "LuminaLenses"), PersistentDataType.SHORT)) return
        val currentCharge: Short = meta.persistentDataContainer.get(NamespacedKey(plugin, "LuminaLenses"), PersistentDataType.SHORT)!!
        if (currentCharge > 0) {
            meta.persistentDataContainer.set(NamespacedKey(plugin, "LuminaLenses"), PersistentDataType.SHORT, (currentCharge - 1).toShort())
            val lore = meta.lore!!
            for (i in lore.indices) {
                if (lore[i] != null && ChatColor.stripColor(lore[i])!!.contains("Battery")) {
                    lore[i] = Util.colorcode("&#3efeecB&#47e2efa&#50c5f1t&#5aa9f4t&#638df6e&#6c70f9r&#7554fby " + meta.persistentDataContainer.get(NamespacedKey(plugin, "LuminaLenses"), PersistentDataType.SHORT) + "%")
                }
            }
            meta.lore = lore
            helmet.itemMeta = meta
        }
    }

    private fun addNoctalEffects(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, 400, 0, false, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 400, 0, false, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 400, 0, false, false, false))
    }
}