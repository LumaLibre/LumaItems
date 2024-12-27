package dev.jsinco.luma.shapes;

import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ShapeUtil {

    // The necessary parts of the Cuboid object blocklist condensed to a function for better performance
    public static List<Block> getCuboidBlocks(final Location loc1, final Location loc2) {
        Preconditions.checkArgument(loc1.getWorld() == loc2.getWorld(), "Points must be in the same world");
        final World world = loc1.getWorld();
        final List<Block> bL = new ArrayList<>();


        for (int x = Math.min(loc1.getBlockX(), loc2.getBlockX()); x <= Math.max(loc1.getBlockX(), loc2.getBlockX()); ++x) {
            for (int y = Math.min(loc1.getBlockY(), loc2.getBlockY()); y <= Math.max(loc1.getBlockY(), loc2.getBlockY()); ++y) {
                for (int z = Math.min(loc1.getBlockZ(), loc2.getBlockZ()); z <= Math.max(loc1.getBlockZ(), loc2.getBlockZ()); ++z) {
                    bL.add(world.getBlockAt(x, y, z));
                }
            }
        }
        return bL;
    }

    public static Set<Block> circle(Location center, int size, int segment) {
        Set<Block> blockList = new HashSet<>();
        int angleIncrement = 360 / segment;
        for (int radius = 0; radius < size; radius++) {
            int i = 0;
            while (i < 360) {
                double angle = i * Math.PI / 180;
                double x = Math.round(radius * Math.cos(angle));
                double z = Math.round(radius * Math.sin(angle));
                Location loc = center.clone().add(x, 0, z);
                blockList.add(loc.getBlock());
                i += angleIncrement;
            }
        }
        return blockList;
    }
}
