package dev.lumas.lumaitems.commands.subcommands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.model.brigadier.BrigadierSubCommand
import dev.lumas.lumaitems.commands.CommandManager
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.Locale

@Register(Autowire.BRIGADIER)
@CommandMeta(
    name = "edit",
    aliases = ["editor", "itemedit"],
    description = "Edit the name, lore, enchantments, and flags of the held item",
    usage = "/<command> edit <name|lore|enchant|flag|inspect>",
    permission = "lumaitems.command.edit",
    parent = CommandManager::class,
    playerOnly = true
)
class ItemEditorCommand : BrigadierSubCommand {

    companion object {
        private val MINI_MESSAGE = MiniMessage.miniMessage() //MiniMessage.builder().strict(true).build()
        private val DISPLAY_MINI_MESSAGE = MiniMessage.miniMessage()
        private val ENCHANTMENTS by lazy {
            RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
        }

        private val ITEM_FLAGS = ItemFlag.entries.associateBy { it.name.lowercase(Locale.ROOT) }
    }

    override fun buildTree(
        builder: LiteralArgumentBuilder<CommandSourceStack>,
        commands: Commands
    ): LiteralArgumentBuilder<CommandSourceStack> {
        return builder
            .executes(::showHelp)
            .then(Commands.literal("inspect").executes(::inspect))
            .then(nameBranch())
            .then(loreBranch())
            .then(enchantmentBranch())
            .then(flagBranch())
    }

    private fun nameBranch(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("name")
            .executes(::showName)
            .then(Commands.literal("set")
                .then(Commands.argument("name", StringArgumentType.greedyString())
                    .executes { ctx -> setName(ctx, StringArgumentType.getString(ctx, "name")) }
                )
            )
            .then(Commands.literal("clear").executes(::clearName))
            .then(Commands.literal("reset").executes(::clearName))
    }

    private fun loreBranch(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("lore")
            .executes(::listLore)
            .then(Commands.literal("list").executes(::listLore))
            .then(Commands.literal("add")
                .then(Commands.argument("text", StringArgumentType.greedyString())
                    .executes { ctx -> addLore(ctx, StringArgumentType.getString(ctx, "text")) }
                )
            )
            .then(Commands.literal("insert")
                .then(Commands.argument("line", IntegerArgumentType.integer(1))
                    .then(Commands.argument("text", StringArgumentType.greedyString())
                        .executes { ctx -> insertLore(
                            ctx,
                            IntegerArgumentType.getInteger(ctx, "line"),
                            StringArgumentType.getString(ctx, "text")
                        ) }
                    )
                )
            )
            .then(Commands.literal("set")
                .then(Commands.argument("line", IntegerArgumentType.integer(1))
                    .then(Commands.argument("text", StringArgumentType.greedyString())
                        .executes { ctx -> setLore(
                            ctx,
                            IntegerArgumentType.getInteger(ctx, "line"),
                            StringArgumentType.getString(ctx, "text")
                        ) }
                    )
                )
            )
            .then(Commands.literal("remove")
                .then(Commands.argument("line", IntegerArgumentType.integer(1))
                    .executes { ctx -> removeLore(ctx, IntegerArgumentType.getInteger(ctx, "line")) }
                )
            )
            .then(Commands.literal("clear").executes(::clearLore))
    }

    private fun enchantmentBranch(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("enchant")
            .executes(::listEnchantments)
            .then(Commands.literal("list").executes(::listEnchantments))
            .then(Commands.literal("add")
                .then(enchantmentArgument()
                    .then(Commands.argument("level", IntegerArgumentType.integer(1, 255))
                        .executes { ctx -> addEnchantment(
                            ctx,
                            StringArgumentType.getString(ctx, "enchantment"),
                            IntegerArgumentType.getInteger(ctx, "level")
                        ) }
                    )
                )
            )
            .then(Commands.literal("remove")
                .then(enchantmentArgument()
                    .executes { ctx -> removeEnchantment(
                        ctx,
                        StringArgumentType.getString(ctx, "enchantment")
                    ) }
                )
            )
            .then(Commands.literal("clear").executes(::clearEnchantments))
    }

