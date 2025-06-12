package dev.jsinco.luma.lumaitems.items.misc

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.manager.CustomItem
import dev.jsinco.luma.lumaitems.util.QuickTasks
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class FrozenFlareShieldItem : CustomItem {

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#B9C5E5>F<#C1C7DF>r<#C8C8D9>o<#D0CAD2>z<#D7CCCC>e<#DFCDC6>n<#E6CFC0>f<#EED1B9>l<#F5D2B3>a<#FDD4AD>r<#F5D2B4>e <#E4CEC2>S<#DBCDC9>h<#D3CBD0>i<#CAC9D7>e<#C2C7DE>l<#B9C5E5>d</b>")
            .customEnchants("<#B9C5E5>Countdown Lighter")
            .quotes("<#B9C5E5>\"<#BCC6E2>S<#C0C7DF>h<#C3C7DD>i<#C7C8DA>e<#CAC9D7>l<#CDCAD4>d<#D1CAD1>i<#D4CBCF>n<#D8CCCC>g<#DBCDC9>? <#E2CEC3>S<#E5CFC1>u<#E9D0BE>r<#ECD0BB>e<#EFD1B8>. <#F6D3B3>E<#FAD3B0>x<#FDD4AD>p<#FAD3B0>l<#F6D3B3>o<#F3D2B5>d<#EFD1B8>i<#ECD0BB>n<#E9D0BE>g<#E5CFC1>?", "<#DECDC6>A<#DBCDC9>b<#D8CCCC>s<#D4CBCF>o<#D1CAD1>l<#CDCAD4>u<#CAC9D7>t<#C7C8DA>e<#C3C7DD>l<#C0C7DF>y<#BCC6E2>.<#B9C5E5>\"")
            .persistentData("frozenflare-shield")
            .material(Material.SHIELD)
            .vanillaEnchants(Enchantment.UNBREAKING to 10, Enchantment.MENDING to 1, Enchantment.SHARPNESS to 5, Enchantment.FIRE_ASPECT to 5)
            .tier(Tier.WINTER_2024)
            .lore("While blocking with this shield,", "hold sneak to charge up a", "powerful explosion.", "", "<red>Cooldown: 15s")
            .buildPair()

//        val item = ItemFactory(
//            "&#7f815d&lD&#8e8f60&la&#9d9c63&lr&#acaa67&lk &#bbb86a&lW&#cac56d&li&#d9d370&lc&#e1db71&lk &#e0de6e&lS&#dfe16c&lh&#dee369&li&#dde667&le&#dce864&ll&#dbeb62&ld",
//            mutableListOf("&#e1da72C&#e3da6fo&#e5da6cu&#e7da6an&#e8db67t&#eadb64d&#ecdb61o&#eedb5ew&#f0db5cn &#f2db59L&#f4db56i&#f6db53g&#f7dc50h&#f9dc4et&#fbdc4be&#fddc48r"),
//            mutableListOf("&#e1da72\"&#e2da70B&#e3da6fo&#e4da6dt&#e5da6ch &#e6da6ap&#e7da68r&#e9db67o&#eadb65t&#ebdb63e&#ecdb62c&#eddb60t&#eedb5fo&#efdb5dr &#f0db5ba&#f1db5an&#f2db58d &#f3db57d&#f4db55e&#f5db53s&#f7dc52t&#f8dc50r&#f9dc4eo&#fadc4dy&#fbdc4be&#fcdc4ar&#fddc48\"","","While blocking with this shield,", "hold sneak to charge up a", "powerful explosion", "", "&cCooldown: 15 secs"),
//            Material.SHIELD,
//            mutableListOf("darkwickshield"),
//            mutableMapOf(Enchantment.UNBREAKING to 10, Enchantment.MENDING to 1, Enchantment.SHARPNESS to 5, Enchantment.FIRE_ASPECT to 5)
//        )
//        item.tier = "&#c46bfb&lH&#c86eee&la&#cd71e2&ll&#d174d5&ll&#d677c8&lo&#da7abc&lm&#de7daf&la&#e380a2&lr&#e78395&le&#eb8689&ls &#f0897c&l2&#f48c6f&l0&#f98f63&l2&#fd9256&l3"
//        return Pair("darkwickshield", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.RIGHT_CLICK -> {
                countdownLighter(player)
            }
            Action.PLAYER_CROUCH -> {
                countdownLighter(player)
            }
            else -> return false
        }
        return true
    }


    private fun countdownLighter(player: Player) {
        if (QuickTasks.isOnCooldown(this, player)) return


        Bukkit.getScheduler().scheduleSyncDelayedTask(instance(), {
            if (!player.isSneaking || QuickTasks.isOnCooldown(this, player)) return@scheduleSyncDelayedTask
            player.world.createExplosion(player.location, 7f, false, false, player)
            player.world.spawnParticle(Particle.FLAME, player.location, 50, 0.5, 0.5, 0.5, 0.8)
            player.world.spawnParticle(Particle.SOUL_FIRE_FLAME, player.location, 50, 0.5, 0.5, 0.5, 0.8)
            QuickTasks.addCooldown(this, player, 300L)
        },40)
    }
}