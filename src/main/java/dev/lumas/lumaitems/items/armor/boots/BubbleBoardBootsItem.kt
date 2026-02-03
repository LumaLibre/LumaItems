package dev.lumas.lumaitems.items.armor.boots

import dev.lumas.lumaitems.enums.DefaultAttributes
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.model.AttributeContainer
import dev.lumas.lumaitems.util.AbilityUtil
import dev.lumas.lumaitems.util.Executors
import dev.lumas.lumaitems.util.Executors.syncEntity
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack


class BubbleBoardBootsItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        val key = "bubble-board-boots"
        return ItemFactory.builder()
            .name("<b><gradient:#A2BFFE:#c9bef4:#ddbad6:#edc6bb>Bubble Board Boots</gradient></b>")
            .customEnchants("<#c9bef4>Bubble Launch")
            .material(Material.NETHERITE_BOOTS)
            .persistentData(key)
            .tier(Tier.SUMMER_2025)
            .attributeModifiers(
                DefaultAttributes.NETHERITE_BOOTS.appendThenGetAttributes(
                    AttributeContainer.of(key, Attribute.STEP_HEIGHT, AttributeModifier.Operation.ADD_NUMBER, 1.0, EquipmentSlotGroup.FEET),
                    AttributeContainer.of(key, Attribute.JUMP_STRENGTH, AttributeModifier.Operation.ADD_NUMBER, 0.2, EquipmentSlotGroup.FEET)
                )
            )
            .lore(
                "Add a little 'pop' to",
                "your step.",
                "",
                "Climb singular blocks",
                "without jumping and",
                "jump higher while",
                "wearing these boots.",
                "",
                "<#c9bef4>Sneak</#c9bef4> and hold to propel",
                "yourself into the air.",
            )
            .vanillaEnchants(
                Enchantment.PROTECTION to 6,
                Enchantment.FEATHER_FALLING to 7,
                Enchantment.DEPTH_STRIDER to 3,
                Enchantment.MENDING to 1,
                Enchantment.UNBREAKING to 9,
                Enchantment.SOUL_SPEED to 3
            )
            .buildPair()
    }


    override fun onPlayerCrouch(player: Player, event: PlayerToggleSneakEvent) {
        if (!event.isSneaking) {
            return
        }
        var ticksHeld = 0

        Executors.asyncTimer(0, 1) { task ->
            if ((ticksHeld > 1 && !player.isSneaking) || !AbilityUtil.isOnGround(player)) {
                task.cancel()
            }

            ticksHeld += 1

            if (ticksHeld >= 20) {
                player.world.spawnParticle(Particle.BUBBLE, player.location, 20, 0.5, 0.0, 0.5, 0.1)
                player.world.spawnParticle(Particle.DUST, player.location, 15, 0.5, 0.0, 0.5, DustOptions(Util.hex2BukkitColor("#c9bef4"), 1f))
                player.playSound(player.location, Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, 0.5f, 3f)
            }

            if (ticksHeld == 35) {
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f)
            }

            if (ticksHeld >= 35 && !player.isSneaking) {
                player.syncEntity {
                    val vec = player.eyeLocation.direction.clone().multiply(1.5).setY(2.5)
                    player.velocity = vec
                }

                task.cancel()
            }
        }
    }

}