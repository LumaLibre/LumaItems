package dev.jsinco.luma.lumaitems.items.weapons.hatchet

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.QuickTasks
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import dev.jsinco.luma.lumaitems.util.tiers.ThanksgivingEventTier
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.Consumable
import io.papermc.paper.datacomponent.item.FoodProperties
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class HoneyWickHatchetItem : CustomItemFunctions() {

    private val dropItem: ItemStack = ItemFactory.builder()
        .material(Material.HONEYCOMB)
        .persistentData("honeywickhatchet")
        .vanillaEnchants(Enchantment.UNBREAKING to 1)
        .build()
        .createItem()

    @Suppress("UnstableApiUsage")
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory.builder()
            .name("<b><#DB6F02>H<#ED8313>o<#FF9624>n<#FFA725>e<#FFB825>y<#FFC761>w<#FFD69C>i<#FFD69C>c<#FFD69C>k <#f6f0e4>Hatchet</b>")
            .customEnchants("<gray>Consumable", "<#ffb825>Perfect Drizzle")
            .persistentData("honeywickhatchet")
            .material(Material.NETHERITE_AXE)
            .tier(ThanksgivingEventTier.THANKSGIVING_2024)
            .lore("Attacked entities will", "receive extra damage.", "", "<#645B82>Consume <white>this item", "to gain temporary buffs.", "", "<red>Cooldown: 1m")
            .vanillaEnchants(Enchantment.SHARPNESS to 8, Enchantment.LOOTING to 5, Enchantment.FIRE_ASPECT to 4, Enchantment.UNBREAKING to 10, Enchantment.MENDING to 1)
            .build()
            .createItem()


        val foodProps: FoodProperties.Builder = FoodProperties.food()
            .canAlwaysEat(true)
            .nutrition(2)
            .saturation(3.5f)

        item.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft("golden_axe"))
        item.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable())
        item.setData(DataComponentTypes.FOOD, foodProps)
        return Pair("honeywickhatchet", item)
    }

    override fun onEntityDamage(player: Player, event: EntityDamageByEntityEvent) {
        val target = event.entity as? LivingEntity ?: return
        val rand = random().nextInt(3,8).also {
            event.damage += 2.0 * it
        }

        if (random().nextInt(100) > 20) {
            return
        }

        val list: MutableList<Entity> = mutableListOf()
        for (i in 0 until rand) {
            list.add(target.world.dropItemNaturally(target.boundingBox.center.toLocation(target.world), dropItem.clone().apply {
                editMeta { it.setDisplayName(random().nextInt(50).toString()) } // prevent stacking
            }))
        }
        target.world.playSound(target.location, Sound.BLOCK_HONEY_BLOCK_HIT, 1.0f, 1.0f)



        Bukkit.getScheduler().runTaskLater(instance(), Runnable {
            list.forEach { it.remove() }
        }, 25)
    }

    override fun onEntityPickupItem(event: EntityPickupItemEvent) {
        if (event.item.itemStack.type == dropItem.type) {
            event.isCancelled = true
        }
    }

    override fun onHopperPickupItem(event: InventoryPickupItemEvent) {
        if (event.item.itemStack.type == dropItem.type) {
            event.isCancelled = true
        }
    }

    override fun onConsumeItem(player: Player, event: PlayerItemConsumeEvent) {
        event.isCancelled = true

        if (QuickTasks.isOnCooldown(this, player.uniqueId)) {
            player.sendActionBar(MiniMessageUtil.mm(ThanksgivingEventTier.THANKSGIVING_2024.cannotConsumeMessage))
            return
        }

        QuickTasks.addCooldown(this, player.uniqueId, 1200L)

        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 600, 2)) // speed 3 for 30s
        player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 600, 2)) // regen 3 for 30s
        player.addPotionEffect(PotionEffect(PotionEffectType.ABSORPTION, 600, 2)) // abs 3 for 30s
        player.addPotionEffect(PotionEffect(PotionEffectType.SATURATION, 600, 2)) // sat 3 for 30s

        player.sendActionBar(MiniMessageUtil.mm(ThanksgivingEventTier.THANKSGIVING_2024.consumeMessages.random()))
    }
}