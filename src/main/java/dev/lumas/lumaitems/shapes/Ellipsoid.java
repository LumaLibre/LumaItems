package dev.lumas.lumaitems.shapes;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

public class Ellipsoid implements Shape3D {

    private final Location center;
    private double radiusX, radiusY, radiusZ;
    private double density;

    public Ellipsoid(Location center, double radiusX, double radiusY, double radiusZ, double density) {
        this.center = center;
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.radiusZ = radiusZ;
        this.density = density;
    }

    public Location getCenter() {
        return center;
    }

    public double getRadiusX() {
        return radiusX;
    }

    public double getRadiusY() {
        return radiusY;
    }

    public double getRadiusZ() {
        return radiusZ;
    }

    public double getDensity() {
        return density;
    }

    public boolean isInEllipsoid(Location location) {
        double dx = (location.getX() - center.getX()) / radiusX;
        double dy = (location.getY() - center.getY()) / radiusY;
        double dz = (location.getZ() - center.getZ()) / radiusZ;
        return dx * dx + dy * dy + dz * dz <= 1;
    }

    public Location getRandomLocation() {
        double x, y, z;
        do {
            x = (Math.random() * 2 - 1) * radiusX;
            y = (Math.random() * 2 - 1) * radiusY;
            z = (Math.random() * 2 - 1) * radiusZ;
        } while ((x * x) / (radiusX * radiusX) + (y * y) / (radiusY * radiusY) + (z * z) / (radiusZ * radiusZ) > 1);

        return center.clone().add(x, y, z);
    }

    public Set<Block> getEllipsoid() {
        return getEllipsoid(center, radiusX, radiusY, radiusZ);
    }

    public Set<Block> getHollowEllipsoid() {
        return getHollowEllipsoid(center, radiusX, radiusY, radiusZ, density);
    }

    public static Set<Block> getEllipsoid(Location center, double rx, double ry, double rz) {
        Set<Block> blocks = new HashSet<>();

        int minX = (int) Math.floor(center.getX() - rx);
        int maxX = (int) Math.ceil(center.getX() + rx);
        int minY = (int) Math.floor(center.getY() - ry);
        int maxY = (int) Math.ceil(center.getY() + ry);
        int minZ = (int) Math.floor(center.getZ() - rz);
        int maxZ = (int) Math.ceil(center.getZ() + rz);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    double dx = (x + 0.5 - center.getX()) / rx;
                    double dy = (y + 0.5 - center.getY()) / ry;
                    double dz = (z + 0.5 - center.getZ()) / rz;

                    if (dx * dx + dy * dy + dz * dz <= 1.0) {
                        blocks.add(new Location(center.getWorld(), x, y, z).getBlock());
                    }
                }
            }
        }

        return blocks;
    }

    public static Set<Block> getHollowEllipsoid(Location center, double rx, double ry, double rz, double density) {
        Set<Block> blocks = new HashSet<>();
        double rateDiv = Math.PI / density;

        for (double phi = 0; phi <= Math.PI; phi += rateDiv) {
            double y = ry * Math.cos(phi);
            double sinPhi = Math.sin(phi);

            for (double theta = 0; theta <= Math.TAU; theta += rateDiv) {
                double x = rx * Math.cos(theta) * sinPhi;
                double z = rz * Math.sin(theta) * sinPhi;

                blocks.add(center.clone().add(x, y, z).getBlock());
            }
        }

        return blocks;
    }
}

