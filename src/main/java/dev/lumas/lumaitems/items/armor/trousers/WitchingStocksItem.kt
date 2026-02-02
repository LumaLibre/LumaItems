package dev.lumas.lumaitems.items.armor.trousers

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.model.AttributeContainer
import dev.lumas.lumaitems.util.extensions.isLocationOnGround
import dev.lumas.lumaitems.util.tiers.Tier
import java.util.UUID
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack

class WitchingStocksItem : CustomItemFunctions() {

    companion object {
        private val TRACKED = HashSet<UUID>()
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#320343:#5a2d3e:#876541:#8da649:#74eb62>Witching Stocks</gradient></b>")
            .customEnchants("<gold>Double Jump")
            .persistentData("witching-stocks")
            .material(Material.NETHERITE_LEGGINGS)
            .tier(Tier.HALLOWEEN_2025)
            .attributeModifiers(
                AttributeContainer.builder()
                    .setKey("witching-stocks")
                    .setAttribute(Attribute.SAFE_FALL_DISTANCE)
                    .setSlot(EquipmentSlotGroup.LEGS)
                    .setOperation(AttributeModifier.Operation.ADD_SCALAR)
                    .setAmount(1.0)
                    .build()
            )
            .vanillaEnchants(
                Enchantment.PROTECTION to 5,
                Enchantment.UNBREAKING to 8,
                Enchantment.MENDING to 1,
                Enchantment.SWIFT_SNEAK to 3,
                Enchantment.THORNS to 3
            )
            .lore(
                "A set of leggings woven",
                "with faint witchcraft.",
                "",
                "While falling, <gold>sneak</gold> to",
                "perform a double jump."
            )
            .buildPair()
    }

    override fun onPlayerCrouch(player: Player, event: PlayerToggleSneakEvent) {
        if (player.isSneaking || player.isFlying || player.isLocationOnGround() || TRACKED.contains(player.uniqueId)) {
            return
        }
        TRACKED.add(player.uniqueId)
        player.velocity = player.location.direction.multiply(0.6).setY(0.7)
    }

    override fun onMove(player: Player, event: PlayerMoveEvent) {
        if (!TRACKED.contains(player.uniqueId) || !player.isLocationOnGround()) {
            return
        }
        TRACKED.remove(player.uniqueId)
    }
}