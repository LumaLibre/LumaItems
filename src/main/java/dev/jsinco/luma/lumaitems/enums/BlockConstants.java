package dev.jsinco.luma.lumaitems.enums;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public enum BlockConstants {

    BLACKLISTED( "SHULKER_BOX",
            Material.CHEST, Material.BARREL, Material.TRAPPED_CHEST,
            Material.FURNACE, Material.BLAST_FURNACE, Material.SMOKER,
            Material.HOPPER, Material.BREWING_STAND, Material.DROPPER,
            Material.DISPENSER, Material.BEDROCK, Material.END_PORTAL_FRAME,
            Material.SPAWNER, Material.COMMAND_BLOCK, Material.BARRIER,
            Material.STRUCTURE_BLOCK, Material.JIGSAW, Material.END_GATEWAY,
            Material.BUDDING_AMETHYST, Material.FARMLAND, Material.DIRT_PATH,
            Material.END_PORTAL, Material.REINFORCED_DEEPSLATE, Material.TRIAL_SPAWNER,
            Material.VAULT
    ),
    ORES(
            Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE, Material.COPPER_ORE,
            Material.DEEPSLATE_COPPER_ORE, Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE, Material.GOLD_ORE,
            Material.DEEPSLATE_GOLD_ORE, Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE, Material.NETHER_GOLD_ORE,
            Material.NETHER_QUARTZ_ORE, Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE
    ),
    COLORED_GLASS(
            Material.LIGHT_BLUE_STAINED_GLASS, Material.BLACK_STAINED_GLASS, Material.BLUE_STAINED_GLASS,
            Material.BROWN_STAINED_GLASS, Material.CYAN_STAINED_GLASS, Material.GRAY_STAINED_GLASS,
            Material.GREEN_STAINED_GLASS, Material.LIGHT_GRAY_STAINED_GLASS, Material.LIME_STAINED_GLASS,
            Material.MAGENTA_STAINED_GLASS, Material.ORANGE_STAINED_GLASS, Material.PINK_STAINED_GLASS,
            Material.PURPLE_STAINED_GLASS, Material.RED_STAINED_GLASS, Material.WHITE_STAINED_GLASS,
            Material.YELLOW_STAINED_GLASS
    ),
    GLASS(
            COLORED_GLASS.materials, Material.GLASS, Material.TINTED_GLASS
    ),
    CROPS(
            Material.WHEAT, Material.CARROTS, Material.POTATOES, Material.BEETROOTS,
            Material.NETHER_WART, Material.COCOA, Material.SWEET_BERRIES, Material.SUGAR_CANE,
            Material.MELON, Material.PUMPKIN, Material.KELP_PLANT
    )
    ;

    private final List<Material> materials;

    BlockConstants(String match, Material... materials) {
        this.materials = new ArrayList<>(List.of(materials));
        for (var material : Material.values()) {
            if (Pattern.compile(match).matcher(material.name()).matches()) {
                this.materials.add(material);
            }
        }
    }

    BlockConstants(List<Material> materialList, Material... materials) {
        this.materials = new ArrayList<>(materialList);
        this.materials.addAll(List.of(materials));
    }

    BlockConstants(Material... materials) {
        this.materials = List.of(materials);
    }

    public List<Material> getMaterials() {
        return materials;
    }

    public boolean contains(Material material) {
        return materials.contains(material);
    }

    public List<Material> getButExclude(Material... exclude) {
        List<Material> materials = new ArrayList<>(this.materials);
        for (var material : exclude) {
            materials.remove(material);
        }
        return materials;
    }
}
