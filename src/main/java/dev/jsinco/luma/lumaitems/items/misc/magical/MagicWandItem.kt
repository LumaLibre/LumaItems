package dev.jsinco.luma.lumaitems.items.misc.magical

import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItem
import dev.jsinco.luma.lumaitems.obj.PersistentDataRecord
import dev.jsinco.luma.lumaitems.obj.MagicItemCooldown
import dev.jsinco.luma.lumaitems.particles.ParticleDisplay
import dev.jsinco.luma.lumaitems.particles.Particles
import dev.jsinco.luma.lumaitems.shapes.Sphere
import dev.jsinco.luma.lumaitems.util.AbilityUtil
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.concurrent.ConcurrentLinkedQueue

@Suppress("Duplicates")
class MagicWandItem : CustomItem {

    enum class Spell(val cooldownInSecs: Int) {
        BOOST_EFFECTS(30),
        LAUNCH(10),
        EXPLOSION_SPHERE(60),
    }

    companion object {
        private const val SPELL_KEY = "magicwand_spell"
        private const val DEFAULT_SPELL = "BOOST_EFFECTS"
        private val cooldowns: ConcurrentLinkedQueue<MagicItemCooldown> = ConcurrentLinkedQueue()
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#C7305D>M<#962F72>a<#642D87>g<#8D3A71>i<#B6475C>c <#C45078>W<#A94CAA>a<#A94CAA>n<#C55CC6>d</b>")
            .customEnchants("<#B6475C>Illusion")
            .lore("Click to cast spells. Click while", "sneaking to change spells.", "",
                "<gradient:#C7305D:#ff9ccb>Boost <dark_gray>- <white>Temporarily", "gain multiple buff effects. <red>30s</red>",
                "",
                "<gradient:#C7305D:#ff9ccb>Launch <dark_gray>- <white>Click to cast", "a spell which launches and", "damages nearby entities. <red>10s</red>",
                "",
                "<gradient:#C7305D:#ff9ccb>Clear <dark_gray>- <white>Cast a spell", "which clears out an area", "of blocks upon landing. <red>1m</red>")
            .material(Material.BLAZE_ROD)
            .persistentData("magicwand")
            //.tier(Tier.CARNIVAL_2024)
            // Halloween re-release
            .tier(Tier.HALLOWEEN_2025)
            .vanillaEnchants(mutableMapOf(Enchantment.BANE_OF_ARTHROPODS to 5, Enchantment.FIRE_ASPECT to 4, Enchantment.SHARPNESS to 5))
            .persistentDataRecords(PersistentDataRecord.create(SPELL_KEY, PersistentDataType.STRING, DEFAULT_SPELL))
            .buildPair()
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.RIGHT_CLICK -> {
                event as PlayerInteractEvent

                if (player.isSneaking) {
                    nextSpell(player, event.item ?: return false)
                    return true
                }

                val spell = event.item?.let { getSpell(it) } ?: return false

                if (cooldowns.any { it.playerUUID == player.uniqueId && (it.spellEnum as Spell) == spell }) {
                    return false
                }


                when (spell) {
                    Spell.BOOST_EFFECTS -> boostEffectsSpell(player)
                    Spell.LAUNCH -> spawnSpellBall(player, Spell.LAUNCH)
                    Spell.EXPLOSION_SPHERE -> spawnSpellBall(player, Spell.EXPLOSION_SPHERE)
                }
            }

            Action.PROJECTILE_LAND -> {
                event as ProjectileHitEvent

                val spell = event.entity.persistentDataContainer.get(NamespacedKey(instance(), SPELL_KEY), PersistentDataType.STRING)?.uppercase()?.let { Spell.valueOf(it) } ?: return false

                when (spell) {
                    Spell.LAUNCH -> launchSpell(player, event.hitBlock?.location ?: event.hitEntity?.location ?: return false)
                    Spell.EXPLOSION_SPHERE -> spawnExplosionSphere(event.hitBlock?.location ?: event.hitEntity?.location ?: return false, player)
                    else -> return false
                }
            }

            Action.ASYNC_RUNNABLE -> {
                if (cooldowns.isEmpty()) return false

                val currentTime = System.currentTimeMillis()
                for (cooldown in cooldowns) {
                    if (cooldown.cooldown + ((cooldown.spellEnum as Spell).cooldownInSecs * 1000) < currentTime) {
                        cooldowns.remove(cooldown)
                    }
                }
            }

            else -> return false
        }
        return true
    }


    private fun spawnSpellBall(player: Player, spell: Spell) {
        val particleDisplay = ParticleDisplay.of(Particle.DUST).withColor(Util.getRandomColor())
        AbilityUtil.spawnSpell(player, null, "magicwand", 150) {
            Particles.sphere(0.2, 4.0, particleDisplay.withLocation(it.location))
        }.also {
            it.persistentDataContainer.set(NamespacedKey(instance(), SPELL_KEY), PersistentDataType.STRING, spell.name)
        }
    }


    private fun getSpell(item: ItemStack) =
        item.itemMeta?.persistentDataContainer?.get(NamespacedKey(instance(), SPELL_KEY), PersistentDataType.STRING)?.uppercase()?.let { Spell.valueOf(it) }

    private fun nextSpell(player: Player, item: ItemStack) {
        val currentSpell = getSpell(item) ?: return
        val nextSpell = Spell.entries.let { spells ->
            val currentIndex = spells.indexOf(currentSpell)
            val nextIndex = if (currentIndex >= spells.size - 1) 0 else currentIndex + 1
            spells[nextIndex]
        }
        item.itemMeta = item.itemMeta?.apply {
            persistentDataContainer.set(NamespacedKey(instance(), SPELL_KEY), PersistentDataType.STRING, nextSpell.name)
        }
        MiniMessageUtil.msg(player, "Spell changed to <gradient:#C7305D:#ff9ccb>${Util.formatMaterialName(nextSpell.name)}")
    }

    // Spells

    private fun boostEffectsSpell(player: Player) {
        cooldowns.add(MagicItemCooldown(player.uniqueId, Spell.BOOST_EFFECTS, System.currentTimeMillis()))
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 500, 2))
        player.addPotionEffect(PotionEffect(PotionEffectType.JUMP_BOOST, 500, 2))
        player.addPotionEffect(PotionEffect(PotionEffectType.HASTE, 500, 2))
    }

    private fun launchSpell(player: Player, loc: Location) {
        val entities = loc.getNearbyLivingEntities(5.0)
        if (entities.isEmpty()) return

        cooldowns.add(MagicItemCooldown(player.uniqueId, Spell.LAUNCH, System.currentTimeMillis()))
        val particleDisplay = ParticleDisplay.of(Particle.DUST).withColor(Util.getRandomColor())
        for (livingEntity in entities) {
            if (AbilityUtil.noDamagePermission(player, livingEntity)) continue
            object : BukkitRunnable() {
                var i = 0
                override fun run() {
                    if (i++ > 2) {
                        cancel()
                    }
                    livingEntity.velocity = livingEntity.location.toVector().subtract(loc.toVector()).add(Vector(0.0,5.0,0.0)).multiply(23.5).normalize()

                    Particles.line(livingEntity.location, loc, 0.2, particleDisplay)
                    livingEntity.damage(5.0)
                }
            }.runTaskTimer(instance(), 0, 2)
        }
    }

    private fun spawnExplosionSphere(loc: Location, player: Player) {
        if (AbilityUtil.noBuildPermission(player, loc.block)) return
        cooldowns.add(MagicItemCooldown(player.uniqueId, Spell.EXPLOSION_SPHERE, System.currentTimeMillis()))
        val sphere = Sphere(loc, 4.0, 5.0)
        for (block in sphere.sphere) {
            player.breakBlock(block)
        }
        loc.world.spawnParticle(Particle.EXPLOSION, loc, 1, 0.0, 0.0, 0.0, 0.0)
        loc.world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f)
    }



}