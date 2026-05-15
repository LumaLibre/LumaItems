package dev.lumas.lumaitems.model.item;

import com.google.common.base.Preconditions;
import dev.lumas.lumaitems.LumaItems;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;

public record AttributeContainer(String key,
                                 Attribute attribute,
                                 AttributeModifier.Operation operation,
                                 double amount,
                                 @Nullable EquipmentSlotGroup slot) {

    private static final String CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";

    public NamespacedKey getKey() {
        return new NamespacedKey(LumaItems.getInstance(), key);
    }

    public EquipmentSlotGroup getSlot() {
        if (slot == null) {
            return EquipmentSlotGroup.ANY;
        }
        return slot;
    }

    public AttributeModifier modifier() {
        return new AttributeModifier(getKey(), amount, operation, getSlot());
    }

    public static String generateStringKey(int length) {
        StringBuilder keyBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = (int) (Math.random() * CHARS.length());
            keyBuilder.append(CHARS.charAt(randomIndex));
        }
        return keyBuilder.toString();
    }


    public static AttributeContainer of(String key, Attribute attribute, AttributeModifier.Operation operation, double amount, @Nullable EquipmentSlotGroup slot) {
        return new AttributeContainer(key, attribute, operation, amount, slot);
    }

    public static AttributeContainer of(NamespacedKey key, Attribute attribute, AttributeModifier.Operation operation, double amount, @Nullable EquipmentSlotGroup slot) {
        return new AttributeContainer(key.getKey(), attribute, operation, amount, slot);
    }

    public static AttributeContainer from(Attribute attribute, AttributeModifier attributeModifier) {
        return of(attributeModifier.getKey(), attribute, attributeModifier.getOperation(), attributeModifier.getAmount(), attributeModifier.getSlotGroup());
    }

    public static @Unmodifiable List<AttributeContainer> fromDefaults(Material type) {
        return type.getDefaultAttributeModifiers().entries().stream()
                .filter(it -> it.getValue() != null)
                .map(it -> from(it.getKey(), it.getValue()))
                .toList();
    }

    public static Map<Attribute, AttributeModifier> ofMap(Attribute attribute, String key, double amount, AttributeModifier.Operation operation, EquipmentSlotGroup slot) {
        return Map.of(attribute, new AttributeModifier(new NamespacedKey(LumaItems.getInstance(), key), amount, operation, slot));
    }
    public static Map<Attribute, AttributeModifier> ofMap(NamespacedKey key, Attribute attribute, double amount, AttributeModifier.Operation operation, EquipmentSlotGroup slot) {
        return Map.of(attribute, new AttributeModifier(key, amount, operation, slot));
    }


    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(NamespacedKey key) {
        return new Builder().setKey(key);
    }


    public static class Builder {
        private String key;
        private Attribute attribute;
        private AttributeModifier.Operation operation;
        private double amount;
        private @Nullable EquipmentSlotGroup slot;

        public Builder setKey(String key) {
            this.key = key;
            return this;
        }

        public Builder setKey(NamespacedKey key) {
            this.key = key.getKey();
            return this;
        }

        public Builder setAttribute(Attribute attribute) {
            this.attribute = attribute;
            return this;
        }

        public Builder setOperation(AttributeModifier.Operation operation) {
            this.operation = operation;
            return this;
        }

        public Builder setAmount(double amount) {
            this.amount = amount;
            return this;
        }

        public Builder setSlot(EquipmentSlotGroup slot) {
            this.slot = slot;
            return this;
        }

        public AttributeContainer build() {
            Preconditions.checkNotNull(key, "Key must not be null");
            Preconditions.checkNotNull(attribute, "Attribute must not be null");
            Preconditions.checkNotNull(operation, "Operation must not be null");
            Preconditions.checkArgument(amount != 0, "Amount must not be zero");
            return new AttributeContainer(key, attribute, operation, amount, slot);
        }
    }
}
