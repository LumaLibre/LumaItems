package dev.lumas.lumaitems.shapes;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

// TODO: Not yet completed.
/**
 * Cylinders are just circles stacked on top of each other.
 * <br>
 * No Hollow circles or cylinders yet, I'm too lazy for that.
 */
public class Cylinder implements Shape3D {

    private final Location center;
    private final int radius;
    private final int density;
    private final int height;

    public Cylinder(Location center, int radius, int density, int height) {
        this.center = center;
        this.radius = radius;
        this.density = density;
        this.height = height;
    }

    // Getters

    public Location getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }

    public double getDensity() {
        return density;
    }

    public double getHeight() {
        return height;
    }

    public Set<Block> blockList() {
        Set<Block> blockList = new HashSet<>();

        if (height > 0) {
            for (int i = 0; i < height; i++) {
                blockList.addAll(ShapeUtil.circle(center.clone().add(0, i, 0), radius, density));
            }
        } else {
            for (int i = 0; i > height; i--) {
                blockList.addAll(ShapeUtil.circle(center.clone().add(0, i, 0), radius, density));
            }
        }
        return blockList;
    }

    public Set<Block> oscillatedBlockList() {
        Set<Block> blockList = new HashSet<>();

        int i = 1;

        while (i < height) {
            blockList.addAll(ShapeUtil.circle(center.clone().add(0, i, 0), i, density + i));
            i++;
        }

        while (i > 0) {
            blockList.addAll(ShapeUtil.circle(center.clone().subtract(0, i - 2, 0), i, density + i));
            i--;
        }

        return blockList;
    }


    public double getVolume() {
        return Math.PI * Math.pow(radius, 2) * height;
    }


    public boolean isInCylinder(Location location) {
        double x = location.getX();
        double z = location.getZ();
        double y = location.getY();
        double x1 = center.getX();
        double z1 = center.getZ();
        double y1 = center.getY();
        return Math.pow(x - x1, 2) + Math.pow(z - z1, 2) <= Math.pow(radius, 2) && y >= y1 && y <= y1 + height;
    }

    public boolean isInMarge(Location location, double marge) {
        double x = location.getX();
        double z = location.getZ();
        double y = location.getY();
        double x1 = center.getX();
        double z1 = center.getZ();
        double y1 = center.getY();
        return Math.pow(x - x1, 2) + Math.pow(z - z1, 2) <= Math.pow(radius + marge, 2) && y >= y1 && y <= y1 + height;
    }


}
