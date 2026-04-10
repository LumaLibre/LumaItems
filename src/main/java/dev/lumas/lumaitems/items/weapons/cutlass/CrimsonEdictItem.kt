package dev.lumas.lumaitems.items.weapons.cutlass

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.extensions.itemStack
import dev.lumas.lumaitems.util.extensions.setTexture
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack

class CrimsonEdictItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#A71C2E:#E26736:#C31F54>Crimson Edict</gradient></b>")
            .customEnchants("<#A71C2E>Head Hunter")
            .material(Material.NETHERITE_SWORD)
            .persistentData("crimson-edict")
            .tier(Tier.WONDERLAND_2026)
            .lore(
                "A sword that can behead",
                "mobs, including players.",
                "",
                "When <#A71C2E>killing</#A71C2E> entities,",
                "their head may drop",
                "as an item."
            )
            .vanillaEnchants(
                Enchantment.SHARPNESS to 8,
                Enchantment.SMITE to 5,
                Enchantment.LOOTING to 4,
                Enchantment.FIRE_ASPECT to 5,
                Enchantment.MENDING to 1
            )
            .buildPair()
    }

    override fun onEntityDeath(player: Player, event: EntityDeathEvent) {

        val entity = event.entity
        val mapped = MappedHeads.fromEntityType(entity.type) ?: return

        if (random().nextInt(101) < mapped.weight) {
            val item = mapped.head(entity)
            event.drops.add(item)
        }
    }

    private enum class MappedHeads(val entityType: EntityType, val weight: Int, val head: (LivingEntity) -> ItemStack) {
        PLAYER(EntityType.PLAYER, 2, { killed ->
            Material.PLAYER_HEAD.itemStack { meta ->
                meta.setTexture(killed as Player)
            }
        }),
        ZOMBIE(EntityType.ZOMBIE, 5, { _ -> Material.ZOMBIE_HEAD.itemStack() }),
        CREEPER(EntityType.CREEPER, 5, { _ -> Material.CREEPER_HEAD.itemStack() }),
        SKELETON(EntityType.SKELETON, 5, { _ -> Material.SKELETON_SKULL.itemStack() }),
        WITHER_SKELETON(EntityType.WITHER_SKELETON, 5, { _ -> Material.WITHER_SKELETON_SKULL.itemStack() }),
        PIGLIN(EntityType.PIGLIN, 2, { _ -> Material.PIGLIN_HEAD.itemStack() }),
        ENDER_DRAGON(EntityType.ENDER_DRAGON, 1, { _ -> Material.DRAGON_HEAD.itemStack() });

        companion object {
            fun fromEntityType(entityType: EntityType): MappedHeads? {
                return entries.find { it.entityType == entityType }
            }
        }
    }
}