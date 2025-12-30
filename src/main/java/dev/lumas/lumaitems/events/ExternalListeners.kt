package dev.lumas.lumaitems.events

import com.gamingmesh.jobs.api.JobsPrePaymentEvent
import dev.lumas.lumacore.manager.modules.AutoRegister
import dev.lumas.lumacore.manager.modules.RegisterType
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.events.GeneralListeners.Companion.relicFile
import dev.lumas.lumaitems.enums.Rarity
import dev.lumas.lumaitems.relics.RelicCreator
import dev.lumas.lumaitems.util.Util
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import kotlin.random.Random

// todo: get rid of this shit
// Listeners that belong to external plugins
@AutoRegister(RegisterType.LISTENER)
class ExternalListeners : Listener {

    val plugin: LumaItems = LumaItems.getInstance()

    @EventHandler
    fun onJobsPrePayment(event: JobsPrePaymentEvent) {
        if (Random.nextInt(10000) > 2 || event.job.name == "Hunter") return
        val player = event.player?.player ?: return


        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val rarity = Rarity.genericRarities.random()
            val material: Material =
                Material.valueOf(relicFile.getStringList("relic-materials.${rarity.name.lowercase()}").random())

            val relic = RelicCreator(
                rarity.algorithmWeight,
                -1,
                rarity,
                material
            ).getRelicItem()

            Bukkit.getScheduler().runTask(plugin, Runnable {
                Util.giveItem(player, relic)
            })
        })
    }
}