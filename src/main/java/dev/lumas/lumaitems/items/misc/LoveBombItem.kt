package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.annotations.Ignore
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.extensions.setPersistentKey
import dev.lumas.lumaitems.util.extensions.toBukkitColor
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.UseCooldown
import org.bukkit.Material
import org.bukkit.entity.AreaEffectCloud
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

@Ignore
class LoveBombItem : CustomItemFunctions() {

    @Suppress("UnstableApiUsage")
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory.builder()
            .name("<b><gradient:#ff0000:#ff00ff>Love Bomb</gradient></b>")
            .material(Material.WIND_CHARGE)
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
        val entity = event.entity
        entity.setPersistentKey("love-bomb", PersistentDataType.SHORT, 1)
        entity.setGravity(true)
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val entity = event.entity

        val areaEffectCloud = entity.world.spawnEntity(entity.location, EntityType.AREA_EFFECT_CLOUD) as AreaEffectCloud
        areaEffectCloud.setColor("#ff00ff".toBukkitColor())
    }
}