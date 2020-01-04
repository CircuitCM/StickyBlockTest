package Events;

import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;

public final class FallingBlockSpawnEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final ArrayList<Block> blocks;
    private final FallingBlock[] fbs;

    public FallingBlockSpawnEvent(FallingBlock[] fallingBlocks, ArrayList<Block> locs) {
        blocks= locs;
        fbs= fallingBlocks;
    }

    public ArrayList<Block> getBlocks() {
        return blocks;
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