    private fun flagBranch(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("flag")
            .executes(::listFlags)
            .then(Commands.literal("list").executes(::listFlags))
            .then(Commands.literal("add")
                .then(flagArgument().executes { ctx -> addFlag(ctx, StringArgumentType.getString(ctx, "flag")) })
            )
            .then(Commands.literal("remove")
                .then(flagArgument().executes { ctx -> removeFlag(ctx, StringArgumentType.getString(ctx, "flag")) })
            )
            .then(Commands.literal("clear").executes(::clearFlags))
    }

    private fun enchantmentArgument() = Commands.argument("enchantment", StringArgumentType.word())
        .suggests { _, suggestions ->
            val partial = suggestions.remaining.lowercase(Locale.ROOT)
            ENCHANTMENTS.asSequence()
                .filter { it.key.namespace == NamespacedKey.MINECRAFT }
                .map { it.key.key }
                .filter { it.startsWith(partial) }
                .sorted()
                .forEach(suggestions::suggest)
            suggestions.buildFuture()
        }

    private fun flagArgument() = Commands.argument("flag", StringArgumentType.word())
        .suggests { _, suggestions ->
            val partial = suggestions.remaining.lowercase(Locale.ROOT)
            ITEM_FLAGS.keys.asSequence()
                .filter { it.startsWith(partial) }
                .sorted()
                .forEach(suggestions::suggest)
            suggestions.buildFuture()
        }

    private fun showHelp(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        player.sendMessage(mm("<gold><b>Held Item Editor</b></gold> <dark_gray>— click a command to use it"))
        player.sendMessage(commandHelp("/lumaitems edit inspect", "Show all editable item data"))
        player.sendMessage(commandHelp("/lumaitems edit name set ", "Set a MiniMessage name"))
        player.sendMessage(commandHelp("/lumaitems edit lore add ", "Append a MiniMessage lore line"))
        player.sendMessage(commandHelp("/lumaitems edit enchant add ", "Add a vanilla enchantment"))
        player.sendMessage(commandHelp("/lumaitems edit flag add ", "Add an item flag"))
        return Command.SINGLE_SUCCESS
    }

    private fun inspect(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        val item = heldItem(player) ?: return 0
        val meta = item.itemMeta

        player.sendMessage(mm(
            "<gold><b>Item Editor</b></gold> <dark_gray>—</dark_gray> <white><type></white>",
            Placeholder.unparsed("type", item.type.key.asString())
        ))
        player.sendMessage(Component.text("Name: ").color(net.kyori.adventure.text.format.NamedTextColor.GRAY)
            .append(meta.displayName() ?: mm("<dark_gray><i>default</i></dark_gray>")))

        val lore = meta.lore().orEmpty()
        player.sendMessage(mm("<gray>Lore:</gray> <white><count> line(s)</white>", Placeholder.unparsed("count", lore.size.toString())))
        lore.forEachIndexed { index, line -> player.sendMessage(numberedLine(index + 1, line)) }

        val enchants = meta.enchants.entries.sortedBy { it.key.key.key }
        player.sendMessage(mm("<gray>Enchantments:</gray> <white><count></white>", Placeholder.unparsed("count", enchants.size.toString())))
        enchants.forEach { (enchantment, level) ->
            player.sendMessage(mm(
                "  <dark_gray>•</dark_gray> <white><enchantment></white> <gray>level <level></gray>",
                Placeholder.unparsed("enchantment", enchantment.key.asString()),
                Placeholder.unparsed("level", level.toString())
            ))
        }

        val flags = meta.itemFlags.sortedBy { it.name }
        player.sendMessage(mm("<gray>Flags:</gray> <white><count></white>", Placeholder.unparsed("count", flags.size.toString())))
        if (flags.isNotEmpty()) {
            player.sendMessage(mm("  <white><flags></white>", Placeholder.unparsed("flags", flags.joinToString(", ") { it.name.lowercase(Locale.ROOT) })))
        }
        return Command.SINGLE_SUCCESS
    }

    private fun showName(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        val item = heldItem(player) ?: return 0
        val name = item.itemMeta.displayName()
        player.sendMessage(Component.text("Name: ").color(net.kyori.adventure.text.format.NamedTextColor.GRAY)
            .append(name ?: mm("<dark_gray><i>default</i></dark_gray>")))
        return Command.SINGLE_SUCCESS
    }

