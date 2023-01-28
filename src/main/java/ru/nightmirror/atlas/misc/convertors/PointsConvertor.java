package ru.nightmirror.atlas.misc.convertors;

import org.bukkit.Location;

import java.util.LinkedHashSet;

public class PointsConvertor {
    public static LinkedHashSet<Location> fromTwoPointMakeSquare(LinkedHashSet<Location> locations) {
        if (locations == null || locations.size() != 2) return locations;

        LinkedHashSet<Location> square = new LinkedHashSet<>();
        Location first = (Location) locations.toArray()[0];
        Location second = (Location) locations.toArray()[1];
        square.add(new Location(first.getWorld(), first.getBlockX(), first.getBlockY(), first.getBlockZ()));
        square.add(new Location(first.getWorld(), first.getBlockX(), first.getBlockY(), second.getBlockZ()));
        square.add(new Location(first.getWorld(), second.getBlockX(), second.getBlockY(), second.getBlockZ()));
        square.add(new Location(first.getWorld(), second.getBlockX(), second.getBlockY(), first.getBlockZ()));
        return square;
    }
}
