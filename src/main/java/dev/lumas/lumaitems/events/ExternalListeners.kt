package dev.lumas.lumaitems.events

import com.gamingmesh.jobs.api.JobsPrePaymentEvent
import dev.lumas.lumacore.manager.modules.AutoRegister
import dev.lumas.lumacore.manager.modules.RegisterType
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.enums.Rarity
import dev.lumas.lumaitems.relics.RelicCreator
import dev.lumas.lumaitems.util.Executors
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
        if (Random.nextInt(10000) > 2 || event.job.name == "Hunter") return
        val player = event.player?.player ?: return

        Executors.async {
            val rarity = Rarity.genericRarities.random()
            val material = rarity.materials.random()

            val relic = RelicCreator(
                rarity.algorithmWeight,
                -1,
                rarity,
                material
            ).getRelicItem()

            Executors.sync {
                Util.giveItem(player, relic)
            }
        }
    }
}