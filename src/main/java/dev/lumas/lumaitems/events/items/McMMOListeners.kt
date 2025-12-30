package dev.lumas.lumaitems.events.items

import com.gmail.nossr50.events.skills.woodcutting.TreeFellerDestroyTreeEvent
import dev.lumas.lumacore.manager.modules.AutoRegister
import dev.lumas.lumacore.manager.modules.RegisterType
import dev.lumas.lumaitems.enums.Action
import org.bukkit.event.EventHandler

@AutoRegister(RegisterType.LISTENER)
class McMMOListeners : ItemListener() {

    @EventHandler
    fun onMcMMOTreeFellerDestroyTree(event: TreeFellerDestroyTreeEvent) {
        val player = event.player
        val data = player.inventory.itemInMainHand.itemMeta?.persistentDataContainer ?: return
        fire(data, Action.MCMMO_TREE_FELLER_DESTROY_TREE, player, event)
    }
}