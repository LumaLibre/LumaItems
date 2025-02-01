package dev.jsinco.luma.lumaitems.enums;

import org.bukkit.inventory.EquipmentSlotGroup;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public enum ArmorType {

    NETHERITE(Map.of(
            EquipmentSlotGroup.HEAD, 3.0,
            EquipmentSlotGroup.CHEST, 8.0,
            EquipmentSlotGroup.LEGS, 6.0,
            EquipmentSlotGroup.FEET, 3.0
    ), 3.0, 1.0),

    DIAMOND(Map.of(
            EquipmentSlotGroup.HEAD, 3.0,
            EquipmentSlotGroup.CHEST, 8.0,
            EquipmentSlotGroup.LEGS, 6.0,
            EquipmentSlotGroup.FEET, 3.0
    ), 2.0, 0.0),;

    private final Map<EquipmentSlotGroup, Double> armorValues;
    private final double toughness;
    private final double knockbackResistance;

    ArmorType(Map<EquipmentSlotGroup, Double> armorValues, double toughness, double knockbackResistance) {
        this.armorValues = armorValues;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
    }

    public double getArmorValue(EquipmentSlotGroup slot) {
        return armorValues.getOrDefault(slot, 0.0);
    }

    public double getToughness() {
        return toughness;
    }

    public double getKnockbackResistance() {
        return knockbackResistance;
    }
}