    private fun setName(ctx: CommandContext<CommandSourceStack>, input: String): Int {
        val player = ctx.source.sender as Player
        val item = heldItem(player) ?: return 0
        val name = parseMiniMessage(player, input) ?: return 0
        item.editMeta { it.displayName(name) }
        player.sendMessage(Component.text("Name set to ").color(net.kyori.adventure.text.format.NamedTextColor.GREEN).append(name))
        return Command.SINGLE_SUCCESS
    }

    private fun clearName(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        val item = heldItem(player) ?: return 0
        if (!item.itemMeta.hasDisplayName()) {
            player.sendMessage(mm("<yellow>This item already uses its default name."))
            return 0
        }
        item.editMeta { it.displayName(null) }
        player.sendMessage(mm("<green>Reset the item name."))
        return Command.SINGLE_SUCCESS
    }

    private fun listLore(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        val item = heldItem(player) ?: return 0
        val lore = item.itemMeta.lore().orEmpty()
        if (lore.isEmpty()) {
            player.sendMessage(mm("<yellow>This item has no lore."))
            return Command.SINGLE_SUCCESS
        }
        player.sendMessage(mm("<gold><b>Lore</b></gold> <gray>(<count> lines)</gray>", Placeholder.unparsed("count", lore.size.toString())))
        lore.forEachIndexed { index, line -> player.sendMessage(numberedLine(index + 1, line)) }
        return Command.SINGLE_SUCCESS
    }

    private fun addLore(ctx: CommandContext<CommandSourceStack>, input: String): Int {
        val player = ctx.source.sender as Player
        val item = heldItem(player) ?: return 0
        val line = parseMiniMessage(player, input) ?: return 0
        val lore = item.itemMeta.lore().orEmpty().toMutableList()
        lore.add(line)
        item.editMeta { it.lore(lore) }
        player.sendMessage(mm("<green>Added lore line <line>.</green>", Placeholder.unparsed("line", lore.size.toString())))
        return Command.SINGLE_SUCCESS
    }

    private fun insertLore(ctx: CommandContext<CommandSourceStack>, lineNumber: Int, input: String): Int {
        val player = ctx.source.sender as Player
        val item = heldItem(player) ?: return 0
        val lore = item.itemMeta.lore().orEmpty().toMutableList()
        if (lineNumber > lore.size + 1) return invalidLine(player, lineNumber, lore.size, allowAppend = true)
        val line = parseMiniMessage(player, input) ?: return 0
        lore.add(lineNumber - 1, line)
        item.editMeta { it.lore(lore) }
        player.sendMessage(mm("<green>Inserted lore line <line>.</green>", Placeholder.unparsed("line", lineNumber.toString())))
        return Command.SINGLE_SUCCESS
    }

    private fun setLore(ctx: CommandContext<CommandSourceStack>, lineNumber: Int, input: String): Int {
        val player = ctx.source.sender as Player
        val item = heldItem(player) ?: return 0
        val lore = item.itemMeta.lore().orEmpty().toMutableList()
        if (lineNumber > lore.size) return invalidLine(player, lineNumber, lore.size)
        val line = parseMiniMessage(player, input) ?: return 0
        lore[lineNumber - 1] = line
        item.editMeta { it.lore(lore) }
        player.sendMessage(mm("<green>Updated lore line <line>.</green>", Placeholder.unparsed("line", lineNumber.toString())))
        return Command.SINGLE_SUCCESS
    }

    private fun removeLore(ctx: CommandContext<CommandSourceStack>, lineNumber: Int): Int {
        val player = ctx.source.sender as Player
        val item = heldItem(player) ?: return 0
        val lore = item.itemMeta.lore().orEmpty().toMutableList()
        if (lineNumber > lore.size) return invalidLine(player, lineNumber, lore.size)
        lore.removeAt(lineNumber - 1)
        item.editMeta { it.lore(lore.ifEmpty { null }) }
        player.sendMessage(mm("<green>Removed lore line <line>.</green>", Placeholder.unparsed("line", lineNumber.toString())))
        return Command.SINGLE_SUCCESS
    }

    private fun clearLore(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        val item = heldItem(player) ?: return 0
        if (!item.itemMeta.hasLore()) {
            player.sendMessage(mm("<yellow>This item has no lore."))
            return 0
        }
        item.editMeta { it.lore(null) }
        player.sendMessage(mm("<green>Cleared all lore."))
        return Command.SINGLE_SUCCESS
    }

