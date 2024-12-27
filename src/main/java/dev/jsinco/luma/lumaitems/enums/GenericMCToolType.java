package dev.jsinco.luma.lumaitems.enums;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Enum for generic Minecraft tool types.
 * Determine their type without regard for the type of material.
 */
public enum GenericMCToolType {

    HELMET,
    CHESTPLATE,
    LEGGINGS,
    BOOTS,
    SWORD,
    PICKAXE,
    AXE,
    SHOVEL,
    HOE,
    CROSSBOW,
    BOW,
    TRIDENT,
    SHIELD,
    ELYTRA,
    FISHING_ROD,
    MAGICAL,
    MACE;

    public static final List<String> magicMaterials = List.of("BLAZE_ROD");

    @Nullable
    public static GenericMCToolType getToolType(ItemStack item) {
        return getToolType(item.getType().toString());
    }

    @Nullable
    public static GenericMCToolType getToolType(Material material) {
        return getToolType(material.toString());
    }

    @Nullable
    public static GenericMCToolType getToolType(String string) {
        string = string.toUpperCase();
        if (magicMaterials.contains(string)) {
            return MAGICAL;
        }

        for (GenericMCToolType toolType : GenericMCToolType.values()) {
            if (string.contains(toolType.toString())) {
                return toolType;
            }
        }
        return null;
    }
}
