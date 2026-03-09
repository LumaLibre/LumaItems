package dev.lumas.lumaitems.items.misc.jobs

import dev.lumas.lumaitems.configuration.files.JobsBoostersYml
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.model.PersistentDataRecord
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.util.extensions.Executors
import dev.lumas.lumaitems.util.extensions.QuickTasks
import dev.lumas.lumaitems.util.extensions.asEnum
import dev.lumas.lumaitems.util.extensions.getPersistentKey
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.sendFormatted
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class JobsBoosterItem : CustomItemFunctions() {

    companion object {
        private val KEY = "jobs-boost".namespacedKey()
        private val TYPE_KEY = "jobs-boost-type".namespacedKey()
        private val DURATION_KEY = "jobs-boost-duration".namespacedKey()
        private val VALUE_KEY = "jobs-boost-value".namespacedKey()

        private val DEFAULT_BOOST_TYPE = BoostType.EXP
        private const val DEFAULT_BOOST_DURATION = "4h"
        private const val DEFAULT_BOOST_VALUE = 0.25
    }

    enum class BoostType(val display: String, val material: Material, val commands: () -> List<String>) {
        EXP("Jobs Exp Booster", Material.EXPERIENCE_BOTTLE,{
            Registry.CONFIGS.getOrThrow(JobsBoostersYml::class).expBoostCommands
        }),
        MONEY("Jobs Money Booster", Material.AMETHYST_SHARD, {
            Registry.CONFIGS.getOrThrow(JobsBoostersYml::class).moneyBoostCommands
        }),
        EXP_AND_MONEY("Jobs Money & Exp Booster", Material.DIAMOND, {
            Registry.CONFIGS.getOrThrow(JobsBoostersYml::class).expAndMoneyBoostCommands
        })
    }


    override fun createItem(): Pair<String, ItemStack> {
        return KEY.key to createBooster(DEFAULT_BOOST_TYPE, DEFAULT_BOOST_DURATION, DEFAULT_BOOST_VALUE)
    }

    fun createBooster(type: BoostType, duration: String, value: Double): ItemStack {
        return ItemFactory.builder()
            .name(friendlyName(type, duration, value))
            .lore("<gray>Right-click to redeem!")
            .persistentData(KEY)
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .hideEnchants(true)
            .addSpace(false)
            .material(Material.AMETHYST_SHARD)
            .tier(Tier.BLANK)
            .persistentDataRecords(
                PersistentDataRecord.create(TYPE_KEY, PersistentDataType.STRING, type.name),
                PersistentDataRecord.create(DURATION_KEY, PersistentDataType.STRING, duration),
                PersistentDataRecord.create(VALUE_KEY, PersistentDataType.DOUBLE, value)
            )
            .buildItem()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (QuickTasks.getActiveCooldowns(this) > 0) {
            player.sendFormatted("This item is on cooldown.")
            return
        }
        // TODO: global cooldown
        val item = event.item ?: return

        val type = item.getPersistentKey(TYPE_KEY, PersistentDataType.STRING)?.asEnum(BoostType::class.java) ?: return

        val duration = item.getPersistentKey(DURATION_KEY, PersistentDataType.STRING) ?: DEFAULT_BOOST_DURATION
        val value = item.getPersistentKey(VALUE_KEY, PersistentDataType.DOUBLE) ?: DEFAULT_BOOST_VALUE


        event.isCancelled = true
        item.amount -= 1

        Executors.global {
            for (command in type.commands()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.placeholders(player, type, duration, value))
            }
        }
    }


    private fun doubleToMultiplier(value: Double, usePercent: Boolean = false): String {
        return if (value < 1 || usePercent) {
            "${(value * 100).toInt()}%"
        } else {
            "${(value + 1).toInt()}x"
        }
    }

    private fun friendlyName(type: BoostType, duration: String, value: Double): String {
        return "<#f498f6><b>${doubleToMultiplier(value)} ${type.display}</b> <#F7FFC9>(${duration})"
    }

    private fun String.placeholders(player: Player, type: BoostType, duration: String, value: Double): String {
        return this.replace("{player}", player.name)
            .replace("{item}", friendlyName(type, duration, value))
            .replace("{duration}", duration)
            .replace("{value}", value.toString())
    }
}