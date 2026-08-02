package dev.lumas.lumaitems.items.tools.rod

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.BukkitVectors
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.extensions.syncDelayed
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.isHoldingTwoRods
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random
import net.minecraft.world.entity.projectile.FishingHook
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.craftbukkit.entity.CraftFishHook
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.FishHook
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class FluxRodItem : CustomItemFunctions() {

    private companion object {
        val activeMultiHooks: MutableMap<UUID, MultiHook> = ConcurrentHashMap()
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#6d2b3c:#ba516d:#d9a0ae>Flux Rod</gradient></b>")
            .customEnchants("<#ba516d>Multi-Hook")
            .tagline("#ba516d", "Third time's the charm.")
            .lore(
                "A fishing rod with the",
                "rare ability to cast out",
                "multiple lines at once.",
                "",
                "<#ba516d>Right-click</#ba516d> to reel in a",
                "single, individual line.",
            )
            .material(Material.FISHING_ROD)
            .persistentData("flux-rod")
            .vanillaEnchants(
                Enchantment.LURE to 4,
                Enchantment.LUCK_OF_THE_SEA to 5,
                Enchantment.UNBREAKING to 3,
                Enchantment.MENDING to 1
            )
            .tier(Tier.VALENTIDE_2026)
            .buildPair()
    }

    override fun asyncGlobalTask() { // lazy cleanup
        for ((uuid, multiHook) in activeMultiHooks) {
            val player = Bukkit.getPlayer(uuid)
            if (player == null) { // hooks discard themselves once their owner is gone
                activeMultiHooks.remove(uuid, multiHook)
                continue
            }

            player.sync {
                if (multiHook.isInvalid() || multiHook.isOrphanedBy(player)) {
                    multiHook.destroy()
                    activeMultiHooks.remove(uuid, multiHook)
                }
            }
        }
    }

    override fun onFish(player: Player, event: PlayerFishEvent) {
        val hook = event.hook
        val multiHook = activeMultiHooks[player.uniqueId]

        if (multiHook != null && multiHook.contains(hook)) {
            if (multiHook.handleState(event, event.state)) {
                activeMultiHooks.remove(player.uniqueId, multiHook)
            }
            return
        }

        if (event.state != PlayerFishEvent.State.FISHING) return

        multiHook?.destroy()
        activeMultiHooks.remove(player.uniqueId)

        if (player.isHoldingTwoRods()) return

        val originalDir = hook.velocity.clone().normalize()
        val speed = hook.velocity.length()
        val newMultiHook = MultiHook(hook).also { activeMultiHooks[player.uniqueId] = it }

        for (i in 0 until 2) {
            var rand = random().nextDouble(10.0, 40.0)
            if (i % 2 == 0) {
                rand = -rand
            }
            val dir = BukkitVectors.rotateVectorY(originalDir, Math.toRadians(rand))
            newMultiHook.create(dir, speed)
        }
    }


    private class MultiHook(
        val origin: FishHook
    ) {

        val hookPool: MutableSet<FishHook> = mutableSetOf(origin)


        fun create(dir: Vector, speed: Double) {
            val player = this.origin.shooter as Player
            val hook = player.launchProjectile(FishHook::class.java)
            //hook.shooter = player
            hook.velocity = dir.multiply(speed)
            hook.minWaitTime = this.origin.minWaitTime
            hook.maxWaitTime = this.origin.maxWaitTime
            hook.minLureTime = this.origin.minLureTime
            hook.maxLureTime = this.origin.maxLureTime
            hook.applyLure = this.origin.applyLure
            hook.isRainInfluenced = this.origin.isRainInfluenced
            hook.isSkyInfluenced = this.origin.isSkyInfluenced
            hook.setLureAngle(this.origin.minLureAngle, this.origin.maxLureAngle)

            // apply lureSpeed with NMS
            val originNMSFishingHook = (this.origin as? CraftFishHook)?.handle ?: throw IllegalStateException()
            val nmsFishingHook: FishingHook = (hook as? CraftFishHook)?.handle ?: throw IllegalStateException()

            // time to reflect
            try {
                val lureSpeedField = FishingHook::class.java.getDeclaredField("lureSpeed")
                lureSpeedField.isAccessible = true
                val lureSpeedValue = lureSpeedField.getInt(originNMSFishingHook) - Random.nextInt(-200, 200)

                lureSpeedField.setInt(nmsFishingHook, lureSpeedValue)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            hookPool.add(hook)
        }


        fun handleState(event: PlayerFishEvent, state: PlayerFishEvent.State): Boolean {
            when (state) {
                PlayerFishEvent.State.CAUGHT_FISH, PlayerFishEvent.State.CAUGHT_ENTITY, PlayerFishEvent.State.REEL_IN -> {
                    if (hookPool.count { it.isValid } == 1) {
                        return true
                    }
                    event.player.syncDelayed(1) {
                        for (hook in hookPool) {
                            if (hook.isValid && hook != event.hook) {
                                resetPlayerHook(event.player, hook)
                                break
                            }
                        }
                    }
                    return false
                }

                PlayerFishEvent.State.BITE -> {
                    resetPlayerHook(event.player, event.hook)
                    return false
                }

                PlayerFishEvent.State.IN_GROUND -> {
                    hookPool.forEach { it.remove() }
                    return true
                }

                else -> return false
            }
        }


        fun resetPlayerHook(player: Player, newHook: FishHook) {
            if (!newHook.isValid) {
                throw IllegalArgumentException("Hook is not valid")
            }
            val nmsFishingHook: FishingHook = (newHook as CraftFishHook).handle
            val nmsPlayer: net.minecraft.world.entity.player.Player = (player as org.bukkit.craftbukkit.entity.CraftHumanEntity).handle

            if (nmsPlayer.fishing != nmsFishingHook) {
                nmsPlayer.fishing = nmsFishingHook
            }
        }


        fun contains(hook: FishHook): Boolean {
            return hookPool.any { it.uniqueId == hook.uniqueId }
        }

        fun isOrphanedBy(player: Player): Boolean {
            val nmsPlayer: net.minecraft.world.entity.player.Player = (player as org.bukkit.craftbukkit.entity.CraftHumanEntity).handle
            val current = nmsPlayer.fishing?.bukkitEntity?.uniqueId ?: return true
            return hookPool.none { it.isValid && it.uniqueId == current }
        }

        fun destroy() {
            hookPool.forEach { if (it.isValid) it.remove() }
            hookPool.clear()
        }

        fun isInvalid(): Boolean {
            return hookPool.all { !it.isValid }
        }
    }
}