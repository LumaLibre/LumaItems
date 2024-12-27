package dev.jsinco.luma.lumaitems.items.armor

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.manager.CustomItem
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import java.util.UUID
import kotlin.random.Random

class WonderBeanieItem : CustomItem {

    companion object {
        val plugin: LumaItems = LumaItems.getInstance()
        private val recursionProtection: MutableList<UUID> = mutableListOf()
        private val causeEffectWhiteList: List<EntityPotionEffectEvent.Cause> = listOf(
            EntityPotionEffectEvent.Cause.AREA_EFFECT_CLOUD,
            EntityPotionEffectEvent.Cause.ARROW,
            EntityPotionEffectEvent.Cause.AXOLOTL,
            EntityPotionEffectEvent.Cause.DOLPHIN,
            EntityPotionEffectEvent.Cause.POTION_DRINK,
            EntityPotionEffectEvent.Cause.POTION_SPLASH,
            EntityPotionEffectEvent.Cause.TOTEM
        )
    }

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#723d8a&lW&#715b85&lo&#71797f&ln&#70967a&ld&#6fb474&le&#6ed26f&lr &#75db79&lB&#82cf92&le&#8fc3ac&la&#9cb7c5&ln&#a9abdf&li&#b69ff8&le",
            mutableListOf("&#723d8aC&#715486a&#716c81u&#70837dl&#709b79d&#6fb275r&#6fca70o&#6ee16cn", "&#723d8aB&#715e84o&#707f7eo&#709f78s&#6fc072t &#6ee16cI"),
            mutableListOf("&#723d8a\"&#7c4b9aI &#8559a9f&#8f67b9e&#9975c9e&#a383d9l &#ac91e8d&#b69ff8i&#aca8e4z&#a1b2d0z&#97bbbcy&#8dc5a8.&#83ce94.&#78d880.&#6ee16c\"","","This beanie amplifies all consumable", "potion effects you receive", "", "Wearing this beanie grants", "an extra health boost"),
            Material.NETHERITE_HELMET,
            mutableListOf("wonderbeanie"),
            mutableMapOf(Enchantment.UNBREAKING to 10, Enchantment.PROTECTION to 7, Enchantment.RESPIRATION to 5, Enchantment.AQUA_AFFINITY to 1, Enchantment.BLAST_PROTECTION to 7, Enchantment.MENDING to 1)
        )
        item.attributeModifiers[Attribute.ARMOR] = AttributeModifier(UUID.randomUUID(), "generic.armor", 3.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD)
        item.attributeModifiers[Attribute.ARMOR_TOUGHNESS] = AttributeModifier(UUID.randomUUID(), "generic.armorToughness", 3.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD)
        item.attributeModifiers[Attribute.KNOCKBACK_RESISTANCE] = AttributeModifier(UUID.randomUUID(), "generic.knockback_resistance", 1.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD)
        item.attributeModifiers[Attribute.MAX_HEALTH] = AttributeModifier(UUID.randomUUID(), "generic.maxHealth", 4.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD)

        item.tier = "&#c46bfb&lH&#c86eee&la&#cd71e2&ll&#d174d5&ll&#d677c8&lo&#da7abc&lm&#de7daf&la&#e380a2&lr&#e78395&le&#eb8689&ls &#f0897c&l2&#f48c6f&l0&#f98f63&l2&#fd9256&l3"
        return Pair("wonderbeanie", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.POTION_EFFECT -> {
                if (recursionProtection.contains(player.uniqueId)) return false
                else if (player.equipment.helmet == null || !player.equipment.helmet.itemMeta.persistentDataContainer.has(NamespacedKey(plugin, "wonderbeanie"), PersistentDataType.SHORT)) return false

                event as EntityPotionEffectEvent
                if (!causeEffectWhiteList.contains(event.cause)) return false

                val effect = event.newEffect ?: return false
                event.isCancelled = true

                recursionProtection.add(player.uniqueId)
                player.addPotionEffect(PotionEffect(effect.type, (effect.duration + Random.nextInt(200, 400)), (effect.amplifier + Random.nextInt(1,3)), effect.isAmbient, effect.hasParticles(), effect.hasIcon()))
                recursionProtection.remove(player.uniqueId)
            }
            else -> return false
        }
        return true
    }
}