package dev.lumas.lumaitems.items.astral.sets

import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.enums.ToolType
import dev.lumas.lumaitems.items.astral.AstralSet
import dev.lumas.lumaitems.items.astral.AstralSetFactory
import dev.lumas.lumaitems.util.AbilityUtil
import dev.lumas.lumaitems.util.Util
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Arrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector

class KazkanSet : AstralSet {

    companion object {
        private val holdingPlayers: ConcurrentLinkedQueue<ClickHoldingPlayer> = ConcurrentLinkedQueue()
        private val playerLinkedArrows: ConcurrentHashMap<Player, List<Arrow>> = ConcurrentHashMap()
    }

    override fun setItems(): List<ItemStack> {
        val factory = AstralSetFactory("kazkan-set","Kazkan", mutableListOf("&#AC87FBInvigorated"))

        factory.commonEnchants = mutableMapOf(
            Enchantment.UNBREAKING to 7,
        )

        factory.astralSetItem(
            Material.IRON_AXE,
            mutableMapOf(Enchantment.SHARPNESS to 6, Enchantment.LOOTING to 3, Enchantment.BANE_OF_ARTHROPODS to 5),
            mutableListOf("Right-click and hold to begin", "charging up this weapon's", "power.", "", "Attack damage increases", "with time spent charging.")
        )

        factory.astralSetItem(
            Material.CROSSBOW,
            mutableMapOf(Enchantment.QUICK_CHARGE to 2, Enchantment.PIERCING to 4),
            mutableListOf("Arrows launched from this bow", "will be frozen in place.", "", "Left-click to launch them in", "the direction you're looking."),
            includeCommonEnchants = true,
            customName = null,
            attributeModifiers = null,
            customEnchants = listOf("&#AC87FBTime Dilation")
        )

        factory.astralSetItem(
            Material.BLAZE_ROD,
            mutableMapOf(),
            mutableListOf("&7Right-click &fon entities", "slow them down for a short", "period of time.", "", "&7Left-click &fto cast a spell", "that will damage enemies.", "", "&c13 Lapis per spell"),
            includeCommonEnchants = true,
            customName = "&#AC87FB&lKazkan &fStaff",
            attributeModifiers = null,
            customEnchants = null
        )

        factory.astralSetItem(
            Material.SHIELD,
            mutableMapOf(Enchantment.MENDING to 1, Enchantment.FIRE_ASPECT to 4, Enchantment.SHARPNESS to 5),
            mutableListOf(),
            includeCommonEnchants = true,
            attributeModifiers = null,
            customName = null,
            customEnchants = mutableListOf()
        )

        return factory.createdAstralItems
    }

