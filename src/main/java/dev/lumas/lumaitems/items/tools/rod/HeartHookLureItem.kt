package dev.lumas.lumaitems.items.tools.rod

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.util.AbilityUtil
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerFishEvent.State
import org.bukkit.inventory.ItemStack

class HeartHookLureItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#ED7CFF:#FDADCA:#EB5067>HeartHook Lure")
            .customEnchants("<gray>Unbreakable", "<#FF7CAD>Unwavering Lure", "<#ED7CFF>Grapple")
            .vanillaEnchants(Enchantment.LURE to 6)
            .tier(Tier.VALENTIDE_2025)
            .unbreakable(unbreakable = true)
            .persistentData("hearthook-lure")
            .material(Material.FISHING_ROD)
            .lore(
                "This rod's hook will travel",
                "further, faster, and not",
                "be affected by skylight.",
                "",
                "Hook onto a nearby",
                "entity to instantly reel it",
                "to you, or <#EB5067>sneak<white> to reel",
                "yourself to the entity."
            )
            .buildPair()
    }

    override fun onFish(player: Player, event: PlayerFishEvent) {
        val hook = event.hook
        when (event.state) {
            State.FISHING -> {
                hook.isSkyInfluenced = false
                hook.velocity = hook.velocity.multiply(1.8)
            }
            State.CAUGHT_ENTITY -> {
                val hookedEntity = hook.hookedEntity ?: return
                if (AbilityUtil.noDamagePermission(player, hookedEntity) || hookedEntity !is LivingEntity) return

                if (player.isSneaking) {
                    player.teleportAsync(hookedEntity.location)
                } else {
                    hookedEntity.teleportAsync(player.location)
                }
                event.isCancelled = true
                hook.remove()
            }
            else -> return
        }
    }
}