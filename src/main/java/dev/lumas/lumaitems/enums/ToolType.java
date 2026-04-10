package dev.lumas.lumaitems.enums;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Enum for generic Minecraft tool types.
 * Determine their type without regard for the type of material.
 */
public enum ToolType {

    HELMET,
    CHESTPLATE,
    LEGGINGS,
    BOOTS,
    SWORD,
    SPEAR,
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
    MACE,
    SHEARS;

    public static final List<String> magicMaterials = List.of("BLAZE_ROD");


    public boolean is(Material material) {
        return getToolType(material) == this;
    }

    @Nullable
    public static ToolType getToolType(ItemStack item) {
        return getToolType(item.getType().toString());
    }

    @Nullable
    public static ToolType getToolType(Material material) {
        return getToolType(material.toString());
    }

    @Nullable
    public static ToolType getToolType(String string) {
        string = string.toUpperCase();
        if (magicMaterials.contains(string)) {
            return MAGICAL;
        }

        for (ToolType toolType : ToolType.values()) {
            if (string.contains(toolType.toString())) {
                return toolType;
            }
        }
        return null;
    }
}
