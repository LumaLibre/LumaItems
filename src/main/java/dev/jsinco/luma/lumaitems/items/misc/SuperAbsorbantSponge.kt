package dev.jsinco.luma.lumaitems.items.misc

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.shapes.Sphere
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.data.Waterlogged
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class SuperAbsorbantSponge : CustomItemFunctions() {

    private val key = NamespacedKey(instance(), "super-absorbant-sponge")
    private val modeKey = NamespacedKey(instance(), "sponge-mode")

    enum class AbsorptionMode(val displayName: String, val materials: List<Material>) {
        WATER("<#359BBD>Water", listOf(Material.WATER)),
        LAVA("<#FF4500>Lava", listOf(Material.LAVA)),
        BOTH("<#359BBD>Water <#F4E06D>& <#FF4500>Lava", listOf(Material.WATER, Material.LAVA));
    }

    override fun createItem(): Pair<String, ItemStack> {

        val item = ItemFactory.builder()
            .material(Material.SPONGE)
            .name("<b><gradient:#E3C565:#C2A05D>Super </gradient><gradient:#C2A05D:#F4E06D>Absor</gradient><gradient:#F4E06D:#C2A05D>bant S</gradient><gradient:#C2A05D:#E3C565>ponge</gradient></b>")
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .customEnchants("<#E3C565>N<#D8B962>u<#CDAC60>l<#C2A05D>l<#D3B562>i<#E3CB68>f<#F4E06D>y")
            .lore(
                "Capable of absorbing <#359BBD>water <white>and",
                "even <#FF4500>lava <white>infinitely without drying.",
                "",
                "Press your <#F4E06D>swap key (F) <white>to",
                "switch between the absorption",
                "of <#359BBD>water <white>and <#FF4500>lava<white>. Or both...",
                "",
                "<#F4E06D>Current Mode: <#359BBD>Water <#F4E06D>& <#FF4500>Lava"
            )
            .tier(Tier.DEBUG) // Should be changed later on
            .persistentData(key.key)
            .buildPair()

        val meta = item.second.itemMeta
        meta?.persistentDataContainer?.set(modeKey, PersistentDataType.STRING, AbsorptionMode.BOTH.name)
        item.second.itemMeta = meta

        return item
    }

    override fun onPlaceBlock(player: Player, event: BlockPlaceEvent) {
        if (event.isCancelled) return
        val item = player.inventory.itemInMainHand
        val mode = getModeFromItem(item)
        removeNearbyBlocks(event.block, 6, *mode.materials.toTypedArray())
        event.isCancelled = true
    }

    override fun onPlayerSwapHands(player: Player, event: PlayerSwapHandItemsEvent) {

        val item = player.inventory.itemInMainHand
        val meta = item.itemMeta ?: return
        if (!meta.persistentDataContainer.has(key)) return

        val currentMode = getModeFromItem(item)
        val newMode = AbsorptionMode.entries[(currentMode.ordinal + 1) % AbsorptionMode.entries.size]
        meta.persistentDataContainer.set(modeKey, PersistentDataType.STRING, newMode.name)

        val newLore = meta.lore()?.map {
            val currentLoreText = LegacyComponentSerializer.legacySection().serialize(it)
            if (currentLoreText.contains("Current Mode:")) {
                MiniMessageUtil.mm("<#F4E06D>Current Mode: ${newMode.displayName}")
            } else {
                it // Shouldn't happen
            }
        } ?: listOf(MiniMessageUtil.mm("<#F4E06D>Current Mode: <#F4E06D><bold>${newMode.displayName}"))

        meta.lore(newLore)
        item.itemMeta = meta

        player.sendActionBar(MiniMessageUtil.mm("<#F4E06D>Now absorbing ${newMode.displayName}"))
        player.playSound(player.location, Sound.UI_HUD_BUBBLE_POP, 2.0f, 1.25f)

        event.isCancelled = true
    }

    private fun removeNearbyBlocks(sponge: Block, radius: Int, vararg types: Material) {

        val world = sponge.world
        val location = sponge.location

        val sphere = Sphere(location, radius.toDouble(), 0.0)
        val blocksInSphere = sphere.sphereFast

        for (targetBlock in blocksInSphere) {
            if (types.contains(Material.WATER)) removeWaterlogged(targetBlock)
            if (targetBlock.type !in types) continue
            targetBlock.type = Material.AIR
            world.spawnParticle(Particle.CLOUD, targetBlock.location.add(0.5, 0.5, 0.5), 10, 0.2, 0.2, 0.2, 0.02)
        }

        world.playSound(location, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.5f)
        world.playSound(location, Sound.BLOCK_WET_GRASS_BREAK, 1.0f, 1.0f)

        Bukkit.getScheduler().runTaskLaterAsynchronously(LumaItems.getInstance(), Runnable { if (sponge.type in types) sponge.type = Material.AIR }, 1L)
    }

    private fun removeWaterlogged(block: Block) {
        when (block.type) {
            Material.KELP, Material.KELP_PLANT,
            Material.SEAGRASS, Material.TALL_SEAGRASS -> {
                block.breakNaturally()
            }
            else -> {
                val blockData = block.blockData
                if (blockData is Waterlogged) {
                    blockData.isWaterlogged = false
                    block.blockData = blockData
                }
            }
        }
    }

    private fun getModeFromItem(item: ItemStack): AbsorptionMode {
        val meta = item.itemMeta ?: return AbsorptionMode.BOTH
        val modeString = meta.persistentDataContainer.get(modeKey, PersistentDataType.STRING) ?: AbsorptionMode.BOTH.name
        return Util.enumValueOfOrNull(AbsorptionMode::class.java, modeString) ?: AbsorptionMode.BOTH
    }

}