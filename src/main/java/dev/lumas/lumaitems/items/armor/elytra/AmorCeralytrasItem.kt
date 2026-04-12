package dev.lumas.lumaitems.items.armor.elytra

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.AbilityUtil
import dev.lumas.lumaitems.util.extensions.syncTimer
import dev.lumas.lumaitems.util.Tier
import java.util.UUID
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.ItemStack

open class AmorCeralytrasItem : CustomItemFunctions() {

    companion object {
        private val BOOST_COUNT: MutableMap<UUID, Int> = mutableMapOf()
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#fd9a7f>A<#fda583>m<#fdb088>o<#fdba8c>r <#fdc590>C<#fdd095>e<#fddb99>r<#fddb9c>a<#fdd19f>l<#fdc7a2>y<#fdbea5>t<#fdb4a7>r<#fdaaaa>a<#fda0ad>s")
            .customEnchants("<#FD9A7F>Eros")
            .material(Material.ELYTRA)
            .persistentData("amorceralytras")
            .lore(
                "Allows the wearer to triple",
                "jump when <#FD9A7F>sneaking</#FD9A7F> midair.",
                "",
                "<#FD9A7F>Sneak</#FD9A7F> and <#FD9A7F>hold</#FD9A7F> for 4s",
                "to activate a high boost."
            )
            .vanillaEnchants(Enchantment.MENDING to 1, Enchantment.PROTECTION to 8, Enchantment.UNBREAKING to 10, Enchantment.FEATHER_FALLING to 5, Enchantment.PROJECTILE_PROTECTION to 4)
            .tier(Tier.VALENTIDE_2026)
            .buildPair()
    }

    override fun onMove(player: Player, event: PlayerMoveEvent) {
        if (!BOOST_COUNT.contains(player.uniqueId) || !AbilityUtil.isOnGround(player)) return
        BOOST_COUNT.remove(player.uniqueId)
    }

    override fun onPlayerCrouch(player: Player, event: PlayerToggleSneakEvent) {
        if (!player.isSneaking && AbilityUtil.isOnGround(player)) {

            player.syncTimer(0, 5) { task ->
                if (!player.isSneaking || !AbilityUtil.isOnGround(player)) {
                    if (BOOST_COUNT.contains(player.uniqueId) && BOOST_COUNT[player.uniqueId]!! >= 80) {
                        player.velocity = player.velocity.multiply(8.2).setY(3.0)
                    }
                    BOOST_COUNT.remove(player.uniqueId)
                    task.cancel()
                    return@syncTimer
                }

                BOOST_COUNT[player.uniqueId] = BOOST_COUNT.getOrDefault(player.uniqueId, 0) + 5


                if (BOOST_COUNT[player.uniqueId]!! % 80 == 0) {
                    player.world.spawnParticle(Particle.DUST, player.location, 40, 0.5, 0.0, 0.5, 0.5, DustOptions(Color.RED, 1f))
                    player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f)
                }
            }
        }

        if (player.isSneaking || player.isInWater || player.isFlying || AbilityUtil.isOnGround(player) ||
            (BOOST_COUNT.contains(player.uniqueId) && BOOST_COUNT[player.uniqueId]!! >= 2)) return

        player.velocity = player.location.direction.multiply(0.6).setY(0.7)
        BOOST_COUNT[player.uniqueId] = BOOST_COUNT.getOrDefault(player.uniqueId, 0) + 1
    }

}

/*
        val item = ItemFactory(
            "&#fd9a7f&lA&#fda583&lm&#fdb088&lo&#fdba8c&lr &#fdc590&lC&#fdd095&le&#fddb99&lr&#fddb9c&la&#fdd19f&ll&#fdc7a2&ly&#fdbea5&lt&#fdb4a7&lr&#fdaaaa&la&#fda0ad&ls",
            mutableListOf("&#FD9A7FEros"),
            mutableListOf("Allows the wearer to triple", "jump when sneaking midair", "", "Crouch and hold for 4 secs", "to activate a high boost"),
            Material.ELYTRA,
            mutableListOf("armorceralytras"),
            mutableMapOf(Enchantment.MENDING to 1, Enchantment.PROTECTION to 8, Enchantment.UNBREAKING to 10, Enchantment.FEATHER_FALLING to 5, Enchantment.PROJECTILE_PROTECTION to 4)
        )
        item.tier = "&#fb5a5a&lV&#fb6069&la&#fc6677&ll&#fc6c86&le&#fc7294&ln&#fd78a3&lt&#fd7eb2&li&#fb83be&ln&#f788c9&le&#f38dd4&ls &#f092df&l2&#ec97e9&l0&#e89cf4&l2&#e4a1ff&l4"
        return Pair("armorceralytras", item.createItem())
 */