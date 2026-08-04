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
import dev.lumas.core.util.Text
import dev.lumas.lumaitems.api.ItemManager
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.asComponent
import dev.lumas.lumaitems.util.extensions.asPlainText
import dev.lumas.lumaitems.util.extensions.send
import io.papermc.paper.command.brigadier.CommandSourceStack
import java.lang.reflect.Modifier
import java.util.concurrent.CompletableFuture
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Register(Autowire.BRIGADIER)
@CommandMeta(
    name = "givetier",
    description = "Obtain every custom item of a tier",
    usage = "/<command> givetier <tier> [target] [drop] [silent]",
    permission = "lumaitems.command.givetier",
    parent = CommandManager::class
)
class GiveTierCommand : BrigadierSubCommand {

    private companion object {
        private const val MAX_RECIPIENTS = 3 // TODO: shitty temporary hardcoded block

        private val TIERS: Map<String, Tier> by lazy {
            Tier::class.java.fields
                .filter { Modifier.isStatic(it.modifiers) && it.type == Tier::class.java }
                .mapNotNull { field -> (field.get(null) as? Tier)?.takeIf { it.tierString.isNotBlank() }?.let { field.name to it } }
                .toMap()
        }
    }

    @BrigadierExecutor
    fun run(
        src: CommandSourceStack,
        @Argument(value = "tier") tierName: String,
        @Argument(value = "target", optional = true) targets: List<@JvmSuppressWildcards Player>?,
        @Argument(value = "drop", optional = true) drop: Boolean?,
        @Argument(value = "silent", optional = true) silent: Boolean?
    ) {
        val sender: CommandSender = src.sender

        val recipients: List<Player> = targets ?: ((sender as? Player)?.let { listOf(it) } ?: run {
            sender.send("<red>Must specify a target when running from console")
            return
        })

        if (recipients.size > MAX_RECIPIENTS) {
            sender.send("<red>Dangerous give tier command: too many recipients")
            return
        }

        val tier = findTier(tierName) ?: run {
            sender.send("<red>No tier named $tierName")
            return
        }

        val label = tier.toComponent().asPlainText()
        val items = ItemManager.getAllItems().filter { !it.isEmpty && it.isTier(label) }

        if (items.isEmpty()) {
            sender.send("<red>No custom items are on the $label tier")
            return
        }

        for (recipient in recipients) {
            for (item in items) {
                Util.giveItem(recipient, item, drop ?: false)
            }

            if (silent != true) {
                Text.msg(
                    recipient,
                    "<reset>You have been given</reset> <gold>${items.size}</gold> <reset>items from the</reset> ".asComponent()
                        .append(tier.toComponent())
                        .append(" <reset>tier</reset>".asComponent())
                )
            }
        }

        if (sender !in recipients) {
            sender.send("Gave <gold>${items.size}</gold> $label items to <gold>${recipients.size}</gold> player(s).")
        }
    }

    private fun findTier(name: String): Tier? {
        return TIERS.entries.firstOrNull { (constant, tier) ->
            constant.equals(name, ignoreCase = true) || tier.toComponent().asPlainText().equals(name, ignoreCase = true)
        }?.value
    }

    private fun ItemStack.isTier(label: String): Boolean {
        val lore = this.lore() ?: return false
        return lore.any { line ->
            val plain = line.asPlainText()
            val bullet = plain.indexOf('•')
            bullet >= 0 && plain.substring(bullet + 1).trim().equals(label, ignoreCase = true)
        }
    }

    @Suggests("tier")
    fun suggestTier(ctx: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val partial = builder.remaining.lowercase()
        TIERS.keys.asSequence()
            .filter { it.lowercase().startsWith(partial) }
            .forEach(builder::suggest)
        return builder.buildFuture()
    }
}
