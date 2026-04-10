package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.annotations.FireAnyways
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.model.PersistentDataRecord
import dev.lumas.lumaitems.util.extensions.actionBar
import dev.lumas.lumaitems.util.extensions.flagFor
import dev.lumas.lumaitems.util.extensions.getPersistentKey
import dev.lumas.lumaitems.util.extensions.isFlagged
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.setPersistentKey
import dev.lumas.lumaitems.util.tiers.Tier
import kotlin.random.Random
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

@FireAnyways(Action.BREAK_BLOCK, Action.INVENTORY_CLICK)
class VerdigrisIdolItem : CustomItemFunctions() {

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
        private val OXIDATION_KEY: NamespacedKey = "verdigris-idol-oxidation".namespacedKey()

        private const val MAX_OXIDATION = 30
        private const val OXIDATION_CHANCE = 0.1

        private val COPPER_DUST = Particle.DustOptions(Color.fromRGB(87, 160, 107), 1.5f)

        private val TARGET_BLOCKS = mapOf(
            Material.STONE to Material.COPPER_ORE,
            Material.DEEPSLATE to Material.DEEPSLATE_COPPER_ORE
        )

        private val BASE_LORE = listOf(
            "Forged in an age of living metal.",
            "Its duty has not yet faded away.",
            "",
            "<#42BFB8>Hold</#42BFB8> to transmute mined Stone",
            "and Deepslate to Copper Ore.",
            "",
            "<red>Oxidizes as its power is used.</red>",
            "<gray> ┗ Deoxidize with Fire Charges.</gray>",
            ""
        )

        private fun stageFromValue(value: Int): OxidationStage = OxidationStage.entries[value / 10]

