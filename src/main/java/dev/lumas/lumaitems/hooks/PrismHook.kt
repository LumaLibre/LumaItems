package dev.lumas.lumaitems.hooks

import dev.lumas.lumaitems.registry.Identifier
import dev.lumas.lumaitems.registry.StringIdentifier
import dev.lumas.lumaitems.util.extensions.safeLazy
import org.bukkit.Bukkit
import org.bukkit.block.BlockState
import org.bukkit.entity.Player
import org.prism_mc.prism.paper.api.PrismPaperApi
import org.prism_mc.prism.paper.api.activities.PaperActivity

class PrismHook : Hook {

    val prism by safeLazy {
        Bukkit.getServicesManager()
            .getRegistration(PrismPaperApi::class.java)
            ?.provider
    }


    override fun identifier(): Identifier {
        return StringIdentifier.of("prism")
    }


    fun getPrismAPI(): PrismPaperApi? {
        return prism
    }

    fun recordBlockBreak(player: Player, block: BlockState): Boolean {
        val api = prism ?: return false
        val actionType = api.actionTypeRegistry().actionType("block-break").orElse(null) ?: return false
        val action = api.actionFactory().createBlockAction(actionType, block);

        val activity = PaperActivity.builder()
            .action(action)
            .location(block.location)
            .cause(player)
            .build();

        api.recordingService().addToQueue(activity);
        return true
    }

    fun recordBlockPlace(player: Player, block: BlockState): Boolean {
        val api = prism ?: return false
        val actionType = api.actionTypeRegistry().actionType("block-place").orElse(null) ?: return false
        val action = api.actionFactory().createBlockAction(actionType, block);

        val activity = PaperActivity.builder()
            .action(action)
            .location(block.location)
            .cause(player)
            .build();

        api.recordingService().addToQueue(activity);
        return true
    }
}