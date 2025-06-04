package dev.jsinco.luma.lumaitems.items.misc

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.manager.CustomItem
import dev.jsinco.luma.lumaitems.obj.QuickTasks
import io.papermc.paper.datacomponent.DataComponentTypes
//import io.papermc.paper.datacomponent.item.TooltipDisplay
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.UUID
import kotlin.random.Random

class WitchsBrewItem : CustomItem {

    companion object {
        val potionEffects: List<PotionEffectType> = listOf(
            PotionEffectType.HASTE,
            PotionEffectType.JUMP_BOOST,
            PotionEffectType.ABSORPTION,
            PotionEffectType.BAD_OMEN,
            PotionEffectType.CONDUIT_POWER,
            PotionEffectType.DOLPHINS_GRACE,
            PotionEffectType.WATER_BREATHING,
            PotionEffectType.LUCK,
            PotionEffectType.GLOWING,
            PotionEffectType.HEALTH_BOOST,
            PotionEffectType.INVISIBILITY,
            PotionEffectType.LEVITATION,
            PotionEffectType.NIGHT_VISION,
            PotionEffectType.REGENERATION,
            PotionEffectType.SATURATION,
            PotionEffectType.SLOW_FALLING,
            PotionEffectType.SPEED,
            PotionEffectType.RESISTANCE,
            PotionEffectType.FIRE_RESISTANCE,
            PotionEffectType.STRENGTH
        )
    }

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#320342&lW&#541042&li&#761d43&lt&#982a43&lc&#ba3744&lh&#dc4444&l'&#cb434c&ls &#ba4354&lB&#a9425b&lr&#984263&le&#87416b&lw",
            mutableListOf("&#dc4444F&#c4434fi&#ab425an&#934165e &#8b4372A&#94477fg&#9d4c8de&#a6509ad"),
            mutableListOf("&#320342\"&#370745A &#3d0b47p&#420f4ao&#47134ct&#4d164fi&#521a51o&#571e54n&#5d2257'&#622659s &#672a5cg&#6c2e5ea&#723261m&#773563b&#7c3966l&#823d68e&#87416b\"","","This potion came from the workshop", "of a witch. It's recommended to", "not drink it","","&cCooldown: 20 secs"),
            Material.POTION,
            mutableListOf("witchsbrew"),
            mutableMapOf(Enchantment.MENDING to 1, Enchantment.KNOCKBACK to 4)
        )
        item.tier = "&#c46bfb&lH&#c86eee&la&#cd71e2&ll&#d174d5&ll&#d677c8&lo&#da7abc&lm&#de7daf&la&#e380a2&lr&#e78395&le&#eb8689&ls &#f0897c&l2&#f48c6f&l0&#f98f63&l2&#fd9256&l3"

        val potion = item.createItem()
        //potion.setData(DataComponentTypes.TOOLTIP_DISPLAY,  TooltipDisplay.tooltipDisplay().hideTooltip(true).build())
        potion.editMeta {
            it as PotionMeta
            it.color = Color.PURPLE
        }

        return Pair("witchsbrew", potion)
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.CONSUME_ITEM -> {
                event as PlayerItemConsumeEvent

                event.isCancelled = true
                if (QuickTasks.isOnCooldown(this, player)) return false
                QuickTasks.addCooldown(this, player, 400)
                player.playSound(player.location, Sound.ENTITY_WITCH_DRINK, 1f, 1f)

                val amount = Random.nextInt(5,10)
                for (i in 0..amount) {
                    val effect = potionEffects[Random.nextInt(potionEffects.size)]
                    player.addPotionEffect(PotionEffect(effect, Random.nextInt(1000), Random.nextInt(4), true, true, true))
                }

                val potion = event.item
                val potionMeta = potion.itemMeta as PotionMeta
                potionMeta.color = Color.fromRGB(Random.nextInt(244), Random.nextInt(244), Random.nextInt(244))
                for (item in player.inventory) {
                    if (item == potion) {
                        item.itemMeta = potionMeta
                    }
                }
            }
            else -> return false
        }
        return true
    }
}