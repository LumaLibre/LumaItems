package dev.lumas.lumaitems.configuration;

import dev.lumas.lumaitems.registry.Identifier;
import dev.lumas.lumaitems.registry.RegistryItem;
import dev.lumas.lumaitems.registry.StringIdentifier;
import eu.okaeri.configs.OkaeriConfig;

public abstract class OkaeriFile extends OkaeriConfig implements RegistryItem {

    @Override
    public Identifier identifier() {
        File annotation = getClass().getAnnotation(File.class);
        if (annotation == null) {
            throw new IllegalStateException("OkaeriFile must be annotated with @File");
        }
        return StringIdentifier.of(annotation.value());
    }
}
