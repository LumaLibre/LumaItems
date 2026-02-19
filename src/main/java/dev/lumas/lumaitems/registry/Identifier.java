package dev.lumas.lumaitems.registry;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

public interface Identifier {
    @NotNull
    NamespacedKey asNameSpacedKey();

    @NotNull
    String asSimpleString();
}
