package dev.jsinco.luma.lumaitems.obj;

import dev.jsinco.luma.lumaitems.LumaItems;
import dev.jsinco.luma.lumaitems.enums.DefaultAttributes;
import dev.jsinco.luma.lumaitems.enums.EntityAttributes;
import kotlin.Pair;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public record AttributeContainer(String key,
                                 Attribute attribute,
                                 AttributeModifier.Operation operation,
                                 double amount,
                                 @Nullable EquipmentSlotGroup slot) {

    private final static char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    private final static Random random = new Random();

    public AttributeContainer(Attribute attribute, double amount, @Nullable EquipmentSlotGroup slot) {
        this("def-" + generateKey(), attribute, AttributeModifier.Operation.ADD_NUMBER, getAmountBasedFromPlayer(attribute, amount), slot);
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

    public static String generateKey() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 7; i++) {
            sb.append(chars[random.nextInt(chars.length)]);
        }

        return sb.toString();
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
