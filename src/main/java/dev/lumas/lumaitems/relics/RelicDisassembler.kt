package dev.lumas.lumaitems.relics

import dev.lumas.lumacore.utility.Logging
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.enums.Rarity
import dev.lumas.lumaitems.manager.FileManager
import dev.lumas.lumaitems.util.Util
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*
import kotlin.random.Random

object RelicDisassembler {
    val disassemblerBlocks: MutableList<Block> = mutableListOf()
    private val file = FileManager("relics.yml").generateYamlFile()

    private val confirmCooldownTasks: MutableMap<UUID, Int> = mutableMapOf()
    private val plugin: LumaItems = LumaItems.getInstance()

    @JvmStatic fun setupDisassemblerBlocks() {
        if (file.getConfigurationSection("disassembler.blocks") == null) {
            plugin.logger.warning("disassembler.blocks config section is null!")
            return
        }
        for (key in file.getConfigurationSection("disassembler.blocks")?.getKeys(false) ?: return) {
            val world = Bukkit.getWorld(file.getString("disassembler.blocks.$key.world") ?: continue)
            if (world == null) {
                Logging.warningLog("World for disassembler block $key is null!")
                continue
            }
            val loc = Location(
                world,
                file.getDouble("disassembler.blocks.$key.x"),
                file.getDouble("disassembler.blocks.$key.y"),
                file.getDouble("disassembler.blocks.$key.z")
            ).toCenterLocation()

            disassemblerBlocks.add(loc.block)
        }
    }

    fun getCommandToExecute(itemStack: ItemStack, player: Player): String? {
        val rarity = Rarity.valueOf(
            itemStack.itemMeta?.persistentDataContainer?.get(
                NamespacedKey(plugin, "relic-rarity"),
                PersistentDataType.STRING
            ) ?: return null
        )

        val commands: MutableMap<String, Int> = mutableMapOf()
        val configSec = file.getConfigurationSection("disassembler.commands")?.getKeys(false) ?: return null
        for (key in configSec) {
            val chance = Integer.parseInt(key)
            if (chance == 0) continue
            commands[file.getString("disassembler.commands.$key") ?: "non"] = chance
        }
        if (rarity == Rarity.ASTRAL) {
            commands["lumaitems relic %player% core astral"] = 100
        }

        var selectedCommand = commands.keys.random()
        while (commands[selectedCommand]!! < Random.nextInt(100)) {
            selectedCommand = commands.keys.random()
        }
        return selectedCommand.replace("%player%", player.name)
    }

    // returns a command to be executed
    fun getCommandToExecute(itemStack: ItemStack, action: Action, player: Player): String? {
        val rarity = Rarity.valueOf(
            itemStack.itemMeta?.persistentDataContainer?.get(
                NamespacedKey(plugin, "relic-rarity"),
                PersistentDataType.STRING
            ) ?: return null
        )
        if (!rescheduleCooldownTask(player)) {
            return null
        } else if (rarity == Rarity.ASTRAL && action.isLeftClick) {
            player.sendMessage(Util.colorcode("${Util.legacyPrefix} You must left click to disassemble &#F7FFC9Astral &#E2E2E2Relics"))
            return null
        }

        val commands: MutableMap<String, Int> = mutableMapOf()
        val configSec = file.getConfigurationSection("disassembler.commands")?.getKeys(false) ?: return null
        for (key in configSec) {
            val chance = Integer.parseInt(key)
            if (chance == 0) continue
            commands[file.getString("disassembler.commands.$key") ?: "non"] = chance
        }
        if (rarity == Rarity.ASTRAL) {
            commands["lumaitems relic %player% core astral"] = 100
        }

        var selectedCommand = commands.keys.random()
        while (commands[selectedCommand]!! < Random.nextInt(100)) {
            selectedCommand = commands.keys.random()
        }
        return selectedCommand.replace("%player%", player.name)
    }

    private fun rescheduleCooldownTask(player: Player): Boolean {
        var returnValue = false
        if (confirmCooldownTasks.contains(player.uniqueId)) {
            Bukkit.getScheduler().cancelTask(confirmCooldownTasks[player.uniqueId]!!)
            returnValue = true
        } else {
            player.sendMessage("${Util.legacyPrefix} Are you sure you want to disassemble this item? Click again to confirm.")
        }

        confirmCooldownTasks[player.uniqueId] = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, {
            confirmCooldownTasks.remove(player.uniqueId)
        }, 200L)
        return returnValue
    }
}