        private fun fullLore(stage: OxidationStage): List<String> = BASE_LORE + stage.loreLine
    }

    override fun createItem(): Pair<String, ItemStack> {
        return buildForStage(OxidationStage.FRESH, 0).let { KEY to it }
    }

    private fun buildForStage(stage: OxidationStage, oxidationValue: Int): ItemStack {
        return ItemFactory.builder()
            .name("<b><gradient:#57C78A:#42BFB8:#2E9FD4>Verdigris Idol</gradient></b>")
            .customEnchants("<gradient:#57C78A:#2E9FD4>Mineral Attunement</gradient>")
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .hideEnchants(true)
            .lore(*fullLore(stage).toTypedArray())
            .material(stage.material)
            .persistentData(KEY)
            .persistentDataRecords(
                PersistentDataRecord.create(OXIDATION_KEY, PersistentDataType.INTEGER, oxidationValue)
            )
            .maxStackSize(1)
            .tier(Tier.WONDERLAND_2026)
            .buildPair()
            .second
    }

    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        val mainHand = player.inventory.itemInMainHand
        val mainHandIdol = mainHand.takeIf { it.isMatchingItem(KEY_NS) }
        val offHandIdol = if (mainHandIdol == null) player.inventory.itemInOffHand.takeIf { it.isMatchingItem(KEY_NS) } else null
        val helmetIdol = if (mainHandIdol == null && offHandIdol == null) player.inventory.helmet?.takeIf { it.isMatchingItem(KEY_NS) } else null
        val idol = mainHandIdol ?: offHandIdol ?: helmetIdol ?: return

        val block = event.block
        val copperOre = TARGET_BLOCKS[block.type] ?: return

        val currentOxidation = idol.getPersistentKey(OXIDATION_KEY, PersistentDataType.INTEGER) ?: 0
        val currentStage = stageFromValue(currentOxidation)

        val newIdol = advanceOxidation(idol, player, currentOxidation)
        if (newIdol != null) {
            when {
                mainHandIdol != null -> player.inventory.setItemInMainHand(newIdol)
                offHandIdol != null -> player.inventory.setItemInOffHand(newIdol)
                else -> player.inventory.helmet = newIdol
            }
        }

        if (currentStage == OxidationStage.OXIDIZED) return
        if (currentStage.probability < 1.0 && Random.nextDouble() >= currentStage.probability) return

        val tool = if (mainHandIdol != null) (newIdol ?: idol) else mainHand

        event.isDropItems = false
        block.world.dropItemNaturally(block.location, computeDrops(tool, copperOre))

        val loc = block.location.toCenterLocation()
        block.world.spawnParticle(Particle.DUST, loc, 15, 0.35, 0.35, 0.35, 0.0, COPPER_DUST)
        block.world.playSound(loc, Sound.BLOCK_COPPER_PLACE, 0.9f, 1.1f)
    }

    override fun onInventoryClick(player: Player, event: InventoryClickEvent) {
        val cursor = event.cursor.takeIf { it.type == Material.FIRE_CHARGE } ?: return
        val clickedItem = event.currentItem?.takeIf { it.isMatchingItem(KEY_NS) } ?: return

        event.isCancelled = true

        val currentOxidation = clickedItem.getPersistentKey(OXIDATION_KEY, PersistentDataType.INTEGER) ?: 0
        if (currentOxidation == 0) {
            if (!player.isFlagged(this)) {
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.8f)
            }
            return
        }

        val chargesUsed = minOf(cursor.amount, currentOxidation)
        val newOxidation = currentOxidation - chargesUsed

        val oldStage = stageFromValue(currentOxidation)
        val newStage = stageFromValue(newOxidation)

        if (newStage != oldStage) {
            event.clickedInventory?.setItem(event.slot, buildForStage(newStage, newOxidation))
            player.actionBar(when (newStage) {
                OxidationStage.FRESH -> "<#57C78A>The Verdigris Idol has been polished clean!"
                OxidationStage.EXPOSED -> "<#B8CC5A>The Verdigris Idol is recovering!"
                OxidationStage.WEATHERED -> "<#7DA862>The Verdigris Idol is now less weathered."
                else -> return
            })
        } else {
            clickedItem.setPersistentKey(OXIDATION_KEY, PersistentDataType.INTEGER, newOxidation)
        }

        player.playSound(player.location, Sound.ITEM_FIRECHARGE_USE, 0.6f, 1.4f)
        player.flagFor(this, 1) // Event may fire multiple times apparently

        val remaining = cursor.amount - chargesUsed
        @Suppress("DEPRECATION") // No alternatives, not scheduled for removal
        event.setCursor(if (remaining <= 0) ItemStack(Material.AIR) else cursor.asQuantity(remaining))
    }

    private fun advanceOxidation(idol: ItemStack, player: Player, currentOxidation: Int): ItemStack? {
        if (currentOxidation >= MAX_OXIDATION) return null
        if (Random.nextDouble() >= OXIDATION_CHANCE) return null

        val newOxidation = currentOxidation + 1
        val oldStage = stageFromValue(currentOxidation)
        val newStage = stageFromValue(newOxidation)

        if (newStage != oldStage) {
            player.actionBar(when (newStage) {
                OxidationStage.EXPOSED -> "<#B8CC5A>The Verdigris Idol is becoming exposed!"
                OxidationStage.WEATHERED -> "<#7DA862>The Verdigris Idol is starting to weather..."
                OxidationStage.OXIDIZED -> "<#5A6B56>The Verdigris Idol has fully oxidized!"
                else -> return null
            })
            return buildForStage(newStage, newOxidation)
        }

        idol.setPersistentKey(OXIDATION_KEY, PersistentDataType.INTEGER, newOxidation)
        return null
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

    override fun onPlaceBlock(player: Player, event: BlockPlaceEvent) {
        if (event.itemInHand.isMatchingItem(KEY_NS)) event.isCancelled = true
    }

    override fun onPrepareCraft(player: Player, event: PrepareItemCraftEvent) {
        event.inventory.result = null
    }
}
