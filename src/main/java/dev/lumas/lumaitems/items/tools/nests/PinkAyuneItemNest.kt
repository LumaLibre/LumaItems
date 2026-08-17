package dev.lumas.lumaitems.items.tools.nests

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.getOreColor
import dev.lumas.lumaitems.util.extensions.isTagged
import dev.lumas.lumaitems.util.extensions.itemStack
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.spell
import dev.lumas.lumaitems.util.extensions.syncDelayed
import dev.lumas.lumaitems.util.tags.Kind
import dev.lumas.lumaitems.util.tags.LinkedTags
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.ItemMergeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector

abstract class PinkAyuneTool : CustomItemFunctions() {

    private val procStreak: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

    abstract val key: NamespacedKey

    abstract fun isValidBlock(block: Block): Boolean

    open fun transformDrop(drop: ItemStack): ItemStack = drop

    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        val block = event.block
        if (!isValidBlock(block)) {
            return
        }

        val loc = block.location.toCenterLocation()
        val uuid = player.uniqueId
        val threshold = if (procStreak.contains(uuid)) 35 else 25
        if (random.nextInt(100) >= threshold) {
            procStreak.remove(uuid) // streak broken
            if (random.nextInt(100) < 18) {
                event.isDropItems = false
                block.world.playSound(block.location, Sound.ENTITY_WITCH_CELEBRATE, 0.1f, 1.8f)
                block.world.spawnParticle(Particle.INSTANT_EFFECT, loc, 20, 0.4, 0.5, 0.4, 0.0, "#FFBCE2".spell())
            }
            return
        }
        procStreak.add(uuid)

        event.isCancelled = true

        val color = (block.getOreColor() ?: block.blockData.mapColor).spell()
        block.world.spawnParticle(Particle.INSTANT_EFFECT, loc, 20, 0.4, 0.5, 0.4, 0.0, color)
        block.world.playSound(block.location, Sound.ENTITY_FIREWORK_ROCKET_BLAST_FAR, 0.5f, 0.9f)
        block.world.playSound(block.location, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR, 0.1f, 1.1f)

        val drops = block.getDrops(player.inventory.itemInMainHand)
        val hasBlockAbove = !block.getRelative(BlockFace.UP).isEmpty
        val spawnLoc = if (hasBlockAbove) {
            block.location.add(0.5, 0.5, 0.5)
        } else {
            block.location.add(0.5, 1.0, 0.5)
        }
        val facing = player.facing.direction

        for (drop in drops) {
            val single = transformDrop(drop.asOne())
            repeat(drop.amount.coerceAtMost(64)) {
                val item = player.world.dropItem(spawnLoc, single)
                item.velocity = if (hasBlockAbove) {
                    Vector(
                        facing.x * 0.3 + (random.nextDouble() - 0.5) * 0.1,
                        0.12,
                        facing.z * 0.3 + (random.nextDouble() - 0.5) * 0.1
                    )
                } else {
                    Vector(
                        (random.nextDouble() - 0.5) * 0.25,
                        0.2 + random.nextDouble() * 0.15,
                        (random.nextDouble() - 0.5) * 0.25
                    )
                }

                item.pickupDelay = 20
                item.thrower = player.uniqueId
                item.persistentDataContainer.set(key, PersistentDataType.SHORT, 1)
                item.syncDelayed(30) { item.persistentDataContainer.remove(key) }
            }
        }
    }

    override fun onItemMerge(player: Player, event: ItemMergeEvent) {
        event.isCancelled = true
    }
}

class PinkAyuneMattockItem : PinkAyuneTool() {
    override val key: NamespacedKey = "pink-ayune-mattock".namespacedKey()

    override fun isValidBlock(block: Block): Boolean = block.isTagged(Kind.INCLUSIVE_ORES)

    override fun createItem() = ItemFactory.builder()
        .name("<b><gradient:#FFBCE2:#FAEDCB:#DBCDF0>Pink Ayuné Mattock</gradient></b>")
        .customEnchants("<#FEC1DF>Gambler's Remark")
        .persistentData(key)
        .material(Material.NETHERITE_PICKAXE)
        .tier(Tier.LUMARINE_2026)
        .vanillaEnchants(
            Enchantment.EFFICIENCY to 7,
            Enchantment.FORTUNE to 5,
            Enchantment.UNBREAKING to 10,
            Enchantment.MENDING to 1
        )
        .lore(
            "<#FEC1DF>Struck</#FEC1DF> ores may burst",
            "into an extra yield of",
            "drops. Each lucky strike",
            "stacks the odds for the",
            "next strike.",
            "",
            "Some ores, though, may",
            "turn up empty.",
        )
        .buildPair()
}

class PinkAyuneHatchetItem : PinkAyuneTool() {
    override val key: NamespacedKey = "pink-ayune-hatchet".namespacedKey()

    override fun isValidBlock(block: Block): Boolean = block.isTagged(Tag.LOGS)

    private val AIR = Material.AIR.itemStack()

    override fun transformDrop(drop: ItemStack): ItemStack {
        val wood = LinkedTags.LOG_TO_WOOD.get(drop.type) ?: return AIR
        return ItemStack.of(wood, drop.amount)
    }

    override fun createItem() = ItemFactory.builder()
        .name("<b><gradient:#FFBCE2:#FAEDCB:#DBCDF0>Pink Ayuné Hatchet</gradient></b>")
        .customEnchants("<#FEC1DF>Gambler's Remark")
        .persistentData(key)
        .material(Material.NETHERITE_AXE)
        .tier(Tier.LUMARINE_2026)
        .vanillaEnchants(
            Enchantment.EFFICIENCY to 7,
            Enchantment.FORTUNE to 5,
            Enchantment.UNBREAKING to 10,
            Enchantment.MENDING to 1
        )
        .lore(
            "<#FEC1DF>Felled</#FEC1DF> logs may burst",
            "into an extra yield of",
            "drops. Each lucky strike",
            "stacks the odds for the",
            "next strike.",
            "",
            "Some logs, though, may",
            "turn up empty.",
        )
        .buildPair()
}
