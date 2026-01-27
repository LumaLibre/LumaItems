package dev.lumas.lumaitems.registry;

import dev.lumas.lumaitems.LumaItems;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public record NamespacedIdentifier(NamespacedKey key) implements Identifier {

    public static NamespacedIdentifier lumaitems(String key) {
        return new NamespacedIdentifier(new NamespacedKey(LumaItems.getInstance(), key));
    }

    @NotNull
    @Override
    public NamespacedKey asNameSpacedKey() {
        return key;
    }

    @NotNull
    @Override
    public String asSimpleString() {
        return key.getKey();
    }
}
