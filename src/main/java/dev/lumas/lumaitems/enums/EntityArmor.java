package dev.lumas.lumaitems.enums;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public enum EntityArmor {

    HELMET,
    CHESTPLATE,
    LEGGINGS,
    BOOTS;


    public static @Nullable EntityArmor getEquipmentSlotFromType(Material material) {
        String name = material.toString();

        for (EntityArmor equipment : EntityArmor.values()) {
            if (name.contains(equipment.name())) {
                return equipment;
            }
        }
        return null;
    }

    public void setEntityArmorSlot(LivingEntity entity, ItemStack itemStack) {
        if (entity.getEquipment() == null) {
            return;
        }
        switch (this) {
            case HELMET -> entity.getEquipment().setHelmet(itemStack);
            case CHESTPLATE -> entity.getEquipment().setChestplate(itemStack);
            case LEGGINGS -> entity.getEquipment().setLeggings(itemStack);
            case BOOTS -> entity.getEquipment().setBoots(itemStack);
        }
    }

}
