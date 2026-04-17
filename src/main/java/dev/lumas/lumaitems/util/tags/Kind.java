package dev.lumas.lumaitems.util.tags;

import com.destroystokyo.paper.MaterialSetTag;
import dev.lumas.lumaitems.util.Util;
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
import java.util.regex.Pattern;

/**
 * More {@link Tag}s
 */
@ApiStatus.NonExtendable
public interface Kind<T extends Keyed> extends Tag<T> {

    Tag<Material> GLAZED_TERRACOTTA = Kind.material("glazed_terracotta").endsWith("GLAZED_TERRACOTTA").ensureSize("GLAZED_TERRACOTTA", 16).lock();

    Tag<Material> ORES = Kind.material("ores", Tag.COAL_ORES, Tag.IRON_ORES, Tag.COPPER_ORES, Tag.GOLD_ORES, Tag.REDSTONE_ORES, Tag.EMERALD_ORES, Tag.LAPIS_ORES, Tag.DIAMOND_ORES).lock();

    /**
     * Tag of all ores including {@link Material#NETHER_QUARTZ_ORE} but excluding {@link Material#ANCIENT_DEBRIS}
     */
    Tag<Material> INCLUSIVE_ORES = Kind.material("inclusive_ores", ORES, Material.NETHER_QUARTZ_ORE).lock();

    Tag<Material> GLASS = Kind.material("glass", Pattern.compile("(?i)^(?:glass|tinted_glass|[a-z]+_stained_glass(?:_pane)?|glass_pane)$")).lock();

    Tag<Material> GLASS_BLOCK = Kind.material("glass_block", Pattern.compile("(?i)^(?:glass|tinted_glass|[a-z]+_stained_glass)$")).lock();

    Tag<Material> COLORED_GLASS = Kind.material("colored_glass").endsWith("STAINED_GLASS").ensureSize("COLORED_GLASS", 16).lock();

    Tag<Material> COLORED_GLASS_PANE = Kind.material("colored_glass_pane").endsWith("STAINED_GLASS_PANE").ensureSize("COLORED_GLASS_PANE", 16).lock();

    Tag<Material> COLORED_GLASS_INCLUSIVE = Kind.material("colored_glass_inclusive", COLORED_GLASS, COLORED_GLASS_PANE).lock();

    Tag<Material> CROPS = Kind.material("crops", Tag.CROPS, Material.NETHER_WART).lock();

    /**
     * Generally any type of block that holds data or is illegal to obtain in survival.
     */
    Tag<Material> BLACKLIST = Kind.material("blacklist", List.of(Tag.SHULKER_BOXES, Tag.SIGNS), Material.CHEST, Material.BARREL, Material.TRAPPED_CHEST, Material.FURNACE, Material.BLAST_FURNACE, Material.SMOKER, Material.HOPPER, Material.BREWING_STAND, Material.DROPPER, Material.DISPENSER, Material.BEDROCK, Material.END_PORTAL_FRAME, Material.SPAWNER, Material.COMMAND_BLOCK, Material.REPEATING_COMMAND_BLOCK, Material.CHAIN_COMMAND_BLOCK, Material.BARRIER, Material.STRUCTURE_BLOCK, Material.JIGSAW, Material.END_GATEWAY, Material.BUDDING_AMETHYST, Material.FARMLAND, Material.DIRT_PATH, Material.END_PORTAL, Material.REINFORCED_DEEPSLATE, Material.TRIAL_SPAWNER, Material.VAULT).lock();

    Tag<Material> WOODS = Kind.material("woods", Tag.LOGS, Tag.PLANKS, Tag.WOODEN_BUTTONS, Tag.WOODEN_DOORS, Tag.WOODEN_FENCES, Tag.WOODEN_PRESSURE_PLATES, Tag.WOODEN_SHELVES, Tag.WOODEN_SLABS, Tag.WOODEN_STAIRS, Tag.WOODEN_TRAPDOORS, Tag.ALL_SIGNS).lock();

    Tag<Material> SAPLING_GROWABLE = Kind.material("soils", Pattern.compile(".*(MOSS|DIRT|MYCELIUM|PODZOL|GRASS_BLOCK|MUD|MUDDY_MANGROVE_ROOTS)")).lock();

    static MaterialSetTag material(@NotNull String key, @NotNull Predicate<Material> filter) {
        return new MaterialSetTag(Util.namespacedKey(key), filter.and(material -> !material.isLegacy()));
    }

    static MaterialSetTag material(@NotNull String key, @NotNull Pattern regex) {
        return new MaterialSetTag(Util.namespacedKey(key), material -> regex.matcher(material.name()).matches() && !material.isLegacy());
    }

    static MaterialSetTag material(@NotNull String key) {
        return new MaterialSetTag(Util.namespacedKey(key));
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
