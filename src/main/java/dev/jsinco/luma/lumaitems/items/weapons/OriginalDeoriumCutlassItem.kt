package dev.jsinco.luma.lumaitems.items.weapons

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.manager.CustomItem
import dev.jsinco.luma.lumaitems.util.AbilityUtil
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import java.util.UUID
import kotlin.math.cos
import kotlin.math.sin

class OriginalDeoriumCutlassItem  : CustomItem {

    companion object {
        val plugin: LumaItems = LumaItems.getInstance()
        val cooldown: MutableList<UUID> = mutableListOf()
    }

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "og-&#8020fb&lD&#932df9&le&#a639f7&lo&#ba46f5&lr&#cd52f4&li&#e05ff2&lu&#f36cf0&lm &#fa74eb&lC&#f477e4&lu&#ed7bdd&lt&#e77ed6&ll&#e182cf&la&#da85c8&ls&#d489c1&ls",
            mutableListOf("&#8f2ecaE&#9432cfv&#9935d3e&#9e39d8n&#a33dddt &#a840e1H&#ae44e6o&#b347ear&#b84befi&#bd4ff4z&#c252f8o&#c756fdn"),
            mutableListOf("&#8020fb\"&#8523faL&#8926fae&#8e2af9t &#932df9t&#9730f8h&#9c33f7e &#a137f7d&#a63af6a&#aa3df5r&#af40f5k&#b444f4n&#b847f4e&#bd4af3s&#c24df2s &#c650f2c&#cb54f1o&#d057f1n&#d45af0s&#d95defu&#de61efm&#e364eee &#e767edt&#ec6aedh&#f16eece&#f571ecm&#fa74eb\"","","Right-click to summon a gravity well", "at a targeted block","","Entities nearby the well will","be damaged and weakened", "", "&cCooldown: 30 secs"),
            Material.NETHERITE_SWORD,
            mutableListOf("og-deoriumcutlass"),
            mutableMapOf(Enchantment.SHARPNESS to 8, Enchantment.SMITE to 8, Enchantment.LOOTING to 5, Enchantment.SWEEPING_EDGE to 4, Enchantment.UNBREAKING to 10, Enchantment.MENDING to 1)
        )
        item.tier = "&#c46bfb&lH&#c86eee&la&#cd71e2&ll&#d174d5&ll&#d677c8&lo&#da7abc&lm&#de7daf&la&#e380a2&lr&#e78395&le&#eb8689&ls &#f0897c&l2&#f48c6f&l0&#f98f63&l2&#fd9256&l3"
        return Pair("og-deoriumcutlass", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.RIGHT_CLICK -> {
                if (cooldown.contains(player.uniqueId)) return false
                val block = player.getTargetBlockExact(50) ?: return false
                createPullVoid(block.location.add(0.0,1.0,0.0), player)
                cooldownPlayer(player.uniqueId)
            }
            else -> return false
        }
        return true
    }


    private fun createPullVoid(location: Location, p: Player) {
        val points = 50
        val step = 60
        val armorStand = location.world.spawnEntity(p.location.add(0.0,350.0,0.0), EntityType.ARMOR_STAND) as ArmorStand
        armorStand.isVisible = false
        armorStand.teleport(location)
        armorStand.isInvulnerable = false
        armorStand.isSmall = true
        val repeatable = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, {
            for (entity in armorStand.getNearbyEntities(10.0,10.0,10.0)) {
                if (entity.type == EntityType.ARMOR_STAND || entity.type == EntityType.PLAYER || AbilityUtil.noDamagePermission(p, entity)) continue
                val direction: Vector = armorStand.location.subtract(entity.location).toVector()
                val distance: Double = entity.location.distance(armorStand.location)

                if (distance <= 2.5 && entity is LivingEntity) {
                    entity.damage(5.0, p)
                    entity.velocity = Vector(0, 0, 0)

                    entity.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, 40, 1, false, false, false))
                    entity.world.spawnParticle(Particle.DUST, entity.location, 5, 0.6, 0.6, 0.6, 0.8,
                        Particle.DustOptions(Color.PURPLE, 1f)
                    )
                }
                entity.velocity = direction.normalize().multiply(distance / 20)
            }

            for (i in 0 until points) {
                val dx: Double = cos(step + Math.PI * 2 * (i.toDouble() / points))
                val dz: Double = sin(step + Math.PI * 2 * (i.toDouble() / points))
                armorStand.location.world.spawnParticle(Particle.WITCH, armorStand.location.x + dx, armorStand.location.y, armorStand.location.z + dz, 1, 0.0, 0.0, 0.0, 0.1)
                armorStand.location.world.spawnParticle(Particle.DUST, armorStand.location.x + dx, armorStand.location.y, armorStand.location.z + dz, 1, 0.0, 0.0, 0.0, 0.5, Particle.DustOptions(Color.PURPLE, 1f))
            }
            armorStand.location.world.playSound(armorStand.location, Sound.ENTITY_WITHER_AMBIENT, 0.08f, 2f)
        },0, 5L)

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, {
            Bukkit.getScheduler().cancelTask(repeatable)
            armorStand.remove()
        }, 120L)
    }


    private fun cooldownPlayer(uuid: UUID) {
        cooldown.add(uuid)
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, {
            cooldown.remove(uuid)
        }, 600L)
    }
}