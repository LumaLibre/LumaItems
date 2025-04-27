package dev.jsinco.luma.lumaitems.items.magical

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.PersistentDataRecord
import dev.jsinco.luma.lumaitems.obj.MagicItemCooldown
import dev.jsinco.luma.lumaitems.particles.ParticleDisplay
import dev.jsinco.luma.lumaitems.particles.Particles
import dev.jsinco.luma.lumaitems.util.AbilityUtil
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.concurrent.ConcurrentLinkedQueue

@Suppress("Duplicates")
class BookOfKnowledgeItem : CustomItemFunctions() {

    enum class Spell(val cooldownInSecs: Int) {
        DRAIN(30),
        VALIANT_EXPLODE(60)
    }

    companion object {
        private val vector0 = Vector(0, 0, 0)

        private const val SPELL_KEY = "bookofknowledge_spell"
        private const val DEFAULT_SPELL = "DRAIN"
        const val STRING_KEY = "bookofknowledge"
        private val cooldowns: ConcurrentLinkedQueue<MagicItemCooldown> = ConcurrentLinkedQueue()
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#7A2E19>B<#8F4A2D>o<#A56541>o<#BA8154>k <#B48559>o<#9A6F49>f <#64412A>K<#724B2E>n<#815531>o<#8F5E35>w<#9D6838>l<#9D6838>e<#9D6838>d<#9D6838>g<#9D6838>e</b>")
            .customEnchants("<#CF9C68>Mastery")
            .lore("Experience orbs give more", "experience while holding this", "book.", "",
                "Click to cast spells. Click while",
                "sneaking to change spells.",
                "",
                "<gradient:#CF9C68:#815531>Drain <dark_gray>- <white>Click to siphon health", "from nearby entities. <red>30s</red>",
                "",
                "<gradient:#CF9C68:#815531>Valiant Explosion <dark_gray>- <white>Cast a", "spell which will damage", "and explode entities. <red>1m</red>")
            .material(Material.BOOK)
            .persistentData(STRING_KEY)
            .tier(Tier.CARNIVAL_2024)
            .vanillaEnchants(mutableMapOf(Enchantment.BANE_OF_ARTHROPODS to 5, Enchantment.FIRE_ASPECT to 4, Enchantment.SHARPNESS to 5))
            .persistentDataRecords(
                PersistentDataRecord.create(SPELL_KEY, PersistentDataType.STRING, DEFAULT_SPELL)
            )
            .buildPair()
    }

    override fun onPlayerPickupExp(player: Player, event: PlayerPickupExperienceEvent) {
        event.experienceOrb.experience += 1
    }


    override fun onLeftClick(player: Player, event: PlayerInteractEvent) {
        if (!Util.isItemInSlot(STRING_KEY, EquipmentSlot.HAND, player)) return
        nextSpell(player, event.item ?: return)
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (player.isSneaking) {
            nextSpell(player, event.item ?: return)
            return
        }

        val spell = event.item?.let { getSpell(it) } ?: return

        if (cooldowns.any { it.playerUUID == player.uniqueId && (it.spellEnum as Spell) == spell }) {
            return
        }

        when (spell) {
            Spell.DRAIN -> drainSpell(player)
            Spell.VALIANT_EXPLODE -> spawnSpellBall(player, Spell.VALIANT_EXPLODE)
        }
        player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.8f, 1.2f)
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val spell = event.entity.persistentDataContainer.get(NamespacedKey(instance(), SPELL_KEY), PersistentDataType.STRING)?.uppercase()?.let { Spell.valueOf(it) } ?: return

        when (spell) {
            Spell.VALIANT_EXPLODE -> valiantExplodeSpell(player, event.hitEntity as? LivingEntity ?: return)
            else -> return
        }
    }

    override fun onAsyncRunnable(player: Player) {
        if (cooldowns.isEmpty()) return

        val currentTime = System.currentTimeMillis()
        for (cooldown in cooldowns) {
            if (cooldown.cooldown + ((cooldown.spellEnum as Spell).cooldownInSecs * 1000) < currentTime) {
                cooldowns.remove(cooldown)
            }
        }
    }





    private fun spawnSpellBall(player: Player, spell: Spell) {
        val particleDisplay = ParticleDisplay.of(Particle.DUST).withColor(Util.getRandomColor())
        AbilityUtil.spawnSpell(player, null, STRING_KEY, 150) {
            Particles.sphere(0.2, 4.0, particleDisplay.withLocation(it.location))
        }.also {
            it.persistentDataContainer.set(NamespacedKey(instance(), SPELL_KEY), PersistentDataType.STRING, spell.name)
        }
    }

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
        MiniMessageUtil.msg(player, "Spell changed to <gradient:#CF9C68:#815531>${Util.formatMaterialName(nextSpell.name)}")
    }


    private fun getSpell(item: ItemStack) =
        item.itemMeta?.persistentDataContainer?.get(NamespacedKey(instance(), SPELL_KEY), PersistentDataType.STRING)?.uppercase()?.let { Spell.valueOf(it) }


    private fun drainSpell(player: Player) {
        cooldowns.add(MagicItemCooldown(player.uniqueId, Spell.DRAIN, System.currentTimeMillis()))
        val particleDisplay = ParticleDisplay.of(Particle.DUST).withColor(Util.getRandomColor())
        val entities: List<LivingEntity> = player.getNearbyEntities(15.0, 15.0,15.0).filterIsInstance<LivingEntity>()
        var i = 0
        object : BukkitRunnable() {
            override fun run() {
                if (i++ > 5) {
                    cancel()
                    return
                }

                for (entity in entities) {
                    if (AbilityUtil.noDamagePermission(player, entity) || entity.isDead) {
                        continue
                    }

                    entity.damage(2.0)
                    entity.world.playSound(entity.location, Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f)
                    player.heal(2.0)
                    Particles.line(player.boundingBox.center.toLocation(player.world), entity.boundingBox.center.toLocation(entity.world), 0.2, particleDisplay)
                }
            }
        }.runTaskTimer(instance(), 0, 20)
    }

    private fun valiantExplodeSpell(player: Player, target: LivingEntity) {
        if (AbilityUtil.noDamagePermission(player, target)) return
        cooldowns.add(MagicItemCooldown(player.uniqueId, Spell.VALIANT_EXPLODE, System.currentTimeMillis()))
        val particleDisplay = ParticleDisplay.of(Particle.DUST).withColor(Util.getRandomColor()).withLocation(target.location)


        AbilityUtil.damageOverTicks(target, player, target.health / 3, 4, {
            target.velocity = vector0
            target.world.playSound(target.location, Sound.ITEM_TOTEM_USE, 1.0f, 7f)
        }, {
            if (!AbilityUtil.noBuildPermission(player, target.location.block)) {
                target.world.spawnParticle(Particle.FLAME, target.location, 25, 0.5, 0.5, 0.5, 0.5)
                target.world.spawnParticle(Particle.EXPLOSION, target.location, 1, 0.0, 0.0, 0.0, 0.0)
                target.world.createExplosion(target.location, 20f, true, false, player)
            }
        })
        Particles.meguminExplosion(instance(), 5.0, particleDisplay)
        target.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 40, 50, false, false, false))
    }
}