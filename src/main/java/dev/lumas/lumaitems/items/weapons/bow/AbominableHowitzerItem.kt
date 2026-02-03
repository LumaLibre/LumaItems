package dev.lumas.lumaitems.items.weapons.bow

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.util.Executors
import dev.lumas.lumaitems.util.Executors.syncEntity
import dev.lumas.lumaitems.util.QuickTasks
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Snowman
import org.bukkit.event.block.EntityBlockFormEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class AbominableHowitzerItem : CustomItemFunctions() {

    companion object {
        private val KEY = Util.namespacedKey("abominable-howitzer")
        private val SCALE = AttributeModifier(KEY, -0.5, AttributeModifier.Operation.ADD_NUMBER)
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#c8bdf7:#625998:#8AA4DD:#c8bdf7>Abominable Howitzer</gradient></b>")
            .customEnchants("<#8AA4DD>Derp Launcher")
            .persistentData(KEY)
            .material(Material.BOW)
            .tier(Tier.CHRISTMAS_2025)
            .vanillaEnchants(
                Enchantment.INFINITY to 1,
                Enchantment.MENDING to 1,
            )
            .lore(
                "<#8AA4DD>Fires</#8AA4DD> explosive snowmen",
                "that detonate on impact.",
            )
            .buildPair()
    }


    override fun onPlayerShootBow(player: Player, event: EntityShootBowEvent) {
        if (QuickTasks.isOnCooldown(this, player)) {
            event.isCancelled = true
            return
        }
        QuickTasks.addCooldown(this, player, 20)

        val projectile = event.projectile

        val snowman = player.world.spawn(projectile.location, Snowman::class.java) {
            it.isPersistent = false
            it.velocity = projectile.velocity
            it.isDerp = random().nextBoolean()
            Util.setPersistentKey(it, KEY, PersistentDataType.SHORT, 1)
            val attr =  it.getAttribute(Attribute.SCALE) ?: return@spawn
            attr.addTransientModifier(SCALE)
        }

        event.projectile = snowman

        Executors.asyncTimer(0, 1) { task ->
            if (snowman.isDead) {
                task.cancel()
            } else if (snowman.hasHitBlock()) {
                snowman.syncEntity {
                    snowman.world.createExplosion(snowman.location, 2.4f, false, false)
                    snowman.remove()
                }
                task.cancel()
            }
        }
    }

    override fun onEntityDeath(player: Player, event: EntityDeathEvent) {
        event.droppedExp = 0
        event.drops.clear()
    }

    override fun onEntityFormBlock(event: EntityBlockFormEvent) {
        event.isCancelled = true
        if (!event.entity.isDead) {
            event.entity.world.createExplosion(event.entity.location, 2.4f, false, false)
            event.entity.remove()
        }
    }

    private fun Entity.hasHitBlock(): Boolean {
        if (this.isDead) {
            return true
        }

        val block = this.location.block
        return !block.getRelative(1, 0, 0).type.isAir ||
                !block.getRelative(-1, 0, 0).type.isAir ||
                !block.getRelative(0, 0, 1).type.isAir ||
                !block.getRelative(0, 0, -1).type.isAir
    }
}