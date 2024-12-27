package dev.jsinco.luma.items.tools

import dev.jsinco.luma.items.ItemFactory
import dev.jsinco.luma.manager.CustomItemFunctions
import dev.jsinco.luma.shapes.Sphere
import dev.jsinco.luma.util.AbilityUtil
import dev.jsinco.luma.util.MiniMessageUtil
import dev.jsinco.luma.util.tiers.ThanksgivingEventTier
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.Consumable
import io.papermc.paper.datacomponent.item.FoodProperties
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.UUID

class HolidayHamItem : CustomItemFunctions() {

    companion object {
        private val cooldownPlayers: MutableSet<UUID> = mutableSetOf()
        private const val COOLDOWN_TIME = 3600L
    }

    @Suppress("UnstableApiUsage")
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory.builder()
            .name("<b><#CC8D7A>H<#D89C7C>o<#E4AC7D>l<#F0BB7F>i<#F3C48B>d<#F7CD97>a<#FAD5A3>y <#FEE2B5>H<#FEE6BC>a<#FFEAC2>m</b>")
            .customEnchants("<gray>Consumable", "<#f0bb7f>Leftovers")
            .persistentData("holidayham")
            .material(Material.NETHERITE_PICKAXE)
            .tier(ThanksgivingEventTier.THANKSGIVING_2024)
            .lore("<#645B82>Consume <white>this item", "to self-destruct and", "clear all blocks within", "a small radius.", "", "<red>Cooldown: 3m")
            .vanillaEnchants(Enchantment.FORTUNE to 5, Enchantment.EFFICIENCY to 9, Enchantment.UNBREAKING to 12, Enchantment.MENDING to 1)
            .spoofEnchants(true)
            .build()
            .createItem()

        val foodProps: FoodProperties.Builder = FoodProperties.food()
            .canAlwaysEat(true)
            .nutrition(2)
            .saturation(3.5f)

        item.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft("golden_pickaxe"))
        item.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable())
        item.setData(DataComponentTypes.FOOD, foodProps)
        return Pair("holidayham", item)
    }


    override fun onConsumeItem(player: Player, event: PlayerItemConsumeEvent) {
        event.isCancelled = true
        if (cooldownPlayers.contains(player.uniqueId)) {
            player.sendActionBar(MiniMessageUtil.mm(ThanksgivingEventTier.THANKSGIVING_2024.cannotConsumeMessage))
            return
        }
        cooldownPlayers.add(player.uniqueId)
        clearBlocksInRadius(player) // explosion
        player.addPotionEffect(PotionEffect(PotionEffectType.HASTE, 600, 2)) // haste 3 for 30s

        player.sendActionBar(MiniMessageUtil.mm(ThanksgivingEventTier.THANKSGIVING_2024.consumeMessages.random())) // random message

        Bukkit.getScheduler().runTaskLater(INSTANCE, Runnable {
            cooldownPlayers.remove(player.uniqueId)
        }, COOLDOWN_TIME)
    }

    private fun clearBlocksInRadius(player: Player) {
        val loc: Location = player.location
        val sphere = Sphere(loc, 6.0, 11.0).sphere.filter {
            !AbilityUtil.blockTypeBlacklist.contains(it.type) && it.isSolid
        }
        for (block in sphere) {
            player.breakBlock(block)
        }
        loc.world.spawnParticle(Particle.EXPLOSION, loc, 1, 0.0, 0.0, 0.0, 0.0)
        loc.world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.0f)
        loc.world.playSound(loc, Sound.ENTITY_ALLAY_ITEM_THROWN, 0.5f, 1.6f)
    }
}