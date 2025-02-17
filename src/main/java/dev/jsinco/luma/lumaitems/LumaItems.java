package dev.jsinco.luma.lumaitems;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import dev.jsinco.luma.lumaitems.api.LumaItemsAPI;
import dev.jsinco.luma.lumaitems.commands.CommandManager;
import dev.jsinco.luma.lumaitems.commands.UpgradeBukkitCommand;
import dev.jsinco.luma.lumaitems.events.ExternalListeners;
import dev.jsinco.luma.lumaitems.events.GeneralListeners;
import dev.jsinco.luma.lumaitems.events.items.JobsListeners;
import dev.jsinco.luma.lumaitems.events.items.Listeners;
import dev.jsinco.luma.lumaitems.events.items.PassiveListeners;
import dev.jsinco.luma.lumaitems.guis.AbstractGui;
import dev.jsinco.luma.lumaitems.enums.Action;
import dev.jsinco.luma.lumaitems.manager.FileManager;
import dev.jsinco.luma.lumaitems.manager.GlowManager;
import dev.jsinco.luma.lumaitems.manager.ItemManager;
import dev.jsinco.luma.lumaitems.placeholders.PAPIManager;
import dev.jsinco.luma.lumaitems.relics.RelicCrafting;
import dev.jsinco.luma.lumaitems.relics.RelicDisassembler;
import dev.jsinco.luma.lumaitems.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.logging.Level;

public final class LumaItems extends JavaPlugin {

    private static LumaItems instance;
    private static boolean withProtocolLib;
    private static boolean withMythicMobs;
    private static boolean withmcMMO;
    private static PAPIManager papiManager;
    private static PassiveListeners passiveListeners;
    private static ItemManager itemManagerInstance;

    @Override
    public void onEnable() {
        instance = this;
        FileManager.generateDefaultFiles();
        withProtocolLib = getServer().getPluginManager().isPluginEnabled("ProtocolLib");
        withMythicMobs = getServer().getPluginManager().isPluginEnabled("MythicMobs");
        withmcMMO = getServer().getPluginManager().isPluginEnabled("mcMMO");

        passiveListeners = new PassiveListeners(this);
        itemManagerInstance = new ItemManager(this);

        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            log("Players are online, registering items asynchronously");
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                initItemManager(itemManagerInstance);
                initListeners();
                log("Finished asynchronous item registration!");
            });
        } else {
            initItemManager(itemManagerInstance);
            initListeners();
        }


        GlowManager.initGlowTeams();
        RelicCrafting.registerRecipes();
        RelicDisassembler.setupDisassemblerBlocks();


        getCommand("lumaitems").setExecutor(new CommandManager(this));
        getCommand("upgrade").setExecutor(new UpgradeBukkitCommand());

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            papiManager = new PAPIManager(this);
            papiManager.register();
        }
    }

    private void initItemManager(ItemManager itemManager) {
        try {
            itemManager.registerItems();
            passiveListeners.onPluginAction(Action.PLUGIN_ENABLE); // Fire this as soon as we're done registering our items
            passiveListeners.getPassiveListener(Action.RUNNABLE).runTaskTimer(this, 0L, PassiveListeners.DEFAULT_PASSIVE_LISTENER_TICKS);
            passiveListeners.getPassiveListener(Action.ASYNC_RUNNABLE).runTaskTimerAsynchronously(this, 0L, PassiveListeners.ASYNC_PASSIVE_LISTENER_TICKS);
            passiveListeners.getGlobalTask().runTaskTimerAsynchronously(this, 0L, PassiveListeners.ASYNC_GLOBAL_TASK_TICKS);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "An error occurred while registering items", e);
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void initListeners() {
        getServer().getPluginManager().registerEvents(new Listeners(this), this);
        getServer().getPluginManager().registerEvents(new GeneralListeners(this), this);
        getServer().getPluginManager().registerEvents(new ExternalListeners(this), this);
        getServer().getPluginManager().registerEvents(new JobsListeners(), this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this); // Immediately disable all listeners to prevent any further events from firing
        passiveListeners.onPluginAction(Action.PLUGIN_DISABLE); // Then fire this for whatever items need to use this


        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder(false) instanceof AbstractGui) {
                player.closeInventory();
            }
        }
        if (papiManager != null) {
            papiManager.unregister();
        }

        // here
        try {
            Field singleTonField = LumaItemsAPI.class.getDeclaredField("singleton");
            singleTonField.setAccessible(true);
            if (singleTonField.get(LumaItemsAPI.class) == null) {
                return;
            }
            singleTonField.set(null, null);
            LumaItems.log("API Singleton instance has been reset!");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LumaItems.log("Failed to reset API Singleton instance!", e);
        }
        // here
    }

    @NotNull
    @Override
    public File getFile() {
        return super.getFile();
    }

    public static LumaItems getInstance() {
        return instance;
    }

    @Nullable
    public static ProtocolManager getProtocolManager() {
        return withProtocolLib ? ProtocolLibrary.getProtocolManager() : null;
    }

    public static boolean isWithMythicMobs() {
        return withMythicMobs;
    }

    public static boolean isWithmcMMO() {
        return withmcMMO;
    }

    public static ItemManager getItemManagerInstance() {
        return itemManagerInstance;
    }

    public static void log(String m) {
        Bukkit.getConsoleSender().sendMessage(Util.colorcode("&2[LumaItems] " + m)); // &#f498f6
    }

    public static void log(String m, Throwable throwable) {
        log("&#a7d9ff" + m);
        log("&6" + throwable.getMessage());
        for (StackTraceElement ste : throwable.getStackTrace()) {
            log("&#a7d9ff" + ste.toString());
        }
    }
}
