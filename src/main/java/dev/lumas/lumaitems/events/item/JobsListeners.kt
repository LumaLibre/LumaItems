package dev.lumas.lumaitems.events.item

import com.gamingmesh.jobs.api.JobsExpGainEvent
import com.gamingmesh.jobs.api.JobsPrePaymentEvent
import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.Register
import dev.lumas.lumaitems.configuration.files.RelicsYml
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.items.astral.GrubbyRelicItem
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.equipmentSources
import kotlin.random.Random
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority

@Register(Autowire.LISTENER, requires = "Jobs")
class JobsListeners : ItemListener() {

    companion object {
        private const val HUNTER_JOB = "Hunter"
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onJobsExpGain(event: JobsExpGainEvent) {
        val player = event.player.player ?: return
        if (this.isTreeFeller(player)) return
        fire(player.equipmentSources(), Action.JOBS_EXP_GAIN, player, event, optimize = false, withContainer = true)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onJobsPrePayment(event: JobsPrePaymentEvent) {
        val player = event.player?.player ?: return
        if (!this.isTreeFeller(player)) {
            fire(player.equipmentSources(), Action.JOBS_PRE_PAYMENT, player, event, optimize = false, withContainer = true)
        }

        if (Random.nextInt(20_000) > 2 || event.job.name == HUNTER_JOB || Registry.CONFIGS.getOrThrow(RelicsYml::class).disableNaturalRelicWorlds.contains(player.world.name)) return

        val grubby = Registry.CUSTOM_ITEMS.getOrThrow(GrubbyRelicItem::class).createItem().second
        Util.giveItem(player, grubby)
    }
}