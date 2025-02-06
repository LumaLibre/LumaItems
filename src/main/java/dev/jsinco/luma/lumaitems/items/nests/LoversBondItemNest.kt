package dev.jsinco.luma.lumaitems.items.nests

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.enums.BlockConstants
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.obj.QuickTasks
import dev.jsinco.luma.lumaitems.shapes.Sphere
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.UUID


class SoulboundSeeker : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#7B76D0>S<#8D78C8>o<#9E7BC0>u<#B07DB8>l<#C27FB0>b<#D382A8>o<#E584A0>u<#DC84AA>n<#D384B4>d <#C084C7>S<#B784D1>e<#AE84DB>e<#A584E5>k<#AF80D5>e<#B97BC4>r <#CE72A3>S<#D86E93>e<#E26982>t")
            .material(Material.RED_DYE)
            .tier(Tier.VALENTIDE_2025)
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .lore(
                "<gray>Right-click to redeem!",
                "",
                "Each mattock is intended to",
                "be held onto by another player.",
                "",
                "Whilst your partner has the other",
                "mattock in their inventory, mined",
                "ore veins will be broken instantly.",
                "",
                "While holding this mattock,",
                "<#7B76D0>sneak <white>& press your <#7B76D0>swap key (F)",
                "to teleport to your partner."
            )
            .persistentData("lovers-bond-capsule")
            .buildPair()
    }


    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        val item = event.item ?: return
        item.amount -= 1

        val parent = SoulboundSeekerItem()
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 1f)
        Util.giveItem(player, parent.create(SoulboundSeekerItem.Style.STYLE_1).createItem())
        Util.giveItem(player, parent.create(SoulboundSeekerItem.Style.STYLE_2).createItem())
    }
}


class SoulboundSeekerItem : CustomItemFunctions() {

    // Static
    companion object {
        private val key = NamespacedKey(LumaItems.getInstance(), "lovers-bond-mattock")
        private val secretKey = NamespacedKey(LumaItems.getInstance(), "lovers-bond-secret")
        private val cachedBonds: MutableMap<UUID, String> = mutableMapOf()
    }

    // Item creation

    var secret: String
    init {
        val chars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        secret = (1..7).map { chars.random() }.joinToString("")
    }

    enum class Style(val styleName: String, val baseColor: String) {
        STYLE_1(
            "<b><#B54476>S<#BB4C7B>o<#C15481>u<#C75C86>l<#CD648B>b<#D36C90>o<#D97496>u<#DF7C9B>n<#E584A0>d <#DE7591>S<#DA6E89>e<#D66781>e<#D26079>k<#CF5872>e<#CB516A>r",
            "<#B54476>"
        ),
        STYLE_2(
            "<b><#5951E2>S<#6357E2>o<#6C5EE3>u<#7664E3>l<#7F6BE4>b<#8971E4>o<#9277E4>u<#9C7EE5>n<#A584E5>d <#9C75DE>S<#976EDA>e<#9267D6>e<#8D60D2>k<#8958CF>e<#8451CB>r",
            "<#5951E2>"
        )
    }

    fun create(style: Style): ItemFactory {
        return ItemFactory.builder()
            .name(style.styleName)
            .customEnchants("${style.baseColor}Better Together")
            .lore(
                "Each mattock is intended to",
                "be held onto by another player.",
                "",
                "Whilst your partner has the other",
                "mattock in their inventory, mined",
                "ore veins will be broken instantly.",
                "",
                "While holding this mattock,",
                "${style.baseColor}sneak <white>& press your ${style.baseColor}swap key (F)",
                "to teleport to your partner."
            )
            .material(Material.NETHERITE_PICKAXE)
            .tier(Tier.VALENTIDE_2025)
            .persistentData(key.key)
            .stringPersistentDatas(mutableMapOf(secretKey to secret))
            .vanillaEnchants(Enchantment.EFFICIENCY to 7, Enchantment.UNBREAKING to 10, Enchantment.SILK_TOUCH to 1, Enchantment.MENDING to 1)
            .build()
    }

    override fun createItem(): Pair<String, ItemStack> {
        val style = Style.entries.random()
        return Pair(key.key, create(style).createItem())
    }


    // Functionality

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        val bondedPlayer = getBondedPlayer(player) ?: return
        player.sendMessage("You are bonded with ${bondedPlayer.name}")
    }

    override fun onPlayerSwapHands(player: Player, event: PlayerSwapHandItemsEvent) {
        if (!player.isSneaking) {
            return
        }
        event.isCancelled = true

        if (QuickTasks.isOnCooldown(this, player.uniqueId)) {
            return
        }

        val bondedPlayer = getBondedPlayer(player) ?: return run {
            player.sendActionBar(MiniMessageUtil.mm("<red>\uD83D\uDC94 <gray>Couldn't find your partner."))
        }

        player.teleportAsync(bondedPlayer.location)
        QuickTasks.addCooldown(this, player.uniqueId, 30L)
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
"NOOO DON'T HAVE THE DROPS GO INTO THE OTHER PLAYERS INV IT'S SO STUPID" - kat

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
            // Add particles + Sound
        }

        allDrops.forEach { (item, exp) ->

        }
 */