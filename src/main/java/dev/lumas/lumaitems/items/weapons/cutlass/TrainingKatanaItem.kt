package dev.lumas.lumaitems.items.weapons.cutlass

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.AttributeContainer
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.particles.ParticleDisplay
import dev.lumas.lumaitems.util.extensions.Executors
import dev.lumas.lumaitems.util.extensions.syncTimer
import dev.lumas.lumaitems.util.tiers.Tier
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class TrainingKatanaItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        val k = "training-katana"
        val item = ItemFactory.builder()
            .name("<yellow><b>Training Katana")
            .customEnchants("<green>Level Field")
            .material(Material.NETHERITE_SWORD)
            .persistentData(k)
            .vanillaEnchants()
            .attributeModifiers(
                AttributeContainer.ofMap(
                    Attribute.ATTACK_SPEED, k, 4.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ANY
                )
            )
            .tier(Tier.WINTER_2024)
            .build()
            .addDataComponents(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft("wooden_sword"))
            .createItem()

        return Pair(k, item)
    }


    override fun onLeftClick(player: Player, event: PlayerInteractEvent) {
        val particleDisplay = ParticleDisplay.of(Particle.SWEEP_ATTACK)
        val snowball = player.launchProjectile(Snowball::class.java)
        snowball.item = ItemStack(Material.AIR)
        snowball.setGravity(false)
        var count = 0
        Executors.asyncTimer(0, 1) {
            if (snowball.isDead || count++ > 5) {
                it.cancel()
                return@asyncTimer
            }


            particleDisplay.spawn(snowball.location)
        }
    }

    //val particleDisplay = ParticleDisplay.of(Particle.SWEEP_ATTACK)
    //
    //        // add 4 block in the direction the player is facing and get the loction
    //        val startLoc = player.location.clone().add(
    //            player.location.direction.multiply(1)
    //        )
    //        val targetLoc = player.location.clone().add(
    //            player.location.direction.multiply(5)
    //        )
    //
    //
    //        Particles.line(startLoc, targetLoc, 0.7, particleDisplay)


    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        val clickedEntity = player.getTargetEntity(10) as? LivingEntity ?: return

        val darkness = PotionEffect(PotionEffectType.DARKNESS, 150, 1)

        clickedEntity.addPotionEffect(darkness)
        player.addPotionEffect(darkness)

        var t = 0
        player.syncTimer(0, 25) {
            clickedEntity.damage(3.0, player)
            if (t++ > 10) {
                it.cancel()
            }
        }
    }
}