package dev.lumas.lumaitems.commands.subcommands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import dev.lumas.core.annotation.Argument
import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.BrigadierExecutor
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.annotation.Suggests
import dev.lumas.core.model.brigadier.BrigadierSubCommand
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.model.item.AttributeContainer
import dev.lumas.lumaitems.util.extensions.send
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlotGroup
import java.util.concurrent.CompletableFuture

@Register(Autowire.BRIGADIER)
@CommandMeta(
    name = "addattributemod",
    description = "Add an attribute modifier to the held item",
    usage = "/<command> addattributemod <attribute> <operation> <amount> [slot]",
    permission = "lumaitems.command.addattributemod",
    playerOnly = true,
    parent = CommandManager::class
)
class AddAttributeCommand : BrigadierSubCommand {

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

    @BrigadierExecutor
    fun run(
        src: CommandSourceStack,
        @Argument("attribute") attributeName: String,
        @Argument("operation") operationName: String,
        @Argument("amount") amount: Double,
        @Argument(value = "slot", optional = true) slotName: String?
    ) {
        val player = src.sender as Player
        val item = player.inventory.itemInMainHand

        if (item.type.isAir) {
            player.send("You must be holding an item!")
            return
        }

        val lookupKey = NamespacedKey.fromString(attributeName.lowercase()) ?: NamespacedKey.minecraft(attributeName.lowercase())
        val attribute = Registry.ATTRIBUTE.get(lookupKey) ?: run {
            player.send("Unknown attribute: $attributeName")
            return
        }

        val operation = AttributeModifier.Operation.entries.find { it.name.equals(operationName, ignoreCase = true) } ?: run {
            player.send("Unknown operation: $operationName")
            return
        }

        val slot = if (slotName != null) {
            SLOT_GROUPS[slotName.lowercase()] ?: run {
                player.send("Unknown slot: $slotName")
                return
            }
        } else {
            EquipmentSlotGroup.ANY
        }

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

        player.send("Added ${attribute.key.key} modifier: $amount (${operation.name.lowercase()}) on slot $slot")
    }

    @Suggests("attribute")
    fun suggestAttribute(ctx: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val partial = builder.remaining.lowercase()
        Registry.ATTRIBUTE.asSequence()
            .map { it.key.key }
            .filter { it.lowercase().startsWith(partial) }
            .forEach(builder::suggest)
        return builder.buildFuture()
    }

    @Suggests("operation")
    fun suggestOperation(ctx: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val partial = builder.remaining.lowercase()
        AttributeModifier.Operation.entries.asSequence()
            .map { it.name.lowercase() }
            .filter { it.startsWith(partial) }
            .forEach(builder::suggest)
        return builder.buildFuture()
    }

    @Suggests("slot")
    fun suggestSlot(ctx: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val partial = builder.remaining.lowercase()
        SLOT_GROUPS.keys.asSequence()
            .filter { it.startsWith(partial) }
            .forEach(builder::suggest)
        return builder.buildFuture()
    }
}