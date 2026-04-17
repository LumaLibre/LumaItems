package dev.lumas.lumaitems.items.tools.hatchet

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.shapes.Sphere
import dev.lumas.lumaitems.util.AbilityUtil
import dev.lumas.lumaitems.util.tags.Kind
import dev.lumas.lumaitems.util.extensions.QuickTasks
import dev.lumas.lumaitems.util.extensions.actionBar
import dev.lumas.lumaitems.util.extensions.breakNaturallyWithLog
import dev.lumas.lumaitems.util.Tier
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.Consumable
import io.papermc.paper.datacomponent.item.FoodProperties
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

class HolidayHamItem : CustomItemFunctions() {

    private companion object {
        private val CONSUME_MESSAGES = listOf(
            "<gray>:3 <#fddeaf>Yum!",
            "<gray>:o <#fddeaf>Delicious!",
            "<gray>:* <#B2E55D>I think I'm gonna be sick...",
            "<gray>:) <#fddeaf>That was tasty!",
            "<gray>:| <#fddeaf>That tasted kind of funny...?"
        );
        private const val TOO_FULL = "›.‹ <#E55DB0>I'm too full right now!"
    }

    @Suppress("UnstableApiUsage")
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory.builder()
            .name("<b><#CC8D7A>H<#D89C7C>o<#E4AC7D>l<#F0BB7F>i<#F3C48B>d<#F7CD97>a<#FAD5A3>y <#FEE2B5>H<#FEE6BC>a<#FFEAC2>m</b>")
            .customEnchants("<gray>Consumable", "<#f0bb7f>Leftovers")
            .persistentData("holidayham")
            .material(Material.NETHERITE_PICKAXE)
            //.tier(ThanksgivingEventTier.THANKSGIVING_2024)
            // Halloween re-release
            .tier(Tier.HALLOWEEN_2025)
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
        if (QuickTasks.isOnCooldown(this, player.uniqueId)) {
            player.actionBar(TOO_FULL)
            return
        }
        QuickTasks.addCooldown(this, player.uniqueId, 3600L)
        clearBlocksInRadius(player) // explosion
        player.addPotionEffect(PotionEffect(PotionEffectType.HASTE, 600, 2)) // haste 3 for 30s

        player.actionBar(CONSUME_MESSAGES.random()) // random message
    }

    private fun clearBlocksInRadius(player: Player) {
        val loc: Location = player.location
        if (AbilityUtil.noBuildPermission(player, loc.block)) {
            return
        }
        val sphere = Sphere(loc, 6.0, 11.0).sphere.filter {
            !Kind.BLACKLIST.isTagged(it.type) && it.isSolid
        }
        for (block in sphere) {
            block.breakNaturallyWithLog(player)
        }
        loc.world.spawnParticle(Particle.EXPLOSION, loc, 1, 0.0, 0.0, 0.0, 0.0)
        loc.world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.0f)
        loc.world.playSound(loc, Sound.ENTITY_ALLAY_ITEM_THROWN, 0.5f, 1.6f)
    }
}