    override fun identifier(): String {
        return "kazkan-set"
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.RIGHT_CLICK -> {
                val genericMCToolType: ToolType = ToolType.getToolType(player.inventory.itemInMainHand) ?: return false

                if (genericMCToolType == ToolType.AXE) {
                    val holdingPlayer = holdingPlayers.find { it.player.uniqueId == player.uniqueId }
                        ?: ClickHoldingPlayer(player).also { holdingPlayers.add(it) }
                    holdingPlayer.updateClickTime()
                } else if (genericMCToolType == ToolType.MAGICAL) {
                    val entity = player.getTargetEntity(15) as? LivingEntity ?: return false
                    if (entity !is Player && AbilityUtil.takeSpellLapisCost(player, 13)) {
                        entity.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 200, 30, false, false, false))
                    }
                }
            }

            Action.LEFT_CLICK -> {
                val genericMCToolType: ToolType = ToolType.getToolType(player.inventory.itemInMainHand) ?: return false
                if (genericMCToolType == ToolType.CROSSBOW) {
                    handleArrowFiring(player)
                } else if (genericMCToolType == ToolType.MAGICAL) {
                    if (AbilityUtil.takeSpellLapisCost(player, 13)) {
                        AbilityUtil.spawnSpell(player, Particle.WITCH, "kazkan-set", 120L)
                    }
                }

            }

            Action.ENTITY_DAMAGE -> {
                val genericMCToolType: ToolType = ToolType.getToolType(player.inventory.itemInMainHand) ?: return false
                event as EntityDamageByEntityEvent
                if (genericMCToolType != ToolType.AXE) return false
                val holdingPlayer = holdingPlayers.find { it.player.uniqueId == player.uniqueId } ?: return false
                if (!holdingPlayer.isHolding()) return false

                event.damage += (holdingPlayer.totalUpdates / 4).coerceAtMost(14)
            }

            Action.ASYNC_RUNNABLE -> {
                for (holdingPlayer in holdingPlayers) {
                    if (!holdingPlayer.isHolding()) {
                        holdingPlayers.remove(holdingPlayer)
                    }
                }
                for (linkedArrowEntry in playerLinkedArrows) {
                    for (arrow in linkedArrowEntry.value) {
                        if (arrow.isDead) {
                            playerLinkedArrows.remove(linkedArrowEntry.key)
                        }
                        if (linkedArrowEntry.value.isEmpty()) {
                            playerLinkedArrows.remove(linkedArrowEntry.key)
                        }
                    }
                }
            }

            Action.PROJECTILE_LAUNCH -> {
                event as ProjectileLaunchEvent

                val projectile = event.entity as? Arrow ?: return false

                projectile.velocity = projectile.velocity.multiply(0.01)
                projectile.setGravity(false)
                projectile.isPersistent = false
                projectile.isCritical = false

                val linkedArrows: MutableList<Arrow> = playerLinkedArrows[player]?.toMutableList() ?: mutableListOf()
                if (linkedArrows.size >= 5) {
                    linkedArrows[0].setGravity(true)
                    linkedArrows.removeAt(0)
                }

                playerLinkedArrows[player] = linkedArrows.plus(projectile)

                Bukkit.getScheduler().runTaskLater(instance(), Runnable {
                    if (!projectile.isOnGround) {
                        projectile.setGravity(true)
                        val arrows = playerLinkedArrows[player]?.toMutableList() ?: return@Runnable
                        arrows.remove(projectile)
                        playerLinkedArrows[player] = arrows
                    }
                }, 150)
            }

            Action.PROJECTILE_LAND -> {
                event as ProjectileHitEvent
                val entity = event.hitEntity as? LivingEntity ?: return false
                if (AbilityUtil.noDamagePermission(player, entity)) return false

                entity.world.playSound(entity.location, Sound.ENTITY_EVOKER_CAST_SPELL, 1f, 1f)
                entity.world.playSound(entity.location, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1f, 1f)
                entity.world.spawnParticle(Particle.SOUL_FIRE_FLAME, entity.eyeLocation, 20, 0.5, 0.5, 0.5, 0.5)
                entity.world.spawnParticle(Particle.DUST, entity.eyeLocation, 10, 0.4, 0.4, 0.4, 0.8,
                    Particle.DustOptions(Util.hex2BukkitColor("#AC87FB"), 2f)
                )

                entity.damage(17.0, player)
            }

            else -> return false
        }
        return true
    }

    private fun adjustArrowDirection(arrow: Projectile, player: Player) {
        // adjust arrows direction based on where player is looking
        // loc.setDirection(player.location.toVector().subtract(armorStand.location.toVector()).normalize());
        val targetEntity = player.getTargetEntity(75)

        val direction: Vector = if (targetEntity != null) {
            val targetLocation = targetEntity.boundingBox.center.toLocation(targetEntity.world)
            targetLocation.toVector().subtract(arrow.location.toVector()).normalize()
        } else {
            player.location.direction
        }
        arrow.location.yaw = player.location.yaw
        arrow.location.pitch = player.location.pitch
        arrow.velocity = direction.multiply(3.2)
        arrow.setGravity(true)
    }

    private fun handleArrowFiring(player: Player) {
        val linkedArrows = playerLinkedArrows[player] ?: return
        val arrow: Arrow = if (linkedArrows.isNotEmpty()) linkedArrows.first() else {
            playerLinkedArrows.remove(player)
            return
        }
        adjustArrowDirection(arrow, player)
        arrow.isCritical = true
        playerLinkedArrows[player] = linkedArrows.drop(1)
    }


    private class ClickHoldingPlayer(val player: Player) {
        val initialClickTime: Long = System.currentTimeMillis()

        var lastClickTime: Long = initialClickTime
        var totalClickTime: Long = lastClickTime - initialClickTime
        var totalUpdates = 0

        fun updateClickTime() {
            lastClickTime = System.currentTimeMillis()
            totalClickTime = lastClickTime - initialClickTime
            totalUpdates++
        }

        fun isHolding(): Boolean {
            return System.currentTimeMillis() - lastClickTime < 300
        }
    }
}