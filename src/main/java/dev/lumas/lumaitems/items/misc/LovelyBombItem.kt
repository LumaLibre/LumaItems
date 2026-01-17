package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class LovelyBombItem : CustomItemFunctions(){

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#C5ADFF:#8F79F8:#B36EAF:#DB6B90:#77C679:#CBF6B7>Love Bombs</gradient></b>")
            .customEnchants("<#c5adff>Lovesplosion")
            .persistentData("lovely-bombs")
            .material(Material.SNOWBALL)
            .vanillaEnchants(
                Enchantment.UNBREAKING to 5,
            )
            .lore(
                "<#c5adff>Right click</#c5adff> to throw",
                "a love bomb dealing",
                "area damage to mobs."
            )
            .tier(Tier.DEBUG)
            .buildPair()
    }

    override fun onProjectileLaunch(player: Player, event: ProjectileLaunchEvent) {
        event.isCancelled = true
        val original = event.entity
        player.world.spawn(original.location, Snowball::class.java).apply {
            velocity = original.velocity
            item = ItemStack.of(Material.RED_DYE)
            Util.setPersistentKey(this, "lovely-bombs", PersistentDataType.SHORT, 1)
            shooter = player
        }
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val snowball = event.entity as? Snowball ?: return
        snowball.world.createExplosion(snowball.location, 3.0f, false, false)
    }
}