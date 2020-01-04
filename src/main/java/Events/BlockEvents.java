package Events;

import Cores.WorldDataCore;
import Factories.HyperScheduler;
import PositionalKeys.ChunkCoord;
import PositionalKeys.HyperKeys;
import PositionalKeys.LocalCoord;
import Settings.HSettings;
import Storage.ChunkValues;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jctools.maps.NonBlockingHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BlockEvents implements Listener {

    private final WorldDataCore wd;
    private final NonBlockingHashMap<ChunkCoord,ChunkValues> chunkValues;
    private final JavaPlugin blockPhysics;

    public BlockEvents(WorldDataCore wd, JavaPlugin p){
        this.wd=wd;
        this.chunkValues = wd.vs.chunkValues;
        blockPhysics= p;
    }

    private int xl = 1000000;
    private int zl = 1000000;
    private ChunkCoord chunkCoord = null;
    private HashMap<LocalCoord,byte[]> locValues= null;

    @EventHandler
    public void blockBreak(BlockBreakEvent e){
        Block b = e.getBlock(); int x=b.getX(),z=b.getZ();
        LocalCoord lc = HyperKeys.localCoord[(b.getY()<<8)|(x<<28>>>28<<4)|(z<<28>>>28)];
        x>>=4; z>>=4;
        if (xl != x || zl != z) {
            xl = x;
            zl = z;
            chunkCoord = new ChunkCoord(x, z);
            locValues = chunkValues.get(chunkCoord).blockVals;
        }
        byte[] blockData = locValues.get(lc);
        if(blockData!=null) {
            if (--blockData[1] > 0) {
                e.setCancelled(true);
                return;
            }
            if (HSettings.ASYNC) HyperScheduler.worldDataUpdater.runTask(() -> wd.blockBreak(b));
            else wd.blockBreak(b);
        }
    }


    @EventHandler
    public void blockPlace(BlockPlaceEvent e){
        Block b = e.getBlockPlaced();
        Material m =b.getType();
        switch(m) {
            case STATIONARY_WATER:
            case STATIONARY_LAVA:
            case WATER:
            case LAVA:
            case SAND:
            case GRAVEL:
            case TNT:
            case AIR:
            case FIRE:
            case ANVIL:
                Bukkit.broadcastMessage("-"+"\n§bNon-applicable block used, event ignored");
                break;
            default:
                Bukkit.broadcastMessage("-"+"§b\nPlace Event Triggered");
                if(HSettings.ASYNC) HyperScheduler.worldDataUpdater.runTask(() -> wd.blockPlace(b));
                else wd.blockPlace(b);
        }
    }

    @EventHandler
    public void entityExplode(EntityExplodeEvent e){
        List<Block> blockList = e.blockList();
        if(!blockList.isEmpty()) {
            int loop = -1, size = blockList.size();
            Block[] checkList = new Block[size];
            Material[] typeList = new Material[size];
            Material m;
            for (Block b : blockList) {
                m = b.getType();
                if(m==null||m==Material.AIR)continue;
                checkList[++loop] = b;
                typeList[loop] = m;
            }
            if(checkList[0]!=null) Bukkit.getScheduler().runTaskLater(blockPhysics, () -> explosionHandler(checkList, typeList), 1);
        }
    }
    @EventHandler
    public void blockExplode(BlockExplodeEvent e){
        List<Block> blockList = e.blockList();
        if(!blockList.isEmpty()) {
            int loop = -1, size = blockList.size(); Material m;
            Block[] checkList = new Block[size];
            Material[] typeList = new Material[size];
            for (Block b : blockList) {
                m = b.getType();
                if(m==null||m==Material.AIR)continue;
                checkList[++loop] = b;
                typeList[loop] = m;
            }
            if(checkList[0]!=null) Bukkit.getScheduler().runTaskLater(blockPhysics, () -> explosionHandler(checkList, typeList), 1);
        }
    }

    private void explosionHandler(Block[] blockList, Material[] typeList) {
        int x, z, y, loop, mark = -1, mark2 = -1, size = blockList.length, count = 0;
        Block b;
        Material m;
        ChunkValues cv;
        World w = Bukkit.getWorld(HSettings.GAME_WORLD);
        LocalCoord lc;

        for (loop = -1; ++loop < size; ) {
            b = blockList[loop];
            if (b == null) break;
            m = typeList[loop];
            y = b.getY();
            x = b.getX();
            z = b.getZ();
            lc = HyperKeys.localCoord[(y << 8) | (x << 28 >>> 28 << 4) | (z << 28 >>> 28)];
            x >>= 4;
            z >>= 4;
            if (xl != x || zl != z) {
                xl = x;
                zl = z;
                chunkCoord = new ChunkCoord(x, z);
//                if(chunkValues.isEmpty())return;
                cv = chunkValues.get(chunkCoord);
                if (cv == null) return;
                locValues = cv.blockVals;
            }
            byte[] blockData = locValues.getOrDefault(lc, null);

            if (blockData == null)
                Bukkit.broadcastMessage("No block data at: x " + x + " y " + y + " z " + z + ", deleted in other explosion event?\nWill retry data find in multibreak");
            else if (--blockData[1] > 0) {
                w.getBlockAt(b.getX(), y, b.getZ()).setType(m);
                blockList[loop] = null;
                if (mark == -1) mark = loop;
                continue;
            }
            if (mark2 == -1) mark2 = loop;
            ++count;
        }
        if (count > 0) {
            if (mark == 0) {
                blockList[0] = blockList[mark2];
                blockList[mark2] = null;
            }
            if (count > 1) {
                if (HSettings.ASYNC) HyperScheduler.worldDataUpdater.runTask(() -> wd.explosionUpdate(blockList));
                else wd.explosionUpdate(blockList);
            } else {
                final Block bl = blockList[0];
                if (HSettings.ASYNC) HyperScheduler.worldDataUpdater.runTask(() -> wd.blockBreak(bl));
                else wd.blockBreak(bl);
            }
        }
    }
        /*final byte[] world = new byte[maxChunks * SIZE_CHUNK];
        final Map<Short, Integer> chunks = new HashMap<>();

        int chunkOffset(int x, int z) {
            return chunks.get((x & 0x00FF) | (z << 8)) * SIZE_CHUNK;
        }*/
        /*final ByteBuffer blocks = ByteBuffer.allocateDirect(maxChunks * 4096);
        final Map<Short, ByteBuffer> chunks = new HashMap<>();
        final Queue<Integer> indices = IntStream.range(0, maxChunks).collect(toCollection(new LinkedList<>()));
        final SeekableByteChannel saved = ...;

        ByteBuffer chunk(int x, int z) {
            return chunks.computeIfAbsent((x & 0x00FF) | (z << 8), position -> {
                if (indices.isEmpty()) throw RuntimeException("Out of world space!");
                final int offset = indices.poll() * SIZE;
                blocks.position(offset).limit(offset + SIZE);
                saved.position(position * SIZE).read(blocks);
                return blocks.flip().slice();
            });
        }

        static byte block(ByteBuffer chunk, int x, int y, int z) {
            return chunk.get(y | (z | (x << 4)) << 8)
        }
        byte block(int x, int y, int z) { return block(chunk(x & 15, z & 15), x, y, z); }*/


    @EventHandler
    public void FallingBlockSpawn(FallingBlockSpawnEvent e){
        FallingBlock[] fallingBlocks = e.getFallingBlocks();
        ArrayList<Block> blockLocations = e.getBlocks();
        if(HSettings.ASYNC) HyperScheduler.worldDataUpdater.runTask(()->wd.cacheFallingBlocks(fallingBlocks,blockLocations));
        else wd.cacheFallingBlocks(fallingBlocks,blockLocations);

    }

    @EventHandler
    public void blockFall(EntityChangeBlockEvent e){
        Entity ent = e.getEntity();
        switch(ent.getType()){
            case FALLING_BLOCK:
                Block b = e.getBlock();
                if(HSettings.ASYNC) HyperScheduler.worldDataUpdater.runTask(() -> wd.setFallenBlocks((FallingBlock) ent, b));
                else wd.setFallenBlocks((FallingBlock) ent, b);
        }
    }
}
