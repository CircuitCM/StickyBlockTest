package Events;

import org.bukkit.Location;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;

public final class FallingBlockSpawnEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final ArrayList<Location> ls;
    private final FallingBlock[] fbs;

    public FallingBlockSpawnEvent(ArrayList<Location> locs, FallingBlock[] fallingBlocks) {
        ls= locs;
        fbs= fallingBlocks;
    }

    public ArrayList<Location> getLocations() {
        return ls;
    }

    public FallingBlock[] getFallingBlocks() {
        return fbs;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}