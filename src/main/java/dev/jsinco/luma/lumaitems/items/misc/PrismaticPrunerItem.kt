package dev.jsinco.luma.lumaitems.items.misc

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class PrismaticPrunerItem : CustomItemFunctions() {

    private val nameSpacedKey = Util.namespacedKey("prismaticpruners")
    private val wool = ItemStack(Material.WHITE_WOOL)

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#C18DF5>P<#C784E9>r<#CD7BDE>i<#D372D2>s<#D969C7>m<#DF5FBB>a<#E556AF>t<#EB4DA4>i<#F14498>c <#DC6F8B>P<#D28485>r<#C89A7E>u<#BDAF78>n<#B3C471>e<#A8DA6B>r<#9EEF64>s")
            .customEnchants("<#CD7BDE>Clippers")
            .lore(
                "<#C18DF5>Right-click <white>any entity",
                "to shear it and get wool."
                )
            .material(Material.SHEARS)
            .vanillaEnchants(Enchantment.UNBREAKING to 4, Enchantment.EFFICIENCY to 4, Enchantment.MENDING to 1)
            .tier(Tier.VALENTIDE_2025)
            .persistentData("prismaticpruners")
            .buildPair()
    }


    override fun onPlayerInteractEntity(player: Player, event: PlayerInteractAtEntityEvent) {
        val entity = event.rightClicked as? LivingEntity ?: return
        if (entity.persistentDataContainer.has(nameSpacedKey) || entity.type == EntityType.SHEEP || entity.type == EntityType.PLAYER) {
            return
        }

        entity.persistentDataContainer.set(nameSpacedKey, PersistentDataType.BOOLEAN, true)
        entity.world.dropItemNaturally(entity.eyeLocation, wool.asQuantity(random().nextInt(1, 3)))
        entity.world.playSound(entity.location, Sound.ENTITY_SHEEP_SHEAR, 1f, 1f)
        entity.world.spawnParticle(Particle.DUST, entity.location, 4, 0.5, 0.5, 0.5, 0.1, DustOptions(Color.RED, 1f))
    }

}