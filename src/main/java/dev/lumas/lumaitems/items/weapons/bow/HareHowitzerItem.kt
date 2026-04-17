package dev.lumas.lumaitems.items.weapons.bow

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.Executors
import dev.lumas.lumaitems.util.extensions.QuickTasks
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Rabbit
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class HareHowitzerItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#a75b72:#bfa4db:#f0dfd2:#c59065>Hare Howitzer</gradient></b>")
            .customEnchants("<gold>O'Hare")
            .vanillaEnchants(
                Enchantment.UNBREAKING to 5,
                Enchantment.INFINITY to 1,
                Enchantment.MENDING to 1
            )
            .lore(
                "After drawing, fires a",
                "mock rabbit.",
                "",
                "Rabbits fired will explode",
                "upon hitting a block.",
            )
            .tier(Tier.EASTER_2025)
            .material(Material.BOW)
            .persistentData("hare-howitzer")
            .buildPair()
    }

    override fun onPlayerShootBow(player: Player, event: EntityShootBowEvent) {
        if (QuickTasks.isOnCooldown(this, player)) {
            event.isCancelled = true
            return
        }
        QuickTasks.addCooldown(this, player, 20)

        val rabbit = player.world.spawn(event.projectile.location, Rabbit::class.java) {
            it.isPersistent = false
            it.velocity = event.projectile.velocity
            it.fallDistance = 100f
            Util.setPersistentKey(it, "hare-howitzer", PersistentDataType.SHORT, 1)
        }

        event.projectile = rabbit

        Executors.asyncTimer(0, 1) { task ->
            if (!rabbit.isValid) {
                task.cancel()
            } else if (hasHitBlock(rabbit)) {
                rabbit.sync {
                    rabbit.world.createExplosion(rabbit.location, 2.4f, false, false)
                }
                task.cancel()
            }
        }
    }

    override fun onEntityDeath(player: Player, event: EntityDeathEvent) {
        event.droppedExp = 0
        event.drops.clear()
    }

    private fun hasHitBlock(entity: Entity): Boolean {
        if (entity.isDead) {
            return true
        }

        val block = entity.location.block
        return !block.getRelative(1, 0, 0).type.isAir ||
                !block.getRelative(-1, 0, 0).type.isAir ||
                !block.getRelative(0, 0, 1).type.isAir ||
                !block.getRelative(0, 0, -1).type.isAir
    }
}