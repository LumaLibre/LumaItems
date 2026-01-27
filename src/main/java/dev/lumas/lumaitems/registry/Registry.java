package dev.lumas.lumaitems.registry;

import dev.lumas.lumaitems.configuration.ConfigManager;
import dev.lumas.lumaitems.configuration.OkaeriFile;
import dev.lumas.lumaitems.configuration.files.RelicsYml;
import dev.lumas.lumaitems.manager.CustomItem;
import dev.lumas.lumaitems.manager.NamedCustomItem;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public final class Registry<T extends RegistryItem> implements Iterable<Map.Entry<Identifier, T>> {

    public static final Registry<CustomItem> CUSTOM_ITEM_REGISTRY = new Registry<>();
    public static final Registry<NamedCustomItem> NAMED_CUSTOM_ITEM_REGISTRY = new Registry<>();
    public static final Registry<OkaeriFile> CONFIG_REGISTRY = fromClassesWithCrafter(new ConfigManager(), RelicsYml.class);


    private final Map<Identifier, T> map;

    public Registry(Collection<T> values) {
        this.map = new HashMap<>();
        values.forEach(item -> {
            map.put(item.identifier(), item);
        });
    }

    public Registry() {
        this.map = new HashMap<>();
    }

    @Nullable
    public T get(Identifier identifier) {
        return map.get(identifier);
    }

    @Nullable
    public <A extends T> A get(KClass<A> kClass) {
        return get(JvmClassMappingKt.getJavaClass(kClass));
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <A extends T> A get(Class<A> clazz) {
        return (A) map.values().stream()
                .filter(it -> it.getClass().equals(clazz))
                .findFirst()
                .orElse(null);
    }

    @NotNull
    public <A extends T> A getOrThrow(KClass<A> kClass) {
        return getOrThrow(JvmClassMappingKt.getJavaClass(kClass));
    }

    @NotNull
    public <A extends T> A getOrThrow(Class<A> clazz) {
        A item = get(clazz);
        if (item == null) {
            throw new IllegalStateException("No registry item found for class " + clazz.getName());
        }
        return item;
    }

    public T put(T item) {
        return map.put(item.identifier(), item);
    }

    public Collection<T> values() {
        return map.values();
    }

    public Collection<Identifier> keySet() {
        return map.keySet();
    }

    public <K extends Identifier> Collection<K> keySet(KClass<K> kClass) {
        return keySet(JvmClassMappingKt.getJavaClass(kClass));
    }

    public <K extends Identifier> Collection<K> keySet(Class<K> clazz) {
        List<K> keys = new ArrayList<>();
        for (Identifier id : map.keySet()) {
            if (clazz.isInstance(id)) {
                keys.add(clazz.cast(id));
            }
        }
        return keys;
    }

    public int size() {
        return map.size();
    }

    public Stream<Map.Entry<Identifier, T>> stream() {
        return map.entrySet().stream();
    }

    @Override
    public @NotNull Iterator<Map.Entry<Identifier, T>> iterator() {
        return map.entrySet().iterator();
    }

    @SuppressWarnings({"unchecked", "RedundantCast"})
    @SafeVarargs
    public static <E extends RegistryItem> Registry<E> fromClassesWithCrafter(RegistryCrafter crafter, Class<? extends E>... classes) {
        List<E> eClasses = new ArrayList<>();
        for (Class<?> clazz : classes) {
            if (crafter instanceof RegistryCrafter.Extension<?> crafter1) {
                eClasses.add((E) crafter1.craft(clazz));
            } else if (crafter instanceof RegistryCrafter.NoExtension crafter2) {
                eClasses.add((E) crafter2.craft(clazz));
            } else {
                throw new IllegalArgumentException("Unknown crafter type");
            }
        }
        return new Registry<>(eClasses.stream().filter(Objects::nonNull).toList());
    }

    @SafeVarargs
    public static <E extends RegistryItem> Registry<E> fromClasses(Class<? extends E>... classes) {
        return ConstructableClassBuilder.builder().addClasses(classes).build();
    }

    public static <E extends RegistryItem> Registry<E> fromClasses(Collection<Class<?>> constructorParamTypes, Collection<Object> constructorParams, Collection<Class<? extends E>> classes) {
        return ConstructableClassBuilder.builder().addConstructorParameters(constructorParamTypes, constructorParams).addClasses(classes).build();
    }


    public static class ConstructableClassBuilder {
        private final List<Class<?>> constructorClassTypes = new ArrayList<>();
        private final List<Object> constructorClassValues = new ArrayList<>();
        private final List<Class<?>> classes = new ArrayList<>();

        public static ConstructableClassBuilder builder() {
            return new ConstructableClassBuilder();
        }

        public ConstructableClassBuilder addConstructorParameter(Class<?> type, Object value) {
            constructorClassTypes.add(type);
            constructorClassValues.add(value);
            return this;
        }

        public ConstructableClassBuilder addConstructorParameters(Collection<Class<?>> types, Collection<Object> values) {
            constructorClassTypes.addAll(types);
            constructorClassValues.addAll(values);
            return this;
        }

        public ConstructableClassBuilder addClass(Class<?> clazz) {
            classes.add(clazz);
            return this;
        }

        public ConstructableClassBuilder addClasses(Class<?>... clazz) {
            classes.addAll(List.of(clazz));
            return this;
        }

        public <E> ConstructableClassBuilder addClasses(Collection<Class<? extends E>> clazz) {
            classes.addAll(clazz);
            return this;
        }


        public <T extends RegistryItem> Registry<T> build() {
            Class<?>[] constructorTypes = constructorClassTypes.toArray(new Class<?>[0]);
            Object[] constructorValues = constructorClassValues.toArray(new Object[0]);

            List<T> tClasses = new ArrayList<>();
            for (Class<?> clazz : classes) {
                if (!RegistryItem.class.isAssignableFrom(clazz)) {
                    throw new IllegalArgumentException("Class " + clazz.getName() + " does not implement RegistryItem");
                }

                try {
                    tClasses.add((T) clazz.getConstructor(constructorTypes).newInstance(constructorValues));
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                         InvocationTargetException e) {
                    throw new RuntimeException("No constructor found for " + clazz.getName(), e);
                }
            }
            return new Registry<>(tClasses);
        }

    }
}
