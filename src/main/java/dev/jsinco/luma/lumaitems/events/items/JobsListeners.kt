package dev.jsinco.luma.lumaitems.events.items

import com.gamingmesh.jobs.api.JobsExpGainEvent
import com.gamingmesh.jobs.api.JobsPrePaymentEvent
import dev.jsinco.luma.lumacore.manager.modules.AutoRegister
import dev.jsinco.luma.lumacore.manager.modules.RegisterType
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.util.Util
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority

@AutoRegister(RegisterType.LISTENER)
class JobsListeners : ItemListener() {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onJobsExpGain(event: JobsExpGainEvent) {
        val player = event.player.player ?: return
        if (this.isTreeFeller(player)) return
        fire(Util.getAllEquipmentNBT(player), Action.JOBS_EXP_GAIN, player, event, true)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onJobsPrePayment(event: JobsPrePaymentEvent) {
        val player = event.player.player ?: return
        if (this.isTreeFeller(player)) return
        fire(Util.getAllEquipmentNBT(player), Action.JOBS_PRE_PAYMENT, player, event, true)
    }
}