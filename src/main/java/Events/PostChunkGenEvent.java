package Events;


import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.ChunkEvent;

public class PostChunkGenEvent extends ChunkEvent {
    private static final HandlerList handlerList;
    private static final Chunk dummyChunk;
    public final int XZ;
    public final byte[] yNoise;
    static {
        handlerList= new HandlerList();
        dummyChunk = new Chunk() {
            @Override
            public int getX() {
                return 0;
            }

            @Override
            public int getZ() {
                return 0;
            }

            @Override
            public World getWorld() {
                return null;
            }

            @Override
            public Block getBlock(int i, int i1, int i2) {
                return null;
            }

            @Override
            public ChunkSnapshot getChunkSnapshot() {
                return null;
            }

            @Override
            public ChunkSnapshot getChunkSnapshot(boolean b, boolean b1, boolean b2) {
                return null;
            }

            @Override
            public Entity[] getEntities() {
                return new Entity[0];
            }

            @Override
            public BlockState[] getTileEntities() {
                return new BlockState[0];
            }

            @Override
            public boolean isLoaded() {
                return false;
            }

            @Override
            public boolean load(boolean b) {
                return false;
            }

            @Override
            public boolean load() {
                return false;
            }

            @Override
            public boolean unload(boolean b, boolean b1) {
                return false;
            }

            @Override
            public boolean unload(boolean b) {
                return false;
            }

            @Override
            public boolean unload() {
                return false;
            }
        };
    }


    public PostChunkGenEvent(int XZ, byte[] yNoise) {
        super(dummyChunk);
        this.XZ=XZ;
        this.yNoise=yNoise;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
