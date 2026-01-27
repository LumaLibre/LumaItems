package dev.lumas.lumaitems.enums;

import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Enum for generic tooltypes
public enum GenericToolType {
    ARMOR(List.of("HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS")),
    WEAPON(List.of("SWORD", "AXE", "BOW", "CROSSBOW", "TRIDENT", "SHIELD", "MACE")),
    TOOL(List.of("PICKAXE", "AXE", "SHOVEL", "HOE", "ROD"));

    private final List<String> gearType;

    GenericToolType(List<String> list) {
        this.gearType = list;
    }

    public List<String> getGearTypes() {
        return gearType;
    }

    public static List<String> getArmorStrings() {
        return ARMOR.getGearTypes();
    }

    public static List<String> getWeaponStrings() {
        return WEAPON.getGearTypes();
    }

    public static List<String> getToolStrings() {
        return TOOL.getGearTypes();
    }

    @Nullable
    public static GenericToolType getGenericToolType(Material material) {
        for (String string :  GenericToolType.getArmorStrings()) {
            if (material.toString().contains(string)) return GenericToolType.ARMOR;
        }
        for (String string :  GenericToolType.getToolStrings()) {
            if (material.toString().contains(string)) return GenericToolType.TOOL;
        }
        for (String string :  GenericToolType.getWeaponStrings()) {
            if (material.toString().contains(string)) return GenericToolType.WEAPON;
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
