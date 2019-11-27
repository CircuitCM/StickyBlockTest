package Methods;

import Storage.ValueStorage;
import org.bukkit.Location;

import java.util.*;

import static Enums.Coords.*;

public class BreakUpdate {

    private ValueStorage vs;

    public BreakUpdate(ValueStorage vs){

        this.vs= vs;

    }

    public void breakChecks( Location l1, Collection<Location>[] toUpdate, Set<Location> fallQuery) {

        if(!vs.contains(l1)) return;

        Queue<Location> locquery = new LinkedList<>();
        Set<Location> queued = new HashSet<>();

        locquery.add(l1);

        while(!locquery.isEmpty()) {
            Location l = locquery.poll();

            Location[] ls = {
                UP.getLoc(l), DOWN.getLoc(l),
                EAST.getLoc(l), SOUTH.getLoc(l),
                WEST.getLoc(l), NORTH.getLoc(l)};

            Boolean[] ms = new Boolean[ls.length];

            for (int i = 0; i < ls.length; i++) {
                ms[i] = !(queued.contains(ls[i])&&
                    !vs.contains(ls[i]));
            }

            int a = vs.get(l);
            vs.del(l);
            fallQuery.add(l);

            if (ms[0]) {
                locquery.add(ls[0]);
                queued.add(ls[0]);
            }
            if (ms[1] && a > 0) {
                locquery.add(ls[1]);
                queued.add(ls[1]);
            }
            for (int i = 2; i < ls.length; i++) {
                int relativeBlock = vs.getOrMax(ls[i]);
                if (ms[i] && a < relativeBlock) {
                    locquery.add(ls[i]);
                    queued.add(ls[i]);
                    continue;
                }
                if (ms[i] && a >= relativeBlock) {
                    toUpdate[relativeBlock].add(ls[i]);
                }
            }
        }
    }
}
