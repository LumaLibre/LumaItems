package dev.lumas.lumaitems.items.armor.boots

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.extensions.addCooldown
import dev.lumas.lumaitems.util.extensions.isOnCooldown
import dev.lumas.lumaitems.util.Tier
import org.bukkit.EntityEffect
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

class StrawberryChapeauItem : CustomItemFunctions() {

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#fc5b8d:#ff8283:#ffaead:#cb354e>Strawberry Chapeau</gradient></b>")
            .customEnchants("<#fc5b8d>Satiate", "<#EC6E95>Totem")
            .material(Material.NETHERITE_HELMET)
            .persistentData("strawberry-chapeau")
            .tier(Tier.VALENTIDE_2026)
            .tagline("#fc5b8d", "Such a fancy name!")
            .lore(
                "While <#fc5b8d>worn</#fc5b8d>, this hat",
                "will saturate based",
                "on the health of any",
                "entity you kill.",
                "",
                "Upon <#EC6E95>death</#EC6E95>, this hat",
                "will prevent you from",
                "dying.",
                "",
                "<red>Cooldown: 1hr</red>"
            )
            .vanillaEnchants(
                Enchantment.PROTECTION to 8,
                Enchantment.BLAST_PROTECTION to 6,
                Enchantment.THORNS to 4,
                Enchantment.UNBREAKING to 6,
                Enchantment.MENDING to 1
            )
            .buildPair()
    }


    override fun onPlayerDeath(player: Player, event: PlayerDeathEvent) {
        if (event.isCancelled || player.isOnCooldown(this)) {
            return // Already has a totem equipped or is on cooldown
        }

        player.addCooldown(this, 72000L) // 1 hour
        event.isCancelled = true
        event.reviveHealth = 15.0
        player.playEffect(EntityEffect.PROTECTED_FROM_DEATH)
        player.damageItemStack(EquipmentSlot.HEAD, 50)
    }


    override fun onEntityDeath(player: Player, event: EntityDeathEvent) {
        if (event.isCancelled) return

        val entityMaxHealth = event.entity.getAttribute(Attribute.MAX_HEALTH)?.value?.toFloat() ?: return
        val actualGain = (player.foodLevel.toFloat() - player.saturation).coerceIn(0f, entityMaxHealth)
        if (actualGain <= 0f) return

        player.saturation += actualGain
        player.damageItemStack(EquipmentSlot.HEAD, entityMaxHealth.toInt().div(4).coerceAtMost(16))
    }
}