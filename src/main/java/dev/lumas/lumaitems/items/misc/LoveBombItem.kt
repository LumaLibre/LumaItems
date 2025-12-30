package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.util.disabling.Ignore
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.UseCooldown
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.ItemStack

@Ignore
class LoveBombItem : CustomItemFunctions() {

    @Suppress("UnstableApiUsage")
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory.builder()
            .name("<b><gradient:#ff0000:#ff00ff>Love Bomb</gradient></b>")
            .material(Material.SNOWBALL)
            .persistentData("love-bomb")
            .build()
            .addDataComponents(
                DataComponentTypes.USE_COOLDOWN,
                UseCooldown.useCooldown(20f).build()
            )
            .createItem()
        return Pair("love-bomb", item)
    }


    override fun onProjectileLaunch(player: Player, event: ProjectileLaunchEvent) {
        event.isCancelled = true
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        super.onProjectileLand(player, event)
    }
}