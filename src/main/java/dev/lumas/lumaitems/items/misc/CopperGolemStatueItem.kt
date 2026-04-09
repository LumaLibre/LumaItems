package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.annotations.FireAnyways
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.MiniMessageUtil
import dev.lumas.lumaitems.util.extensions.actionBar
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.tiers.Tier
import kotlin.random.Random
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.ItemStack

@FireAnyways(Action.BREAK_BLOCK)
class CopperGolemStatueItem : CustomItemFunctions() {

    private enum class OxidationStage(
        val probability: Double,
        val loreLine: String,
        val material: Material
    ) {
        FRESH(1.0, "<#57C78A>✦ Unoxidized <gray>(100% efficiency)", Material.COPPER_GOLEM_STATUE),
        EXPOSED(0.7, "<#B8CC5A>✦ Exposed <gray>(70% efficiency)", Material.EXPOSED_COPPER_GOLEM_STATUE),
        WEATHERED(0.4, "<#7DA862>✦ Weathered <gray>(40% efficiency)", Material.WEATHERED_COPPER_GOLEM_STATUE),
        OXIDIZED(0.0, "<#5A6B56>✦ Fully Oxidized <gray>(0% efficiency)", Material.OXIDIZED_COPPER_GOLEM_STATUE)
    }

    companion object {
        private const val KEY = "verdigris-idol"
        private val KEY_NS: NamespacedKey = KEY.namespacedKey()

        private const val OXIDATION_CHANCE = 0.005
        private const val DEOXIDATION_CHANCE = 0.01

        private val COPPER_DUST = Particle.DustOptions(Color.fromRGB(87, 160, 107), 1.5f)

        private val TARGET_BLOCKS = mapOf(
            Material.STONE to Material.COPPER_ORE,
            Material.DEEPSLATE to Material.DEEPSLATE_COPPER_ORE
        )

        private val BASE_LORE = listOf(
            "Forged in an age of living metal.",
            "Its duty has not yet faded.",
            "",
            "<#42BFB8>Hold</#42BFB8> to transmute mined Stone",
            "and Deepslate to Copper Ore.",
            "",
            "<red>Oxidizes as its power is used.</red>",
            "<gray><b>·</b> <i>Other blocks may cleanse it.</i></gray>",
            ""
        )

        private fun stageOf(material: Material): OxidationStage? =
            OxidationStage.entries.firstOrNull { it.material == material }

        private fun fullLore(stage: OxidationStage): List<String> = BASE_LORE + stage.loreLine
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#57C78A:#42BFB8:#2E9FD4>Verdigris Idol</gradient></b>")
            .customEnchants("<gradient:#57C78A:#2E9FD4>Mineral Attunement</gradient>")
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .hideEnchants(true)
            .lore(*fullLore(OxidationStage.FRESH).toTypedArray())
            .material(Material.COPPER_GOLEM_STATUE)
            .persistentData(KEY)
            .tier(Tier.WONDERLAND_2026)
            .buildPair()
    }

    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        val mainHand = player.inventory.itemInMainHand
        var idolIsMainHand = false
        val idol = mainHand.takeIf { it.isMatchingItem(KEY_NS) }?.also { idolIsMainHand = true }
            ?: player.inventory.itemInOffHand.takeIf { it.isMatchingItem(KEY_NS) }
            ?: player.inventory.helmet?.takeIf { it.isMatchingItem(KEY_NS) }
            ?: return

        val block = event.block
        val copperOre = TARGET_BLOCKS[block.type]

        if (copperOre == null) {
            retreatStage(idol, player)
            return
        }

        val stage = stageOf(idol.type) ?: OxidationStage.FRESH
        advanceStage(idol, player, stage)

        if (stage == OxidationStage.OXIDIZED) return
        if (stage.probability < 1.0 && Random.nextDouble() >= stage.probability) return

        val tool = if (idolIsMainHand) idol else mainHand

        event.isDropItems = false
        block.world.dropItemNaturally(block.location, computeDrops(tool, copperOre))

        val loc = block.location.toCenterLocation()
        block.world.spawnParticle(Particle.DUST, loc, 15, 0.35, 0.35, 0.35, 0.0, COPPER_DUST)
        block.world.playSound(loc, Sound.BLOCK_COPPER_PLACE, 0.9f, 1.1f)
    }

    private fun advanceStage(idol: ItemStack, player: Player, currentStage: OxidationStage) {
        if (currentStage == OxidationStage.OXIDIZED) return
        if (Random.nextDouble() >= OXIDATION_CHANCE) return
        val nextStage = OxidationStage.entries[currentStage.ordinal + 1]
        updateItemAppearance(idol, nextStage)
        player.actionBar(when (nextStage) {
            OxidationStage.EXPOSED -> "<#B8CC5A>The Verdigris Idol is becoming exposed!"
            OxidationStage.WEATHERED -> "<#7DA862>The Verdigris Idol is starting to weather..."
            OxidationStage.OXIDIZED -> "<#5A6B56>The Verdigris Idol has fully oxidized!"
            else -> return
        })
    }

    private fun retreatStage(idol: ItemStack, player: Player) {
        val currentStage = stageOf(idol.type) ?: return
        if (currentStage == OxidationStage.FRESH) return
        if (Random.nextDouble() >= DEOXIDATION_CHANCE) return
        val prevStage = OxidationStage.entries[currentStage.ordinal - 1]
        updateItemAppearance(idol, prevStage)
        player.actionBar(when (prevStage) {
            OxidationStage.FRESH -> "<#57C78A>The Verdigris Idol has been polished clean!"
            OxidationStage.EXPOSED -> "<#B8CC5A>The Verdigris Idol is recovering!"
            OxidationStage.WEATHERED -> "<#7DA862>The Verdigris Idol is now less weathered."
            else -> return
        })
    }

    private fun computeDrops(tool: ItemStack, oreBlock: Material): ItemStack {
        if (tool.containsEnchantment(Enchantment.SILK_TOUCH)) return ItemStack(oreBlock)
        val fortune = tool.getEnchantmentLevel(Enchantment.FORTUNE)
        return ItemStack(Material.RAW_COPPER, fortuneRawCopper(fortune))
    }

    // Mirrors vanilla copper ore fortune behaviour
    private fun fortuneRawCopper(fortune: Int): Int {
        val base = Random.nextInt(2, 6)
        if (fortune <= 0) return base
        val multiplier = maxOf(1, Random.nextInt(fortune + 2))
        return base * multiplier
    }

    private fun updateItemAppearance(item: ItemStack, stage: OxidationStage) {
        val meta = item.itemMeta ?: return
        meta.lore(MiniMessageUtil.mml(fullLore(stage)).map { it.colorIfAbsent(NamedTextColor.WHITE) })
        item.type = stage.material
        item.itemMeta = meta
    }

    override fun onPlaceBlock(player: Player, event: BlockPlaceEvent) {
        if (event.itemInHand.isMatchingItem(KEY_NS)) event.isCancelled = true
    }

    override fun onPrepareCraft(player: Player, event: PrepareItemCraftEvent) {
        event.inventory.result = null
    }
}
