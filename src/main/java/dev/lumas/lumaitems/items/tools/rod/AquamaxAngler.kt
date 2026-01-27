package dev.lumas.lumaitems.items.tools.rod

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.inventory.ItemStack

class AquamaxAngler : CustomItemFunctions() {

    companion object {
        private val AQUATIC_TYPES = Tag.ENTITY_TYPES_AQUATIC
            .values
            .plus(EntityType.DROWNED)
            .plus(EntityType.FROG) // aquatic in nature
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#22afba:#54d8ba:#abf8e4:#7be0b5:#10c0ac>Aquamax Angler</gradient></b>")
            .customEnchants("<#7be0b5>Deep Sea Lure")
            .material(Material.FISHING_ROD)
            .persistentData("aquamax-angler")
            .tier(Tier.CHRISTMAS_2025)
            .vanillaEnchants(
                Enchantment.LURE to 4,
                Enchantment.SHARPNESS to 7,
                Enchantment.IMPALING to 10,
                Enchantment.UNBREAKING to 6,
                Enchantment.MENDING to 1
            )
            .lore(
                "A fishing rod that is",
                "unable to bait typical",
                "fish and is only capable",
                "of reeling in aquatic",
                "creatures."
            )
            .buildPair()
    }

    override fun onFish(player: Player, event: PlayerFishEvent) {
        if (event.state != PlayerFishEvent.State.CAUGHT_FISH) {
            return
        }

        val hook = event.hook
        val caught = event.caught ?: return
        val newEntity = player.world.spawnEntity(hook.location, AQUATIC_TYPES.random())
        val direction = player.location.toVector().subtract(hook.location.toVector()).normalize()
        newEntity.velocity = direction.multiply(1.2).apply {
            y += 0.15 // y bias for arc
        }
        caught.remove()
        hook.hookedEntity = newEntity
    }
}