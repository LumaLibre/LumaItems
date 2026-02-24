package dev.lumas.lumaitems.manager;

import com.google.common.reflect.ClassPath;
import dev.lumas.lumacore.utility.ContextLogger;
import dev.lumas.lumaitems.LumaItems;
import dev.lumas.lumaitems.annotations.Ignore;
import dev.lumas.lumaitems.items.astral.AstralSet;
import dev.lumas.lumaitems.model.CustomItem;
import dev.lumas.lumaitems.model.NamedCustomItem;
import dev.lumas.lumaitems.registry.NamespacedIdentifier;
import dev.lumas.lumaitems.registry.Registry;
import dev.lumas.lumaitems.registry.StringIdentifier;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class ItemManager {



    private static final ContextLogger LOGGER = ContextLogger.getLogger(true);
    private static final String BASE_PACKAGE = "dev.lumas.lumaitems.items";


    /**
     * Get a Custom Item by its display name.
     * Spaces are replaced with underscores ('_'), colors are negated, and the name is case-insensitive.
     * @param name Display name of the Custom Item
     * @return Custom Item if found, null otherwise
     */
    @Nullable
    public static ItemStack getItemByName(String name) {
        var customItem = Registry.NAMED_CUSTOM_ITEMS.get(StringIdentifier.normalized(name));
        if (customItem == null) {
            return null;
        }
        return customItem.getCustomItem().createItem().component2();
    }

    @Nullable
    public static ItemStack getItemByKey(String key) {
        var customItem = Registry.CUSTOM_ITEMS.get(NamespacedIdentifier.lumaitems(key));
        if (customItem == null) {
            return null;
        }
        return customItem.createItem().component2();
    }

    /**
     * Get a Custom Item by its key.
     * @param key Key of the Custom Item
     * @return Custom Item if found, null otherwise
     */
    @Nullable
    public static CustomItem getCustomItem(String key) {
        return Registry.CUSTOM_ITEMS.get(NamespacedIdentifier.lumaitems(key));
    }

    /**
     * @return an immutable list of all physical items
     */
    public static List<ItemStack> getAllItems() {
        List<ItemStack> list = new ArrayList<>();
        for (CustomItem item : Registry.CUSTOM_ITEMS.values()) {
            try {
                list.add(item.createItem().component2());
            } catch (Exception e) {
                LOGGER.error("Failed to create item for " + item.getClass().getSimpleName(), e);
            }
        }
        return list;
    }


//    public ItemManager(LumaItems plugin) {
//        this.plugin = plugin;
//    }


    /**
     * Registers all Custom Items in the packages list
     */
    public void registerItems(Runnable callback) throws IOException {
        File file = LumaItems.getInstance().getFile();

        try (URLClassLoader classLoader = new URLClassLoader(new URL[] { file.toURI().toURL() }, this.getClass().getClassLoader())) {
            ClassPath classPath = ClassPath.from(classLoader);
            Set<String> packages = getPackages(BASE_PACKAGE, classPath);
            for (String pack : packages) {
                registerForPackage(pack, classPath);
            }
            callback.run();
        }
    }

    public void registerForPackage(String pack, ClassPath classPath) {
        Set<Class<?>> classes = findClasses(pack, classPath);

        for (Class<?> clazz : classes) {
            try {
                if (CustomItem.class.isAssignableFrom(clazz) && !clazz.isInterface() &&
                        !Modifier.isAbstract(clazz.getModifiers()) && !clazz.isAnnotationPresent(Ignore.class)
                ) {
                    CustomItem item = (CustomItem) clazz.getDeclaredConstructor().newInstance();
                    registerItem(item);
                }
            } catch (IllegalStateException e) {
                LOGGER.error("Failed to register class " + clazz.getSimpleName(), e);
                LOGGER.error("Zip file closed? Exiting early.");
                break;
            } catch (Throwable e) {
                LOGGER.error("Failed to register class " + clazz.getSimpleName(), e);
            }
        }
    }

    public void registerItem(CustomItem item) {
        Registry.CUSTOM_ITEMS.put(item);

        if (!AstralSet.class.isAssignableFrom(item.getClass())) {
            String customTabName = item.tabCompleteName();
            NamedCustomItem namedCustomItem = new NamedCustomItem(item, customTabName);
            Registry.NAMED_CUSTOM_ITEMS.put(namedCustomItem);
        }
    }



    /**
     * Recursively finds all packages in a base package
     * @param basePackage Base package to search
     * @param classPath ClassPath to search in
     * @return Set of packages found
     */
    private Set<String> getPackages(String basePackage, ClassPath classPath) {
        return classPath.getAllClasses().stream()
                .map(ClassPath.ClassInfo::getPackageName)
                .filter(packageName -> packageName.startsWith(basePackage))
                .collect(Collectors.toSet());
    }

    /**
     * Finds all classes in a package
     * @param packageName Package to search
     * @param classPath ClassPath to search in
     * @return Set of classes in the package
     * Credit: <a href="https://www.spigotmc.org/threads/register-all-listeners-in-package.399219/">...</a>
     */
    private Set<Class<?>> findClasses(String packageName, ClassPath classPath) {
        return classPath
                .getAllClasses()
                .stream()
                .filter(clazz -> clazz.getPackageName()
                        .equalsIgnoreCase(packageName))
                .map(it -> {
                    try {
                        return it.load();
                    } catch (NoClassDefFoundError e) {
                        LOGGER.warning("Failed to load class " + it.getName() + " due to missing dependency: " + e.getMessage());
                        return null;
                    } catch (Throwable e) {
                        LOGGER.error("Failed to load class " + it.getName(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
