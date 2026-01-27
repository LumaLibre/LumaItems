package dev.lumas.lumaitems.items.misc.jobs

import com.gamingmesh.jobs.api.JobsExpGainEvent
import com.gamingmesh.jobs.api.JobsPrePaymentEvent
import com.gmail.nossr50.api.AbilityAPI as MCMMOAbilityAPI
import dev.lumas.lumacore.utility.Logging
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.hooks.McMMOHook
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.tiers.Tier
import io.papermc.paper.persistence.PersistentDataContainerView
import org.bukkit.Material
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

    private val nameSpacedKey = Util.namespacedKey(jobType.key)

    override fun createItem(): Pair<String, ItemStack> {
        val level = random().nextInt(2, 6)
        return createItem(level)
    }

    fun createItem(level: Int): Pair<String, ItemStack> {
        val jobName = Util.formatEnumerator(jobType.name)
        return ItemFactory.Builder()
            .name("<b>${jobType.colorize("Archive")}</b> <!b><#F7FFC9>of Astralis</#F7FFC9></!b>")
            .material(Material.BOOK)
            .tier(Tier.CHRISTMAS_2025)
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .persistentData(jobType.key)
            .lore(
                "A mysterious book",
                "inscribed with words",
                "you can't quite make",
                "out...",
                "",
                "Permanent ${jobType.colorize("$level%")} $jobName",
                "boost while held."
            )
            .persistentDataValue(level.toShort())
            .hideEnchants(true)
            .addSpace(false)
            .buildPair()
    }

    override fun executeWithContainer(type: Action, player: Player, event: Any, container: PersistentDataContainerView): Boolean {
        try {
            if (Registry.HOOKS.getOrThrow(McMMOHook::class).isWith() && MCMMOAbilityAPI.treeFellerEnabled(player)) {
                // Patched, but leaving disabled for now.
                return false
            }
        } catch (throwable: Throwable) {
            Logging.errorLog("Error checking mcMMO tree feller state for player ${player.name}", throwable)
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


    override fun tabCompleteName(): String {
        return "archive_of_astralis_${jobType.name.lowercase()}"
    }


    enum class JobType(val color: String) {
        ALCHEMIST("#7b66fb"),
        BLACKSMITH("#8B8B8B"),
        BUILDER("#58d9a3"),
        COOK("#ffa675"),
        DIGGER("#fff27f"),
        FARMER("#8eff99"),
        FISHERMAN("#6db0ff"),
        HUNTER("#ff4b3f"),
        LUMBERJACK("#bcf067"),
        MINER("#CD8EFF");

        val key = "archive-of-astralis-${this.name.lowercase()}"
        fun colorize(text: String) = "<${this.color}>$text</${this.color}>"
    }
}
