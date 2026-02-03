package dev.lumas.lumaitems.items.weapons.bow

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.EnumWrappers
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.hooks.ProtocolLibHook
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.manager.GlowManager
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.util.Executors
import dev.lumas.lumaitems.util.Executors.sync
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.isItemInSlots
import dev.lumas.lumaitems.util.tiers.Tier
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.ExperienceOrb
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType


class VigilanceDriverItem : CustomItemFunctions() {

    companion object {
        private val referencedEntities: ConcurrentHashMap<UUID, List<LivingEntity>> = ConcurrentHashMap()
        private val KEY = Util.namespacedKey("vigilance-driver")
        private val BLINDNESS = PotionEffect(PotionEffectType.BLINDNESS, 300, 0, true, false, false)
        private val COLORS by lazy { GlowManager.PACKET_COLORS.filter { it != EnumWrappers.ChatFormatting.WHITE } }
        private const val RANGE = 80.0


        init {
            Registry.HOOKS.getOrThrow(ProtocolLibHook::class).getProtocolManager()?.addPacketListener(
                object: PacketAdapter(LumaItems.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Server.NAMED_SOUND_EFFECT, PacketType.Play.Server.ENTITY_SOUND) {
                    override fun onPacketSending(event: PacketEvent) {
                        if (Util.isItemInSlot(KEY, EquipmentSlot.HAND, event.player)) {
                            event.isCancelled = true
                        }
                    }
                }
            )
        }
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#2c272c:#842e5e:#bc6997:#392739>Vigilance Driver</gradient></b>")
            .customEnchants( "<#3B1C3B>Passthrough", "<#3B1C3B>Paranoia")
            .material(Material.CROSSBOW)
            .persistentData(KEY)
            .tier(Tier.HALLOWEEN_2025)
            .vanillaEnchants(
                Enchantment.UNBREAKING to 4,
                Enchantment.QUICK_CHARGE to 3,
                Enchantment.PIERCING to 5,
                Enchantment.MENDING to 1
            )
            .lore(
                "Arrows fired from this",
                "bow pass through blocks.",
                "",
                "While held, nearby and",
                "far-away entities will",
                "have their silhouettes",
                "revealed to you at the",
                "cost of reduced sight,",
                "movement, and hearing.",
                // tODO: Lore
            )
            .buildPair()
    }


    override fun asyncGlobalTask() {
        referencedEntities.forEach { (playerUUID, entities) ->
            val player = Bukkit.getPlayer(playerUUID) ?: run { referencedEntities.remove(playerUUID); return@forEach }
            if (!player.isItemInSlots(KEY, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND)) {
                player.sync {
                    this.removePlayer(player, entities)
                }
            }
        }
    }

    override fun onPluginDisableGlobal() {
        Bukkit.getOnlinePlayers().forEach { player ->
            this.removePlayer(player, referencedEntities[player.uniqueId] ?: listOf())
        }
    }

    override fun onRunnable(player: Player) {
        this.effectPlayer(player)
    }

    override fun onProjectileLaunch(player: Player, event: ProjectileLaunchEvent) {
        val arrow = event.entity
        val hit = player.rayTraceEntities(RANGE.plus(10).toInt(), true)?.hitEntity as? LivingEntity
        var count = 0

        arrow.setNoPhysics(true)
        arrow.velocity = arrow.velocity.multiply(1.1)
        hit?.damage(11.0, player)



        Executors.asyncTimer(0, 2) {
            if (arrow.location.distanceSquared(player.location) >= RANGE * RANGE || arrow.isDead || ++count > 5) {
                arrow.sync { arrow.remove() }
                it.cancel()
            }
        }
    }

    override fun onPlayerItemHeld(player: Player, event: PlayerItemHeldEvent) {
        val item = player.inventory.getItem(event.newSlot)

        if (item == null || !Util.hasPersistentKey(item, KEY)) {
            this.removePlayer(player, referencedEntities[player.uniqueId] ?: listOf())
        } else {
            this.effectPlayer(player)
        }
    }

    override fun onPlayerSwapHands(player: Player, event: PlayerSwapHandItemsEvent) {
        if (!Util.hasPersistentKey(event.mainHandItem, KEY)) {
            this.removePlayer(player, referencedEntities[player.uniqueId] ?: listOf())
        } else {
            this.effectPlayer(player)
        }
    }

    override fun onEntityDeath(player: Player, event: EntityDeathEvent) {
        val newDrops = event.drops.toList()
        val newExp = 0
        event.drops.clear()
        event.droppedExp = 0
        newDrops.forEach { drop ->
            player.world.dropItemNaturally(player.eyeLocation, drop)
        }
        player.world.spawn(player.location, ExperienceOrb::class.java) { orb ->
            orb.experience = newExp
        }
    }




    private fun removePlayer(player: Player, entities: List<LivingEntity>) {

        player.removePotionEffect(PotionEffectType.BLINDNESS)

        // Remove glow from all entities
        for (entity in entities) {
            if (entity !is Player) {
                GlowManager.removeProtocolTeam(player, entity)
            }

            if (!entity.isGlowing && !entity.hasPotionEffect(PotionEffectType.GLOWING)) {
                GlowManager.setProtocolGlowPacket(player, entity, false)
            }

        }
        referencedEntities.remove(player.uniqueId)
    }

    private fun effectPlayer(player: Player) {
        player.addPotionEffect(BLINDNESS)
        player.location.getNearbyLivingEntities(RANGE).forEach { entity ->
            if (entity == player) return@forEach
            val entities = referencedEntities.getOrDefault(player.uniqueId, listOf())
            if (entities.contains(entity)) {
                return@forEach
            }

            referencedEntities[player.uniqueId] = entities + entity


            if (entity !is Player) {
                GlowManager.setProtocolTeamColor(player, entity, COLORS.random())
            }
            if (!entity.isGlowing && !entity.hasPotionEffect(PotionEffectType.GLOWING)) {
                GlowManager.setProtocolGlowPacket(player, entity, true)
            }
        }
    }
}