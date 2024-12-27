package dev.jsinco.luma.lumaitems.items.misc

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.manager.CustomItem
import dev.jsinco.luma.lumaitems.enums.DefaultAttributes
import dev.jsinco.luma.lumaitems.util.Util
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.util.UUID

class PrideCrownItem : CustomItem {

    companion object {
        val colors: List<DustOptions> = listOf(
            DustOptions(Util.hex2BukkitColor("#F36B6B"), 0.9f),
            DustOptions(Util.hex2BukkitColor("#F3B36B"), 0.9f),
            DustOptions(Util.hex2BukkitColor("#F3EA6B"), 0.9f),
            DustOptions(Util.hex2BukkitColor("#89E280"), 0.9f),
            DustOptions(Util.hex2BukkitColor("#7485E3"), 0.9f),
            DustOptions(Util.hex2BukkitColor("#A374E3"), 0.9f),
            DustOptions(Util.hex2BukkitColor("#D179DE"), 0.9f),
        )
    }

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#F49595&lP&#F9EB97&lr&#C6F9AC&li&#A8D9F6&ld&#E2BBFD&le &f&lCrown",
            mutableListOf(),
            mutableListOf("&7Legends whisper the &#F49595&lP&#F9EB97&lr&#C6F9AC&li&#A8D9F6&ld&#E2BBFD&le &f&lCrown", "&7reveals unseen beauty within, coaxing", "&7a blossoming of confidence and charm", "&7that captivates all who witness it."),
            Material.LARGE_AMETHYST_BUD,
            mutableListOf("pridecrown"),
            mutableMapOf(Enchantment.PROTECTION to 8)
        )
        item.autoHat = true
        item.tier = "&#731385&lP&#4332B9&lr&#1351ED&li&#0C6A87&ld&#058221&le &#7FB715&l2&#F9EB08&l0&#EF7A05&l2&#E40902&l4"
        item.attributeModifiers = DefaultAttributes.NETHERITE_HELMET.appendThenGetAttributes(
            Attribute.MAX_HEALTH, AttributeModifier(UUID.randomUUID(),"genericMaxHealth", 6.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD)
        )
        return Pair("pridecrown", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.RUNNABLE -> {
                for (i in 0..2) {
                    player.world.spawnParticle(
                        Particle.DUST, player.eyeLocation.add(0.0, 0.45, 0.0), 2, 0.2, 0.0, 0.2, colors.random())
                }

                player.world.spawnParticle(
                    Particle.END_ROD, player.eyeLocation.add(0.0, 0.45, 0.0), 1, 0.2, 0.0, 0.2, 0.01
                )

            }

            Action.RIGHT_CLICK -> {
                event as PlayerInteractEvent
                if (!Util.isItemInSlot("pridecrown", EquipmentSlot.HAND, player)) {
                    return false
                }
                event.isCancelled = true

                val item = event.item ?: return false
                if (player.equipment.helmet == null) {
                    player.equipment.helmet = item
                    item.amount = 0
                }
            }
            else -> return false
        }
        return true
    }
}