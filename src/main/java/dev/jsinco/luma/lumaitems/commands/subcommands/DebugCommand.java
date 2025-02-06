package dev.jsinco.luma.lumaitems.commands.subcommands;

import dev.jsinco.luma.lumaitems.LumaItems;
import dev.jsinco.luma.lumaitems.commands.SubCommand;
import dev.jsinco.luma.lumaitems.particles.ParticleDisplay;
import dev.jsinco.luma.lumaitems.particles.Particles;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

public class DebugCommand implements SubCommand {
    @Override
    public void execute(@NotNull LumaItems plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        Player player = (Player) sender;

        ParticleDisplay star = ParticleDisplay.of(Particle.DUST)
                .withColor(Color.WHITE)
                .mixWith(Color.RED)
                .withLocation(player.getLocation());

        //Particles.meguminExplosion()
        //BukkitTask magicCircles(Plugin plugin, double radius, double rate, double radiusRate, double distance, ParticleDisplay display)
        //neopaganPentagram(double size, double rate, double extend, ParticleDisplay star, ParticleDisplay circle)
        Particles.neopaganPentagram(Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]), star, star);
        //Particles.magicCircles(LumaItems.getInstance(), Double.parseDouble(args[1]), Double.parseDouble(args[2]),
        //        Double.parseDouble(args[3]), Double.parseDouble(args[4]), particleDisplay);
    }

    @Nullable
    @Override
    public List<String> tabComplete(@NotNull LumaItems plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 2) {
            return List.of("size");
        } else if (args.length == 3) {
            return List.of("rate");
        } else if (args.length == 4) {
            return List.of("extend");
        }
        return null;
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
