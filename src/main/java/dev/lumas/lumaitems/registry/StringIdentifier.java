package dev.lumas.lumaitems.registry;

@SuppressWarnings("unused")
public record StringIdentifier(String key) implements Identifier {

    public static StringIdentifier of(String key) {
        return new StringIdentifier(key);
    }

    public static StringIdentifier normalized(String key) {
        return new StringIdentifier(key.toLowerCase().replace(" ", "_"));
    }
}
