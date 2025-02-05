package dev.jsinco.luma.lumaitems.enums;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public enum BlockConstants {

    BLACKLISTED( "SHULKER_BOX",
            Material.CHEST, Material.BARREL, Material.TRAPPED_CHEST,
            Material.FURNACE, Material.BLAST_FURNACE, Material.SMOKER,
            Material.HOPPER, Material.BREWING_STAND, Material.DROPPER,
            Material.DISPENSER, Material.BEDROCK, Material.END_PORTAL_FRAME,
            Material.SPAWNER, Material.COMMAND_BLOCK, Material.BARRIER,
            Material.STRUCTURE_BLOCK, Material.JIGSAW, Material.END_GATEWAY,
            Material.BUDDING_AMETHYST, Material.FARMLAND, Material.DIRT_PATH,
            Material.END_PORTAL, Material.REINFORCED_DEEPSLATE
    ),
    ORES(
            Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE, Material.COPPER_ORE,
            Material.DEEPSLATE_COPPER_ORE, Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE, Material.GOLD_ORE,
            Material.DEEPSLATE_GOLD_ORE, Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE, Material.NETHER_GOLD_ORE,
            Material.NETHER_QUARTZ_ORE, Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
            Material.ANCIENT_DEBRIS
    ),


    ;

    private final List<Material> materials;

    BlockConstants(String match, Material... materials) {
        this.materials = new ArrayList<>(List.of(materials));
        for (var material : Material.values()) {
            if (material.name().contains(match)) {
                this.materials.add(material);
            }
        }
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
}
