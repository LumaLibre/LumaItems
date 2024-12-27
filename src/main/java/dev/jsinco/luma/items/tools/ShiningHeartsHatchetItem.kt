package dev.jsinco.luma.items.tools

import dev.jsinco.luma.items.ItemFactory
import dev.jsinco.luma.enums.Action
import dev.jsinco.luma.manager.CustomItem
import dev.jsinco.luma.util.AbilityUtil
import dev.jsinco.luma.util.Util
import dev.jsinco.luma.util.disabling.Disable
import dev.jsinco.luma.util.disabling.WorldName
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import java.util.UUID
import kotlin.random.Random

@Disable(WorldName.EVENT_NEW)
class ShiningHeartsHatchetItem : CustomItem {

    companion object {
        private val blockAbility: MutableSet<UUID> = mutableSetOf() // recursive block break
    }

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#fbbdb7&lS&#fbb8b7&lh&#fbb4b7&li&#fcafb7&ln&#fcabb7&li&#fca6b6&ln&#fca2b6&lg &#fc9db6&lH&#fd99b6&le&#fd94b6&la&#fd90b1&lr&#fd8ba8&lt&#fd869f&ls &#fd8196&lH&#fd7d8d&la&#fd7884&lt&#fd737b&lc&#fd6e72&lh&#fd6a69&le&#fd6560&lt",
            mutableListOf("&#fd8ba8Lover's Present"),
            mutableListOf("Grants a chance to drop additional", "logs when cutting trees down", "", "When attacking, nearby enemies have", "a chance to be damaged as well"),
            Material.NETHERITE_AXE,
            mutableListOf("shiningheartshatchet"),
            mutableMapOf(Enchantment.MENDING to 1, Enchantment.UNBREAKING to 10, Enchantment.EFFICIENCY to 8, Enchantment.FORTUNE to 5, Enchantment.SWEEPING_EDGE to 4, Enchantment.SHARPNESS to 7)
        )
        item.tier = "&#fb5a5a&lV&#fb6069&la&#fc6677&ll&#fc6c86&le&#fc7294&ln&#fd78a3&lt&#fd7eb2&li&#fb83be&ln&#f788c9&le&#f38dd4&ls &#f092df&l2&#ec97e9&l0&#e89cf4&l2&#e4a1ff&l4"
        return Pair("shiningheartshatchet", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.BREAK_BLOCK -> {
                if (Random.nextInt(100) > 3) return false
                event as BlockBreakEvent


                val drops = event.block.drops
                var block: Block? = null
                for (drop in drops) {
                    if (drop.type.name.endsWith("_LOG")) {
                        block = event.block
                        block.world.dropItemNaturally(block.location, ItemStack(drop.type, 14))
                        break
                    }
                }
                if (block == null) return false

                block.world.spawnParticle(Particle.WITCH, block.location, 10, 0.5, 0.5, 0.5, 0.0)
                block.world.playSound(block.location, Sound.ENTITY_FIREWORK_ROCKET_BLAST_FAR, 0.7f, 1.0f)
            }

            Action.ENTITY_DAMAGE -> {
                if (Random.nextInt(100) > 45 || blockAbility.contains(player.uniqueId)) return false
                event as EntityDamageByEntityEvent
                val entity = event.entity as? LivingEntity ?: return false
                if (AbilityUtil.noDamagePermission(player, entity)) return false

                val entities = event.entity.getNearbyEntities(6.0, 6.0, 6.0) .mapNotNull { it as? LivingEntity }.toMutableList()
                entities.add(entity)


                entity.world.playSound(entity.location, Sound.ITEM_AXE_STRIP, 1f, 0.9f)
                entity.world.playSound(entity.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.7f, 0.9f)

                blockAbility.add(player.uniqueId)
                for (entity1: LivingEntity in Util.splitRandomList(entities, entities.size / 2) as List<LivingEntity>) {
                    if (entity1 == player || AbilityUtil.noDamagePermission(player, entity1)) continue
                    entity1.world.spawnParticle(Particle.SWEEP_ATTACK, entity.location, 3, 0.5, 0.5, 0.5, 0.1)
                    entity1.damage(6.0, player)
                }
                blockAbility.remove(player.uniqueId)
            }

            else -> return false
        }
        return true
    }
}