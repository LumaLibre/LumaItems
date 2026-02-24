package dev.lumas.lumaitems.util

import dev.lumas.lumaitems.util.extensions.asEnum
import dev.lumas.lumaitems.util.extensions.equipmentContainers
import dev.lumas.lumaitems.util.extensions.formatSnakeCase
import dev.lumas.lumaitems.util.extensions.getPersistentKey
import dev.lumas.lumaitems.util.extensions.handContainers
import dev.lumas.lumaitems.util.extensions.hasPersistentKey
import dev.lumas.lumaitems.util.extensions.isItemInSlot
import dev.lumas.lumaitems.util.extensions.itemStack
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.setBase64Texture
import dev.lumas.lumaitems.util.extensions.setPersistentKey
import dev.lumas.lumaitems.util.extensions.toBukkitColor
import dev.lumas.lumaitems.util.extensions.toColor
import io.papermc.paper.entity.PlayerGiveResult
import java.awt.Color as AwtColor
import kotlin.random.Random
import net.md_5.bungee.api.ChatColor as BungeeChatColor
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType


object Util {

    const val WITH_DELIMITER = "((?<=%1\$s)|(?=%1\$s))"


    fun giveItem(player: Player, item: ItemStack): PlayerGiveResult = player.give(item)
    fun giveItem(player: Player, items: Collection<ItemStack>): PlayerGiveResult = player.give(items)

    fun getAllEquipmentNBT(player: Player) = player.equipmentContainers()
    fun getHandNBT(player: Player) = player.handContainers()

    fun formatEnchantKey(key: String) = formatEnumerator(key.replace("minecraft:", ""))
    fun formatEnumerator(s: String) = s.formatSnakeCase()

    fun isItemInSlot(identifier: String, slot: EquipmentSlot, player: Player) = player.isItemInSlot(identifier, slot)
    fun isItemInSlot(identifier: NamespacedKey, slot: EquipmentSlot, player: Player) = player.isItemInSlot(identifier, slot)

    fun playerHeadFromBase64(base64: String, amt: Int) = Material.PLAYER_HEAD.itemStack { it.setBase64Texture(base64) }
    fun setBase64Texture(meta: ItemMeta, base64: String?) = meta.setBase64Texture(base64)

    fun hex2BukkitColor(colorStr: String) = colorStr.toBukkitColor()
    fun hex2AwtColor(colorStr: String) = colorStr.toColor()

    fun <E : Enum<E>> enumValueOfOrNull(enumClass: Class<E>, name: String) = name.asEnum(enumClass)

    @JvmStatic
    fun namespacedKey(key: String) = key.namespacedKey()
    fun removePersistentKey(persistentDataHolder: PersistentDataHolder, key: NamespacedKey) = persistentDataHolder.persistentDataContainer.remove(key)
    fun <P, C : Any> setPersistentKey(persistentDataHolder: PersistentDataHolder, key: NamespacedKey, dataType: PersistentDataType<P, C>, value: C) = persistentDataHolder.setPersistentKey(key, dataType, value)
    fun <P : Any, C : Any> getPersistentKey(persistentDataHolder: PersistentDataHolder, key: String, dataType: PersistentDataType<P, C>) = persistentDataHolder.getPersistentKey(key, dataType)
    fun <P : Any, C : Any> getPersistentKey(persistentDataHolder: PersistentDataHolder, key: NamespacedKey, dataType: PersistentDataType<P, C>) = persistentDataHolder.getPersistentKey(key, dataType)
    fun <P, C : Any> setPersistentKey(persistentDataHolder: PersistentDataHolder, key: String, dataType: PersistentDataType<P, C>, value: C) = persistentDataHolder.setPersistentKey(key.namespacedKey(), dataType, value)
    fun <P, C : Any> setPersistentKey(item: ItemStack, key: NamespacedKey, dataType: PersistentDataType<P, C>, value: C) = item.setPersistentKey(key, dataType, value)
    fun <P : Any, C : Any> getPersistentKey(item: ItemStack, key: NamespacedKey, dataType: PersistentDataType<P, C>) = item.getPersistentKey(key, dataType)
    fun hasPersistentKey(itemStack: ItemStack, key: NamespacedKey) = itemStack.hasPersistentKey(key)
    fun hasPersistentKey(persistentDataHolder: PersistentDataHolder?, key: NamespacedKey) = persistentDataHolder.hasPersistentKey(key)
    fun hasPersistentKey(persistentDataHolder: PersistentDataHolder?, key: String) = persistentDataHolder.hasPersistentKey(key.namespacedKey())



    fun getRandomColor(): AwtColor {
        val hue = Random.nextFloat();
        val saturation = (Random.nextInt(2000) + 1000) / 10000f
        val luminance = 0.9f;
        return AwtColor.getHSBColor(hue, saturation, luminance)
    }


    fun createBasicItem(name: String, lore: List<String>, material: Material, datas: List<String>, glint: Boolean): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta!!
        meta.setDisplayName(colorcode(name))
        meta.lore = colorcodeList(lore)
        for (data in datas) {
            meta.persistentDataContainer.set(data.namespacedKey(), PersistentDataType.SHORT, 1)
        }
        if (glint) {
            meta.addEnchant(Enchantment.UNBREAKING, 10, true)
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        }
        item.itemMeta = meta
        return item
    }


    fun colorcode(text: String): String {
        val texts = text.split(String.format(WITH_DELIMITER, "&").toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val finalText = StringBuilder()
        var i = 0
        while (i < texts.size) {
            if (texts[i].equals("&", ignoreCase = true)) {
                //get the next string
                i++
                if (texts[i][0] == '#') {
                    finalText.append(BungeeChatColor.of(texts[i].substring(0, 7)).toString() + texts[i].substring(7))
                } else {
                    finalText.append(ChatColor.translateAlternateColorCodes('&', "&" + texts[i]))
                }
            } else {
                finalText.append(texts[i])
            }
            i++
        }
        return finalText.toString()
    }


    fun colorcodeList(list: List<String>): List<String> {
        val coloredList: MutableList<String> = ArrayList()
        for (string in list) {
            coloredList.add(colorcode(string))
        }
        return coloredList
    }
}