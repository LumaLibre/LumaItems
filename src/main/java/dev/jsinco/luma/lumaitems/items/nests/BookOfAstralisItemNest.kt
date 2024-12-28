package dev.jsinco.luma.lumaitems.items.nests

import com.gamingmesh.jobs.api.JobsExpGainEvent
import com.gamingmesh.jobs.api.JobsPrePaymentEvent
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import io.papermc.paper.persistence.PersistentDataContainerView
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

abstract class BookOfAstralisItemNest(private val jobType: JobType) : CustomItemFunctions() {
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
        MINER
    }

    val key = "archiveofastralis_${jobType.name.lowercase()}"
    private fun nameSpacedKey() = NamespacedKey(instance(), key)

    fun genericBookOfAstralis(): ItemFactory.Builder {
        return ItemFactory.Builder()
            .name("<b><#f498f6>Archive</b> <!b><#F7FFC9>of Astralis")
            .material(Material.BOOK)
            .tier(Tier.WINTER_2024)
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .persistentData(key)
    }

    override fun executeWithContainer(type: Action, player: Player, event: Any, container: PersistentDataContainerView): Boolean {
        val level: Short = container.get(nameSpacedKey(), PersistentDataType.SHORT) ?: 1

        when (type) {
            Action.JOBS_EXP_GAIN -> {
                event as JobsExpGainEvent
                if (event.job.name.equals(jobType.name, ignoreCase = true)) {
                    event.exp *= level / (100.0 + 1)
                }
            }
            Action.JOBS_PRE_PAYMENT -> {
                event as JobsPrePaymentEvent
                if (event.job.name.equals(jobType.name, ignoreCase = true)) {
                    event.amount *= level / (100.0 + 1)
                }
            }
            else -> return false
        }
        return true
    }
}

class AlchemistBookOfAstralisItem : BookOfAstralisItemNest(JobType.ALCHEMIST) {
    override fun createItem(): Pair<String, ItemStack> {
        val level = random().nextInt(2, 6)
        return genericBookOfAstralis()
            .lore(
                "<red>$level% <gray>Alchemist Job EXP &",
                "<gray>money boost whilst held.")
            .persistentDataValue(level.toShort())
            .buildPair()
    }
}

class BlacksmithBookOfAstralisItem : BookOfAstralisItemNest(JobType.BLACKSMITH) {
    override fun createItem(): Pair<String, ItemStack> {
        val level = random().nextInt(2, 6)
        return genericBookOfAstralis()
            .lore(
                "<red>$level% <gray>Blacksmith Job EXP &",
                "<gray>money boost whilst held.")
            .persistentDataValue(level.toShort())
            .buildPair()
    }
}

class BuilderBookOfAstralisItem : BookOfAstralisItemNest(JobType.BUILDER) {
    override fun createItem(): Pair<String, ItemStack> {
        val level = random().nextInt(2, 6)
        return genericBookOfAstralis()
            .lore(
                "<red>$level% <gray>Builder Job EXP &",
                "<gray>money boost whilst held.")
            .persistentDataValue(level.toShort())
            .buildPair()
    }
}

class CookBookOfAstralisItem : BookOfAstralisItemNest(JobType.COOK) {
    override fun createItem(): Pair<String, ItemStack> {
        val level = random().nextInt(2, 6)
        return genericBookOfAstralis()
            .lore(
                "<red>$level% <gray>Cook Job EXP &",
                "<gray>money boost whilst held.")
            .persistentDataValue(level.toShort())
            .buildPair()
    }
}

class DiggerBookOfAstralisItem : BookOfAstralisItemNest(JobType.DIGGER) {
    override fun createItem(): Pair<String, ItemStack> {
        val level = random().nextInt(2, 6)
        return genericBookOfAstralis()
            .lore(
                "<red>$level% <gray>Digger Job EXP &",
                "<gray>money boost whilst held.")
            .persistentDataValue(level.toShort())
            .buildPair()
    }
}

class FarmerBookOfAstralisItem : BookOfAstralisItemNest(JobType.FARMER) {
    override fun createItem(): Pair<String, ItemStack> {
        val level = random().nextInt(2, 6)
        return genericBookOfAstralis()
            .lore( // <#f0e9c4>
                "<red>$level% <gray>Farmer Job EXP &",
                "<gray>money boost whilst held.")
            .persistentDataValue(level.toShort())
            .buildPair()
    }
}

class FishermanBookOfAstralisItem : BookOfAstralisItemNest(JobType.FISHERMAN) {
    override fun createItem(): Pair<String, ItemStack> {
        val level = random().nextInt(2, 6)
        return genericBookOfAstralis()
            .lore(
                "<red>$level% <gray>Fisherman Job EXP &",
                "<gray>money boost whilst held.")
            .persistentDataValue(level.toShort())
            .buildPair()
    }
}

class HunterBookOfAstralisItem : BookOfAstralisItemNest(JobType.HUNTER) {
    override fun createItem(): Pair<String, ItemStack> {
        val level = random().nextInt(2, 6)
        return genericBookOfAstralis()
            .lore(
                "<red>$level% <gray>Hunter Job EXP &",
                "<gray>money boost whilst held.")
            .persistentDataValue(level.toShort())
            .buildPair()
    }
}

class LumberjackBookOfAstralisItem : BookOfAstralisItemNest(JobType.LUMBERJACK) {
    override fun createItem(): Pair<String, ItemStack> {
        val level = random().nextInt(2, 6)
        return genericBookOfAstralis()
            .lore(
                "<red>$level% <gray>Lumberjack Job EXP &",
                "<gray>money boost whilst held.")
            .persistentDataValue(level.toShort())
            .buildPair()
    }
}

class MinerBookOfAstralisItem : BookOfAstralisItemNest(JobType.MINER) {
    override fun createItem(): Pair<String, ItemStack> {
        val level = random().nextInt(2, 6)
        return genericBookOfAstralis()
            .lore(
                "<red>$level% <gray>Miner Job EXP &",
                "<gray>money boost whilst held.")
            .persistentDataValue(level.toShort())
            .buildPair()
    }
}
