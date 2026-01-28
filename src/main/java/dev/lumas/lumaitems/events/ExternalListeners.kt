package dev.lumas.lumaitems.events

import com.gamingmesh.jobs.api.JobsPrePaymentEvent
import dev.lumas.lumacore.manager.modules.AutoRegister
import dev.lumas.lumacore.manager.modules.RegisterType
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.items.astral.GrubbyRelicItem
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.util.Util
import kotlin.random.Random
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

// General listeners that belong to external plugins
@AutoRegister(RegisterType.LISTENER)
class ExternalListeners : Listener {

    val plugin: LumaItems = LumaItems.getInstance()

    @EventHandler
    fun onJobsPrePayment(event: JobsPrePaymentEvent) {
        if (Random.nextInt(20_000) > 2 || event.job.name == "Hunter") return
        val player = event.player?.player ?: return

        val grubby = Registry.CUSTOM_ITEMS.getOrThrow(GrubbyRelicItem::class).createItem().second
        Util.giveItem(player, grubby)
    }
}