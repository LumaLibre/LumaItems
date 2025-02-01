package dev.jsinco.luma.lumaitems.items.weapons

import dev.jsinco.luma.lumaitems.enums.DefaultAttributes
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.NeedsEdits
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

@NeedsEdits
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
                DefaultAttributes.NETHERITE_SWORD.appendThenGetAttributes(
                    Attribute.ATTACK_SPEED, k, 4.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ANY
                )
            )
            .tier(Tier.WINTER_2024)
            .build()
            .addDataComponents(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft("wooden_sword"))
            .createItem()

        return Pair(k, item)
    }


    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        val clickedEntity = player.getTargetEntity(10) as? LivingEntity ?: return

        val darkness = PotionEffect(PotionEffectType.DARKNESS, 150, 1)

        clickedEntity.addPotionEffect(darkness)
        player.addPotionEffect(darkness)

        object : BukkitRunnable() {
            var t = 0
            override fun run() {
                clickedEntity.damage(3.0, player)
                if (t++ > 10) {
                    this.cancel()
                }
            }
        }.runTaskTimer(instance(), 0, 25)
    }
}