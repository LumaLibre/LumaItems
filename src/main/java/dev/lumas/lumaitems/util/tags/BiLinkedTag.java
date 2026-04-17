package dev.lumas.lumaitems.util.tags;

import org.bukkit.Keyed;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@NullMarked
public class BiLinkedTag<T extends Keyed, V extends Keyed> {

    private final Map<T, V> mapping = new HashMap<>();
    private final Map<V, T> inverseMapping = new HashMap<>();
    private final Set<T> keys;
    private final Set<V> values;

    protected BiLinkedTag(Map<T, V> mapping) {
        this.mapping.putAll(mapping);
        for (Map.Entry<T, V> entry : mapping.entrySet()) {
            this.inverseMapping.put(entry.getValue(), entry.getKey());
        }
        this.keys = Collections.unmodifiableSet(mapping.keySet());
        this.values = Set.copyOf(mapping.values());
    }

    @Nullable
    public V get(T key) {
        return mapping.get(key);
    }

    @Nullable
    public T getInverse(V value) {
        return inverseMapping.get(value);
    }

    public boolean hasKey(T key) {
        return mapping.containsKey(key);
    }

    public boolean hasValue(V value) {
        return values.contains(value);
    }

    public Set<T> keys() {
        return keys;
    }

    public Set<V> values() {
        return values;
    }

    public static <T extends Keyed, V extends Keyed> Builder<T, V> biLinked() {
        return new Builder<>();
    }

    public static class Builder<T extends Keyed, V extends Keyed> {
        protected final Map<T, V> mapping = new LinkedHashMap<>();

        public Builder<T, V> link(T from, V to) {
            mapping.put(from, to);
            return this;
        }

        public BiLinkedTag<T, V> build() {
            return new BiLinkedTag<>(mapping);
        }
    }
}