package dev.jsinco.luma.lumaitems.events.items

import com.gamingmesh.jobs.api.JobsExpGainEvent
import com.gamingmesh.jobs.api.JobsPrePaymentEvent
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.util.Util
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority

class JobsListeners : ItemListener() {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onJobsExpGain(event: JobsExpGainEvent) {
        val p = event.player.player ?: return
        fire(Util.getAllEquipmentNBT(p), Action.JOBS_EXP_GAIN, p, event, true)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onJobsPrePayment(event: JobsPrePaymentEvent) {
        val p = event.player.player ?: return
        fire(Util.getAllEquipmentNBT(p), Action.JOBS_PRE_PAYMENT, p, event, true)
    }
}