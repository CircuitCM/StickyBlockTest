package Methods;

import Storage.ChunkLocation;
import Storage.ValueStorage;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import static Enums.Coords.*;


public class PlaceUpdate {

    private ValueStorage vs;
    private Material ar = Material.AIR;

    public PlaceUpdate(ValueStorage vs){
        this.vs= vs;

    }

    @SuppressWarnings("deprecation")
    public void placeChecks(Location li,Location l1, Set<Location> fallQuery) {

        Queue<Location> locquery = new LinkedList<>();
        Set<Location> queued = new HashSet<>();

        locquery.add(l1);

        while(!locquery.isEmpty()) {
            Location l = locquery.poll();
            Location[] ls = {
                EAST.getLoc(l), SOUTH.getLoc(l),
                WEST.getLoc(l), NORTH.getLoc(l),
                UP.getLoc(l), DOWN.getLoc(l)};

            Boolean[] ms = new Boolean[ls.length];
            int[] is = new int[ls.length];

            for (int i = 0; i < ls.length; i++) {
                is[i] = vs.getOrMax(ls[i]);

                ms[i] = !(queued.contains(ls[i])||
                    ls[i].getBlock().getType() == ar||ls[i]==li);
            }

            int a = Mathz.lowestTensile(is);
            vs.put(l, a);

            for (int n = 0; n < ls.length; n++) {
                if (ms[n] && a < is[n]) {
                    locquery.add(ls[n]);
                    queued.add(ls[n]);
                }
            }
            if (a < vs.range_value) {
                fallQuery.remove(l);
            }
            if (a >= vs.range_value && !fallQuery.contains(l)) {
                fallQuery.add(l);
                vs.del(l);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void placeChecksFallrecurse(Location l) {

        Location[] ls = {
            EAST.getLoc(l), SOUTH.getLoc(l),
            WEST.getLoc(l), NORTH.getLoc(l),
            UP.getLoc(l), DOWN.getLoc(l)};

        Boolean[] ms = new Boolean[ls.length];
        int[] is = new int[ls.length];

        for(int i=0;i<ls.length;i++){

            is[i] = vs.getOrMax(ls[i]);

            if(ls[i].getBlock().getType()==ar){
                ms[i] = false;
                continue;
            }

            ms[i] = true;
        }

        int a = Mathz.lowestTensile(is);
        vs.put(l,a);

        for(int n=0; n<ls.length;n++){
            if(ms[n] && a < is[n]) {
                placeChecksFall(ls[n]);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void placeChecksChunkRestricted(Location l1, ChunkLocation cl) {

        Queue<Location> locquery = new LinkedList<>();
        Set<Location> queued = new HashSet<>();

        locquery.add(l1);

        while(!locquery.isEmpty()) {
            Location l = locquery.poll();
            Location[] ls = {
                EAST.getLoc(l), SOUTH.getLoc(l),
                WEST.getLoc(l), NORTH.getLoc(l),
                UP.getLoc(l), DOWN.getLoc(l)};

            Boolean[] ms = new Boolean[ls.length];
            int[] is = new int[ls.length];

            for (int i = 0; i < ls.length; i++) {

                is[i] = vs.getOrMax(ls[i]);

                ms[i] = !(queued.contains(ls[i])||
                    !CHUNK(ls[i]).equals(cl)||
                    ls[i].getBlock().getType() == ar);
            }

            int a = Mathz.lowestTensile(is);
            vs.put(l, a);

            for (int n = 0; n < ls.length; n++) {
                if (ms[n] && a < is[n]) {
                    locquery.add(ls[n]);
                    queued.add(ls[n]);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void placeChecksFall(Location l1) {

        Queue<Location> locquery = new LinkedList<>();

        locquery.add(l1);

        while(!locquery.isEmpty()) {
            Location l = locquery.poll();
            Location[] ls = {
                EAST.getLoc(l), SOUTH.getLoc(l),
                WEST.getLoc(l), NORTH.getLoc(l),
                UP.getLoc(l), DOWN.getLoc(l)};

            Boolean[] ms = new Boolean[ls.length];
            int[] is = new int[ls.length];

            for (int i = 0; i < ls.length; i++) {

                is[i] = vs.getOrMax(ls[i]);

                ms[i] = ls[i].getBlock().getType() != ar;
            }

            int a = Mathz.lowestTensile(is);
            vs.put(l, a);

            for (int n = 0; n < ls.length; n++) {
                if (ms[n] && a < is[n]) {
                    locquery.add(ls[n]);
                }
            }
        }

    }
}
