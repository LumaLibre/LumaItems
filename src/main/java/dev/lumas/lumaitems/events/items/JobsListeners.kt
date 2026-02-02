package dev.lumas.lumaitems.events.items

import com.gamingmesh.jobs.api.JobsExpGainEvent
import com.gamingmesh.jobs.api.JobsPrePaymentEvent
import dev.lumas.lumacore.manager.modules.AutoRegister
import dev.lumas.lumacore.manager.modules.RegisterType
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.util.Executors
import dev.lumas.lumaitems.util.Util
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority

@AutoRegister(RegisterType.LISTENER)
class JobsListeners : ItemListener() {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onJobsExpGain(event: JobsExpGainEvent) {
        val player = event.player.player ?: return
        if (this.isTreeFeller(player)) return
        Executors.async {
            fire(Util.getAllEquipmentNBT(player), Action.JOBS_EXP_GAIN, player, event, true)
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onJobsPrePayment(event: JobsPrePaymentEvent) {
        val player = event.player.player ?: return
        if (this.isTreeFeller(player)) return
        Executors.async {
            fire(Util.getAllEquipmentNBT(player), Action.JOBS_PRE_PAYMENT, player, event, true)
        }
    }
}