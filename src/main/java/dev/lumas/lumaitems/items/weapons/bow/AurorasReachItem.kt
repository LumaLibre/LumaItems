package dev.lumas.lumaitems.items.weapons.bow

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.syncTimer
import dev.lumas.lumaitems.util.extensions.toBukkitColor
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class AurorasReachItem : CustomItemFunctions() {

    private companion object {
        private val KEY = "auroras-reach".namespacedKey()
        private val RAINBOW_COLORS = listOf(
            Color.RED,
            "#FF7F00".toBukkitColor(), // Orange
            Color.YELLOW,
            Color.GREEN,
            Color.BLUE,
            Color.fromRGB(0x8B00FF)  // Violet
        )
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.Companion.builder()
            .name("<b><gradient:#e0f7fa:#a8e6cf:#dcedc8:#ffd54f:#ffab40:#ba68c8>Aurora's Reach</gradient></b>")
            .material(Material.BOW)
            .persistentData(KEY)
            .tier(Tier.PRIDE_2026)
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .lore(
                "A bow imbued with the",
                "colours of the rainbow.",
                "",
                "Shoots <gradient:#e0f7fa:#a8e6cf:#ffd54f:#ff8a65:#ba68c8>fireworks</gradient>",
                "instead of arrows."
            )
            .buildPair()
    }

    override fun onPlayerShootBow(player: Player, event: EntityShootBowEvent) {
        val item = event.bow ?: return
        if (!item.isMatchingItem(KEY)) return

        event.isCancelled = true

        val firework = player.world.spawn(player.eyeLocation, Firework::class.java)
        val meta = firework.fireworkMeta
        meta.addEffect(
            FireworkEffect.builder()
                .with(FireworkEffect.Type.BALL_LARGE)
                .withColor(RAINBOW_COLORS.random(), RAINBOW_COLORS.random())
                .withFade(RAINBOW_COLORS.random())
                .trail(true)
                .flicker(true)
                .build()
        )
        meta.power = 0
        firework.fireworkMeta = meta

        val direction: Vector = player.location.direction.normalize()
        val speed = 1.5
        var ticksLived = 0

        firework.syncTimer(0L, 1L) { task ->
            if (firework.isDead || !firework.isValid) {
                task.cancel()
                return@syncTimer
            }

            val nextLocation = firework.location.add(direction.clone().multiply(speed))
            val nextBlock = nextLocation.world?.getBlockAt(nextLocation)
            if (nextBlock?.type?.isSolid == true) {
                firework.detonate()
                task.cancel()
                return@syncTimer
            }

            firework.velocity = direction.clone().multiply(speed)

            if (++ticksLived >= 100) {
                firework.detonate()
                task.cancel()
            }
        }
    }
}