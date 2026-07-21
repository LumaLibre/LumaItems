package dev.lumas.lumaitems.items.tools.rod

import ca.spottedleaf.moonrise.common.util.TickThread
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.isHoldingTwoRods
import dev.lumas.lumaitems.util.extensions.syncDelayed
import dev.lumas.lumaitems.util.extensions.withMeta
import io.papermc.paper.event.entity.FishHookStateChangeEvent
import java.lang.reflect.Field
import kotlin.math.abs
import kotlin.math.sign
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.FluidTags
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.MoverType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import org.bukkit.Material
import org.bukkit.craftbukkit.entity.CraftFishHook
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.FishHook
import org.bukkit.entity.Item
import org.bukkit.event.entity.EntityRemoveEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionType

class FiberglassRodItem : CustomItemFunctions() {

    private companion object {
        const val CATCH_IMMUNITY_TICKS = 100L

        // https://minecraft.wiki/w/Fishing
        val LAVA_SWAPS = mapOf(
            Material.COD to Material.COOKED_COD,
            Material.SALMON to Material.COOKED_SALMON,
            Material.LILY_PAD to Material.CRIMSON_FUNGUS,
            Material.INK_SAC to Material.GLOW_INK_SAC,
            Material.POTION to Material.GLASS_BOTTLE,
        )

        val LAVA_ADDITIONS = listOf(
            ItemStack.of(Material.BEETROOT_SOUP),
            ItemStack.of(Material.GOLDEN_DANDELION, 2),
            ItemStack.of(Material.ENCHANTED_GOLDEN_APPLE),
            ItemStack.of(Material.GOLDEN_CARROT, 2),
            ItemStack.of(Material.GOLDEN_APPLE),
            ItemStack.of(Material.OMINOUS_BOTTLE),
            ItemStack.of(Material.GLOWSTONE_DUST, 4),
            ItemStack.of(Material.BLAZE_ROD),
            ItemStack.of(Material.FIRE_CHARGE),
            ItemStack.of(Material.DRAGON_BREATH, 3),
            ItemStack.of(Material.POTION).withMeta { meta ->
                meta as PotionMeta
                meta.basePotionType = PotionType.FIRE_RESISTANCE
            }
        )

        val CATCH_STATES = setOf(
            PlayerFishEvent.State.BITE,
            PlayerFishEvent.State.LURED,
            PlayerFishEvent.State.CAUGHT_FISH,
            PlayerFishEvent.State.CAUGHT_ENTITY,
        )
    }

    override fun createItem() = ItemFactory.builder()
        .name("<b><gradient:#f05035:#ff8b38:#ec7464:#CB364C:#2a2e30>Fiberglass Rod</gradient></b>")
        .customEnchants("<#CB364C>Tantalum")
        .material(Material.FISHING_ROD)
        .persistentData("fiberglass-rod")
        .tier(Tier.LUMARINE_2026)
        .vanillaEnchants(
            Enchantment.LURE to 5,
            Enchantment.LUCK_OF_THE_SEA to 4,
            Enchantment.UNBREAKING to 3,
            Enchantment.MENDING to 1
        )
        .lore(
            "A fishing rod made",
            "out of heat-resistant",
            "fiberglass.",
            "",
            "<#CB364C>Cast</#CB364C> the line of this",
            "rod in to any body of",
            "lava to catch fish."
        )
        .buildPair()

