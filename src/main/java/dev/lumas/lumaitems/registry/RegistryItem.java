package dev.lumas.lumaitems.registry;

import org.jetbrains.annotations.NotNull;

public interface RegistryItem {
    @NotNull
    Identifier identifier();
}
