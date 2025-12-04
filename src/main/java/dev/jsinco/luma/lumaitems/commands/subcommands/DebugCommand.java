package dev.jsinco.luma.lumaitems.commands.subcommands;

import dev.jsinco.luma.lumacore.manager.commands.CommandInfo;
import dev.jsinco.luma.lumacore.manager.modules.AutoRegister;
import dev.jsinco.luma.lumacore.manager.modules.RegisterType;
import dev.jsinco.luma.lumaitems.LumaItems;
import dev.jsinco.luma.lumaitems.commands.CommandManager;
import dev.jsinco.luma.lumaitems.commands.SubCommand;
import dev.jsinco.luma.lumaitems.shapes.Sphere;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@AutoRegister(RegisterType.SUBCOMMAND)
@CommandInfo(
        name = "debug",
        description = "Debug command",
        usage = "/<command> debug",
        permission = "lumaitems.command.debug",
        playerOnly = false,
        parent = CommandManager.class
)
public class DebugCommand implements SubCommand {
    @Override
    public boolean execute(@NotNull LumaItems plugin, @NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        double radius = Double.parseDouble(args[0]);
        double density = Double.parseDouble(args[1]);

        Sphere sphere = new Sphere(player.getLocation(), radius, density);
        sphere.getSphere().forEach(block -> {
            block.setType(Material.GOLD_BLOCK);
        });
        return true;
    }

    @Nullable
    @Override
    public List<String> tabComplete(@NotNull LumaItems plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        return null;
    }

}
