package dev.jsinco.luma.lumaitems.items.magical

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.manager.CustomItem
import dev.jsinco.luma.lumaitems.obj.QuickTasks
import dev.jsinco.luma.lumaitems.util.AbilityUtil
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import dev.jsinco.luma.lumaitems.util.Util
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Ageable
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.UUID

class SummertideShellItem : CustomItem {

    private enum class AbilityType(val fName: String) {
        SPEED("&#2CD7ABFastBreak"),
        SPELL("&#F67A67Pacifier"),
        NOURISH("&#83DA56Nourish")
    }

    companion object {
        const val ID = "summertideshell"
    }

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#00B9AA&lS&#00C3B9&lu&#01CCC7&lm&#01D6D6&lm&#2CD7AB&le&#57D880&lr&#83DA56&lt&#AEDB2B&li&#D9DC00&ld&#E2BF1E&le &#F5865B&lS&#F67A67&lh&#F66E73&le&#F7627F&ll&#F7568B&ll",
            mutableListOf("&#01d6d6Acultramarine"),
            mutableListOf("&#2CD7ABFastBreak &7- &fRight-click", "to gain a temporary speed", "boost.", "",
                "&#F67A67Pacifier &7- &fCast a spell which", "turns any age-able into a baby", "and prevents it from growing up.", "",
                "&#83DA56Nourish &7- &fCast a spell which", "heals the target to full health", "and saturation.", "",
                "&cCooldown: 15s"),
            Material.NAUTILUS_SHELL,
            mutableListOf(ID),
            mutableMapOf(Enchantment.KNOCKBACK to 3, Enchantment.SHARPNESS to 4, Enchantment.UNBREAKING to 6)
        )
        item.tier = "&#F34848&lS&#E36643&lo&#D3843E&ll&#C3A239&ls&#B3C034&lt&#A3DE2F&li&#93FC2A&lc&#7DE548&le&#66CD66&l &#50B684&l2&#399EA1&l0&#2387BF&l2&#0C6FDD&l4"
        item.stringPersistentDatas[NamespacedKey(instance(), "ability-type")] = AbilityType.SPEED.name
        return Pair(ID, item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.RIGHT_CLICK -> {
                event as PlayerInteractEvent
                val activeAbilityType: AbilityType = event.item?.itemMeta?.persistentDataContainer?.get(NamespacedKey(
                    instance(), "ability-type"), PersistentDataType.STRING)?.uppercase()?.let {
                    try {
                        AbilityType.valueOf(it)
                    } catch (e: IllegalArgumentException) {
                        AbilityType.SPEED
                    }
                } ?: return false

                if (player.isSneaking) {
                    val meta = event.item?.itemMeta ?: return false
                    val newAbilityType = if (AbilityType.entries.indexOf(activeAbilityType) == AbilityType.entries.size - 1) {
                        AbilityType.entries[0]
                    } else {
                        AbilityType.entries[AbilityType.entries.indexOf(activeAbilityType) + 1]
                    }
                    meta.persistentDataContainer.set(NamespacedKey(instance(), "ability-type"), PersistentDataType.STRING, newAbilityType.name)
                    event.item?.itemMeta = meta

                    player.sendMessage(Util.colorcode("${Util.prefix} Changed to ${newAbilityType.fName} &#E2E2E2spell."))
                } else {
                    when (activeAbilityType) {
                        AbilityType.SPEED -> speedAbility(player)
                        AbilityType.SPELL -> spellAbility(player)
                        AbilityType.NOURISH -> nourishAbility(player)
                    }
                }
            }

            Action.PROJECTILE_LAND -> {
                event as ProjectileHitEvent
                val snowball = event.entity as? Snowball ?: return false
                val entity = event.hitEntity as? LivingEntity ?: return false
                if (AbilityUtil.noDamagePermission(player, entity)) return false

                if (snowball.hasMetadata("spell-ability")) {

                    spellAbilityLand(player, entity)
                } else if (snowball.hasMetadata("nourish-ability")) {
                    nourishAbilityLand(player, entity)
                }
            }

            else -> return false
        }
        return true
    }


    private fun speedAbility(player: Player) {
        if (QuickTasks.isOnCooldown(this, player.uniqueId)) {
            MiniMessageUtil.msg(player, "You are on cooldown for this item.")
            return
        }
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 200, 4, false, false, false))
    }

    private fun spellAbility(player: Player) {
        if (QuickTasks.isOnCooldown(this, player.uniqueId)) {
            MiniMessageUtil.msg(player, "You are on cooldown for this item.")
            return
        }
        AbilityUtil.spawnSpell(player, Particle.ENTITY_EFFECT, ID, 120L).setMetadata("spell-ability", FixedMetadataValue(instance(), true))
    }

    private fun spellAbilityLand(player: Player, entity: LivingEntity) {
        if (entity is Ageable) {
            entity.setBaby()
            entity.ageLock = true
            QuickTasks.addCooldown(this, player.uniqueId, 250)
        }
    }

    private fun nourishAbility(player: Player) {
        if (QuickTasks.isOnCooldown(this, player.uniqueId)) {
            MiniMessageUtil.msg(player, "You are on cooldown for this item.")
            return
        }
        AbilityUtil.spawnSpell(player, Particle.CRIMSON_SPORE, ID, 120L).setMetadata("nourish-ability", FixedMetadataValue(instance(), true))
    }

    private fun nourishAbilityLand(player: Player, entity: LivingEntity) {
        entity.health = entity.getAttribute(Attribute.MAX_HEALTH)?.value ?: 1.0
        if (entity is Player) {
            entity.foodLevel = 20
            entity.saturation = 20.0F
        }
        QuickTasks.addCooldown(this, player.uniqueId, 250)
    }
}