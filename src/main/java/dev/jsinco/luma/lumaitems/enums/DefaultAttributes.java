package dev.jsinco.luma.lumaitems.enums;

import dev.jsinco.luma.lumaitems.LumaItems;
import dev.jsinco.luma.lumaitems.obj.AttributeContainer;
import org.bukkit.Material;
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

    //<editor-fold desc="Gear Type Values" defaultstate="uncollapsed">
    // Netherite
    NETHERITE_HELMET(
            container(Attribute.ARMOR, 3.0, EquipmentSlotGroup.HEAD),
            container(Attribute.ARMOR_TOUGHNESS, 3.0, EquipmentSlotGroup.HEAD),
            container(Attribute.KNOCKBACK_RESISTANCE, 1.0, EquipmentSlotGroup.HEAD)
    ),
    NETHERITE_CHESTPLATE(
            container(Attribute.ARMOR, 8.0, EquipmentSlotGroup.CHEST),
            container(Attribute.ARMOR_TOUGHNESS, 3.0, EquipmentSlotGroup.CHEST),
            container(Attribute.KNOCKBACK_RESISTANCE, 1.0, EquipmentSlotGroup.CHEST)
    ),
    NETHERITE_LEGGINGS(
            container(Attribute.ARMOR, 6.0, EquipmentSlotGroup.LEGS),
            container(Attribute.ARMOR_TOUGHNESS, 2.0, EquipmentSlotGroup.LEGS),
            container(Attribute.KNOCKBACK_RESISTANCE, 1.0, EquipmentSlotGroup.LEGS)
    ),
    NETHERITE_BOOTS(
            container(Attribute.ARMOR, 3.0, EquipmentSlotGroup.FEET),
            container(Attribute.ARMOR_TOUGHNESS, 3.0, EquipmentSlotGroup.FEET),
            container(Attribute.KNOCKBACK_RESISTANCE, 1.0, EquipmentSlotGroup.FEET)
    ),
    NETHERITE_SWORD(
            container(Attribute.ATTACK_DAMAGE, 8.0, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 1.6, EquipmentSlotGroup.MAINHAND)
    ),
    NETHERITE_PICKAXE(
            container(Attribute.ATTACK_DAMAGE, 6.0, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 1.2, EquipmentSlotGroup.MAINHAND)
    ),
    NETHERITE_AXE(
            container(Attribute.ATTACK_DAMAGE, 10.0, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 1.0, EquipmentSlotGroup.MAINHAND)
    ),
    NETHERITE_SHOVEL(
            container(Attribute.ATTACK_DAMAGE, 6.5, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 1.0, EquipmentSlotGroup.MAINHAND)
    ),
    NETHERITE_HOE(
            container(Attribute.ATTACK_DAMAGE, 1.0, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 4.0, EquipmentSlotGroup.MAINHAND)
    ),
    // Diamond
    DIAMOND_HELMET(
            container(Attribute.ARMOR, 3.0, EquipmentSlotGroup.HEAD),
            container(Attribute.ARMOR_TOUGHNESS, 2.0, EquipmentSlotGroup.HEAD)
    ),
    DIAMOND_CHESTPLATE(
            container(Attribute.ARMOR, 8.0, EquipmentSlotGroup.CHEST),
            container(Attribute.ARMOR_TOUGHNESS, 2.0, EquipmentSlotGroup.CHEST)
    ),
    DIAMOND_LEGGINGS(
            container(Attribute.ARMOR, 6.0, EquipmentSlotGroup.LEGS),
            container(Attribute.ARMOR_TOUGHNESS, 2.0, EquipmentSlotGroup.LEGS)
    ),
    DIAMOND_BOOTS(
            container(Attribute.ARMOR, 3.0, EquipmentSlotGroup.FEET),
            container(Attribute.ARMOR_TOUGHNESS, 2.0, EquipmentSlotGroup.FEET)
    ),
    DIAMOND_SWORD(
            container(Attribute.ATTACK_DAMAGE, 7.0, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 1.6, EquipmentSlotGroup.MAINHAND)
    ),
    DIAMOND_PICKAXE(
            container(Attribute.ATTACK_DAMAGE, 5.0, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 1.2, EquipmentSlotGroup.MAINHAND)
    ),
    DIAMOND_AXE(
            container(Attribute.ATTACK_DAMAGE, 9.0, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 1.0, EquipmentSlotGroup.MAINHAND)
    ),
    DIAMOND_SHOVEL(
            container(Attribute.ATTACK_DAMAGE, 5.5, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 1.0, EquipmentSlotGroup.MAINHAND)
    ),
    DIAMOND_HOE(
            container(Attribute.ATTACK_DAMAGE, 1.0, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 4.0, EquipmentSlotGroup.MAINHAND)
    ),
    // Gold
    GOLDEN_HELMET(
            container(Attribute.ARMOR, 2.0, EquipmentSlotGroup.HEAD)
    ),
    GOLDEN_CHESTPLATE(
            container(Attribute.ARMOR, 5.0, EquipmentSlotGroup.CHEST)
    ),
    GOLDEN_LEGGINGS(
            container(Attribute.ARMOR, 3.0, EquipmentSlotGroup.LEGS)
    ),
    GOLDEN_BOOTS(
            container(Attribute.ARMOR, 1.0, EquipmentSlotGroup.FEET)
    ),
    GOLDEN_SWORD(
            container(Attribute.ATTACK_DAMAGE, 4.0, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 1.6, EquipmentSlotGroup.MAINHAND)
    ),
    GOLDEN_PICKAXE(
            container(Attribute.ATTACK_DAMAGE, 2.0, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 1.2, EquipmentSlotGroup.MAINHAND)
    ),
    GOLDEN_AXE(
            container(Attribute.ATTACK_DAMAGE, 7.0, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 1.0, EquipmentSlotGroup.MAINHAND)
    ),
    GOLDEN_SHOVEL(
            container(Attribute.ATTACK_DAMAGE, 2.5, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 1.0, EquipmentSlotGroup.MAINHAND)
    ),
    GOLDEN_HOE(
            container(Attribute.ATTACK_DAMAGE, 1.0, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 1.0, EquipmentSlotGroup.MAINHAND)
    ),
    // Iron
    IRON_HELMET(
            container(Attribute.ARMOR, 2.0, EquipmentSlotGroup.HEAD)
    ),
    IRON_CHESTPLATE(
            container(Attribute.ARMOR, 6.0, EquipmentSlotGroup.CHEST)
    ),
    IRON_LEGGINGS(
            container(Attribute.ARMOR, 5.0, EquipmentSlotGroup.LEGS)
    ),
    IRON_BOOTS(
            container(Attribute.ARMOR, 2.0, EquipmentSlotGroup.FEET)
    ),
    IRON_SWORD(
            container(Attribute.ATTACK_DAMAGE, 6.0, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 1.6, EquipmentSlotGroup.MAINHAND)
    ),
    IRON_PICKAXE(
            container(Attribute.ATTACK_DAMAGE, 4.0, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 1.2, EquipmentSlotGroup.MAINHAND)
    ),
    IRON_AXE(
            container(Attribute.ATTACK_DAMAGE, 9.0, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 0.9, EquipmentSlotGroup.MAINHAND)
    ),
    IRON_SHOVEL(
            container(Attribute.ATTACK_DAMAGE, 4.5, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 1.0, EquipmentSlotGroup.MAINHAND)
    ),
    IRON_HOE(
            container(Attribute.ATTACK_DAMAGE, 1.0, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 3.0, EquipmentSlotGroup.MAINHAND)
    ),
    // Chainmail & Stone
    CHAINMAIL_HELMET(
            container(Attribute.ARMOR, 2.0, EquipmentSlotGroup.HEAD)
    ),
    CHAINMAIL_CHESTPLATE(
            container(Attribute.ARMOR, 5.0, EquipmentSlotGroup.CHEST)
    ),
    CHAINMAIL_LEGGINGS(
            container(Attribute.ARMOR, 4.0, EquipmentSlotGroup.LEGS)
    ),
    CHAINMAIL_BOOTS(
            container(Attribute.ARMOR, 1.0, EquipmentSlotGroup.FEET)
    ),
    STONE_SWORD(
            container(Attribute.ATTACK_DAMAGE, 5.0, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 1.6, EquipmentSlotGroup.MAINHAND)
    ),
    STONE_PICKAXE(
            container(Attribute.ATTACK_DAMAGE, 3.0, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 1.2, EquipmentSlotGroup.MAINHAND)
    ),
    STONE_AXE(
            container(Attribute.ATTACK_DAMAGE, 9.0, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 0.8, EquipmentSlotGroup.MAINHAND)
    ),
    STONE_SHOVEL(
            container(Attribute.ATTACK_DAMAGE, 3.5, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 1.0, EquipmentSlotGroup.MAINHAND)
    ),
    STONE_HOE(
            container(Attribute.ATTACK_DAMAGE, 1.0, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 2.0, EquipmentSlotGroup.MAINHAND)
    ),
    // Leather & Wood
    LEATHER_HELMET(
            container(Attribute.ARMOR, 1.0, EquipmentSlotGroup.HEAD)
    ),
    LEATHER_CHESTPLATE(
            container(Attribute.ARMOR, 3.0, EquipmentSlotGroup.CHEST)
    ),
    LEATHER_LEGGINGS(
            container(Attribute.ARMOR, 2.0, EquipmentSlotGroup.LEGS)
    ),
    LEATHER_BOOTS(
            container(Attribute.ARMOR, 1.0, EquipmentSlotGroup.FEET)
    ),
    WOODEN_SWORD(
            container(Attribute.ATTACK_DAMAGE, 4.0, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 1.6, EquipmentSlotGroup.MAINHAND)
    ),
    WOODEN_PICKAXE(
            container(Attribute.ATTACK_DAMAGE, 2.0, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 1.2, EquipmentSlotGroup.MAINHAND)
    ),
    WOODEN_AXE(
            container(Attribute.ATTACK_DAMAGE, 7.0, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 0.8, EquipmentSlotGroup.MAINHAND)
    ),
    WOODEN_SHOVEL(
            container(Attribute.ATTACK_DAMAGE, 2.5, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 1.0, EquipmentSlotGroup.MAINHAND)
    ),
    WOODEN_HOE(
            container(Attribute.ATTACK_DAMAGE, 1.0, EquipmentSlotGroup.MAINHAND),
            container(Attribute.ATTACK_SPEED, 1.0, EquipmentSlotGroup.MAINHAND)
    ),
    ;
    //</editor-fold>

    private final List<AttributeContainer> attributeContainers;

    DefaultAttributes(AttributeContainer... attributeContainers) {
        this.attributeContainers = List.of(attributeContainers);
    }

    public Map<Attribute, AttributeModifier> getAttributes() {
        final Map<Attribute, AttributeModifier> attributeModifierMap = new HashMap<>();
        for (AttributeContainer attributeContainer : attributeContainers) {
            attributeModifierMap.put(attributeContainer.attribute(), new AttributeModifier(
                    attributeContainer.getKey(),
                    attributeContainer.amount(),
                    attributeContainer.operation(),
                    attributeContainer.getSlot()
            ));
        }

        return attributeModifierMap;
    }


    public Map<Attribute, AttributeModifier> appendThenGetAttributes(AttributeContainer... attributeContainer) {
        List<AttributeContainer> newContainers = new ArrayList<>(attributeContainers);
        newContainers.addAll(List.of(attributeContainer));
        return newContainers.stream().collect(HashMap::new, (map, container) ->
                map.put(container.attribute(), new AttributeModifier(
                container.getKey(),
                container.amount(),
                container.operation(),
                container.getSlot()
        )), HashMap::putAll);
    }

    public Map<Attribute, AttributeModifier> appendThenGetAttributes(String key, Attribute attribute, double amount, AttributeModifier.Operation operation, @Nullable EquipmentSlotGroup slot) {
        return appendThenGetAttributes(new AttributeContainer(key, attribute, operation, amount, slot));
    }

    public Map<Attribute, AttributeModifier> appendThenGetAttributes(Attribute attribute, String key, double amount, AttributeModifier.Operation operation, @Nullable EquipmentSlotGroup slot) {
        return appendThenGetAttributes(new AttributeContainer(key, attribute, operation, amount, slot));
    }

    public Map<Attribute, AttributeModifier> appendThenGetAttributes(Attribute attribute, String key, double amount, AttributeModifier.Operation operation) {
        return appendThenGetAttributes(new AttributeContainer(key, attribute, operation, amount, EquipmentSlotGroup.ANY));
    }

    @Nullable
    public static DefaultAttributes getFromMaterial(Material material) {
        for (DefaultAttributes value : values()) {
            if (value.name().equals(material.name())) {
                return value;
            }
        }
        return null;
    }

    public List<AttributeContainer> getAttributeContainers() {
        return attributeContainers;
    }

    public static Map<Attribute, AttributeModifier> of(Attribute attribute, String key, double amount, AttributeModifier.Operation operation, EquipmentSlotGroup slot) {
        return Map.of(attribute, new AttributeModifier(new NamespacedKey(LumaItems.getInstance(), key), amount, operation, slot));
    }

    private static AttributeContainer container(Attribute attribute, double amt, EquipmentSlotGroup slot) {
        return new AttributeContainer(attribute, amt, slot);
    }
}
