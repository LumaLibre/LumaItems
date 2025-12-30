package dev.lumas.lumaitems.items.weapons.cutlass

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.util.Executors
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Display
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack

class ComicStyleWeapon : CustomItemFunctions() {

    companion object {
        private val texts = listOf(
            "Good!",
            "Brilliant!",
            "Bad...",
            "Excellent!",
            "Wow!",
            "Hmmm..."
        )
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("Comic Style Weapon")
            .material(Material.DIAMOND_SWORD)
            .persistentData("comic-style-weapon")
            .buildPair()
    }

    override fun onEntityDamage(player: Player, event: EntityDamageByEntityEvent) {
        val entity = event.entity as? LivingEntity ?: return
        val world = entity.world
        val reach = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE)?.value ?: 5.0
        val spawnLocation = (world.rayTraceEntities(player.eyeLocation, player.eyeLocation.direction, reach, {
            it != player && it is LivingEntity
        })?.hitPosition?.toLocation(world) ?: entity.eyeLocation).add(
            random().nextDouble(0.1, 0.3),
            random().nextDouble(0.1, 0.3),
            random().nextDouble(0.1, 0.3)
        )


        val backgroundColor = Color.fromARGB(24,0,0,0)

        val textDisplay = player.world.spawn(spawnLocation, TextDisplay::class.java) {
            it.backgroundColor = backgroundColor
            it.isSeeThrough = false
            it.isShadowed = true
            it.billboard = Display.Billboard.CENTER
            it.text(Component.text(texts.random()).color(NamedTextColor.NAMES.values().random()))
            it.isPersistent = false
        }
        world.spawnParticle(Particle.WAX_OFF, spawnLocation, 3, 0.2, 0.1, 0.2, 0.1)
        Executors.syncDelayed(6) {
            textDisplay.remove()
        }
    }
}