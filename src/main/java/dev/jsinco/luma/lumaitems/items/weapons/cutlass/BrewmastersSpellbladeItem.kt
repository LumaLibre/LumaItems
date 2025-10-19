package dev.jsinco.luma.lumaitems.items.weapons.cutlass

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.manager.CustomItem
import dev.jsinco.luma.lumaitems.util.AbilityUtil
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import kotlin.random.Random

class BrewmastersSpellbladeItem : CustomItem {

    companion object {
        val opponentEffects: List<PotionEffectType> = listOf(
            PotionEffectType.BLINDNESS,
            PotionEffectType.NAUSEA,
            PotionEffectType.INSTANT_DAMAGE,
            PotionEffectType.HUNGER,
            PotionEffectType.POISON,
            PotionEffectType.SLOWNESS,
            PotionEffectType.MINING_FATIGUE,
            PotionEffectType.UNLUCK,
            PotionEffectType.WEAKNESS,
            PotionEffectType.WITHER
        )
        val attackerEffects: List<PotionEffectType> = listOf(
            PotionEffectType.RESISTANCE,
            PotionEffectType.HASTE,
            PotionEffectType.FIRE_RESISTANCE,
            PotionEffectType.INSTANT_HEALTH,
            PotionEffectType.STRENGTH,
            PotionEffectType.INVISIBILITY,
            PotionEffectType.JUMP_BOOST,
            PotionEffectType.NIGHT_VISION,
            PotionEffectType.REGENERATION,
            PotionEffectType.SATURATION,
            PotionEffectType.SPEED,
            PotionEffectType.WATER_BREATHING
        )
    }

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#813f73&lB&#874976&lr&#8d537a&le&#935d7d&lw&#996780&lm&#9f7184&la&#a57b87&ls&#ab858a&lt&#b18f8e&le&#b79991&lr&#bda394&l'&#c0a68f&ls &#c1a281&lS&#c19f72&lp&#c29b64&le&#c29756&ll&#c39447&ll&#c39039&lb&#c48c2b&ll&#c4881d&la&#c5850e&ld&#c58100&le",
            mutableListOf("&#ab6982H&#b0706de&#b57759x&#bb7d44x&#c08430e&#c58b1bd"),
            mutableListOf("&#813f73\"&#83416fW&#86446bi&#884667e&#8a4863l&#8d4a5fd&#8f4d5be&#914f57r &#945153b&#96534fl&#98564be&#9b5847s&#9d5a43s&#9f5d3fe&#a25f3bd&#a46138, &#a76334e&#a96630n&#ab682ce&#ae6a28m&#b06d24i&#b26f20e&#b5711cs &#b77318c&#b97614u&#bc7810r&#be7a0cs&#c07c08e&#c37f04d&#c58100\"","","This weapon grants a chance to give", "useful effects wielder and harmful", "potion effects to their opponent"),
            Material.NETHERITE_SWORD,
            mutableListOf("brewmastersspellblade"),
            mutableMapOf(Enchantment.SHARPNESS to 8, Enchantment.BANE_OF_ARTHROPODS to 9, Enchantment.UNBREAKING to 10, Enchantment.LOOTING to 5, Enchantment.MENDING to 1)
        )
        item.tier = "&#c46bfb&lH&#c86eee&la&#cd71e2&ll&#d174d5&ll&#d677c8&lo&#da7abc&lm&#de7daf&la&#e380a2&lr&#e78395&le&#eb8689&ls &#f0897c&l2&#f48c6f&l0&#f98f63&l2&#fd9256&l3"
        return Pair("brewmastersspellblade", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {

        when (type) {
            Action.ENTITY_DAMAGE -> {
                event as EntityDamageByEntityEvent
                if (Random.nextInt(100) >= 7 || AbilityUtil.noDamagePermission(player, event.entity)) return false

                if (Random.nextBoolean()) {
                    addEffect(opponentEffects.random(), event.entity as LivingEntity)
                } else {
                    addEffect(attackerEffects.random(), player)
                }
            }
            else -> return false
        }
        return true
    }

    private fun addEffect(potionEffectType: PotionEffectType, entity: LivingEntity) {
        if (Random.nextBoolean()) {
            entity.world.spawnParticle(Particle.WITCH, entity.location, 25, 0.5, 0.5, 0.5, 0.1)
            entity.world.playSound(entity.location, Sound.ITEM_BOTTLE_FILL, 0.5f, 1f)
        } else {
            entity.world.spawnParticle(Particle.WAX_ON, entity.location, 25, 0.5, 0.5, 0.5, 0.1)
            entity.world.playSound(entity.location, Sound.BLOCK_BREWING_STAND_BREW, 0.5f, 0.82f)
        }
        entity.addPotionEffect(potionEffectType.createEffect(200, 1))
    }
}