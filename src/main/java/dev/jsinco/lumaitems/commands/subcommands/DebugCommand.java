package dev.jsinco.lumaitems.commands.subcommands;

import dev.jsinco.lumaitems.LumaItems;
import dev.jsinco.lumaitems.commands.SubCommand;
import dev.jsinco.lumaitems.shapes.Cylinder;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.FoodProperties;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DebugCommand implements SubCommand {
    @Override
    public void execute(@NotNull LumaItems plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        Player player = (Player) sender;

//        int radi = Integer.parseInt(args[1]);
//        int density = Integer.parseInt(args[2]);
//        int height = Integer.parseInt(args[3]);
//        Cylinder cylinder = new Cylinder(player.getLocation(), radi, density, height);
//
//
//        //var blockIterator = cylinder.oscillatedBlockList().iterator();
//
//        /*new BukkitRunnable() {
//            @Override
//            public void run() {
//                if (!blockIterator.hasNext()) {
//                    cancel();
//                    return;
//                }
//                var block = blockIterator.next();
//                block.setType(Material.EMERALD_BLOCK);
//                blockIterator.remove();
//            }
//        }.runTaskTimer(plugin, 0, 1L);*/
//        for (var block : cylinder.blockList()) {
//            block.setType(Material.EMERALD_BLOCK);
//        }
        //spawnFilledSphere(player.getLocation().subtract(0,10, 0), size, seg);


        ItemStack itemStack = new ItemStack(Material.DIAMOND_PICKAXE);

        FoodProperties.Builder food = FoodProperties.food()
                .canAlwaysEat(true)
                .nutrition(2)
                .saturation(3.5f);
        itemStack.setData(DataComponentTypes.FOOD, food);

        player.getInventory().addItem(itemStack);
    }

    public void spawnFilledSphere(Location location, int radius, int rate) {
        for (int i = 0; i < radius; i++) {
            spawnSphere(location, i, rate);
        }
    }

    public void spawnSphere(Location location, int radius, int rate) {

        double PII = Math.PI * 2;

        double rateDiv = Math.PI / rate;

        // To make a sphere we're going to generate multiple circles
        // next to each other.
        for (double phi = 0; phi <= Math.PI; phi += rateDiv) {
            // Cache
            double y1 = radius * Math.cos(phi);
            double y2 = radius * Math.sin(phi);

            for (double theta = 0; theta <= PII; theta += rateDiv) {
                double x = Math.cos(theta) * y2;
                double z = Math.sin(theta) * y2;


                location.clone().add(x, y1, z).getBlock().setType(Material.EMERALD_BLOCK);
            }
        }
    }

    public void spawnCircle(double size, Location center, int segment){
        for (int radius = 0; radius < size; radius++) {
            for (int i = 0; i < 360; i += 360 / segment) {
                double angle = (i * Math.PI / 180);
                double x = Math.round(radius * Math.cos(angle));
                double z = Math.round(radius * Math.sin(angle));
                Location loc = center.clone().add(x, -1, z);
                loc.getBlock().setType(Material.DIAMOND_BLOCK);
            }
        }
    }

    @Nullable
    @Override
    public List<String> tabComplete(@NotNull LumaItems plugin, @NotNull CommandSender sender, @NotNull String[] args) {
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
