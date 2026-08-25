package dev.lumas.lumaitems.hooks

import dev.lumas.lumaitems.registry.Identifier
import dev.lumas.lumaitems.registry.StringIdentifier
import org.bukkit.Bukkit
import org.bukkit.block.BlockState
import org.bukkit.entity.Player
import org.prism_mc.prism.paper.api.PrismPaperApi
import org.prism_mc.prism.paper.api.activities.PaperActivity

class PrismHook : Hook {

    private var cached: Any? = null

    override fun identifier(): Identifier {
        return StringIdentifier.of("prism")
    }

    fun getPrismAPI(): PrismPaperApi? {
        if (!this.isWith()) return null

        cached?.let { return it as PrismPaperApi }
        val api = Bukkit.getServicesManager()
            .getRegistration(PrismPaperApi::class.java)
            ?.provider
        cached = api
        return api
    }

    fun recordBlockBreak(player: Player, block: BlockState): Boolean {
        return this.isWith() && record("block-break", player, block)
    }

    fun recordBlockPlace(player: Player, block: BlockState): Boolean {
        return this.isWith() && record("block-place", player, block)
    }

    private fun record(action: String, player: Player, block: BlockState): Boolean {
        val api = getPrismAPI() ?: return false
        val actionType = api.actionTypeRegistry().actionType(action).orElse(null) ?: return false

        val activity = PaperActivity.builder()
            .action(api.actionFactory().createBlockAction(actionType, block))
            .location(block.location)
            .cause(player)
            .build()

        api.recordingService().addToQueue(activity)
        return true
    }
}
