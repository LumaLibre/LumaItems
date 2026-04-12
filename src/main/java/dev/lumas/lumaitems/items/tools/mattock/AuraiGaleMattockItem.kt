package dev.lumas.lumaitems.items.tools.mattock

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.AttributeContainer
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.shapes.Sphere
import dev.lumas.lumaitems.util.Kind
import dev.lumas.lumaitems.util.extensions.breakNaturallyWithLog
import dev.lumas.lumaitems.util.extensions.isLocationOnGround
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class AuraiGaleMattockItem : CustomItemFunctions() {

    private companion object {
        private const val KEY = "aurai-gale-mattock"
        private val HASTE = PotionEffect(PotionEffectType.HASTE, 210, 2, false, false, true)
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#FAC3C3:#F8C2B4:#FAEBB3:#B8E2FC:#9EB4EC>Aurai Gale Mattock</gradient></b>")
            .customEnchants("<#9EB4EC>Frisk")
            .material(Material.NETHERITE_PICKAXE)
            .persistentData(KEY)
            .tier(Tier.VALENTIDE_2026)
            .lore(
                "<#9EB4EC>Holding</#9EB4EC> this mattock will",
                "significantly increase",
                "your movement speed.",
                "",
                "<#9EB4EC>Mined</#9EB4EC> ore veins will",
                "be broken instantly",
                "while using this tool.",
            )
            .vanillaEnchants(
                Enchantment.EFFICIENCY to 10,
                Enchantment.UNBREAKING to 10,
                Enchantment.MENDING to 1
            )
            .attributeModifiers(
                AttributeContainer.builder()
                    .setKey(KEY)
                    .setAttribute(Attribute.MOVEMENT_SPEED)
                    .setOperation(AttributeModifier.Operation.ADD_NUMBER)
                    .setAmount(0.075)
                    .setSlot(EquipmentSlotGroup.MAINHAND)
                    .build()
            )
            .buildPair()
    }

    override fun onBlockDamage(player: Player, event: BlockDamageEvent) {
        val type = event.block.type
        if (!player.isLocationOnGround() && (Tag.BASE_STONE_OVERWORLD.isTagged(type) || Tag.BASE_STONE_NETHER.isTagged(type) || type == Material.END_STONE)) {
            event.instaBreak = true
        }
    }

    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        val type = event.block.type
        if (!Kind.INCLUSIVE_ORES.isTagged(type)) {
            return
        }
        val blocks = Sphere(event.block.location, 7.0).sphereFast
        val item = player.inventory.itemInMainHand

        for (block in blocks) {
            if (block.type != type) continue
            block.breakNaturallyWithLog(player, item, true)
        }
        player.addPotionEffect(HASTE)
    }
}