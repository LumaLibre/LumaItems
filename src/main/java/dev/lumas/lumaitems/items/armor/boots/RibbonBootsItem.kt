package dev.lumas.lumaitems.items.armor.boots

import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItem
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class RibbonBootsItem : CustomItem {
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#fb79d7&lR&#fb6acb&li&#fc5ac0&lb&#fc4bb4&lb&#fd3ba9&lo&#fd2c9d&ln &#fd3298&lS&#fe3892&lo&#fe3e8d&lc&#ff4487&lk&#ff4a82&ls",
            mutableListOf("&#fb79d7L&#fb85dbi&#fb91deg&#fc9de2h&#fca9e5t&#fcb5e9w&#fcc1ede&#fccdf0i&#fdd9f4g&#fde5f7h&#fdf1fbt"),
            mutableListOf("&#fb79d7\"&#fb78d5R&#fb77d3e&#fb75d0a&#fb74cec&#fc73cch &#fc72cat&#fc70c7h&#fc6fc5e &#fc6ec3s&#fc6dc1k&#fc6bbei&#fc6abce&#fc69bas&#fc68b8, &#fd66b5b&#fd65b3u&#fd64b1t &#fd63afd&#fd62ade&#fd60aaf&#fd5fa8y &#fd5ea6g&#fd5da4r&#fe5ba1a&#fe5a9fv&#fe599di&#fe589bt&#fe5698y&#fe5596'&#fe5494s &#fe5392t&#fe518fi&#fe508de&#ff4f8bs&#ff4e89.&#ff4c86.&#ff4b84.&#ff4a82\"","","&fWearing these socks will prevent all", "&ftypes of fall damage and give a small","&fjump boost"),
            Material.NETHERITE_BOOTS,
            mutableListOf("ribbonboots"),
            mutableMapOf(Enchantment.PROTECTION to 7, Enchantment.FROST_WALKER to 3, Enchantment.UNBREAKING to 8, Enchantment.MENDING to 1)
        )
        return Pair("ribbonboots", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        val entityDamage: EntityDamageEvent? = event as? EntityDamageEvent

        when (type) {
            Action.PLAYER_DAMAGED -> {
                if (entityDamage!!.cause == EntityDamageEvent.DamageCause.FALL) {
                    entityDamage.isCancelled = true
                }
            }
            Action.RUNNABLE -> {
                player.addPotionEffect(PotionEffect(PotionEffectType.JUMP_BOOST, 220, 0, false, false, false))
            }
            else -> return false
        }
        return true
    }
}