package dev.jsinco.luma.lumaitems.items.weapons

import dev.jsinco.luma.lumaitems.util.tiers.Tier
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.shapes.ShapeUtil
import dev.jsinco.luma.lumaitems.util.AbilityUtil
import dev.jsinco.luma.lumaitems.util.AbilityUtil.breakNaturallyWithLog
import dev.jsinco.luma.lumaitems.util.Util
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.random.Random

class GiantInflatableHammerItem : CustomItemFunctions() {

    companion object {
        private val colors = listOf(
            Util.hex2BukkitColor("#fca2ab"),
            Util.hex2BukkitColor("#fccba8"),
            Util.hex2BukkitColor("#fbfcb5"),
            Util.hex2BukkitColor("#b0fc9f"),
            Util.hex2BukkitColor("#a3f1fc")
        )
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#FFA4AD>G<#FFABAD>i<#FFB2AC>a<#FFB9AC>n<#FFBFAB>t <#FFCDAA>I<#FFD7AD>n<#FFE1AF>f<#FEEBB2>l<#FEF5B4>a<#FEFFB7>t<#F1FFB3>a<#E5FFB0>b<#D8FFAC>l<#CBFFA8>e <#B2FFA1>H<#AFFDB4>a<#ADFBC7>m<#AAF8D9>m<#A8F6EC>e<#A5F4FF>r</b>")
            .customEnchants("<#FFA4AD>Astonish")
            .lore("Upon smash attacking, hit", "entities will be stunned.", "", "Occasionally, smash attacks", "with this weapon will push", "entities through the", "ground.")
            .material(Material.MACE)
            .persistentData("giantinflatablehammer")
            .tier(Tier.CARNIVAL_2024)
            .vanillaEnchants(mutableMapOf(Enchantment.MENDING to 1, Enchantment.UNBREAKING to 5, Enchantment.DENSITY to 5, Enchantment.SHARPNESS to 3, Enchantment.WIND_BURST to 2))
            .buildPair()
    }

    override fun onMaceSmashAttack(player: Player, event: EntityDamageByEntityEvent) {
        stunEntity(event.entity as? LivingEntity ?: return)

        val loc = event.entity.location
        if (!AbilityUtil.noBuildPermission(player, loc.block) && Random.nextInt(10) == 1) {
            pushThruGround(loc, player)
        }
    }

    private fun pushThruGround(loc: Location, player: Player) {
        for (block in ShapeUtil.circle(loc.subtract(0.0, 1.0, 0.0), 2, 7)) {

            block.breakNaturallyWithLog(player, true, false)
        }
    }

    private fun stunEntity(entity: LivingEntity) {
        if (entity !is Player) {
            entity.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 150, 10))
        }
        entity.addPotionEffect(PotionEffect(PotionEffectType.DARKNESS, 150, 1))
        entity.world.spawnParticle(Particle.DUST, entity.eyeLocation, 30, 0.5, 0.2, 0.5, 0.1, Particle.DustOptions(
            colors.random(), 1.4f))
    }
}