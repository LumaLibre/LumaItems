package dev.lumas.lumaitems.items.astral.sets

import dev.lumas.lumaitems.annotations.Disable
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.enums.ToolType
import dev.lumas.lumaitems.enums.WorldName
import dev.lumas.lumaitems.items.astral.AstralSet
import dev.lumas.lumaitems.items.astral.AstralSetFactory
import dev.lumas.lumaitems.util.AbilityUtil
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.QuickTasks
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.ExperienceOrb
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

@Disable(WorldName.EVENT_NEW)
class MagmaticSet : AstralSet {

    companion object {
        private val smeltOreTypes = listOf(
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.NETHER_GOLD_ORE,
            Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE, Material.COPPER_ORE,
            Material.DEEPSLATE_COPPER_ORE, Material.ANCIENT_DEBRIS
        )
    }

    override fun setItems(): List<ItemStack> {
        val factory = AstralSetFactory("magmatic-set","Magmatic", mutableListOf("&#AC87FBVolcanic"))

        factory.commonEnchants = mutableMapOf(
            Enchantment.PROTECTION to 5, Enchantment.SHARPNESS to 6, Enchantment.UNBREAKING to 6,
            Enchantment.SWEEPING_EDGE to 4, Enchantment.FIRE_ASPECT to 3, Enchantment.EFFICIENCY to 5,
            Enchantment.FORTUNE to 3, Enchantment.LOOTING to 3, Enchantment.FEATHER_FALLING to 4,
            Enchantment.THORNS to 3
        )

        val materials: List<Material> = listOf(
            Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS,
            Material.DIAMOND_BOOTS, Material.DIAMOND_SWORD, Material.DIAMOND_PICKAXE, Material.DIAMOND_SHOVEL
        )

        for (material in materials) {
            val type = ToolType.getToolType(material) ?: continue
            val lore = when (type) {
                ToolType.PICKAXE -> mutableListOf("Breaking ores with this", "tool will automatically", "smelt them")
                ToolType.SHOVEL -> mutableListOf("Breaking sand with this", "tool will automatically", "convert it to glass")
                ToolType.SWORD -> mutableListOf("Right-click to send out flames", "and ignite entities", "", "&cCooldown: 10s")
                else -> mutableListOf()
            }

            factory.astralSetItemGenericEnchantOnly(
                material,
                lore
            )
        }

        return factory.createdAstralItems
    }

    override fun setIdentifier(): String {
        return "magmatic-set"
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        val genericMCToolType = ToolType.getToolType(player.inventory.itemInMainHand)
        when (type) {
            Action.RIGHT_CLICK -> {
                // poorly written but im in a rush
                if ((Util.isItemInSlot("magmatic-set", EquipmentSlot.HAND, player)|| Util.isItemInSlot("magmatic-set", EquipmentSlot.OFF_HAND, player)) && genericMCToolType == ToolType.SWORD && !QuickTasks.isOnCooldown(this, player)) {
                    AbilityUtil.spawnSpell(player, Particle.FLAME, "magmatic-set", 120L)
                    QuickTasks.addCooldown(this, player.uniqueId, 200L)
                }
            }
            Action.PROJECTILE_LAND -> {
                event as ProjectileHitEvent
                if (AbilityUtil.noDamagePermission(player, event.hitEntity ?: return false)) return false
                igniteEntity(event.hitEntity as LivingEntity)
            }
            Action.BREAK_BLOCK -> {
                event as BlockBreakEvent
                if (genericMCToolType == ToolType.PICKAXE) {
                    if (pickaxeSmelt(event.block, event.block.getDrops(player.inventory.itemInMainHand))) event.isDropItems = false
                } else if (genericMCToolType == ToolType.SHOVEL) {
                    if (shovelSmelt(event.block, event.block.getDrops(player.inventory.itemInMainHand))) event.isDropItems = false
                }
            }

            else -> return false
        }
        return true
    }

    private fun igniteEntity(entity: LivingEntity) {
        for (i in 0..60) {
            entity.world.spawnParticle(Particle.FLAME, entity.location, 1, 0.3, 0.3, 0.3, 0.2)
        }
        entity.world.playSound(entity.location, Sound.ENTITY_BLAZE_SHOOT, 1f, 0.9f)
        entity.fireTicks = 100
    }

    @Suppress("DEPRECATION")
    private fun pickaxeSmelt(blockBroken: Block, drops: Collection<ItemStack>): Boolean {
        if (!smeltOreTypes.contains(blockBroken.type)) return false

        for (drop in drops) {
            when (drop.type) {
                Material.RAW_GOLD, Material.GOLD_NUGGET -> drop.setType(Material.GOLD_INGOT)
                Material.RAW_IRON -> drop.setType(Material.IRON_INGOT)
                Material.RAW_COPPER -> drop.setType(Material.COPPER_INGOT)
                Material.ANCIENT_DEBRIS -> drop.setType(Material.NETHERITE_SCRAP)
                else -> continue
            }

        }
        blockBroken.world.spawn(blockBroken.location, ExperienceOrb::class.java).experience = 1
        for (i in drops.indices) {
            blockBroken.world.dropItemNaturally(blockBroken.location, drops.iterator().next())
        }
        return true
    }

    @Suppress("DEPRECATION")
    private fun shovelSmelt(blockBroken: Block, drops: Collection<ItemStack>): Boolean {
        if (blockBroken.type != Material.SAND && blockBroken.type != Material.RED_SAND) return false
        for (drop in drops) {
            drop.setType(Material.GLASS)
        }
        for (i in drops.indices) {
            blockBroken.world.dropItemNaturally(blockBroken.location, drops.iterator().next())
        }
        return true
    }

}