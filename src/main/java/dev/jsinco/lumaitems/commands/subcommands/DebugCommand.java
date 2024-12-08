package dev.jsinco.lumaitems.commands.subcommands;

import dev.jsinco.lumaitems.LumaItems;
import dev.jsinco.lumaitems.commands.SubCommand;
import dev.jsinco.lumaitems.shapes.Cylinder;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.FoodProperties;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class DebugCommand implements SubCommand {
    @Override
    public void execute(@NotNull LumaItems plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        Player player = (Player) sender;

        ItemStack itemStack = player.getInventory().getItemInMainHand();

        try {
            int number = Integer.parseInt(args[1]);
            itemStack.editMeta(meta -> {
                meta.setCustomModelData(number);
                meta.setGlider(true);
            });
        } catch (NumberFormatException e) {
            itemStack.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft(args[1]));
        }
    }

    @Nullable
    @Override
    public List<String> tabComplete(@NotNull LumaItems plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        return Arrays.stream(Material.values()).map(it -> it.name().toLowerCase()).toList();
    }

    @Nullable
    @Override
    public String permission() {
        return "lumaitems.command.debug";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }
}
