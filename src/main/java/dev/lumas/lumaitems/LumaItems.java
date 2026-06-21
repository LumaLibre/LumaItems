package dev.lumas.lumaitems;

import dev.lumas.core.manager.Modules;
import dev.lumas.core.manager.Reflect;
import dev.lumas.core.util.ContextLogger;
import dev.lumas.lumaitems.enums.Action;
import dev.lumas.lumaitems.events.item.PassiveListeners;
import dev.lumas.lumaitems.guis.LumaItemsAbstractGui;
import dev.lumas.lumaitems.hooks.Hook;
import dev.lumas.lumaitems.api.ItemManager;
import dev.lumas.lumaitems.registry.Registry;
import dev.lumas.lumaitems.relics.RelicCrafting;
import dev.lumas.lumaitems.relics.RelicDisassembler;
import dev.lumas.lumaitems.util.extensions.Executors;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Set;

public final class LumaItems extends JavaPlugin {

    public static final ContextLogger LOGGER = ContextLogger.getLogger(NamedTextColor.DARK_GREEN);

    private static LumaItems instance;
    private static PassiveListeners passiveListeners;
    private static ItemManager itemManager;
    private static Modules moduleManager;
    private static boolean finishedRegistration;

    @Override
    public void onLoad() {
        instance = this;
        passiveListeners = new PassiveListeners(this);
        itemManager = new ItemManager();
        moduleManager = new Modules(this);
    }

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        Reflect reflectionUtil = Reflect.from(getClass());
        reflectionUtil.packages("commands", "commands.subcommands", "events", "events.item");

        Set<Class<?>> classSet = reflectionUtil.scan();
        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            LOGGER.info("Players are online, registering items asynchronously");
            Executors.async(task -> {
                try {
                    initItemManager(itemManager);
                } catch (Throwable e) {
                    LOGGER.error("An error occurred while registering items asynchronously");
                    getServer().getPluginManager().disablePlugin(this);
                }
                LOGGER.info("Finished asynchronous item registration!" + " Took " + (System.currentTimeMillis() - start) + "ms");
            });
        } else {
            initItemManager(itemManager);
            LOGGER.info("Finished synchronous item registration!" + " Took " + (System.currentTimeMillis() - start) + "ms");
        }

        moduleManager.register(classSet);

        RelicCrafting.registerRecipes();
        RelicDisassembler.setupDisassemblerBlocks();

        List<String> enabledHooks = Registry.HOOKS.values()
                .stream().filter(Hook::isWith)
                .map(hook -> hook.identifier().asSimpleString())
                .toList();
        LOGGER.info("Enabled Hooks: <gold>" + String.join(", ", enabledHooks));
    }

    private void initItemManager(ItemManager itemManager) {
        try {
            itemManager.registerItems(() -> LOGGER.info("Registered <gold>" + Registry.CUSTOM_ITEMS.size() + "</gold> classes through reflection"));
            passiveListeners.onPluginAction(Action.PLUGIN_ENABLE); // Fire this as soon as we're done registering our items
            passiveListeners.getPassiveListener(Action.RUNNABLE, PassiveListeners.DEFAULT_PASSIVE_LISTENER_TICKS, true);
            passiveListeners.getPassiveListener(Action.ASYNC_RUNNABLE, PassiveListeners.ASYNC_PASSIVE_LISTENER_TICKS, false);
            passiveListeners.getPassiveListener(Action.FAST_ASYNC_RUNNABLE,  PassiveListeners.FAST_ASYNC_PASSIVE_LISTENER_TICKS, false);
            passiveListeners.getGlobalTask(PassiveListeners.ASYNC_GLOBAL_TASK_TICKS);
            finishedRegistration = true;
        } catch (Exception e) {
            LOGGER.error("An error occurred while registering items", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this); // Immediately disable all listeners to prevent any further events from firing


        try {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.getScheduler().execute(this, () -> {
                    if (player.getOpenInventory().getTopInventory().getHolder(false) instanceof LumaItemsAbstractGui) {
                        player.closeInventory();
                    }
                }, null, 1);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        moduleManager.unregister();
        passiveListeners.onPluginAction(Action.PLUGIN_DISABLE); // Then fire this for whatever items need to use this
        passiveListeners.onPluginActionGlobal(Action.PLUGIN_DISABLE_GLOBAL);
    }

    @NotNull
    @Override
    public File getFile() {
        return super.getFile();
    }

    public static LumaItems getInstance() {
        return instance;
    }

    public static ItemManager getItemManager() {
        return itemManager;
    }

    public static boolean isFinishedRegistration() {
        return finishedRegistration;
    }
}
