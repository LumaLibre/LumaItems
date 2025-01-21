package dev.jsinco.luma.lumaitems.items.nests

import com.gamingmesh.jobs.api.JobsExpGainEvent
import com.gamingmesh.jobs.api.JobsPrePaymentEvent
import com.gmail.nossr50.api.AbilityAPI as mcMMOAbilityAPI
import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import io.papermc.paper.persistence.PersistentDataContainerView
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class AlchemistArchiveOfAstralisItem : ArchiveOfAstralisItemNest(JobType.ALCHEMIST)
class BlacksmithArchiveOfAstralisItem : ArchiveOfAstralisItemNest(JobType.BLACKSMITH)
class BuilderArchiveOfAstralisItem : ArchiveOfAstralisItemNest(JobType.BUILDER)
class CookArchiveOfAstralisItem : ArchiveOfAstralisItemNest(JobType.COOK)
class DiggerArchiveOfAstralisItem : ArchiveOfAstralisItemNest(JobType.DIGGER)
class FarmerArchiveOfAstralisItem : ArchiveOfAstralisItemNest(JobType.FARMER)
class FishermanArchiveOfAstralisItem : ArchiveOfAstralisItemNest(JobType.FISHERMAN)
class HunterArchiveOfAstralisItem : ArchiveOfAstralisItemNest(JobType.HUNTER)
class LumberjackArchiveOfAstralisItem : ArchiveOfAstralisItemNest(JobType.LUMBERJACK)
class MinerArchiveOfAstralisItem : ArchiveOfAstralisItemNest(JobType.MINER)

abstract class ArchiveOfAstralisItemNest(private val jobType: JobType) : CustomItemFunctions() {
    enum class JobType {
        ALCHEMIST,
        BLACKSMITH,
        BUILDER,
        COOK,
        DIGGER,
        FARMER,
        FISHERMAN,
        HUNTER,
        LUMBERJACK,
        MINER;

        val key = "archive-of-astralis-${this.name.lowercase()}"
    }

    private val nameSpacedKey = NamespacedKey(instance(), jobType.key)

    private fun archiveLore(level: Int): MutableList<String> {
        val job = Util.formatMaterialName(jobType.name)
        return mutableListOf(
            "<gray>$job's Archive of Astralis",
            "",
            "<gray>Permanent <#F7FFC9>$level% <gray>$job Job",
            "<gray>boost while held."
        )
    }

    override fun createItem(): Pair<String, ItemStack> {
        val level = random().nextInt(2, 6)
        return ItemFactory.Builder()
            .name("<b><#f498f6>Archive</#f498f6></b> <!b><#F7FFC9>of Astralis</#F7FFC9></!b>")
            .material(Material.BOOK)
            .tier(Tier.WINTER_2024)
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .persistentData(jobType.key)
            .lore(archiveLore(level))
            .persistentDataValue(level.toShort())
            .hideEnchants(true)
            .addSpace(false)
            .buildPair()
    }

    override fun executeWithContainer(type: Action, player: Player, event: Any, container: PersistentDataContainerView): Boolean {
        if (LumaItems.isWithmcMMO() && mcMMOAbilityAPI.treeFellerEnabled(player)) {
            // Kroxxis ticket @1/19/25, mcMMO treefeller with multiple books
            // has way too many variables for me to deal with right now,
            // just disabling for now.
            return false
        }

        val level: Short = container.get(nameSpacedKey, PersistentDataType.SHORT) ?: 2

        when (type) {
            Action.JOBS_EXP_GAIN -> {
                event as JobsExpGainEvent
                if (event.job.name.equals(jobType.name, ignoreCase = true)) {
                    event.exp *= (level / 100.0) + 1
                }
            }
            Action.JOBS_PRE_PAYMENT -> {
                event as JobsPrePaymentEvent
                if (event.job.name.equals(jobType.name, ignoreCase = true)) {
                    event.amount *= (level / 100.0) + 1
                }
            }
            else -> return false
        }
        return true
    }
}
