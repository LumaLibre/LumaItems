package dev.lumas.lumaitems.enums;

import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

import static dev.lumas.lumaitems.enums.ToolType.*;

import java.util.List;

// Enum for generic tooltypes
public enum GenericToolType {

    ARMOR(HELMET, CHESTPLATE, LEGGINGS, BOOTS),
    WEAPON(SWORD, SPEAR, AXE, BOW, CROSSBOW, TRIDENT, SHIELD, MACE),
    TOOL(PICKAXE, AXE, SHOVEL, HOE, FISHING_ROD);

    private final List<ToolType> toolTypes;

    GenericToolType(ToolType... array) {
        this.toolTypes = List.of(array);
    }

    public List<ToolType> getToolTypes() {
        return toolTypes;
    }

    @Nullable
    public static GenericToolType getGenericToolType(Material material) {
        for (GenericToolType genericToolType : GenericToolType.values()) {
            for (ToolType toolType : genericToolType.getToolTypes()) {
                if (toolType.is(material)) {
                    return genericToolType;
                }
            }
        }
        return null;
    }


    public EquipmentSlot getEquipmentSlot() {
        switch (this) {
            case ARMOR -> {
                return EquipmentSlot.CHEST;
            }
            case WEAPON, TOOL -> {
                return EquipmentSlot.HAND;
            }
        }
        return null;
    }
}
