package dev.jsinco.luma.lumaitems.enums;

import dev.jsinco.luma.lumaitems.LumaItems;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public enum DefaultAttributes {

    NETHERITE_HELMET(createArmorAttributes(ArmorType.NETHERITE, EquipmentSlotGroup.HEAD)),
    NETHERITE_CHESTPLATE(createArmorAttributes(ArmorType.NETHERITE, EquipmentSlotGroup.CHEST)),
    NETHERITE_LEGGINGS(createArmorAttributes(ArmorType.NETHERITE, EquipmentSlotGroup.LEGS)),
    NETHERITE_BOOTS(createArmorAttributes(ArmorType.NETHERITE, EquipmentSlotGroup.FEET)),

    // if u wanna add like diamond or gold etc u can like this (but have to add gold to armorType)
    // ex --> DIAMOND_HELMET(createArmorAttributes(ArmorType.DIAMOND, EquipmentSlotGroup.HEAD))

    NETHERITE_SWORD(createWeaponAttributes(8.0, 1.6)),
    NETHERITE_PICKAXE(createWeaponAttributes(6.0, 1.2));

    private final List<AttributeContainer> attributeContainers;

    DefaultAttributes(List<AttributeContainer> attributeContainers) {
        this.attributeContainers = List.copyOf(attributeContainers);
    }

    public Map<Attribute, AttributeModifier> getAttributes() {
        Map<Attribute, AttributeModifier> attributeModifierMap = new HashMap<>();
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

    private static List<AttributeContainer> createArmorAttributes(ArmorType type, EquipmentSlotGroup slot) {
        return List.of(
                new AttributeContainer(Attribute.ARMOR, type.getArmorValue(slot), slot),
                new AttributeContainer(Attribute.ARMOR_TOUGHNESS, type.getToughness(), slot),
                new AttributeContainer(Attribute.KNOCKBACK_RESISTANCE, type.getKnockbackResistance(), slot)
        );
    }

    private static List<AttributeContainer> createWeaponAttributes(double damage, double speed) {
        return List.of(
                new AttributeContainer(Attribute.ATTACK_DAMAGE, damage, EquipmentSlotGroup.HAND),
                new AttributeContainer(Attribute.ATTACK_SPEED, speed, EquipmentSlotGroup.HAND)
        );
    }

    public record AttributeContainer(String key, Attribute attribute, AttributeModifier.Operation operation, double amount, @Nullable EquipmentSlotGroup slot) {
        public AttributeContainer(Attribute attribute, double amount, @Nullable EquipmentSlotGroup slot) {
            this("defaultattribute", attribute, AttributeModifier.Operation.ADD_NUMBER, amount, slot);
        }

        public NamespacedKey getKey() {
            return new NamespacedKey(LumaItems.getInstance(), key);
        }

        public EquipmentSlotGroup getSlot() {
            return slot == null ? EquipmentSlotGroup.ANY : slot;
        }
    }
}
