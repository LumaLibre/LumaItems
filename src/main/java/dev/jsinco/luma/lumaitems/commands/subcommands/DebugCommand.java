package dev.jsinco.luma.lumaitems.commands.subcommands;

import dev.jsinco.luma.lumacore.manager.commands.CommandInfo;
import dev.jsinco.luma.lumacore.manager.modules.AutoRegister;
import dev.jsinco.luma.lumacore.manager.modules.RegisterType;
import dev.jsinco.luma.lumaitems.LumaItems;
import dev.jsinco.luma.lumaitems.commands.CommandManager;
import dev.jsinco.luma.lumaitems.commands.SubCommand;
import dev.jsinco.luma.lumaitems.shapes.Ellipsoid;
import net.kyori.adventure.util.TriState;
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


        return true;
    }

    @Nullable
    @Override
    public List<String> tabComplete(@NotNull LumaItems plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        return null;
    }

}
