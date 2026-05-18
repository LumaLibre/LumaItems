package dev.lumas.lumaitems.events.item

import com.gmail.nossr50.datatypes.skills.SubSkillType
import com.gmail.nossr50.events.skills.secondaryabilities.SubSkillBlockEvent
import com.gmail.nossr50.events.skills.woodcutting.TreeFellerDestroyTreeEvent
import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.Register
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.util.extensions.asSource
import dev.lumas.lumaitems.util.extensions.itemInMainHand
import org.bukkit.event.EventHandler

@Register(Autowire.LISTENER, requires = "mcMMO")
class McMMOListeners : ItemListener() {

    @EventHandler
    fun onMcMMOTreeFellerDestroyTree(event: TreeFellerDestroyTreeEvent) {
        val player = event.player
        val data = player.itemInMainHand.asSource()
        fire(data, Action.MCMMO_TREE_FELLER_DESTROY_TREE, player, event)
    }

    @EventHandler
    fun onMcMMOSubSkillBlock(event: SubSkillBlockEvent) {
        if (event.subSkillType != SubSkillType.HERBALISM_GREEN_THUMB) return
        val player = event.player

        fire(player.itemInMainHand.asSource(), Action.MCMMO_HERBALISM_REPLANT, player, event)
    }
}