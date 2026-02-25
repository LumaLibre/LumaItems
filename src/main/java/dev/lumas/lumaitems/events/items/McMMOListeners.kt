package dev.lumas.lumaitems.events.items

import com.gmail.nossr50.datatypes.skills.SubSkillType
import com.gmail.nossr50.events.skills.secondaryabilities.SubSkillBlockEvent
import com.gmail.nossr50.events.skills.woodcutting.TreeFellerDestroyTreeEvent
import dev.lumas.lumacore.manager.modules.AutoRegister
import dev.lumas.lumacore.manager.modules.RegisterType
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.util.extensions.mainHandContainer
import org.bukkit.event.EventHandler

@AutoRegister(RegisterType.LISTENER, requires = "mcMMO")
class McMMOListeners : ItemListener() {

    @EventHandler
    fun onMcMMOTreeFellerDestroyTree(event: TreeFellerDestroyTreeEvent) {
        val player = event.player
        val data = player.inventory.itemInMainHand.itemMeta?.persistentDataContainer ?: return
        fire(data, Action.MCMMO_TREE_FELLER_DESTROY_TREE, player, event)
    }

    @EventHandler
    fun onMcMMOSubSkillBlock(event: SubSkillBlockEvent) {
        if (event.subSkillType != SubSkillType.HERBALISM_GREEN_THUMB) return
        val player = event.player

        fire(player.mainHandContainer() ?: return, Action.MCMMO_HERBALISM_REPLANT, player, event)
    }
}