    private fun addEnchantment(ctx: CommandContext<CommandSourceStack>, input: String, level: Int): Int {
        val player = ctx.source.sender as Player
        val item = heldItem(player) ?: return 0
        val enchantment = findVanillaEnchantment(input)
        if (enchantment == null) {
            player.sendMessage(mm("<red>Unknown vanilla enchantment:</red> <white><value></white>", Placeholder.unparsed("value", input)))
            return 0
        }
        item.editMeta { it.addEnchant(enchantment, level, true) }
        player.sendMessage(mm(
            "<green>Set <enchantment> to level <level>.</green>",
            Placeholder.unparsed("enchantment", enchantment.key.asString()),
            Placeholder.unparsed("level", level.toString())
        ))
        return Command.SINGLE_SUCCESS
    }

    private fun removeEnchantment(ctx: CommandContext<CommandSourceStack>, input: String): Int {
        val player = ctx.source.sender as Player
        val item = heldItem(player) ?: return 0
        val enchantment = findVanillaEnchantment(input)
        if (enchantment == null) {
            player.sendMessage(mm("<red>Unknown vanilla enchantment:</red> <white><value></white>", Placeholder.unparsed("value", input)))
            return 0
        }
        if (!item.containsEnchantment(enchantment)) {
            player.sendMessage(mm("<yellow>The item does not have <enchantment>.</yellow>", Placeholder.unparsed("enchantment", enchantment.key.asString())))
            return 0
        }
        item.editMeta { it.removeEnchant(enchantment) }
        player.sendMessage(mm("<green>Removed <enchantment>.</green>", Placeholder.unparsed("enchantment", enchantment.key.asString())))
        return Command.SINGLE_SUCCESS
    }

    private fun clearEnchantments(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        val item = heldItem(player) ?: return 0
        val enchantments = item.itemMeta.enchants.keys
            .filter { it.key.namespace == NamespacedKey.MINECRAFT }
        if (enchantments.isEmpty()) {
            player.sendMessage(mm("<yellow>This item has no vanilla enchantments."))
            return 0
        }
        item.editMeta { meta -> enchantments.forEach(meta::removeEnchant) }
        player.sendMessage(mm("<green>Removed <count> enchantment(s).</green>", Placeholder.unparsed("count", enchantments.size.toString())))
        return Command.SINGLE_SUCCESS
    }

    private fun listEnchantments(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        val item = heldItem(player) ?: return 0
        val enchantments = item.itemMeta.enchants.entries.sortedBy { it.key.key.key }
        if (enchantments.isEmpty()) {
            player.sendMessage(mm("<yellow>This item has no enchantments."))
            return Command.SINGLE_SUCCESS
        }
        player.sendMessage(mm("<gold><b>Enchantments</b></gold>"))
        enchantments.forEach { (enchantment, level) ->
            player.sendMessage(mm(
                "<dark_gray>•</dark_gray> <white><enchantment></white> <gray>level <level></gray>",
                Placeholder.unparsed("enchantment", enchantment.key.asString()),
                Placeholder.unparsed("level", level.toString())
            ))
        }
        return Command.SINGLE_SUCCESS
    }

    private fun addFlag(ctx: CommandContext<CommandSourceStack>, input: String): Int {
        val player = ctx.source.sender as Player
        val item = heldItem(player) ?: return 0
        val flag = findFlag(player, input) ?: return 0
        if (item.itemMeta.hasItemFlag(flag)) {
            player.sendMessage(mm("<yellow>The item already has <flag>.</yellow>", Placeholder.unparsed("flag", input.lowercase(Locale.ROOT))))
            return 0
        }
        item.editMeta { it.addItemFlags(flag) }
        player.sendMessage(mm("<green>Added <flag>.</green>", Placeholder.unparsed("flag", flag.name.lowercase(Locale.ROOT))))
        return Command.SINGLE_SUCCESS
    }

