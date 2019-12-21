package Events;

import org.bukkit.entity.FallingBlock;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;

public final class FallingBlockSpawnEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final ArrayList<int[]> ls;
    private final ArrayList<FallingBlock> fbs;

    public FallingBlockSpawnEvent(ArrayList<int[]> locs, ArrayList<FallingBlock> fallingBlocks) {
        ls= locs;
        fbs= fallingBlocks;
    }

    public ArrayList<int[]> getLocations() {
        return ls;
    }

    public ArrayList<FallingBlock> getFallingBlocks() {
        return fbs;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}