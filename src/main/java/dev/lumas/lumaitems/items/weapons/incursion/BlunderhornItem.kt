package dev.lumas.lumaitems.items.weapons.incursion

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.PaperDataComponent
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.TooltipDisplay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class BlunderhornItem : CustomItemFunctions() {

    companion object {
        private const val KEY = "incursion-blunderhorn"

        private const val COOLDOWN_TICKS = 30
        private const val RANGE = 6.5
        private const val CONE_DEGREES = 45.0
        private const val DAMAGE = 90.0
        private const val DAMAGE_FALLOFF = 0.85

        private const val PELLETS = 16
        private val PELLET_DUST = Particle.DustOptions(Color.fromRGB(88, 92, 99), 0.6f)
    }

    override fun createItem(): Pair<String, ItemStack> {
        val (key, item) = ItemFactory.builder()
            .name("<b><gold>Blunderhorn</gold></b>")
            .lore(
                "<gray>Right click for a blast",
                "<gray>Hurts less at range"
            )
            .addSpace(false)
            .material(Material.GOAT_HORN)
            .persistentData(KEY)
            .unbreakable(true)
            .tier(Tier.BLANK)
            .paperDataComponents(
                PaperDataComponent.valued(
                    DataComponentTypes.TOOLTIP_DISPLAY,
                    TooltipDisplay.tooltipDisplay()
                        .addHiddenComponents(
                            DataComponentTypes.UNBREAKABLE,
                            DataComponentTypes.ATTRIBUTE_MODIFIERS
                        )
                        .build()
                )
            )
            .buildPair()

        item.unsetData(DataComponentTypes.INSTRUMENT)
        return key to item
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        if (event.item?.isMatchingItem(KEY) != true) return

        event.setUseItemInHand(Event.Result.DENY)
        event.isCancelled = true

        if (player.hasCooldown(Material.GOAT_HORN)) {
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_HAT, 0.7f, 0.5f)
            return
        }
        fire(player)
    }

    private fun fire(player: Player) {
        player.setCooldown(Material.GOAT_HORN, COOLDOWN_TICKS)

        val muzzle = player.eyeLocation
        val direction = muzzle.direction.normalize()

        muzzle.world.playSound(muzzle, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.8f)
        muzzle.world.playSound(muzzle, Sound.ITEM_FIRECHARGE_USE, 0.9f, 0.7f)
        sprayCone(muzzle, direction)

        val halfAngle = Math.toRadians(CONE_DEGREES / 2.0)
        val tanHalfAngle = tan(halfAngle)
        val cosHalfAngle = cos(halfAngle)
        val apex = muzzle.toVector()

        var hits = 0
        for (target in IncursionArsenal.targetsAround(player, muzzle, RANGE + 2.0)) {
            val hitbox = target.hitbox
            val distance = IncursionArsenal.coneHitDistance(apex, direction, RANGE, tanHalfAngle, cosHalfAngle, hitbox)
            if (distance < 0) continue

            // Occlusion has to be checked towards the target, not down the middle of the cone
            val toTarget = hitbox.center.subtract(apex)
            val length = toTarget.length()
            if (length > 1.0E-4 && !IncursionArsenal.hasClearShot(muzzle, toTarget.multiply(1.0 / length), length)) {
                continue
            }

            IncursionArsenal.hurt(target.entity, player, DAMAGE * (1.0 - (DAMAGE_FALLOFF * (distance / RANGE))))
            hits++
        }

        if (hits > 0) IncursionArsenal.hitFeedback(player)
    }

    private fun sprayCone(muzzle: Location, direction: Vector) {
        val world = muzzle.world
        IncursionArsenal.forced(world, Particle.SMOKE, muzzle, 16, 0.1, 0.6)

        val reference = if (Math.abs(direction.y) > 0.99) Vector(1, 0, 0) else Vector(0, 1, 0)
        val right = direction.clone().crossProduct(reference).normalize()
        val up = right.clone().crossProduct(direction).normalize()
        val spread = tan(Math.toRadians(CONE_DEGREES / 2.0))

        repeat(PELLETS) {
            val angle = random.nextDouble() * Math.PI * 2
            val offset = spread * sqrt(random.nextDouble())
            val pelletDirection = direction.clone()
                .add(right.clone().multiply(cos(angle) * offset))
                .add(up.clone().multiply(sin(angle) * offset))
                .normalize()

            var distance = 0.6
            while (distance <= RANGE) {
                val point = muzzle.clone().add(pelletDirection.clone().multiply(distance))
                if (Bukkit.isOwnedByCurrentRegion(point)) {
                    IncursionArsenal.forced(world, Particle.DUST, point, 1, 0.0, 0.0, PELLET_DUST)
                }
                distance += 0.7
            }
        }
    }
}