    private fun removeFlag(ctx: CommandContext<CommandSourceStack>, input: String): Int {
        val player = ctx.source.sender as Player
        val item = heldItem(player) ?: return 0
        val flag = findFlag(player, input) ?: return 0
        if (!item.itemMeta.hasItemFlag(flag)) {
            player.sendMessage(mm("<yellow>The item does not have <flag>.</yellow>", Placeholder.unparsed("flag", input.lowercase(Locale.ROOT))))
            return 0
        }
        item.editMeta { it.removeItemFlags(flag) }
        player.sendMessage(mm("<green>Removed <flag>.</green>", Placeholder.unparsed("flag", flag.name.lowercase(Locale.ROOT))))
        return Command.SINGLE_SUCCESS
    }

    private fun clearFlags(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        val item = heldItem(player) ?: return 0
        val flags = item.itemMeta.itemFlags.toTypedArray()
        if (flags.isEmpty()) {
            player.sendMessage(mm("<yellow>This item has no flags."))
            return 0
        }
        item.editMeta { it.removeItemFlags(*flags) }
        player.sendMessage(mm("<green>Removed <count> item flag(s).</green>", Placeholder.unparsed("count", flags.size.toString())))
        return Command.SINGLE_SUCCESS
    }

    private fun listFlags(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        val item = heldItem(player) ?: return 0
        val flags = item.itemMeta.itemFlags.sortedBy { it.name }
        if (flags.isEmpty()) {
            player.sendMessage(mm("<yellow>This item has no flags."))
            return Command.SINGLE_SUCCESS
        }
        player.sendMessage(mm("<gold><b>Item flags</b></gold>"))
        flags.forEach { player.sendMessage(mm("<dark_gray>•</dark_gray> <white><flag></white>", Placeholder.unparsed("flag", it.name.lowercase(Locale.ROOT)))) }
        return Command.SINGLE_SUCCESS
    }

    private fun heldItem(player: Player): ItemStack? {
        val item = player.inventory.itemInMainHand
        if (item.type.isAir) {
            player.sendMessage(mm("<red>Hold an item in your main hand first."))
            return null
        }
        return item
    }

    private fun parseMiniMessage(player: Player, input: String): Component? {
        return try {
            Component.empty()
                .decoration(TextDecoration.ITALIC, false)
                .append(MINI_MESSAGE.deserialize(input))
        } catch (exception: Exception) {
            player.sendMessage(mm(
                "<red>Invalid MiniMessage:</red> <white><reason></white>",
                Placeholder.unparsed("reason", exception.message ?: "unknown parsing error")
            ))
            null
        }
    }

    private fun findVanillaEnchantment(input: String): Enchantment? {
        val normalized = input.lowercase(Locale.ROOT)
        val key = NamespacedKey.fromString(normalized) ?: NamespacedKey.minecraft(normalized)
        if (key.namespace != NamespacedKey.MINECRAFT) return null
        return ENCHANTMENTS.get(key)
    }

    private fun findFlag(player: Player, input: String): ItemFlag? {
        val flag = ITEM_FLAGS[input.lowercase(Locale.ROOT)]
        if (flag == null) {
            player.sendMessage(mm("<red>Unknown item flag:</red> <white><value></white>", Placeholder.unparsed("value", input)))
        }
        return flag
    }

    private fun invalidLine(player: Player, requested: Int, size: Int, allowAppend: Boolean = false): Int {
        val range = if (allowAppend) "1-${size + 1}" else if (size == 0) "none" else "1-$size"
        player.sendMessage(mm(
            "<red>Lore line <requested> does not exist.</red> <gray>Valid: <range></gray>",
            Placeholder.unparsed("requested", requested.toString()),
            Placeholder.unparsed("range", range)
        ))
        return 0
    }

    private fun numberedLine(number: Int, line: Component): Component {
        return mm("<dark_gray><number>.</dark_gray> ", Placeholder.unparsed("number", number.toString())).append(line)
    }

    private fun commandHelp(command: String, description: String): Component {
        return mm("<yellow><command></yellow>", Placeholder.unparsed("command", command))
            .clickEvent(ClickEvent.suggestCommand(command))
            .hoverEvent(HoverEvent.showText(mm("<gray>Click to fill the command</gray>")))
            .append(mm(
                " <dark_gray>—</dark_gray> <gray><description></gray>",
                Placeholder.unparsed("description", description)
            ))
    }

    private fun mm(input: String, vararg placeholders: net.kyori.adventure.text.minimessage.tag.resolver.TagResolver): Component {
        return DISPLAY_MINI_MESSAGE.deserialize(input, *placeholders)
    }
}
