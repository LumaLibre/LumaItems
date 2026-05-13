package dev.lumas.lumaitems.util.tags;

import org.bukkit.Material;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@NullMarked
@ApiStatus.NonExtendable
public interface LinkedTags {

    LinkedTag<Material> LOG_TO_SAPLING = LinkedTag.<Material>linked()
            .link(Material.OAK_LOG, Material.OAK_SAPLING)
            .link(Material.BIRCH_LOG, Material.BIRCH_SAPLING)
            .link(Material.SPRUCE_LOG, Material.SPRUCE_SAPLING)
            .link(Material.JUNGLE_LOG, Material.JUNGLE_SAPLING)
            .link(Material.ACACIA_LOG, Material.ACACIA_SAPLING)
            .link(Material.DARK_OAK_LOG, Material.DARK_OAK_SAPLING)
            .link(Material.MANGROVE_LOG, Material.MANGROVE_PROPAGULE)
            .link(Material.CHERRY_LOG, Material.CHERRY_SAPLING)
            .link(Material.PALE_OAK_LOG, Material.PALE_OAK_SAPLING)
            .link(Material.CRIMSON_STEM, Material.CRIMSON_FUNGUS)
            .link(Material.WARPED_STEM, Material.WARPED_FUNGUS)
            .link(Material.BAMBOO, Material.BAMBOO)
            .build();

    LinkedTag<Material> LOG_TO_WOOD = LinkedTag.<Material>linked()
            .link(Material.OAK_LOG, Material.OAK_WOOD)
            .link(Material.BIRCH_LOG, Material.BIRCH_WOOD)
            .link(Material.SPRUCE_LOG, Material.SPRUCE_WOOD)
            .link(Material.JUNGLE_LOG, Material.JUNGLE_WOOD)
            .link(Material.ACACIA_LOG, Material.ACACIA_WOOD)
            .link(Material.DARK_OAK_LOG, Material.DARK_OAK_WOOD)
            .link(Material.MANGROVE_LOG, Material.MANGROVE_PROPAGULE)
            .link(Material.CHERRY_LOG, Material.CHERRY_WOOD)
            .link(Material.PALE_OAK_LOG, Material.PALE_OAK_WOOD)
            .link(Material.CRIMSON_STEM, Material.CRIMSON_HYPHAE)
            .link(Material.WARPED_STEM, Material.WARPED_HYPHAE)
            .link(Material.BAMBOO, Material.BAMBOO_MOSAIC)
            .build();

    LinkedTag<Material> SMELTABLE_ORES = LinkedTag.<Material>linked()
            .link(Material.GOLD_ORE, Material.GOLD_INGOT)
            .link(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT)
            .link(Material.NETHER_GOLD_ORE, Material.GOLD_INGOT)
            .link(Material.RAW_GOLD, Material.GOLD_INGOT)
            .link(Material.RAW_GOLD_BLOCK, Material.GOLD_BLOCK)
            .link(Material.IRON_ORE, Material.IRON_INGOT)
            .link(Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT)
            .link(Material.RAW_IRON, Material.IRON_INGOT)
            .link(Material.RAW_IRON_BLOCK, Material.IRON_BLOCK)
            .link(Material.COPPER_ORE, Material.COPPER_INGOT)
            .link(Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT)
            .link(Material.RAW_COPPER, Material.COPPER_INGOT)
            .link(Material.RAW_COPPER_BLOCK, Material.COPPER_BLOCK)
            .link(Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP)
            .build();
}
