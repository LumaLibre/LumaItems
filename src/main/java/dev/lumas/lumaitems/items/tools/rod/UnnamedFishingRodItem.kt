package dev.lumas.lumaitems.items.tools.rod

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.util.BukkitVectors
import dev.lumas.lumaitems.util.Executors
import dev.lumas.lumaitems.util.disabling.Ignore
import org.bukkit.Bukkit
import org.bukkit.entity.FishHook
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerInputEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

@Ignore // fishing api sucks. TODO: remove
class UnnamedFishingRodItem : CustomItemFunctions() {

    companion object {
        val activeMultiHooks = mutableListOf<MultiHook>()
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<gray>Unnamed Fishing Rod")
            .material(org.bukkit.Material.FISHING_ROD)
            .persistentData("unnamed_fishing_rod")
            .buildPair()
    }


    override fun onFish(player: Player, event: PlayerFishEvent) {
        val multiHook = activeMultiHooks.find { it.originalHook == event.hook }
        if (multiHook == null && event.state == PlayerFishEvent.State.FISHING) {
            MultiHook(event.hook, player).keepAlive()
        } else {
            multiHook?.handleState(event, event.state)
        }
    }

    override fun onInput(player: Player, event: PlayerInputEvent) {
        if (event.input.isRight) {
            activeMultiHooks.find { it.player == player }?.let {
                it.leftHook.retrieve(EquipmentSlot.HAND)
                it.rightHook.retrieve(EquipmentSlot.HAND)
                activeMultiHooks.remove(it)
                Bukkit.broadcastMessage("MultiHook manually ended")
            }
        }
    }



    class MultiHook(
        val originalHook: FishHook,
        val player: Player,
    ) {

        val leftHook: FishHook
        val rightHook: FishHook


        init {
            val originalDir = originalHook.velocity.clone().normalize()
            val speed = originalHook.velocity.length()
            val leftDir = BukkitVectors.rotateVectorY(originalDir, Math.toRadians(10.0))
            val rightDir = BukkitVectors.rotateVectorY(originalDir, Math.toRadians(-10.0))

            this.leftHook = originalHook.clone(leftDir, speed)
            this.rightHook = originalHook.clone(rightDir, speed)
            activeMultiHooks.add(this)
        }


        fun handleState(event: PlayerFishEvent, state: PlayerFishEvent.State) {
            Bukkit.broadcastMessage("MultiHook State: $state")

//            when (state) {
//
//                PlayerFishEvent.State.LURED -> {
//                    //leftHook.set
//                }
//
//                PlayerFishEvent.State.CAUGHT_FISH -> {
//                    Bukkit.broadcastMessage("unknown handle: $state")
////                    val hookedEntity = event.caught
////                    Bukkit.broadcastMessage("Caught fish with multi-hook ${hookedEntity}")
////                    leftHook.hookedEntity = hookedEntity
////                    rightHook.hookedEntity = hookedEntity
//                }
//
//                PlayerFishEvent.State.BITE, PlayerFishEvent.State.FAILED_ATTEMPT -> {
//                    Bukkit.broadcastMessage("Bite in multi-hook: ${event.caught}")
//                    leftHook.hookedEntity = event.caught
//                    rightHook.hookedEntity = event.caught
//
//                    //activeMultiHooks.remove(this)
//                }
//
//                PlayerFishEvent.State.REEL_IN -> {
//                    if (leftHook.hookedEntity != null) {
//                        leftHook.pullHookedEntity()
//                        Bukkit.broadcastMessage("Pulling left hook")
//                    } else {
//                        leftHook.retrieve(EquipmentSlot.HAND)
//                        Bukkit.broadcastMessage("Retrieving left hook")
//                    }
//
//                    if (rightHook.hookedEntity != null) {
//                        rightHook.pullHookedEntity()
//                        Bukkit.broadcastMessage("Pulling right hook")
//                    } else {
//                        rightHook.retrieve(EquipmentSlot.HAND)
//                        Bukkit.broadcastMessage("Retrieving right hook")
//                    }
//                }
//
//
//                PlayerFishEvent.State.IN_GROUND,  PlayerFishEvent.State.FISHING, PlayerFishEvent.State.CAUGHT_ENTITY  -> {
//                    Bukkit.broadcastMessage("unknown handle: $state")
//                }
//
//            }
        }

        fun keepAlive() {
            Executors.asyncTimer(0, 5) { task ->
                if (!leftHook.isValid && !rightHook.isValid) {
                    task.cancel()
                    Bukkit.broadcastMessage("MultiHook ended")
                    activeMultiHooks.remove(this)
                    return@asyncTimer
                }
            }
        }


        fun FishHook.clone(dir: Vector, speed: Double): FishHook {
            val player = this.shooter as Player
            val hook = player.launchProjectile(FishHook::class.java)
            //hook.shooter = player
            hook.velocity = dir.multiply(speed)
            hook.minWaitTime = this.minWaitTime
            hook.maxWaitTime = this.maxWaitTime
            hook.minLureTime = this.minLureTime
            hook.maxLureTime = this.maxLureTime
            hook.applyLure = this.applyLure
            hook.isRainInfluenced = this.isRainInfluenced
            hook.isSkyInfluenced = this.isSkyInfluenced
            hook.setLureAngle(this.minLureAngle, this.maxLureAngle)
            return hook
        }
    }
}