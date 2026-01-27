package dev.lumas.lumaitems.registry;

import dev.lumas.lumaitems.LumaItems;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public record StringIdentifier(String key) implements Identifier {

    public static StringIdentifier of(String key) {
        return new StringIdentifier(key);
    }

    public static StringIdentifier normalized(String key) {
        return new StringIdentifier(key.toLowerCase().replace(" ", "_"));
    }

    @NotNull
    @Override
    public NamespacedKey asNameSpacedKey() {
        return new NamespacedKey(LumaItems.getInstance(), key);
    }

    @NotNull
    @Override
    public String asSimpleString() {
        return key;
    }
}
