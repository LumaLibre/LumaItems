package dev.jsinco.luma.util;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

@Deprecated(forRemoval = true)
public class BestTool {

    public static final List<Material> AXE = new ArrayList<>();
    public static final List<Material> PICKAXE = new ArrayList<>();
    public static final List<Material> SHOVEL = new ArrayList<>();
    public static final List<Material> HOE = new ArrayList<>();
    public static final List<Material> SHEARS = new ArrayList<>();

    public void materialMatching() {
        for (Material material : Material.values()) {
            if (!material.isBlock()) continue;
            String m = material.name().toUpperCase();

            // Is there a better way to do this?

            if (!m.contains("POWDER")) {
                if (m.contains("STONE") || m.contains("GRANITE") || m.contains("DIORITE") || m.contains("ANDESITE") || m.contains("DEEPSLATE") || m.contains("TUFF")
                        || m.contains("BRICK") || m.contains("SANDSTONE") || m.contains("PRISMARINE") || m.contains("BASALT") || m.contains("PURPUR") || m.contains("NETHERRACK")
                        || m.contains("COAL") || m.contains("IRON") || m.contains("GOLD") || m.contains("DIAMOND") || m.contains("EMERALD") || m.contains("LAPIS") || m.contains("REDSTONE")
                        || m.contains("NETHERITE") || m.contains("QUARTZ") || m.contains("DEBRIS") || m.contains("AMETHYST") || m.contains("COPPER") || m.contains("TERRACOTTA")
                        || m.contains("CONCRETE") || m.contains("SHULKER") || m.contains("CALCITE") || m.contains("DRIPSTONE") || m.contains("ICE") || m.contains("MAGMA") || m.contains("OBSIDIAN") || m.contains("NYLIUM") || m.contains("BONE") || m.contains("CORAL") || m.contains("FURNACE") || m.contains("ANVIL")
                        || m.contains("ENCHANTING_TABLE") || m.contains("BREWING_STAND") || m.contains("BELL") || m.contains("CAULDRON") || m.contains("LODESTONE") || m.contains("LIGHTNING_ROD")
                        || m.contains("ENDER_CHEST") || m.contains("RESPAWN_ANCHOR") || m.contains("PISTON") || m.contains("HOPPER") || m.contains("DROPPER") || m.contains("DISPENSER")
                        || m.contains("RAIL") || m.contains("SPAWNER") || m.contains("LANTERN") || m.contains("BEDROCK")) {
                    PICKAXE.add(material);
                }
            }

            if (!m.contains("LEAVES") || !m.contains("ENDER") || !m.contains("WART") || !m.contains("FUNGUS") || !m.contains("ROOTS") || !m.contains("POTTED") || !m.contains("NYLIUM")){
                if (m.contains("OAK") || m.contains("SPRUCE") || m.contains("BIRCH") || m.contains("ACACIA") || m.contains("JUNGLE") || m.contains("MANGROVE")
                        || m.contains("CHERRY") || m.contains("BAMBOO") || m.contains("CRIMSON") || m.contains("WARPED") || m.contains("BED") || m.contains("BANNER")
                        || m.contains("BOOKSHELF") || m.contains("MUSHROOM_BLOCK") || m.contains("MUSHROOM_STEM") || m.contains("PUMPKIN") || m.contains("MELON") || m.contains("COCOA")
                        || m.contains("JACK_O_LANTERN") || m.contains("BEE") || m.contains("TABLE") || m.contains("LOOM") || m.contains("COMPOSTER") || m.contains("CAMPFIRE")
                        || m.contains("NOTE_BLOCK") || m.contains("JUKEBOX") || m.contains("LADDER") || m.contains("CHEST") || m.contains("BARREL") || m.contains("LECTERN")) {
                    AXE.add(material);
                }
            }

            if (!m.contains("SANDSTONE")) {
                if (m.contains("SAND") || m.contains("POWDER") || m.contains("GRAVEL") || m.contains("SNOW") || m.contains("CLAY") || m.contains("SOIL") || m.contains("GRASS_BLOCK")
                        || m.contains("PODZOL") || m.contains("MYCELIUM") || m.contains("DIRT") || m.equals("MUD") || m.equals("MUDDY_MANGROVE_ROOTS")) {
                    SHOVEL.add(material);
                }
            }

            if (m.contains("MOSS") || m.contains("LEAVES") || m.contains("WART") || m.contains("SHROOMLIGHT") || m.contains("KELP") || m.contains("HAY")
                    || m.contains("SCULK") || m.contains("FROGLIGHT") || m.contains("SPONGE")) {
                HOE.add(material);
            }

            if (!m.contains("MOSS")) {
                if (m.contains("WOOL") || m.contains("CARPET")) {
                    SHEARS.add(material);
                }
            }

        }
    }

}
