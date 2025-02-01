package dev.jsinco.luma.lumaitems.enums;

import dev.jsinco.luma.lumaitems.LumaItems;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public enum DefaultAttributes {

    NETHERITE_HELMET(List.of(
            new AttributeContainer(Attribute.ARMOR, 3.0, EquipmentSlotGroup.HEAD),
            new AttributeContainer(Attribute.ARMOR_TOUGHNESS, 3.0, EquipmentSlotGroup.HEAD),
            new AttributeContainer(Attribute.KNOCKBACK_RESISTANCE, 1.0, EquipmentSlotGroup.HEAD))
    ),
    NETHERITE_CHESTPLATE(List.of(
            new AttributeContainer(Attribute.ARMOR, 8.0, EquipmentSlotGroup.CHEST),
            new AttributeContainer(Attribute.ARMOR_TOUGHNESS, 3.0, EquipmentSlotGroup.CHEST),
            new AttributeContainer(Attribute.KNOCKBACK_RESISTANCE, 1.0, EquipmentSlotGroup.CHEST))
    ),
    NETHERITE_LEGGINGS(List.of(
            new AttributeContainer(Attribute.ARMOR, 6.0, EquipmentSlotGroup.LEGS),
            new AttributeContainer(Attribute.ARMOR_TOUGHNESS, 2.0, EquipmentSlotGroup.LEGS),
            new AttributeContainer(Attribute.KNOCKBACK_RESISTANCE, 1.0, EquipmentSlotGroup.LEGS))
    ),
    NETHERITE_BOOTS(List.of(
            new AttributeContainer(Attribute.ARMOR, 3.0, EquipmentSlotGroup.FEET),
            new AttributeContainer(Attribute.ARMOR_TOUGHNESS, 3.0, EquipmentSlotGroup.FEET),
            new AttributeContainer(Attribute.KNOCKBACK_RESISTANCE, 1.0, EquipmentSlotGroup.FEET))
    ),
    NETHERITE_SWORD(List.of(
            new AttributeContainer(Attribute.ATTACK_DAMAGE, 8.0, EquipmentSlotGroup.HAND),
            new AttributeContainer(Attribute.ATTACK_SPEED, 1.6, EquipmentSlotGroup.HAND))
    ),
    NETHERITE_PICKAXE(List.of(
            new AttributeContainer(Attribute.ATTACK_DAMAGE, 6.0, EquipmentSlotGroup.HAND),
            new AttributeContainer(Attribute.ATTACK_SPEED, 1.2, EquipmentSlotGroup.HAND))
    );

    private final List<AttributeContainer> attributeContainers;

    DefaultAttributes(List<AttributeContainer> attributeContainers) {
        this.attributeContainers = new ArrayList<>(attributeContainers);
    }

    public Map<Attribute, AttributeModifier> getAttributes() {
        final Map<Attribute, AttributeModifier> attributeModifierMap = new HashMap<>();
        for (AttributeContainer attributeContainer : attributeContainers) {
            attributeModifierMap.put(attributeContainer.attribute, new AttributeModifier(
                    attributeContainer.getKey(),
                    attributeContainer.amount,
                    attributeContainer.operation,
                    attributeContainer.getSlot()
            ));
        }

        return attributeModifierMap;
    }

    public void addAttribute(AttributeContainer attributeModifier) {
        attributeContainers.add(attributeModifier);
    }
    public void addAttribute(String key, Attribute attribute, double amount, AttributeModifier.Operation operation, @Nullable EquipmentSlotGroup slot) {
        addAttribute(new AttributeContainer(key, attribute, operation, amount, slot));
    }

    public Map<Attribute, AttributeModifier> appendThenGetAttributes(AttributeContainer attributeContainer) {
        addAttribute(attributeContainer);
        return getAttributes();
    }

    public Map<Attribute, AttributeModifier> appendThenGetAttributes(String key,
                                                                     Attribute attribute,
                                                                     double amount,
                                                                    AttributeModifier.Operation operation,
                                                                    @Nullable EquipmentSlotGroup slot) {
        return appendThenGetAttributes(new AttributeContainer(key, attribute, operation, amount, slot));
    }

    public Map<Attribute, AttributeModifier> appendThenGetAttributes(Attribute attribute,
                                                                     String key,
                                                                     double amount,
                                                                     AttributeModifier.Operation operation,
                                                                     @Nullable EquipmentSlotGroup slot) {
        return appendThenGetAttributes(new AttributeContainer(key, attribute, operation, amount, slot));
    }

    public Map<Attribute, AttributeModifier> appendThenGetAttributes(Attribute attribute,
                                                                     String key,
                                                                     double amount,
                                                                     AttributeModifier.Operation operation) {
        return appendThenGetAttributes(new AttributeContainer(key, attribute, operation, amount, EquipmentSlotGroup.ANY));
    }



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

    public static Map<Attribute, AttributeModifier> of(Attribute attribute, String key, double amount, AttributeModifier.Operation operation, EquipmentSlotGroup slot) {
        return Map.of(attribute, new AttributeModifier(new NamespacedKey(LumaItems.getInstance(), key), amount, operation, slot));
    }
}
