package dev.jsinco.luma.lumaitems.items.weapons.cutlass

import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.disabling.Ignore
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack

@Ignore
class BigSwingSword : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        TODO("Not yet implemented")
    }

    override fun onEntityDamage(player: Player, event: EntityDamageByEntityEvent) {
        super.onEntityDamage(player, event)
    }
}