    override fun onFish(player: org.bukkit.entity.Player, event: PlayerFishEvent) {
        if (player.isHoldingTwoRods()) return

        val handle = (event.hook as? CraftFishHook)?.handle
        if (handle is LavaFishingHook && event.state in CATCH_STATES && handle.isLocatedInWater()) {
            event.isCancelled = true
            return
        }

        when (event.state) {
            PlayerFishEvent.State.FISHING -> {
                event.isCancelled = true

                val nmsPlayer = (player as CraftPlayer).handle
                val serverLevel = nmsPlayer.level()
                val hand = if (event.hand == EquipmentSlot.OFF_HAND) InteractionHand.OFF_HAND else InteractionHand.MAIN_HAND
                val rodStack = nmsPlayer.getItemInHand(hand)
                val lureSpeed = (EnchantmentHelper.getFishingTimeReduction(serverLevel, rodStack, nmsPlayer) * 20.0f).toInt()
                val luck = EnchantmentHelper.getFishingLuckBonus(serverLevel, rodStack, nmsPlayer)

                player.syncDelayed(1) {
                    if (!player.isOnline || nmsPlayer.fishing != null) return@syncDelayed
                    val hook = LavaFishingHook(nmsPlayer, serverLevel, luck, lureSpeed)
                    Projectile.spawnProjectile(hook, serverLevel, rodStack)
                    serverLevel.playSound(
                        null, nmsPlayer.x, nmsPlayer.y, nmsPlayer.z,
                        SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL,
                        0.5f, 0.4f / (serverLevel.random.nextFloat() * 0.4f + 0.8f)
                    )
                }
            }

            PlayerFishEvent.State.CAUGHT_FISH -> {
                if ((event.hook as CraftFishHook).handle !is LavaFishingHook) return
                if (event.hook.location.block.type != Material.LAVA) return
                val caught = event.caught as? Item ?: return

                val stack = caught.itemStack
                if (random.nextFloat() < 0.1f) {
                    LAVA_ADDITIONS.randomOrNull()?.let { caught.itemStack = if (stack.amount == 1) it else it.asQuantity(stack.amount) }
                } else {
                    LAVA_SWAPS[stack.type]?.let { caught.itemStack = ItemStack.of(it, stack.amount) }
                }

                //ecaught.fireTicks = 0
                caught.isInvulnerable = true
                caught.syncDelayed(CATCH_IMMUNITY_TICKS) {
                    if (caught.isValid) {
                        caught.fireTicks = 0
                        caught.isInvulnerable = false
                    }
                }
            }

            else -> {}
        }
    }


