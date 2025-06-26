package dev.jsinco.luma.lumaitems.items.misc

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import kotlin.math.max
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.Statistic
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.inventory.ItemStack

class SeasideSurfRodItem : CustomItemFunctions() {

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#EFB7AE>S<#EDBEB1>e<#EAC4B5>a<#E8CBB8>s<#E0CCBE>i<#D9CCC4>d<#D1CDCA>e <#BAD2DA>S<#AFD4E2>u<#A6D0DB>r<#9ECDD4>f <#8BC1C5>R<#81BABE>o<#77B2B6>d")
            .customEnchants("<#95c9cd>Sunside Bets")
            .material(Material.FISHING_ROD)
            .persistentData("seasidesurfrod")
            .tier(Tier.SUMMER_2025)
            .lore(
                "Fish with this rod to",
                "wager your catches!",
                "",
                "Your chances of <#EFB7AE>gambling</#EFB7AE>",
                "and a <#EFB7AE>favorable</#EFB7AE> gamble",
                "increase with the amount",
                "of fish you catch.",
            )
            .vanillaEnchants(
                Enchantment.LURE to 5,
                Enchantment.LUCK_OF_THE_SEA to 5,
                Enchantment.UNBREAKING to 4,
                Enchantment.MENDING to 1
            )
            .buildPair()
    }


    override fun onFish(player: Player, event: PlayerFishEvent) {
        if (event.state != PlayerFishEvent.State.CAUGHT_FISH) {
            return
        }

        val chance: Int =  player.getStatistic(Statistic.FISH_CAUGHT) / 1000
        if (random().nextInt(100) > max((10 + chance).toDouble(), 15.0)) return  // chance to wager is 10% + 1% per 1000 fish caught, max 15%

        val caught = event.caught as? Item ?: return
        val partLoc = caught.location.add(0.0, 1.0, 0.0)
        // 70% base chance + 1% per 1000 fish caught
        if (random().nextInt(100) <= 70 + chance) {
            quickParticle(Particle.DUST, partLoc, DustOptions(Util.hex2BukkitColor("#b08cfd"), 2f))
            quickParticle(Particle.DUST, partLoc, DustOptions(Util.hex2BukkitColor("#bdf4fb"), 2f))
            caught.world.playSound(caught.location, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR, 1f, 1f)
            if (caught.itemStack.maxStackSize > 1) {
                caught.itemStack.amount = random().nextInt(2, 3)
            }
        } else {
            quickParticle(Particle.DUST, partLoc, DustOptions(Util.hex2BukkitColor("#ff0a0a"), 2f))
            quickParticle(Particle.WITCH, partLoc, 0.1)
            caught.world.playSound(caught.location, Sound.ENTITY_WITCH_CELEBRATE, 0.5f, 1f)
            caught.remove()
        }
    }


    private fun quickParticle(particle: Particle, loc: Location, extra: Any) {
        loc.world?.spawnParticle(
            particle, loc, 15, 0.5, 0.5, 0.5, 0.1, extra
        )
    }
}

/*
val item = ItemFactory(
            "&#EFB7AE&lS&#EDBEB1&le&#EAC4B5&la&#E8CBB8&ls&#E0CCBE&li&#D9CCC4&ld&#D1CDCA&le &#BAD2DA&lS&#AFD4E2&lu&#A6D0DB&lr&#9ECDD4&lf &#8BC1C5&lR&#81BABE&lo&#77B2B6&ld",
            mutableListOf("&#95c9cdSunside Bets"),
            mutableListOf("While fishing with this rod, you will","have the chance to catch a surplus","of fish or lose your catch.","","Your chances of wagering and a","favorable gamble increase with the","amount of fish you have caught."),
            Material.FISHING_ROD,
            mutableListOf("seasidesurfrod"),
            mutableMapOf(Enchantment.LURE to 5, Enchantment.LUCK_OF_THE_SEA to 5, Enchantment.UNBREAKING to 9, Enchantment.MENDING to 1)
        )
        item.tier = "&#F34848&lS&#E36643&lo&#D3843E&ll&#C3A239&ls&#B3C034&lt&#A3DE2F&li&#93FC2A&lc&#7DE548&le&#66CD66&l &#50B684&l2&#399EA1&l0&#2387BF&l2&#0C6FDD&l4"
        return Pair("seasidesurfrod", item.createItem())
 */