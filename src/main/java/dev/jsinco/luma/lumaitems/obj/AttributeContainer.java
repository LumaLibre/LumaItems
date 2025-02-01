package dev.jsinco.luma.lumaitems.obj;

import dev.jsinco.luma.lumaitems.LumaItems;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.jetbrains.annotations.Nullable;

public record AttributeContainer(String key,
                                 Attribute attribute,
                                 AttributeModifier.Operation operation,
                                 double amount,
                                 @Nullable EquipmentSlotGroup slot) {

    public AttributeContainer(Attribute attribute, double amount, @Nullable EquipmentSlotGroup slot) {
        this("defaultattribute", attribute, AttributeModifier.Operation.ADD_NUMBER, amount, slot);
    }

    public NamespacedKey getKey() {
        return new NamespacedKey(LumaItems.getInstance(), key);
    }

    public EquipmentSlotGroup getSlot() {
        if (slot == null) {
            return EquipmentSlotGroup.ANY;
        }
        return slot;
    }
}
