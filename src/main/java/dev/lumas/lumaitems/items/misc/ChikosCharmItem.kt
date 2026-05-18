package dev.lumas.lumaitems.items.misc

import com.destroystokyo.paper.MaterialTags
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.actionBar
import dev.lumas.lumaitems.util.extensions.addCooldown
import dev.lumas.lumaitems.util.extensions.flag
import dev.lumas.lumaitems.util.extensions.getPersistentKey
import dev.lumas.lumaitems.util.extensions.hasPersistentKey
import dev.lumas.lumaitems.util.extensions.isFlagged
import dev.lumas.lumaitems.util.extensions.isOnCooldown
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.removeFlag
import dev.lumas.lumaitems.util.extensions.send
import dev.lumas.lumaitems.util.extensions.setPersistentKey
import dev.lumas.lumaitems.util.extensions.syncTimer
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.Tag
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class ChikosCharmOrbItem : CustomItemFunctions() {

    val delegate = ChikosCharmItem()

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#90D13E:#FFE854:#F98C4C:#F68C86:#C45973>Chiko's Charm Orb</gradient></b>")
            .customEnchants("<#C45973>Valet")
            .tier(Tier.WONDERLAND_2026)
            .persistentData("chikos-charm-valet")
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .material(Material.HEART_OF_THE_SEA)
            .lore(
                "<gray>Right-click to redeem.",
                "",
                "A charm perfect fit",
                "for small utilities.",
                "",
                "<#C45973>Right-click</#C45973> to execute",
                "a single pre-defined",
                "command.",
                "",
                "While sneaking, press",
                "your <#C45973>swap key (F)</#C45973> to",
                "change the pre-defined",
                "command in this charm."
            )
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        val item = event.item ?: return
        item.amount -= 1

        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 1f)
        player.give(delegate.createItem().second)
    }
}

class ChikosCharmItem : CustomItemFunctions() {

    private companion object {
        private val KEY = "chikos-charm".namespacedKey()
        private val COMMAND_KEY = "chikos-charm-command".namespacedKey()
    }

    override fun createItem(): Pair<String, ItemStack> {
        val material = Tag.ITEMS_DYES.values.random()

        return ItemFactory.builder()
            .name("<b><gradient:#90D13E:#FFE854:#F98C4C:#F68C86:#C45973>Chiko's Charm</gradient></b>")
            .customEnchants("<#C45973>Valet")
            .material(material)
            .tier(Tier.WONDERLAND_2026)
            .persistentData(KEY)
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .lore(
                "A charm perfect fit",
                "for small utilities.",
                "",
                "<#C45973>Right-click</#C45973> to execute",
                "a single pre-defined",
                "command.",
                "",
                "While sneaking, press",
                "your <#C45973>swap key (F)</#C45973> to",
                "change the pre-defined",
                "command in this charm."
            )
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (player.isOnCooldown(this)) return
        val item = event.item ?: return
        item.getPersistentKey(COMMAND_KEY, PersistentDataType.STRING)
            ?.let {
                //player.playSound(player.location, Sound.ENTITY_ALLAY_ITEM_GIVEN, 0.09f, 1f)
                player.performCommand(it)
                player.addCooldown(this, 10)
            }
            ?: player.actionBar("<i><#C45973>No command set on this charm!")
    }

    override fun onPlayerSwapHands(player: Player, event: PlayerSwapHandItemsEvent) {
        if (!player.isSneaking) return

        val item = event.offHandItem.takeIf { it.hasPersistentKey(KEY) } ?: return

        event.isCancelled = true

        if (player.isFlagged(this) || player.isOnCooldown(this)) return

        player.send("<#C45973>In chat</#C45973>, type out the command you wish for this charm to store.")

        player.flag(this)
        player.addCooldown(this, 10)

        player.syncTimer(0, 1) {
            val mainHandItem = player.inventory.itemInMainHand
            if (!mainHandItem.hasPersistentKey(KEY)) {
                player.removeFlag(this)
                player.send("Cancelled!")
                it.cancel()
            }
        }
    }


    override fun onPlayerQuit(player: Player, event: PlayerQuitEvent) {
        if (player.isFlagged(this)) {
            player.removeFlag(this)
        }
    }

    override fun onAsyncChat(player: Player, event: AsyncChatEvent) {
        if (!player.isFlagged(this)) return

        event.isCancelled = true
        player.removeFlag(this)

        val item = player.inventory.itemInMainHand.takeIf { it.hasPersistentKey(KEY) } ?: return


        val plainMessage = PlainTextComponentSerializer.plainText().serialize(event.originalMessage())

        item.setPersistentKey(COMMAND_KEY, PersistentDataType.STRING, plainMessage)

        player.send("This charm's command is now: <#C45973>/$plainMessage</#C45973>")
    }

    override fun onCommandPreProcess(player: Player, event: PlayerCommandPreprocessEvent) {
        if (!player.isFlagged(this)) return
        event.isCancelled = true
        player.removeFlag(this)

        val item = player.inventory.itemInMainHand.takeIf { it.hasPersistentKey(KEY) } ?: return

        val strippedCommand = event.message.removePrefix("/")
        item.setPersistentKey(COMMAND_KEY, PersistentDataType.STRING, strippedCommand)

        player.send("This charm's command is now: <#C45973>/$strippedCommand</#C45973>")
    }
}