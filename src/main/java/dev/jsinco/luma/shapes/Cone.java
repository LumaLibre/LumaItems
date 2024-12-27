package dev.jsinco.luma.shapes;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

// TODO
public class Cone implements Shape3D {

    private final Location center;
    private final int length;
    private final int density;

    public Cone(Location center, int length, int density) {
        this.center = center;
        this.length = length;
        this.density = density;
    }

    // Getters

    public Location getCenter() {
        return center;
    }

    public int getLength() {
        return length;
    }

    public int getDensity() {
        return density;
    }




    public Set<Block> blockList() {
        Set<Block> blockList = new HashSet<>();
        if (length < 0) {
            for (int i = 0; i > length; i--) {
                blockList.addAll(ShapeUtil.circle(center.clone().add(0, i, 0), i, density + i));
            }
        } else {
            for (int i = 0; i < length; i++) {
                blockList.addAll(ShapeUtil.circle(center.clone().subtract(0, i, 0), i, density + i));
            }
        }
        return blockList;
    }
}
