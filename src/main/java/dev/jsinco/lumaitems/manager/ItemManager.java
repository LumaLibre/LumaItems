package dev.jsinco.lumaitems.manager;

import com.google.common.reflect.ClassPath;
import dev.jsinco.lumaitems.LumaItems;
import dev.jsinco.lumaitems.items.astral.AstralSet;
import dev.jsinco.lumaitems.util.NeedsEdits;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class ItemManager {

    private final LumaItems plugin;

    /**
     * Map of all LumaItems Custom Items
     * Key: Custom Item NBT Key
     * Value: Custom Item Class
     */
    public final static Map<NamespacedKey, CustomItem> customItems = new HashMap<>();


    public final static Map<String, CustomItem> customItemsByName = new HashMap<>();


    /**
     * List of all packages to search for Custom Items
     */
    public final static List<String> packages = List.of(
            "dev.jsinco.lumaitems.items.weapons",
            "dev.jsinco.lumaitems.items.tools",
            "dev.jsinco.lumaitems.items.misc",
            "dev.jsinco.lumaitems.items.armor",
            "dev.jsinco.lumaitems.items.magical",
            "dev.jsinco.lumaitems.items.astral",
            "dev.jsinco.lumaitems.items.astral.sets",
            "dev.jsinco.lumaitems.items.test"
    );


    /**
     * Get a Custom Item by its display name.
     * Spaces are replaced with underscores ('_'), colors are negated, and the name is case-insensitive.
     * @param name Display name of the Custom Item
     * @return Custom Item if found, null otherwise
     */
    @Nullable
    public static ItemStack getItemByName(String name) {
        return customItemsByName.get(name.replace(" ", "_").toLowerCase()).createItem().component2();
    }

    @Nullable
    public static ItemStack getItemByKey(String key) {
        return customItems.get(new NamespacedKey(LumaItems.getInstance(), key)).createItem().component2();
    }

    /**
     * Get a Custom Item by its key.
     * @param key Key of the Custom Item
     * @return Custom Item if found, null otherwise
     */
    @Nullable
    public static CustomItem getCustomItem(String key) {
        return customItems.get(new NamespacedKey(LumaItems.getInstance(), key));
    }

    /**
     * @return an immutable list of all physical items
     */
    public static List<ItemStack> getAllItems() {
        List<ItemStack> list = new ArrayList<>();
        for (CustomItem item : customItems.values()) {
            try {
                list.add(item.createItem().component2());
            } catch (Exception e) {
                LumaItems.log("Failed to create item for " + item.getClass().getSimpleName(), e);
            }
        }
        return list;
    }


    public ItemManager(LumaItems plugin) {
        this.plugin = plugin;
    }

    public void registerCustomItemsByName() {
        for (CustomItem item : customItems.values()) {
            ItemStack itemStack;
            try {
                itemStack = item.createItem().component2();
            } catch (Exception e) {
                LumaItems.log("Failed to create item for " + item.getClass().getSimpleName(), e);
                continue;
            }
            if (AstralSet.class.isAssignableFrom(item.getClass())) {
                continue;
            } else if (!itemStack.hasItemMeta() || !itemStack.getItemMeta().hasDisplayName()) {
                LumaItems.log("Item " + itemStack.getType() + " does not have a display name or meta!");
                continue;
            }
            String formattedName = ChatColor.stripColor(itemStack.getItemMeta().getDisplayName()).replace(" ", "_").toLowerCase();
            customItemsByName.put(formattedName, item);
        }
        LumaItems.log("Finished initializing " + customItemsByName.size() + " physical items by display names");
    }


    /**
     * Registers all Custom Items in the packages list
     */
    public void registerItems() throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        List<Class<?>> classes = new ArrayList<>();

        for (String packagee : packages) {
            classes.addAll(findClasses(packagee));
        }

        for (Class<?> clazz : classes) {
            try {
                if (CustomItem.class.isAssignableFrom(clazz) && !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
                    CustomItem item = (CustomItem) clazz.getDeclaredConstructor().newInstance();
                    customItems.put(new NamespacedKey(plugin, item.createItem().component1()), item);

                    NeedsEdits edits = clazz.getAnnotation(NeedsEdits.class);
                    if (edits != null) {
                        if (!edits.review()) {
                            LumaItems.log("&cClass &6" + clazz.getSimpleName() + " &cneeds edits!");
                        } else {
                            LumaItems.log("&aClass &6" + clazz.getSimpleName() + " &ais ready for review!");
                        }
                    }
                }
            } catch (Exception e) {
                LumaItems.log("Failed to register class " + clazz.getSimpleName(), e);
            }
        }
        LumaItems.log("Registered &6" + customItems.size() + " &#f498f6classes through reflection");
    }

    /**
     * Finds all classes in a package
     * @param packageName Package to search
     * @return Set of classes in the package
     * Credit: <a href="https://www.spigotmc.org/threads/register-all-listeners-in-package.399219/">...</a>
     */
    private Set<Class<?>> findClasses(String packageName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        Method getFileMethod = JavaPlugin.class.getDeclaredMethod("getFile");
        getFileMethod.setAccessible(true);
        File file = (File) getFileMethod.invoke(plugin);
        URLClassLoader classLoader = new URLClassLoader(
                new URL[] { file.toURI().toURL() },
                this.getClass().getClassLoader()
        );
        return ClassPath.from(classLoader)
                .getAllClasses()
                .stream()
                .filter(clazz -> clazz.getPackageName()
                        .equalsIgnoreCase(packageName))
                .map(clazz -> clazz.load())
                .collect(Collectors.toSet());
    }
}
