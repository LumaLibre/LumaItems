package dev.lumas.lumaitems.items.armor.helmet

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.extensions.hasPersistentKey
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import org.bukkit.craftbukkit.entity.CraftLivingEntity
import org.bukkit.craftbukkit.entity.CraftMob
import dev.lumas.lumaitems.util.extensions.isItemInSlot
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.extensions.syncDelayed
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.EntityDamageByEntityEvent
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.UUID

class PiglinMaskItem : CustomItemFunctions() {

    companion object {
        private const val KEY = "piglin-mask"
        private const val RALLY_RADIUS = 24.0
        private const val RALLY_CHECK_COOLDOWN_MS = 5000
        private const val FOOD_POISON_DURATION_TICKS = 200
        private const val ANGER_DURATION_TICKS = 600L // 30 seconds
        private val PIGLIN_TYPES = setOf(EntityType.PIGLIN, EntityType.PIGLIN_BRUTE)
        private val PORK_TYPES = setOf(Material.PORKCHOP, Material.COOKED_PORKCHOP)
        private val rally_timestamps = mutableMapOf<UUID, Long>() // todo: capitalize
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#E8B84B:#E56A91:#CA51CB>Piglin Mask</gradient></b>")
            .customEnchants("<gradient:#E8B84B:#E56A91>Kinshift</gradient>")
            .vanillaEnchants(Enchantment.LOYALTY to 6)
            .lore(
                "A deceptively real piglin",
                "face, eternally borrowed.",
                "",
                "Piglins will mistake you",
                "for one of their own and",
                "come to your defense.",
                "",
                "<red>Do not eat your own kind."
            )
            .material(Material.PIGLIN_HEAD)
            .persistentData(KEY)
            .tier(Tier.WONDERLAND_2026)
            .autoHat(true)
            .buildPair()
    }

    override fun onEntityTargetPlayer(player: Player, event: EntityTargetLivingEntityEvent) {
        if (event.entity.type !in PIGLIN_TYPES) return
        if (!player.isItemInSlot(KEY, EquipmentSlot.HEAD)) return
        event.isCancelled = true
    }

    override fun onArmorChange(player: Player, event: PlayerArmorChangeEvent) {
        if (event.newItem.itemMeta?.hasPersistentKey(KEY) != true) return
        player.location.getNearbyEntitiesByType(Mob::class.java, RALLY_RADIUS)
            .filter { it.type in PIGLIN_TYPES && it.target?.uniqueId == player.uniqueId }
            .forEach { piglin -> piglin.sync { (piglin as CraftMob).handle.brain.eraseMemory(MemoryModuleType.ATTACK_TARGET) } }
    }

    override fun onPlayerDamagedByEntity(player: Player, event: EntityDamageByEntityEvent) {
        if (rally_timestamps.containsKey(player.uniqueId)) {
            if (System.currentTimeMillis() - rally_timestamps[player.uniqueId]!! < RALLY_CHECK_COOLDOWN_MS) return
        }
        if (!player.isItemInSlot(KEY, EquipmentSlot.HEAD)) return

        val attacker: LivingEntity = when (val damager = event.damager) {
            is LivingEntity -> damager
            is Projectile -> damager.shooter as? LivingEntity ?: return
            else -> return
        }

        // Don't rally against other players also wearing this item
        if (attacker is Player && attacker.isItemInSlot(KEY, EquipmentSlot.HEAD)) return

        val nmsAttacker = (attacker as CraftLivingEntity).handle
        val attackerUUID = attacker.uniqueId
        rally_timestamps[player.uniqueId] = System.currentTimeMillis()
        player.location.getNearbyEntitiesByType(Mob::class.java, RALLY_RADIUS)
            .filter { it.type in PIGLIN_TYPES } // todo: add a hard cap on how many can get mad
            .forEach { piglin ->
                piglin.sync {
                    val brain = (piglin as CraftMob).handle.brain
                    brain.setMemory(MemoryModuleType.ANGRY_AT, attackerUUID)
                    brain.setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, nmsAttacker, ANGER_DURATION_TICKS)
                    brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
                }
            }
    }

    override fun onConsumeItem(player: Player, event: PlayerItemConsumeEvent) {
        if (!player.isItemInSlot(KEY, EquipmentSlot.HEAD)) return
        if (event.item.type !in PORK_TYPES) return
        player.syncDelayed(1) { _ ->
            player.addPotionEffect(PotionEffect(PotionEffectType.POISON, FOOD_POISON_DURATION_TICKS, 0))
            player.addPotionEffect(PotionEffect(PotionEffectType.NAUSEA, FOOD_POISON_DURATION_TICKS, 0))
        }
    }
}
