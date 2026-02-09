package dev.lumas.lumaitems.items.armor.helmet

import dev.lumas.lumaitems.guis.SnapshotShulkerBoxInventory
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta

class HareweaverGlassesItem : CustomItemFunctions() {

    val asShulkerBox: (itemStack: ItemStack?) -> ShulkerBox? = asShulkerBox@{
        if (it == null || !it.hasItemMeta()) {
            return@asShulkerBox null
        }
        val meta = it.itemMeta as? BlockStateMeta ?: return@asShulkerBox null
        val blockState = meta.blockState as? ShulkerBox ?: return@asShulkerBox null
        blockState
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#f082a1:#ffd7ac:#a4b878:#a78360:#7f625d>Harewear Glasses</gradient></b>")
            .customEnchants("<#a78360>Visionary")
            .material(Material.NETHERITE_HELMET)
            .tier(Tier.EASTER_2025)
            .persistentData("harewear-glasses")
            .vanillaEnchants(
                Enchantment.FIRE_PROTECTION to 6,
                Enchantment.BLAST_PROTECTION to 6,
                Enchantment.PROJECTILE_PROTECTION to 6,
                Enchantment.AQUA_AFFINITY to 2,
                Enchantment.UNBREAKING to 10,
                Enchantment.MENDING to 1,
            )
            .lore(
                "<#f082a1>Sneak</#f082a1> + <#f082a1>right-click</#f082a1> while",
                "holding a shulker box to",
                "peer into its contents."
            )
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (!player.isSneaking) {
            return
        }

        val shulkerBox = asShulkerBox(event.item) ?: return
        event.isCancelled = true
        val inv = SnapshotShulkerBoxInventory(shulkerBox)
        player.openInventory(inv.inventory)
    }
}

/*
val name = shulkerBox.customName() ?: Component.text("Shulker Box")
player.sendActionBar(
    MiniMessageUtil.mm("<#f082a1>Viewing the contents of<gray>:</gray></#f082a1> ").append(name)
)
 */