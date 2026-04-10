package dev.lumas.lumaitems.util;

import com.destroystokyo.paper.MaterialSetTag;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

/**
 * More {@link Tag}s
 */
@ApiStatus.NonExtendable
public interface Kind<T extends Keyed> extends Tag<T> {

    Tag<Material> GLAZED_TERRACOTTA = Kind.material("glazed_terracotta", material -> material.name().endsWith("_GLAZED_TERRACOTTA"));

    Tag<Material> ORES = Kind.material("ores", Tag.COAL_ORES, Tag.IRON_ORES, Tag.COPPER_ORES, Tag.GOLD_ORES, Tag.REDSTONE_ORES, Tag.EMERALD_ORES, Tag.LAPIS_ORES, Tag.DIAMOND_ORES);

    /**
     * Tag of all ores including {@link Material#NETHER_QUARTZ_ORE} but excluding {@link Material#ANCIENT_DEBRIS}
     */
    Tag<Material> INCLUSIVE_ORES = Kind.material("inclusive_ores", ORES, Material.NETHER_QUARTZ_ORE);

    Tag<Material> GLASS = Kind.material("glass", material -> material.name().matches("(?i)^(?:glass|tinted_glass|[a-z]+_stained_glass(?:_pane)?|glass_pane)$"));

    Tag<Material> GLASS_BLOCK = Kind.material("glass_block", material -> material.name().matches("(?i)^(?:glass|tinted_glass|[a-z]+_stained_glass)$"));

    Tag<Material> COLORED_GLASS = Kind.material("colored_glass", material -> material.name().endsWith("_STAINED_GLASS"));

    Tag<Material> COLORED_GLASS_PANE = Kind.material("colored_glass_pane", material -> material.name().endsWith("_STAINED_GLASS_PANE"));

    Tag<Material> COLORED_GLASS_INCLUSIVE = Kind.material("colored_glass_inclusive", COLORED_GLASS, COLORED_GLASS_PANE);

    Tag<Material> CROPS = Kind.material("crops", Tag.CROPS, Material.NETHER_WART);

    /**
     * Generally any type of block that holds data or is illegal to obtain in survival.
     */
    Tag<Material> BLACKLIST = Kind.material("blacklist", List.of(Tag.SHULKER_BOXES, Tag.SIGNS), Material.CHEST, Material.BARREL, Material.TRAPPED_CHEST, Material.FURNACE, Material.BLAST_FURNACE, Material.SMOKER, Material.HOPPER, Material.BREWING_STAND, Material.DROPPER, Material.DISPENSER, Material.BEDROCK, Material.END_PORTAL_FRAME, Material.SPAWNER, Material.COMMAND_BLOCK, Material.REPEATING_COMMAND_BLOCK, Material.CHAIN_COMMAND_BLOCK, Material.BARRIER, Material.STRUCTURE_BLOCK, Material.JIGSAW, Material.END_GATEWAY, Material.BUDDING_AMETHYST, Material.FARMLAND, Material.DIRT_PATH, Material.END_PORTAL, Material.REINFORCED_DEEPSLATE, Material.TRIAL_SPAWNER, Material.VAULT);

    Tag<Material> WOODS = Kind.material("woods", Tag.LOGS, Tag.PLANKS, Tag.WOODEN_BUTTONS, Tag.WOODEN_DOORS, Tag.WOODEN_FENCES, Tag.WOODEN_PRESSURE_PLATES, Tag.WOODEN_SHELVES, Tag.WOODEN_SLABS, Tag.WOODEN_STAIRS, Tag.WOODEN_TRAPDOORS, Tag.ALL_SIGNS);

    static MaterialSetTag material(@NotNull String key, @NotNull Predicate<Material> filter) {
        return new MaterialSetTag(Util.namespacedKey(key), filter.and(material -> !material.isLegacy()));
    }

    @SafeVarargs
    static MaterialSetTag material(@NotNull String key, Tag<Material>... tags) {
        Collection<Material> materials = new HashSet<>();
        for (Tag<Material> tag : tags) {
            materials.addAll(tag.getValues());
        }
        return new MaterialSetTag(Util.namespacedKey(key), materials);
    }

    static MaterialSetTag material(@NotNull String key, @NotNull Tag<Material> tag, @NotNull Material... materials) {
        Collection<Material> materialCollection = new HashSet<>(tag.getValues());
        Collections.addAll(materialCollection, materials);
        return new MaterialSetTag(Util.namespacedKey(key), materialCollection);
    }

    static MaterialSetTag material(@NotNull String key, @NotNull Collection<Tag<Material>> collection, @NotNull Material... materials) {
        Collection<Material> materialCollection = new HashSet<>();
        for (Tag<Material> tag : collection) {
            materialCollection.addAll(tag.getValues());
        }
        Collections.addAll(materialCollection, materials);
        return new MaterialSetTag(Util.namespacedKey(key), materialCollection);
    }

    static MaterialSetTag material(@NotNull String key, @NotNull Material... materials) {
        return new MaterialSetTag(Util.namespacedKey(key), materials);
    }
}
