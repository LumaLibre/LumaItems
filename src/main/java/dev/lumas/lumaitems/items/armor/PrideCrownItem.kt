package dev.lumas.lumaitems.items.armor

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.AttributeContainer
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack

class PrideCrownItem : CustomItemFunctions() {

    companion object {
        private val key = Util.namespacedKey("pridecrown")
        private val colors: List<DustOptions> = listOf(
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
        return ItemFactory.builder()
            .name("<b><gradient:#ff6666:#ffbd55:#ffff66:#9de24f:#87cefa>Pride</gradient></b> <white><b>Crown</b></white>")
            .lore(
                "<gray>Legends whisper the <b><gradient:#ff6666:#ffbd55:#ffff66:#9de24f:#87cefa>Pride</gradient></b> <white><b>Crown</b></white>",
                "<gray>reveals unseen beauty within, coaxing",
                "<gray>a blossoming of confidence and charm",
                "<gray>that captivates all who witness it."
            )
            .material(Material.LARGE_AMETHYST_BUD)
            .persistentData(key)
            .vanillaEnchants(
                Enchantment.PROTECTION to 8
            )
            .autoHat(true)
            .tier(Tier.PRIDE_2025)
            .attributeModifiers(
                AttributeContainer.ofMap(
                    key, Attribute.MAX_HEALTH, 6.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HEAD
                )
            )
            .buildPair()
    }

    override fun onRunnable(player: Player) {
        if (!Util.isItemInSlot(key, EquipmentSlot.HEAD, player)) {
            return
        }
        for (i in 0..2) {
            player.world.spawnParticle(
                Particle.DUST, player.eyeLocation.add(0.0, 0.45, 0.0), 2, 0.2, 0.0, 0.2, colors.random())
        }

        player.world.spawnParticle(
            Particle.END_ROD, player.eyeLocation.add(0.0, 0.45, 0.0), 1, 0.2, 0.0, 0.2, 0.01
        )
    }
}