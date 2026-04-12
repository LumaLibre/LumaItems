package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.extensions.QuickTasks
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class SweetSummerHoneyItem : CustomItemFunctions() {

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#ed8c00:#ffbe42:#cc5d01:#ffb100:#e8763c>Sweet Summer Honey</gradient></b>")
            .customEnchants("<#ffb100>Pure Bliss")
            .material(Material.HONEY_BOTTLE)
            .persistentData("sweet-summer-honey")
            .tier(Tier.SUMMER_2025)
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .lore(
                "A sweet, sticky honey that",
                "just really raises your blood",
                "sugar levels.",
                "",
                "Holding this bottle will give",
                "you a <#cc5d01>haste</#cc5d01> and <#cc5d01>speed</#cc5d01> boost."
            )
            .buildPair()
    }

    override fun onRunnable(player: Player) {
        player.addPotionEffect(potionEffect(PotionEffectType.HASTE, 350, 0, false))
        player.addPotionEffect(potionEffect(PotionEffectType.SPEED, 350, 0, false))
    }

    override fun onConsumeItem(player: Player, event: PlayerItemConsumeEvent) {
        event.isCancelled = true
        if (!QuickTasks.isOnCooldown(this, player)) {
            QuickTasks.addCooldown(this, player, 8400L) // 7 minutes
            player.addPotionEffect(potionEffect(PotionEffectType.ABSORPTION, 1200, 3, true))
        }
    }

    private fun potionEffect(type: PotionEffectType, ticks: Int, amplifier: Int, showParticles: Boolean): PotionEffect {
        return PotionEffect(type, ticks, amplifier, false, showParticles, true)
    }
}
