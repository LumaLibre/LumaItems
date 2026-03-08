package dev.lumas.lumaitems.items.tools.mattock

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.enums.BlockConstants
import dev.lumas.lumaitems.hooks.ProtocolLibHook
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.model.Synchronizable
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.shapes.Cuboid
import dev.lumas.lumaitems.util.Kind
import dev.lumas.lumaitems.util.PacketGlowColors
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.getOreColor
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.tiers.Tier
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.joml.Vector3f

class VigilanceMattockItem : CustomItemFunctions() {

    companion object {
        private val DISPLAYABLE_BLOCKS = ConcurrentLinkedQueue<DisplayableBlock>()
        private val BLINDNESS = PotionEffect(PotionEffectType.BLINDNESS, 300, 0, true, false, false)
        private val TRANSLATION = Vector3f(0.001f, 0.0002f, 0.001f)
        private val KEY = Util.namespacedKey("vigilance-mattock")
        private const val SCALAR = 0.995f
        private const val RANGE = 3.0
        private const val RENDER_RANGE = 0.09f


        init {
            Registry.HOOKS.getOrThrow(ProtocolLibHook::class).getProtocolManager()?.addPacketListener(
                object: PacketAdapter(LumaItems.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Server.NAMED_SOUND_EFFECT, PacketType.Play.Server.ENTITY_SOUND) {
                    override fun onPacketSending(event: PacketEvent) {
                        if (event.player.inventory.itemInMainHand.itemMeta?.persistentDataContainer?.has(KEY) == true) {
                            event.isCancelled = true
                        }
                    }
                }
            )
        }
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#2c272c:#842e5e:#bc6997:#392739>Vigilance Mattock</gradient></b>")
            .customEnchants("<#3B1C3B>Paranoia")
            .material(Material.NETHERITE_PICKAXE)
            .persistentData(KEY)
            .tier(Tier.HALLOWEEN_2025)
            .lore(
                "While equipped, this",
                "tool will cause nearby",
                "ores to glow at the",
                "cost of reduced sight,",
                "movement, and hearing.",
            )
            .vanillaEnchants(
                Enchantment.SHARPNESS to 7,
                Enchantment.UNBREAKING to 5,
                Enchantment.EFFICIENCY to 8,
                Enchantment.SILK_TOUCH to 1,
                Enchantment.MENDING to 1,
            )
            .buildPair()
    }


    override fun asyncGlobalTask() {
        for (displayableBlock in DISPLAYABLE_BLOCKS) {
            val player = Bukkit.getPlayer(displayableBlock.owner)
            if (player == null) {
                displayableBlock.remove()
                continue
            }

            if (!Util.isItemInSlot(KEY, EquipmentSlot.HAND, player)) {
                this.clearPotionEffects(player)
                displayableBlock.remove()
            }
        }
    }

    override fun onFastAsyncRunnable(player: Player) {
        for (displayableBlock in DISPLAYABLE_BLOCKS.filter { it.owner == player.uniqueId }) {
            displayableBlock.sync {
                if (displayableBlock.canBeRemoved()) {
                    displayableBlock.remove()
                }
            }
        }
        if (player.inventory.itemInMainHand.isMatchingItem(KEY)) {
            player.sync {
                getNearbyOreDisplayableBlocks(player)
                    .takeIf { it.isNotEmpty() }
                    ?.apply { DISPLAYABLE_BLOCKS.addAll(this) }
            }
        }
    }

    override fun onRunnable(player: Player) {
        this.addPotionEffects(player)
    }

    override fun onPlayerItemHeld(player: Player, event: PlayerItemHeldEvent) {
        val item = player.inventory.getItem(event.newSlot)

        if (item == null || !Util.hasPersistentKey(item, KEY)) {
            this.clearDisplayableBlocks(player)
            this.clearPotionEffects(player)
        } else {
            this.addPotionEffects(player)
        }
    }

    override fun onPlayerSwapHands(player: Player, event: PlayerSwapHandItemsEvent) {
        if (Util.hasPersistentKey(event.mainHandItem, KEY)) {
            this.addPotionEffects(player)
        } else {
            this.clearDisplayableBlocks(player)
            this.clearPotionEffects(player)
        }
    }

    override fun onPlayerQuit(player: Player, event: PlayerQuitEvent) {
        this.clearDisplayableBlocks(player)
        this.clearPotionEffects(player)
    }

    override fun onPluginDisableGlobal() {
        DISPLAYABLE_BLOCKS.forEach { it.remove() }
        DISPLAYABLE_BLOCKS.clear()
    }

    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        DISPLAYABLE_BLOCKS.forEach {
            if (it.isAtBlock(event.block)) {
                it.remove()
            }
        }
    }

    private fun addPotionEffects(player: Player) {
        player.sync { player.addPotionEffect(BLINDNESS) }
    }

    private fun clearPotionEffects(player: Player) {
        player.sync { player.removePotionEffect(PotionEffectType.BLINDNESS) }
    }


    private fun getNearbyOreDisplayableBlocks(player: Player): List<DisplayableBlock> {
        val center = player.location
        val cube = Cuboid(center.clone().add(-RANGE, -RANGE, -RANGE), center.clone().add(RANGE, RANGE, RANGE))
        val list = mutableListOf<DisplayableBlock>()

        cube.blockListConsumer { block ->
            if (DISPLAYABLE_BLOCKS.none { it.isAtBlock(block) } && Kind.INCLUSIVE_ORES.isTagged(block.type)) {
                displayableBlock(block, player).let {
                    list.add(it)
                }
            }
        }
        return list
    }


    private fun displayableBlock(block: Block, player: Player): DisplayableBlock {
        val blockData = block.blockData
        val blockDisplay = block.world.spawn(block.location, BlockDisplay::class.java).apply {
            this.block = blockData
            glowColorOverride = block.getOreColor() ?: blockData.mapColor
            interpolationDelay = -1
            interpolationDuration = 0
            isPersistent = false
            viewRange = RENDER_RANGE

            transformation = transformation.apply {
                scale.mul(SCALAR)
                translation.add(TRANSLATION)
            }
        }

        val displayableBlock = DisplayableBlock(player.uniqueId, blockDisplay, RANGE.plus(2.0), block)

        PacketGlowColors.setProtocolGlowPacket(player, blockDisplay, true)
        return displayableBlock
    }

    private fun clearDisplayableBlocks(player: Player) {
        DISPLAYABLE_BLOCKS.filter { it.owner == player.uniqueId }
            .forEach { it.remove() }
    }


    private class DisplayableBlock(
        val owner: UUID,
        val blockDisplay: BlockDisplay,
        val range: Double,
        override val x: Int,
        val y: Int,
        override val z: Int,
        override val world: World
    ) : Synchronizable.BlockPos {

        constructor(owner: UUID, blockDisplay: BlockDisplay, range: Double, block: Block) :
                this(owner, blockDisplay, range, block.x, block.y, block.z, block.world)

        fun ownerAsPlayer(): Player? = Bukkit.getPlayer(owner)

        fun remove() {
            blockDisplay.sync { blockDisplay.remove() }
            DISPLAYABLE_BLOCKS.remove(this)
        }

        fun canBeRemoved(): Boolean {
            val player = ownerAsPlayer() ?: return true
            return player.world != blockDisplay.world || blockDisplay.location.distanceSquared(player.location) > range * range || blockDisplay.location.block.isEmpty
        }

        fun isAtBlock(block: Block): Boolean {
            return block.x == x && block.y == y && block.z == z && block.world == world
        }
    }
}