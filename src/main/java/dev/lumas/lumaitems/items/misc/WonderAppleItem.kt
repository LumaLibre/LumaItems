package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.annotations.Disable
import dev.lumas.lumaitems.model.item.AttributeContainer
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.addCooldown
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.isOnCooldown
import dev.lumas.lumaitems.util.extensions.itemInOffHand
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.syncDelayed
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

@Disable(standard = true, vanilla = true, invert = true)
class WonderAppleItem : CustomItemFunctions() {

    private companion object {
        private val KEY = "wonder-apple".namespacedKey()
        private val HASTE = PotionEffect(PotionEffectType.HASTE, 260, 1, false, false, false)
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#A26EDA:#E56A91:#F3AA4C:#E92F5C>Wonder Apple</gradient></b>")
            .customEnchants("<#E56A91>Haste II")
            .material(Material.GOLDEN_APPLE)
            .persistentData(KEY)
            .tier(Tier.WONDERLAND_2026)
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .lore(
                "A delicious apple that",
                "appears causes weird",
                "effects when <#E56A91>eaten</#E56A91>.",
                "",
                "While <#E56A91>held</#E56A91> in the",
                "offhand, you gain a",
                "Haste II boost."
            )
            .buildPair()
    }

    override fun onRunnable(player: Player) {
        val offhand = player.itemInOffHand
        if (offhand.isMatchingItem(KEY)) {
            player.addPotionEffect(HASTE)
        }
    }

    override fun onConsumeItem(player: Player, event: PlayerItemConsumeEvent) {
        val item = event.item
        if (item.isMatchingItem(KEY)) {
            event.isCancelled = true
        }

        if (player.isOnCooldown(this)) {
            return
        }

        player.addCooldown(this, 120 * 20)

        val attribute = player.getAttribute(Attribute.SCALE) ?: return
        val modifier = AttributeContainer.builder(KEY)
            .setAttribute(Attribute.SCALE)
            .setAmount(random.nextDouble(-3.0, 7.0))
            .setOperation(AttributeModifier.Operation.ADD_SCALAR)
            .build()
            .modifier()
        attribute.addTransientModifier(modifier)
        player.world.playSound(player.location, Sound.ITEM_BOTTLE_FILL, 1f, 1f)

        player.syncDelayed(30 * 20) {
            attribute.removeModifier(modifier)
            player.world.playSound(player.location, Sound.ITEM_BOTTLE_EMPTY, 1f, 1f)
        }
    }
}