package dev.lumas.lumaitems.items.misc

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.util.Executors
import dev.lumas.lumaitems.util.MiniMessageUtil.mm
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.tiers.Tier
import java.util.UUID
import org.bukkit.Material
import org.bukkit.World.Environment
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.Action as BukkitAction
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.FireworkMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class WindforgedRocketItem : CustomItemFunctions() {

    companion object {
        private const val CUSTOM_ITEM_KEY = "windforged-rocket"
        private const val MAX_USES = 3
        private const val WINDOW_MS = 30_000L
    }

    private val key = Util.namespacedKey(CUSTOM_ITEM_KEY)
    private val uuid = Util.namespacedKey("uuid")
    private val usageTimeStamps = mutableMapOf<UUID, MutableList<Long>>()

    private data class CooldownInfo (
        val canUse: Boolean,
        val remainingMs: Long
    )

    private fun createWindforgedRocket(): ItemStack {
        val windforgedRocket = ItemFactory.builder()
            .name("<b><gradient:#E90000:#E90000>Wind</gradient><gradient:#E90000:#FFFFFF>forged Roc</gradient><gradient:#FFFFFF:#FFFFFF>ket</gradient></b>")
            .customEnchants("<#D42424>Boundless Boost")
            .lore(
                "Forged from Zephyr's breath",
                "Soars on western skies",
                "But wails in the <red>Nether</red>",
                "",
                "Must rest after <red>3 surges</red>"
            )
            .material(Material.FIREWORK_ROCKET)
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .tier(Tier.CHRISTMAS_2025)
            .persistentData(CUSTOM_ITEM_KEY)
            .build()
            .createItem()

        val meta = windforgedRocket.itemMeta as FireworkMeta
        meta.power = 1
        meta.persistentDataContainer.set(key, PersistentDataType.BYTE, 1)
        meta.persistentDataContainer.set(uuid, PersistentDataType.STRING, UUID.randomUUID().toString())
        windforgedRocket.itemMeta = meta

        return windforgedRocket
    }

    override fun createItem(): Pair<String, ItemStack> {
        return Pair(CUSTOM_ITEM_KEY, createWindforgedRocket())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        val item = getItem(event) ?: return false
        val slot = getSlot(event) ?: return false

        if (!isWindforgedRocket(item)) return false

        if (!isRightClick(event)) return false

        val cooldown = checkCooldown(player)
        if (!cooldown.canUse) {
            val timer = (cooldown.remainingMs/1000).coerceAtLeast(0)
            player.sendActionBar(mm("<gradient:#E90000:#FFFFFF>The eternal wind resists... ${timer}s before it can surge again</gradient>"))
            cancelElytraEvents(player, event)
            scheduleRestoreIfConsumed(player, slot)

            return false
        }

        applyHinderances(player, event)
        checkCooldown(player, true)
        scheduleRestoreIfConsumed(player, slot)

        return true
    }

    private fun isRightClick(event: Any): Boolean = when (event) {
        is PlayerInteractEvent -> {
            val validActions = event.action in listOf(BukkitAction.RIGHT_CLICK_AIR, BukkitAction.RIGHT_CLICK_BLOCK)
            val isShelf = event.clickedBlock?.type?.name?.endsWith("_SHELF") == true

            validActions && !isShelf
        }
        else -> true
    }

    private fun getItem(event: Any): ItemStack? = when (event) {
        is PlayerInteractEvent -> event.item
        is PlayerElytraBoostEvent -> event.itemStack
        else -> null
    }

    private fun getSlot(event: Any): EquipmentSlot? = when(event) {
        is PlayerInteractEvent -> event.hand
        is PlayerElytraBoostEvent -> event.hand
        else -> null
    }

    private fun cancelElytraEvents(player: Player, event: Any) {
        when(event) {
            is PlayerElytraBoostEvent -> {
                event.isCancelled = true
                event.setShouldConsume(false)
                player.isGliding = false
                player.velocity = player.velocity.multiply(0.1)
            }

            is PlayerInteractEvent -> {
                event.setUseItemInHand(Event.Result.DENY)
            }
        }
    }

    private fun applyHinderances(player:Player, event:Any) {
        // Hinderance #1
        if (player.world.environment == Environment.NETHER) {
            applyHinderance(player, "<gradient:#E90000:#FFFFFF>Unholy air smothers the Windforged Rocket</gradient>", PotionEffectType.POISON)
            cancelElytraEvents(player, event)
        }

        /*
        // Hinderance #2
        if (player.world.hasStorm()) {
            applyHinderance(player, "<gradient:#E90000:#FFFFFF>Zephyr's breath chokes in the rain</gradient>", PotionEffectType.WEAKNESS)
            cancelElytraEvents(player, event)
        }

        // Hinderance #3
        if (player.location.y > 200 && ThreadLocalRandom.current().nextInt(100) < 10) {
            applyHinderance(player, "<gradient:#E90000:#FFFFFF>Zephyr's breath thins at high altitudes</gradient>", PotionEffectType.SLOW_FALLING)
            cancelElytraEvents(player, event)
        }
        */
    }

    private fun applyHinderance(player: Player, message: String, effect: PotionEffectType, duration: Int = 120, amplifier: Int = 0) {
        player.sendActionBar(mm(message))
        player.addPotionEffect(PotionEffect(effect, duration, amplifier))
    }

    private fun isWindforgedRocket(item: ItemStack): Boolean {
        if(item.type != Material.FIREWORK_ROCKET) return false
        val meta = item.itemMeta ?: return false
        return meta.persistentDataContainer.has(key, PersistentDataType.BYTE)
    }

    private fun scheduleRestoreIfConsumed(player: Player, slot: EquipmentSlot) {
        Executors.syncDelayed(1) {
            if(player.inventory.getItem(slot).isEmpty)
                player.inventory.setItem(slot, createWindforgedRocket())
        }
    }

    private fun checkCooldown(player: Player, recordUsage: Boolean = false): CooldownInfo {
        val now = System.currentTimeMillis()
        val list = usageTimeStamps.getOrPut(player.uniqueId) { mutableListOf() }

        list.removeIf { now - it >= WINDOW_MS}

        return if (list.size < MAX_USES) {
            if (recordUsage) list.add(now)
            CooldownInfo(canUse = true, remainingMs = 0)
        } else {
            val remaining = WINDOW_MS - (now - (list.minOrNull() ?: now))
            CooldownInfo(canUse = false, remainingMs = remaining.coerceAtLeast(0))
        }
    }
}