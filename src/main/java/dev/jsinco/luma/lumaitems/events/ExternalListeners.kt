package dev.jsinco.luma.lumaitems.events

import com.gamingmesh.jobs.api.JobsPrePaymentEvent
import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.events.GeneralListeners.Companion.relicFile
import dev.jsinco.luma.lumaitems.enums.Rarity
import dev.jsinco.luma.lumaitems.relics.RelicCreator
import dev.jsinco.luma.lumaitems.util.Util
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import kotlin.random.Random

// Listeners that belong to external plugins
class ExternalListeners(val plugin: LumaItems) : Listener {

    @EventHandler
    fun onJobsPrePayment(event: JobsPrePaymentEvent) {
        if (Random.nextInt(3000) > 2 || event.job.name == "Hunter") return
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