    @Suppress("UnstableApiUsage")
    class LavaFishingHook(
        owner: Player,
        level: Level,
        luck: Int,
        lureSpeed: Int,
    ) : FishingHook(owner, level, luck, lureSpeed) {

        private companion object {
            val NIBBLE: Field = FishingHook::class.java.getDeclaredField("nibble")
                .apply { isAccessible = true }

            @Suppress("UNCHECKED_CAST")
            val DATA_BITING = FishingHook::class.java.getDeclaredField("DATA_BITING")
                .apply { isAccessible = true }
                .get(null) as EntityDataAccessor<Boolean>

            // how far under the surface counts as "sunk", and how hard to climb back out
            const val SUBMERGED_DEPTH = 0.5
            const val RESCUE_SPEED = 0.06
            // guards against walking the whole column on a deep lava sea
            const val MAX_SURFACE_SCAN = 8
        }

        // mirrored to parent
        private var nibbleTicks = 0
            set(value) {
                field = value
                NIBBLE.setInt(this, value)
            }

        override fun fireImmune() = true
        private fun bukkitOwner() = playerOwner!!.bukkitEntity as org.bukkit.entity.Player


        fun isLocatedInWater(): Boolean =
            level().getFluidState(blockPosition()).`is`(FluidTags.WATER)

        private fun fizzleInWater() {
            val level = level()
            if (level is ServerLevel) {
                level.sendParticles(ParticleTypes.SMOKE, x, y + 0.1, z, 8, 0.1, 0.0, 0.1, 0.01)
                playSound(SoundEvents.FIRE_EXTINGUISH, 0.4f, 1.6f)
            }
//            playerOwner?.let {
//                (it.bukkitEntity as org.bukkit.entity.Player).sendActionBar(
//                    Component.text("This rod only bites in lava.", NamedTextColor.RED)
//                )
//            }
            discard(EntityRemoveEvent.Cause.DESPAWN)
        }


        private fun resolveLavaSurface(): Double? {
            var pos = blockPosition()
            if (!level().getFluidState(pos).`is`(FluidTags.LAVA)) {
                // a floating bobber's block pos flickers between the fluid and the air above it
                pos = pos.below()
                if (!level().getFluidState(pos).`is`(FluidTags.LAVA)) return null
            }

            var scanned = 0
            while (scanned < MAX_SURFACE_SCAN && level().getFluidState(pos.above()).`is`(FluidTags.LAVA)) {
                pos = pos.above()
                scanned++
            }

            return pos.y + level().getFluidState(pos).getHeight(level(), pos).toDouble()
        }

        override fun tick() {
            // Before anything else, and before super.tick() can start a water catch cycle.
            if (!level().isClientSide && isLocatedInWater()) {
                fizzleInWater()
                return
            }

            val surfaceY = resolveLavaSurface()

            val bobbingInLava = surfaceY != null && currentState == FishHookState.BOBBING
            if (isNoGravity != bobbingInLava) {
                isNoGravity = bobbingInLava
            }

            if (hookedIn != null || surfaceY == null || (currentState != FishHookState.BOBBING && y > surfaceY)) {
                super.tick()
                return
            }

            interpolation.interpolate()
            baseTick()

            val owner = playerOwner
            if (owner == null) {
                discard(EntityRemoveEvent.Cause.DESPAWN)
                return
            }
            if (level().isClientSide || shouldStopLavaFishing(owner)) return

            val blockPos = blockPosition()

            when (currentState) {
                FishHookState.FLYING -> {
                    deltaMovement = deltaMovement.multiply(0.3, 0.2, 0.3)
                    FishHookStateChangeEvent(bukkitEntity as FishHook, FishHook.HookState.BOBBING).callEvent()
                    currentState = FishHookState.BOBBING
                    return
                }

                FishHookState.BOBBING -> {
                    // vanilla buoyancy math, measured against the resolved lava surface
                    val movement = deltaMovement
                    var force = y + movement.y - surfaceY
                    if (abs(force) < 0.01) {
                        force += sign(force) * 0.1
                    }

                    var newY = movement.y - force * random.nextFloat() * 0.2
                    // nothing may push the hook further down once it's under the surface
                    if (y < surfaceY) newY = newY.coerceAtLeast(0.0)
                    // and if it got dunked anyway, climb out deliberately
                    if (y < surfaceY - SUBMERGED_DEPTH) newY = newY.coerceAtLeast(RESCUE_SPEED)

                    setDeltaMovement(movement.x * 0.9, newY, movement.z * 0.9)
                    outOfWaterTime = 0
                    if (nibbleTicks > 0) {
                        deltaMovement = deltaMovement.add(0.0, -0.1 * random.nextFloat() * random.nextFloat(), 0.0)
                    }
                    catchingLavaFish(blockPos)
                }

                else -> {
                    super.tick()
                    return
                }
            }

            move(MoverType.SELF, deltaMovement)
            applyEffectsFromBlocks()
            updateRotation()
            deltaMovement = deltaMovement.scale(0.92)
            reapplyPosition()
        }

        // copy of #shouldStopFishing()
        private fun shouldStopLavaFishing(owner: Player): Boolean {
            if (!TickThread.isTickThreadFor(owner)) {
                return true
            }
            if (owner.canInteractWithLevel()) {
                val holdingRod = owner.mainHandItem.`is`(Items.FISHING_ROD) || owner.offhandItem.`is`(Items.FISHING_ROD)
                if (holdingRod && distanceToSqr(owner) <= 1024.0) {
                    return false
                }
            }
            discard(EntityRemoveEvent.Cause.DESPAWN)
            return true
        }

        // copy of #catchingFish()
        private fun catchingLavaFish(blockPos: BlockPos) {
            val serverLevel = level() as ServerLevel
            var fishingSpeed = 1
            val above = blockPos.above()
            if (rainInfluenced && random.nextFloat() < 0.25f && level().isRainingAt(above)) {
                fishingSpeed++
            }
            if (skyInfluenced && random.nextFloat() < 0.5f && !level().canSeeSky(above)) {
                fishingSpeed--
            }

            if (nibbleTicks > 0) {
                nibbleTicks--
                if (nibbleTicks <= 0) {
                    timeUntilLured = 0
                    timeUntilHooked = 0
                    entityData.set(DATA_BITING, false)
                    PlayerFishEvent(bukkitOwner(), null, bukkitEntity as FishHook, PlayerFishEvent.State.FAILED_ATTEMPT).callEvent()
                }
            } else if (timeUntilHooked > 0) {
                timeUntilHooked -= fishingSpeed
                if (timeUntilHooked > 0) {
                    fishAngle += random.triangle(0.0, 9.188).toFloat()
                    val angle = (fishAngle * Mth.DEG_TO_RAD).toDouble()
                    val angleSin = Mth.sin(angle)
                    val angleCos = Mth.cos(angle)
                    val fishX = x + angleSin * timeUntilHooked * 0.1f
                    val fishY = (Mth.floor(y) + 1).toDouble()
                    val fishZ = z + angleCos * timeUntilHooked * 0.1f
                    val splashState = serverLevel.getBlockState(BlockPos.containing(fishX, fishY - 1.0, fishZ))
                    if (splashState.`is`(Blocks.LAVA)) {
                        if (random.nextFloat() < 0.15f) {
                            serverLevel.sendParticles(
                                ParticleTypes.LAVA, fishX, fishY - 0.1, fishZ, 1, angleSin.toDouble(), 0.1, angleCos.toDouble(), 0.0
                            )
                        }
                        val particleX = angleSin * 0.04f
                        val particleZ = angleCos * 0.04f
                        serverLevel.sendParticles(
                            ParticleTypes.SMALL_FLAME, fishX, fishY, fishZ, 0, particleZ.toDouble(), 0.01, (-particleX).toDouble(), 1.0
                        )
                        serverLevel.sendParticles(
                            ParticleTypes.SMALL_FLAME, fishX, fishY, fishZ, 0, (-particleZ).toDouble(), 0.01, particleX.toDouble(), 1.0
                        )
                    }
                } else {
                    if (!PlayerFishEvent(bukkitOwner(), null, bukkitEntity as FishHook, PlayerFishEvent.State.BITE).callEvent()) {
                        return
                    }
                    // TODO: change sound
                    playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.25f, 1.0f + (random.nextFloat() - random.nextFloat()) * 0.4f)
                    val surfaceY = y + 0.5
                    val count = (1.0f + bbWidth * 20.0f).toInt()
                    serverLevel.sendParticles(ParticleTypes.LAVA, x, surfaceY, z, count, bbWidth.toDouble(), 0.0, bbWidth.toDouble(), 0.2)
                    serverLevel.sendParticles(ParticleTypes.SMALL_FLAME, x, surfaceY, z, count, bbWidth.toDouble(), 0.0, bbWidth.toDouble(), 0.2)
                    nibbleTicks = Mth.nextInt(random, 20, 40)
                    entityData.set(DATA_BITING, true)
                }
            } else if (timeUntilLured > 0) {
                timeUntilLured -= fishingSpeed
                var teaseChance = 0.15f
                if (timeUntilLured < 20) {
                    teaseChance += (20 - timeUntilLured) * 0.05f
                } else if (timeUntilLured < 40) {
                    teaseChance += (40 - timeUntilLured) * 0.02f
                } else if (timeUntilLured < 60) {
                    teaseChance += (60 - timeUntilLured) * 0.01f
                }

                if (random.nextFloat() < teaseChance) {
                    val angle = (Mth.nextFloat(random, 0.0f, 360.0f) * Mth.DEG_TO_RAD).toDouble()
                    val dist = Mth.nextFloat(random, 25.0f, 60.0f)
                    val fishX = x + Mth.sin(angle) * dist * 0.1
                    val fishY = (Mth.floor(y) + 1).toDouble()
                    val fishZ = z + Mth.cos(angle) * dist * 0.1
                    val splashState = serverLevel.getBlockState(BlockPos.containing(fishX, fishY - 1.0, fishZ))
                    if (splashState.`is`(Blocks.LAVA)) {
                        serverLevel.sendParticles(ParticleTypes.LAVA, fishX, fishY, fishZ, 2 + random.nextInt(2), 0.1, 0.0, 0.1, 0.0)
                    }
                }

                if (timeUntilLured <= 0) {
                    fishAngle = Mth.nextFloat(random, minLureAngle, maxLureAngle)
                    timeUntilHooked = Mth.nextInt(random, minLureTime, maxLureTime)
                    val lured = PlayerFishEvent(bukkitOwner(), null, bukkitEntity as FishHook, PlayerFishEvent.State.LURED)
                    if (!lured.callEvent()) {
                        timeUntilHooked = 0
                    }
                }
            } else {
                resetTimeUntilLured()
            }
        }
    }
}