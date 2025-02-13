package dev.jsinco.luma.lumaitems.util

import com.destroystokyo.paper.profile.ProfileProperty
import dev.jsinco.luma.lumaitems.LumaItems
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.MapColor
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Color as BukkitColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.craftbukkit.block.CraftBlock
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.util.UUID
import kotlin.random.Random
import net.md_5.bungee.api.ChatColor as BungeeChatColor
import java.awt.Color as AwtColor


object Util {

    val STATIC_UUID: UUID = UUID.fromString("e9378e48-0e8e-42a9-9df1-7074b00df6a9")
    const val WITH_DELIMITER = "((?<=%1\$s)|(?=%1\$s))"

    private val plugin: LumaItems = LumaItems.getInstance()
    private val armorEquipmentSlots: List<EquipmentSlot> = listOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)
    private val gearTypes: List<String> = listOf("Helmet", "Chestplate", "Leggings", "Boots", "Sword", "Pickaxe", "Axe", "Shovel", "Hoe", "Rod", "Elytra", "Shield", "Crossbow", "Bow")

    val prefix: String = colorcode("&#b986f9&lInfo &8»&#E2E2E2")



    /**
     * @param text The string of text to apply color/effects to
     * @return Returns a string of text with color/effects applied
     */
    @JvmStatic
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

    @JvmStatic
    fun colorcodeList(list: List<String>): List<String> {
        val coloredList: MutableList<String> = ArrayList()
        for (string in list) {
            coloredList.add(colorcode(string))
        }
        return coloredList
    }

    fun giveItem(player: Player, item: ItemStack) {
        for (i in 0..35) {
            if (player.inventory.getItem(i) == null || player.inventory.getItem(i)!!.isSimilar(item)) {
                player.inventory.addItem(item)
                break
            } else if (i == 35) {
                player.world.dropItem(player.location, item)
            }
        }
    }

    fun giveItem(player: Player, items: Collection<ItemStack>) {
        for (item in items) {
            giveItem(player, item)
        }
    }


    fun getColorCodeByChatColor(colorCode: ChatColor): String {
        return when (colorCode) {
            ChatColor.AQUA -> "§b"
            ChatColor.BLACK -> "§0"
            ChatColor.BLUE -> "§9"
            ChatColor.DARK_AQUA -> "§3"
            ChatColor.DARK_BLUE -> "§1"
            ChatColor.DARK_GRAY -> "§8"
            ChatColor.DARK_GREEN -> "§2"
            ChatColor.DARK_PURPLE -> "§5"
            ChatColor.DARK_RED -> "§4"
            ChatColor.GOLD -> "§6"
            ChatColor.GRAY -> "§7"
            ChatColor.GREEN -> "§a"
            ChatColor.LIGHT_PURPLE -> "§d"
            ChatColor.RED -> "§c"
            ChatColor.YELLOW -> "§e"
            else -> "§f"
        }
    }

    fun getAllEquipmentNBT(player: Player): List<PersistentDataContainer> {
        val nbtList: MutableList<PersistentDataContainer> = mutableListOf()
        player.inventory.itemInMainHand.itemMeta?.persistentDataContainer?.let { nbtList.add(it) }
        player.inventory.itemInOffHand.itemMeta?.persistentDataContainer?.let { nbtList.add(it) }

        for (equipment in player.equipment?.armorContents ?: return nbtList) {
            equipment?.itemMeta?.persistentDataContainer?.let { nbtList.add(it) }
        }
        return nbtList
    }

    fun getHandNBT(player: Player): List<PersistentDataContainer> {
        val nbtList: MutableList<PersistentDataContainer> = mutableListOf()
        player.inventory.itemInMainHand.itemMeta?.persistentDataContainer?.let { nbtList.add(it) }
        player.inventory.itemInOffHand.itemMeta?.persistentDataContainer?.let { nbtList.add(it) }
        return nbtList
    }

    fun isWearingWithNBT(player: Player, identifier: String): Boolean {
        val armorDatas: List<PersistentDataContainer?> =
            armorEquipmentSlots.map { player.equipment?.getItem(it)?.itemMeta?.persistentDataContainer }

        for (data in armorDatas) {
            if (data != null && data.has(NamespacedKey(plugin, identifier), PersistentDataType.SHORT)) return true
        }

        return false
    }


    fun createBasicItem(
        name: String,
        lore: List<String>,
        material: Material,
        datas: List<String>,
        glint: Boolean
    ): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta!!
        meta.setDisplayName(colorcode(name))
        meta.lore = colorcodeList(lore)
        for (data in datas) {
            meta.persistentDataContainer.set(NamespacedKey(plugin, data), PersistentDataType.SHORT, 1)
        }
        if (glint) {
            meta.addEnchant(Enchantment.UNBREAKING, 10, true)
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        }
        item.itemMeta = meta
        return item
    }

    fun getGearType(item: ItemStack): String? {
        return getGearType(item.type)
    }
    fun getGearType(material: Material): String? {
        for (gear in gearTypes) {
            if (material.toString().contains(gear, ignoreCase = true)) return gear
        }
        return null
    }

    fun formatEnchantKey(key: String): String = formatMaterialName(key.replace("minecraft:", ""))


    fun formatMaterialName(s: String): String {
        var name = s.lowercase().replace("_", " ")
        name = name.substring(0, 1).uppercase() + name.substring(1)
        for (i in name.indices) {
            if (name[i] == ' ') {
                name =
                    name.substring(0, i) + " " + name[i + 1].toString().uppercase() + name.substring(
                        i + 2
                    ) // Capitalize first letter of each word
            }
        }
        return name
    }

    fun splitRandomList(list: MutableList<*>, retain: Int): MutableList<*> {
        val newList: MutableList<Any> = mutableListOf()
        for (i in 0 until retain) {
            val random = list.indices.random()
            list[random]?.let { newList.add(it) }
            list.removeAt(random)
        }
        return newList
    }

    fun setEntityEquipment(entity: LivingEntity, item: ItemStack, slot: EquipmentSlot) {
        when (slot) {
            EquipmentSlot.HEAD -> entity.equipment?.helmet = item
            EquipmentSlot.CHEST -> entity.equipment?.chestplate = item
            EquipmentSlot.LEGS -> entity.equipment?.leggings = item
            EquipmentSlot.FEET -> entity.equipment?.boots = item
            EquipmentSlot.HAND -> entity.equipment?.setItemInMainHand(item)
            EquipmentSlot.OFF_HAND -> entity.equipment?.setItemInOffHand(item)
            else -> return
        }
    }

    fun isItemInSlot(identifier: String, slot: EquipmentSlot, player: Player): Boolean {
        return player.equipment?.getItem(slot)?.itemMeta?.persistentDataContainer?.has(NamespacedKey(plugin, identifier), PersistentDataType.SHORT) == true
    }

    fun isItemInSlots(identifier: String, slots: List<EquipmentSlot>, player: Player): Boolean {
        for (slot in slots) {
            if (isItemInSlot(identifier, slot, player)) return true
        }
        return false
    }

    fun playerHeadFromBase64(base64: String, amt: Int): ItemStack {
        val item = ItemStack(Material.PLAYER_HEAD, amt)
        val meta = item.itemMeta as SkullMeta
        val profile = Bukkit.createProfile(UUID.randomUUID())
        profile.properties.add(ProfileProperty("textures", base64))
        meta.playerProfile = profile
        item.setItemMeta(meta)
        return item
    }

    fun setBase64Texture(meta: ItemMeta, base64: String?) {
        if (meta !is SkullMeta || base64 == null) return
        val profile = Bukkit.createProfile(STATIC_UUID)
        profile.properties.add(ProfileProperty("textures", base64))
        meta.playerProfile = profile
    }

    fun hex2BukkitColor(colorStr: String): BukkitColor {
        return BukkitColor.fromRGB(
            colorStr.substring(1, 3).toInt(16),
            colorStr.substring(3, 5).toInt(16),
            colorStr.substring(5, 7).toInt(16)
        )
    }

    @JvmStatic
    fun hex2AwtColor(colorStr: String): AwtColor {
        return AwtColor(
            colorStr.substring(1, 3).toInt(16),
            colorStr.substring(3, 5).toInt(16),
            colorStr.substring(5, 7).toInt(16)
        )
    }

    fun getColor(block: Block): AwtColor {
        val cb: CraftBlock = block as CraftBlock
        val bs: BlockState = cb.nms
        val mc: MapColor = bs.getMapColor(cb.craftWorld.handle, cb.position)
        return AwtColor(mc.col)
    }

    fun javaAwtColorToBukkitColor(color: AwtColor): BukkitColor {
        return BukkitColor.fromARGB(color.alpha, color.red, color.green, color.blue)
    }

    @JvmStatic
    fun blend(vararg c: AwtColor): AwtColor {
        val ratio = 1f / (c.size.toFloat())

        var a = 0
        var r = 0
        var g = 0
        var b = 0

        for (i in c.indices) {
            val rgb: Int = c[i].getRGB()
            val a1 = (rgb shr 24 and 0xff)
            val r1 = ((rgb and 0xff0000) shr 16)
            val g1 = ((rgb and 0xff00) shr 8)
            val b1 = (rgb and 0xff)
            a = (a + (a1 * ratio)).toInt()
            r = (r + (r1 * ratio)).toInt()
            g = (g + (g1 * ratio)).toInt()
            b = (b + (b1 * ratio)).toInt()
        }

        return AwtColor(a shl 24 or (r shl 16) or (g shl 8) or b)
    }

    @JvmStatic
    fun getRandomColor(): AwtColor {
        val hue = Random.nextFloat();
        val saturation = (Random.nextInt(2000) + 1000) / 10000f;
        val luminance = 0.9f;
        return AwtColor.getHSBColor(hue, saturation, luminance);
    }


    @JvmStatic
    fun <E : Enum<E>> enumValueOfOrNull(enumClass: Class<E>, name: String): E? {
        return try {
            java.lang.Enum.valueOf(enumClass, name)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    fun namespacedKey(key: String): NamespacedKey {
        return NamespacedKey(plugin, key)
    }
}