package dev.lumas.lumaitems.util.tags;

import org.bukkit.Keyed;
import org.jspecify.annotations.NullMarked;

import java.util.LinkedHashMap;
import java.util.Map;

@NullMarked
public class LinkedTag<T extends Keyed> extends BiLinkedTag<T, T> {

    protected LinkedTag(Map<T, T> mapping) {
        super(mapping);
    }

    public static <T extends Keyed> Builder<T> linked() {
        return new Builder<>();
    }

    public static class Builder<T extends Keyed> extends BiLinkedTag.Builder<T, T> {

        @Override
        public Builder<T> link(T from, T to) {
            mapping.put(from, to);
            return this;
        }

        @Override
        public LinkedTag<T> build() {
            return new LinkedTag<>(mapping);
        }
    }
}
