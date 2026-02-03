package dev.lumas.lumaitems.items.weapons.bow

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.util.BukkitVectors
import dev.lumas.lumaitems.util.Executors.syncDelayed
import dev.lumas.lumaitems.util.Executors.syncTimer
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.setPersistentKey
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Player
import org.bukkit.entity.SpectralArrow
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType


class UnnamedBowItem : CustomItemFunctions() {

    private companion object {
        val KEY = Util.namespacedKey("unnamed-bow-5")
        val ARROW = ItemStack.of(Material.ARROW)
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("Unnamed Bow 5")
            .material(Material.CROSSBOW)
            .persistentData(KEY)
            .vanillaEnchants(
                Enchantment.QUICK_CHARGE to 4,
                Enchantment.PIERCING to 5,
                Enchantment.UNBREAKING to 5,
                Enchantment.MENDING to 1
            )
            .buildPair()
    }

    override fun onPlayerShootBow(player: Player, event: EntityShootBowEvent) {
        event.projectile = player.launchProjectile(SpectralArrow::class.java).also {
            it.setPersistentKey(KEY, PersistentDataType.SHORT, 1)
            it.itemStack = ARROW
            it.velocity = it.velocity.multiply(4.0)
            if (player.gameMode == GameMode.CREATIVE) {
                it.pickupStatus = AbstractArrow.PickupStatus.CREATIVE_ONLY
            }
        }
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        if (event.hitBlock == null) {
            return
        }

        val projectile = event.entity as? AbstractArrow ?: return
        projectile.syncDelayed(20) {
            if (projectile.isDead) {
                return@syncDelayed
            }

            projectile.setNoPhysics(true)

            var count = 0
            player.syncTimer(0,1) { task ->
                val distance = player.eyeLocation.distanceSquared(projectile.location)
                if (distance < 0.5 * 0.5 || projectile.isDead || ++count > 200) {
                    task.cancel()
                    return@syncTimer
                }

                projectile.velocity = BukkitVectors.flyToLivingEntity(player, projectile, 2.8, 0.9, 0.89)
            }
        }
    }

}