package dev.jsinco.luma.lumaitems.api;

import dev.jsinco.luma.lumaitems.LumaItems;
import dev.jsinco.luma.lumaitems.items.ItemFactory;
import dev.jsinco.luma.lumaitems.manager.CustomItem;
import dev.jsinco.luma.lumaitems.manager.ItemManager;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * LumaItemsAPI. Should always grab a new instance of this class because LumaItems may be subject
 * to reloads.
 */
public final class LumaItemsAPI {

    /**
     * Singleton instance of the LumaItemsAPI
     */
    private static LumaItemsAPI singleton = null;

    /**
     * API Shouldn't be instantiated
     * @see LumaItemsAPI#getInstance()
     */
    private LumaItemsAPI() {
    }


    /**
     * Get an instance of the LumaItemsAPI
     * @return LumaItemsAPI instance
     */
    public static synchronized LumaItemsAPI getInstance() {
        if (singleton == null) {
            singleton = new LumaItemsAPI();
            LumaItems.log("A plugin is accessing the LumaItems API. Creating a new instance! &7(Hash: " + singleton.hashCode() + ")");
        }
        return singleton;
    }

    /**
     * Check if an item is a custom item by comparing NBT tags
     * @param itemStack ItemStack to check
     * @param customItemKey NBT key of the custom item
     * @return true if the item is a custom item, false otherwise
     */
    public boolean isCustomItem(ItemStack itemStack, String customItemKey) {
        if (!itemStack.hasItemMeta()) return false;
        var meta = itemStack.getItemMeta();

        return meta.getPersistentDataContainer().has(new NamespacedKey(LumaItems.getInstance(), customItemKey), PersistentDataType.SHORT);
    }

    /**
     * Check if an item is a custom item by comparing NBT tags
     * @param itemStack ItemStack to check
     * @return true if the item is a custom item, false otherwise
     */
    public boolean isCustomItem(ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) return false;
        var meta = itemStack.getItemMeta();

        return meta.getPersistentDataContainer().has(ItemFactory.LUMAITEM);
    }

    /**
     * Get the CustomItem object of an ItemStack
     * @param itemStack ItemStack to get the CustomItem of
     * @return CustomItem object if the item is a custom item, null otherwise
     */
    @Nullable
    public CustomItem getCustomItem(ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) return null;
        var meta = itemStack.getItemMeta();

        for (var customItem : ItemManager.customItems.entrySet()) {
            if (meta.getPersistentDataContainer().has(customItem.getKey(), PersistentDataType.SHORT)) {
                return customItem.getValue();
            }
        }
        return null;
    }

    /**
     * Get a Custom Item by its key.
     * @param key Key of the Custom Item
     * @return Custom Item if found, null otherwise
     */
    @Nullable
    public CustomItem getCustomItem(String key) {
        return ItemManager.getCustomItem(key);
    }

    /**
     * Get a Custom Item by its display name.
     * Spaces are replaced with underscores ('_'), colors are negated, and the name is case-insensitive.
     * @param name Display name of the Custom Item
     * @return Custom Item if found, null otherwise
     */
    @Nullable
    public ItemStack getItemByName(String name) {
        return ItemManager.getItemByName(name);
    }

    /**
     * @return an immutable list of all physical items
     */
    public List<ItemStack> getAllItems() {
        return ItemManager.getAllItems();
    }

    /**
     * Register an external CustomItem
     * @param customItem CustomItem to register
     */
    public void registerCustomItem(CustomItem customItem) {
        LumaItems.getItemManagerInstance().registerItem(customItem);
    }

    /**
     * Uses reflection to register all Custom Items in a package
     * @param pack Package to register
     * @param file Jar to search for the package
     * @throws IOException if the package cannot be found
     * @throws InvocationTargetException if the method cannot be invoked
     * @throws NoSuchMethodException if the method cannot be found
     * @throws IllegalAccessException if the method cannot be accessed
     */
    public void registerForPackage(String pack, File file) throws IOException {
        LumaItems.getItemManagerInstance().registerForPackage(pack, file);
    }

    /**
     * Get an instance of the ItemFactory builder
     * @return ItemFactory builder
     */
    public ItemFactory.Builder factory() {
        return new ItemFactory.Builder();
    }
}
