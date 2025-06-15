package dev.jsinco.luma.lumaitems.obj;

import dev.jsinco.luma.lumaitems.LumaItems;
import dev.jsinco.luma.lumaitems.enums.EntityAttributes;
import kotlin.Pair;
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

    public static AttributeContainer defaultAttributeContainer(Attribute attribute, double amount, @Nullable EquipmentSlotGroup slot) {
        return new AttributeContainer("defaultattribute", attribute, AttributeModifier.Operation.ADD_NUMBER, getAmountBasedFromPlayer(attribute, amount), slot);
    }

    public static AttributeContainer of(String key, Attribute attribute, AttributeModifier.Operation operation, double amount, @Nullable EquipmentSlotGroup slot) {
        return new AttributeContainer(key, attribute, operation, amount, slot);
    }

    public static AttributeContainer of(NamespacedKey key, Attribute attribute, AttributeModifier.Operation operation, double amount, @Nullable EquipmentSlotGroup slot) {
        return new AttributeContainer(key.getKey(), attribute, operation, amount, slot);
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

    public static double getAmountBasedFromPlayer(Attribute attribute, double value) {
        EntityAttributes defaultPlayerAttributes = EntityAttributes.PLAYER;

        for (Pair<Attribute, Double> attributeContainer : defaultPlayerAttributes.getValues()) {
            if (attributeContainer.getFirst() != attribute) {
                continue;
            }

            // Player has a default value of 4.0 for X attribute,
            // We pass in 1.6 as our argument 'value' and return -2.4
            return value - attributeContainer.getSecond();
        }
        return value;
    }
}
