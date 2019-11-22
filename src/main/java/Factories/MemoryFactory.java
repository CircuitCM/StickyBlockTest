package Factories;

import org.bukkit.Location;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class MemoryFactory {

    public static final byte range_value = 10;

    public static Queue<Location>[] newUpdateQuery(){
        Queue<Location>[] placeUpdate = new LinkedList[range_value];
        for (int i = 0; i<placeUpdate.length; i++) {
            placeUpdate[i]=new LinkedList<>();
        }

        return placeUpdate;
    }

    public static Collection<Location> newFallQuery(){
        return new HashSet<>();
    }
}
