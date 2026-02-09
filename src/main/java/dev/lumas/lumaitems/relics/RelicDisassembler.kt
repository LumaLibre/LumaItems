package dev.lumas.lumaitems.relics

import dev.lumas.lumacore.utility.Logging
import dev.lumas.lumaitems.configuration.files.RelicsYml
import dev.lumas.lumaitems.enums.Rarity
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.Executors
import dev.lumas.lumaitems.util.extensions.sendFormatted
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import java.util.UUID
import kotlin.random.Random
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType


object RelicDisassembler {

    val RELIC_RARITY_KEY = Util.namespacedKey("relic-rarity")
    val DISASSEMBLER_BLOCKS: MutableList<Block> = mutableListOf()

    private val confirmCooldownTasks: MutableMap<UUID, ScheduledTask> = mutableMapOf()

    @JvmStatic
    fun setupDisassemblerBlocks() {
        Registry.CONFIGS.getOrThrow(RelicsYml::class).disassembler.blocks.entries
            .forEach {
                val key = it.key
                val location = it.value
                try {
                    if (location.world == null) {
                        Logging.warningLog("World for disassembler block $key is null!")
                        return@forEach
                    }
                } catch (ignored: IllegalArgumentException) {
                    Logging.warningLog("World for disassembler block $key is unloaded!")
                    return@forEach
                }

                DISASSEMBLER_BLOCKS.add(location.toCenterLocation().block)
            }
    }

    fun getCommandToExecute(itemStack: ItemStack, player: Player): String? {
        val rarity = Rarity.valueOf(
            itemStack.itemMeta?.persistentDataContainer?.get(
                RELIC_RARITY_KEY,
                PersistentDataType.STRING
            ) ?: return null
        )

        val commands: MutableMap<Int, String> = Registry.CONFIGS.getOrThrow(RelicsYml::class).disassembler.commands.toMutableMap()

        if (rarity == Rarity.ASTRAL) {
            commands[100] = "lumaitems relic %player% core astral"
        }

        var selectedCommand = commands.entries.random()
        while (selectedCommand.key < Random.nextInt(101)) {
            selectedCommand = commands.entries.random()
        }
        return selectedCommand.value.replace("%player%", player.name)
    }

    // returns a command to be executed
    fun getCommandToExecute(itemStack: ItemStack, action: Action, player: Player): String? {
        val rarity = Rarity.valueOf(
            itemStack.itemMeta?.persistentDataContainer?.get(
                RELIC_RARITY_KEY,
                PersistentDataType.STRING
            ) ?: return null
        )
        if (!rescheduleCooldownTask(player)) {
            return null
        } else if (rarity == Rarity.ASTRAL && action.isLeftClick) {
            player.sendFormatted("You must left click to disassemble <#F7FFC9>Astral</#F7FFC9> relics")
            return null
        }

        return getCommandToExecute(itemStack, player)
    }

    // TODO: Rewrite
    private fun rescheduleCooldownTask(player: Player): Boolean {
        var returnValue = false
        if (confirmCooldownTasks.contains(player.uniqueId)) {
            confirmCooldownTasks[player.uniqueId]?.cancel()
            returnValue = true
        } else {
            player.sendFormatted("Are you sure you want to disassemble this item? Click again to confirm.")
        }

        confirmCooldownTasks[player.uniqueId] = Executors.asyncDelayed(200) {
            confirmCooldownTasks.remove(player.uniqueId)
        }

        return returnValue
    }
}