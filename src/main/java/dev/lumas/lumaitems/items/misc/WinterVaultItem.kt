package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.annotations.Disable
import dev.lumas.lumaitems.enums.WorldName
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.extensions.Executors
import dev.lumas.lumaitems.util.extensions.QuickTasks
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

@Disable(WorldName.EVENT_NEW)
class WinterVaultItem : CustomItemFunctions() {

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#CBD8E8>W<#B3B1CA>i<#9B8AAC>n<#C9ACBB>t<#F7CEC9>e<#F5D7C7>r <#F0E8C3>V<#DFE1B2>a<#CDDAA0>u<#B0D6B4>l<#93D2C8>t</b>")
            .customEnchants("<#CCD8E9>Launch I")
            .lore("Right-click to be propelled", "into the air.", "", "<red>Cooldown: 15s")
            .material(Material.BREEZE_ROD)
            .vanillaEnchants(Enchantment.UNBREAKING to 10, Enchantment.KNOCKBACK to 4)
            .tier(Tier.WINTER_2024)
            .persistentData("wintervault")
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (QuickTasks.isOnCooldown(this, player.uniqueId)) {
            return
        }

        player.velocity = player.velocity.add(Vector(0.0, 1.5, 0.0)).multiply(2.45)
        player.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1f)


        QuickTasks.addCooldown(this, player.uniqueId, 300L)

        var ticks = 0
        Executors.asyncTimer(0, 1) {
            player.world.spawnParticle(Particle.GUST_EMITTER_SMALL, player.location, 2, 0.3, 0.3, 0.3, 0.1)
            if (ticks++ >= 40) {
                it.cancel()
            }
        }
    }
}