package dev.jsinco.luma.items.tools

import dev.jsinco.luma.enums.Action
import dev.jsinco.luma.util.tiers.Tier
import dev.jsinco.luma.items.ItemFactory
import dev.jsinco.luma.manager.CustomItem
import dev.jsinco.luma.particles.ParticleDisplay
import dev.jsinco.luma.particles.Particles
import dev.jsinco.luma.shapes.ShapeUtil
import dev.jsinco.luma.util.AbilityUtil
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.data.Ageable
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import java.awt.Color
import java.util.UUID

class CircusHarrowerItem : CustomItem {

    companion object {
        private val particleColors: Map<Material, Color> = mapOf(
            Material.WHEAT to Color(220, 187, 101),
            Material.BEETROOTS to Color(164, 39, 44),
            Material.CARROTS to Color(255, 142, 9),
            Material.POTATOES to Color(200, 151, 58),
            Material.NETHER_WART to Color(165, 36, 47),
            Material.MELON to Color(76, 120, 26),
            Material.PUMPKIN to Color(199,121,25),
            Material.COCOA to Color(87, 51, 26)
        )

        private val cooldown: MutableList<UUID> = mutableListOf()
    }


    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#ba6060:#849696>Circ</gradient><gradient:#849696:#dd8a8a>us H</gradient><gradient:#dd8a8a:#eed19b>arr</gradient><gradient:#eed19b:#85665c>ower</gradient></b>")
            .customEnchants("<gradient:#ba6060:#849696>Rin</gradient><gradient:#849696:#dd8a8a>gMas</gradient><gradient:#dd8a8a:#eed19b>ter</gradient>")
            .lore("Right-click while sneaking to", "automatically harvest fully", "grown crops in a <#eed19b>5</#eed19b> block", "radius.", "",
                "<red>Cooldown: 20s</red>")
            .material(Material.NETHERITE_HOE)
            .tier(Tier.CARNIVAL_2024)
            .persistentData("circusharrower")
            .vanillaEnchants(mutableMapOf(Enchantment.MENDING to 1, Enchantment.UNBREAKING to 6, Enchantment.EFFICIENCY to 5, Enchantment.FORTUNE to 4))
            .buildPair()
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.RIGHT_CLICK -> {
                event as PlayerInteractEvent

                if (cooldown.contains(player.uniqueId) || !player.isSneaking) {
                    return false
                }

                val blocks: MutableIterator<Block> = ShapeUtil.circle(player.location.add(0.0, 0.2, 0.0), 5, 25).filter {
                    particleColors.contains(it.type) && it.blockData is Ageable && (it.blockData as Ageable).age == (it.blockData as Ageable).maximumAge
                }.toMutableList().iterator()

                if (!blocks.hasNext() || AbilityUtil.noBuildPermission(player, blocks.next())) {
                    return false
                }

                cooldown.add(player.uniqueId)

                object : BukkitRunnable() {
                    val particleDisplay = ParticleDisplay.of(Particle.DUST)
                    val item = player.inventory.itemInMainHand

                    override fun run() {
                        if (!blocks.hasNext()) {
                            this.cancel()
                            Bukkit.getScheduler().runTaskLater(instance(), Runnable {
                                cooldown.remove(player.uniqueId)
                            }, 400L)
                            return
                        }
                        val block = blocks.next()

                        if (particleColors.contains(block.type)) {
                            Particles.line(player.location.add(0.0,1.0,0.0), block.location, 0.2, particleDisplay.withColor(particleColors[block.type] ?: particleColors.getValue(Material.WHEAT)))
                            //block.world.playSound(block.location, Sound.ENTITY_ALLAY_ITEM_GIVEN, 0.6f, 7f)
                            block.breakNaturally(item, true)
                        }
                        blocks.remove()
                    }
                }.runTaskTimer(instance(), 0, 5L)
            }

            else -> return false
        }
        return true
    }

}