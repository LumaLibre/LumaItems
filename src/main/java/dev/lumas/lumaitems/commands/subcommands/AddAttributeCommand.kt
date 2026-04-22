package dev.lumas.lumaitems.commands.subcommands

import dev.lumas.lumacore.manager.commands.CommandInfo
import dev.lumas.lumacore.manager.modules.AutoRegister
import dev.lumas.lumacore.manager.modules.RegisterType
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.commands.SubCommand
import dev.lumas.lumaitems.model.item.AttributeContainer
import dev.lumas.lumaitems.util.extensions.send
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.attribute.AttributeModifier
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlotGroup

@AutoRegister(RegisterType.SUBCOMMAND)
@CommandInfo(
    name = "addattributemod",
    description = "Add an attribute modifier to the held item",
    usage = "/<command> addattributemod <attribute> <operation> <amount> [slot]",
    permission = "lumaitems.command.addattributemod",
    playerOnly = true,
    parent = CommandManager::class
)
class AddAttributeCommand : SubCommand {

    companion object {
        private val SLOT_GROUPS = mapOf(
            "any" to EquipmentSlotGroup.ANY,
            "mainhand" to EquipmentSlotGroup.MAINHAND,
            "offhand" to EquipmentSlotGroup.OFFHAND,
            "hand" to EquipmentSlotGroup.HAND,
            "armor" to EquipmentSlotGroup.ARMOR,
            "head" to EquipmentSlotGroup.HEAD,
            "chest" to EquipmentSlotGroup.CHEST,
            "legs" to EquipmentSlotGroup.LEGS,
            "feet" to EquipmentSlotGroup.FEET,
            "body" to EquipmentSlotGroup.BODY
        )
    }

    override fun execute(plugin: LumaItems, sender: CommandSender, label: String, args: Array<out String>): Boolean {
        val player = sender as Player
        val item = player.inventory.itemInMainHand

        if (item.type.isAir) {
            player.send("You must be holding an item!")
            return false
        }

        if (args.size < 3) return false

        val lookupKey = NamespacedKey.fromString(args[0].lowercase()) ?: NamespacedKey.minecraft(args[0].lowercase())
        val attribute = Registry.ATTRIBUTE.get(lookupKey)
            ?: run { player.send("Unknown attribute: ${args[0]}"); return false }

        val operation = AttributeModifier.Operation.entries.find { it.name.equals(args[1], ignoreCase = true) }
            ?: run { player.send("Unknown operation: ${args[1]}"); return false }

        val amount = args[2].toDoubleOrNull()
            ?: run { player.send("Invalid amount: ${args[2]}"); return false }

        val slot = args.getOrNull(3)?.let {
            SLOT_GROUPS[it.lowercase()] ?: run { player.send("Unknown slot: $it"); return false }
        } ?: EquipmentSlotGroup.ANY

        val container = AttributeContainer.of(
            AttributeContainer.generateStringKey(8),
            attribute,
            operation,
            amount,
            slot
        )

        item.editMeta { meta ->
            meta.addAttributeModifier(attribute, container.modifier())
        }

        player.send("Added ${attribute.key.key} modifier: $amount (${operation.name.lowercase()}) on slot ${slot})")
        return true
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String?>? {
        return when (args.size) {
            1 -> Registry.ATTRIBUTE.map { it.key.key }
                .filter { it.startsWith(args[0], ignoreCase = true) }
            2 -> AttributeModifier.Operation.entries.map { it.name.lowercase() }
                .filter { it.startsWith(args[1], ignoreCase = true) }
            3 -> listOf("<amount>")
            4 -> SLOT_GROUPS.keys.filter { it.startsWith(args[3], ignoreCase = true) }
            else -> null
        }
    }
}
