package Events;

import Cores.WorldDataCore;
import Factories.HyperScheduler;
import PositionalKeys.ChunkCoord;
import PositionalKeys.HyperKeys;
import PositionalKeys.LocalCoord;
import Settings.WorldRules;
import Storage.ChunkValues;
import Util.Coords;
import Util.DataUtil;
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
        this.chunkValues = wd.chunkValues;
        blockPhysics= p;
    }

    private int b_xl = 1000000;
    private int b_zl = 1000000;
    private ChunkCoord b_chunkCoord = null;
    private HashMap<LocalCoord,byte[]> b_locValues= null;

    @EventHandler
    public void blockBreak(BlockBreakEvent e){
        Block b = e.getBlock(); int x=b.getX(),z=b.getZ(), current=WorldRules.G_TIME;
        LocalCoord lc = HyperKeys.localCoord[(b.getY()<<8)|(x<<28>>>28<<4)|(z<<28>>>28)];
        x>>=4; z>>=4;
        if (b_xl != x || b_zl != z) {
            b_xl = x;
            b_zl = z;
            b_chunkCoord = Coords.CHUNK(x,z);
            b_locValues = chunkValues.get(b_chunkCoord).blockVals;
        }
        byte[] blockData = b_locValues.get(lc);
        if(blockData!=null) {
            if(DataUtil.olderThanAndStamp(blockData,current-5,current)&&blockData[4]>0){
                Bukkit.broadcastMessage("terraForm Offered");
                wd.terraForm_Entry.relaxedOffer(b);
            }
            if (--blockData[2] > 0) {
                e.setCancelled(true);
                return;
            }else if(blockData[3]>0) {
                if (WorldRules.ASYNC) HyperScheduler.worldDataUpdater.runTask(() -> wd.blockBreak(lc,b_chunkCoord,b_locValues));
                else wd.blockBreak(lc,b_chunkCoord,b_locValues);
            }
        }
    }


    private int p_xl = 1000000;
    private int p_zl = 1000000;
    private ChunkCoord p_chunkCoord = null;
    private HashMap<LocalCoord,byte[]> p_locValues= null;

    @EventHandler
    public void blockPlace(BlockPlaceEvent e){
        Block b = e.getBlockPlaced(); int x=b.getX(),z=b.getZ();
        LocalCoord lc = HyperKeys.localCoord[(b.getY()<<8)|(x<<28>>>28<<4)|(z<<28>>>28)];
        x>>=4; z>>=4;
        if (p_xl != x || p_zl != z) {
            p_xl = x;
            p_zl = z;
            p_chunkCoord = Coords.CHUNK(x,z);
            p_locValues = chunkValues.get(p_chunkCoord).blockVals;
        }
        Material m = b.getType();
        switch (m) {
            case STATIONARY_WATER:
            case STATIONARY_LAVA:
            case WATER:
            case LAVA:
            case SAND:
            case GRAVEL:
            case TNT:
            case AIR:
            case FIRE:
            case YELLOW_FLOWER:
            case RED_ROSE:
            case DOUBLE_PLANT:
            case LONG_GRASS:
            case ANVIL:
                Bukkit.broadcastMessage("-" + "\n§bNon-applicable block used, event ignored");
                break;
            default:
                Bukkit.broadcastMessage("-" + "§b\nPlace Event Triggered");
                if (WorldRules.ASYNC) HyperScheduler.worldDataUpdater.runTask(() -> wd.blockPlace(b,lc,p_chunkCoord,p_locValues));
                else wd.blockPlace(b,lc,p_chunkCoord,p_locValues);
        }

    }

    @EventHandler
    public void entityExplode(EntityExplodeEvent e){
        List<Block> blockList = e.blockList();
        int size = blockList.size(), loop=-1;
        if(size>0) {
            Block[] checkList = new Block[size];
            Material[] typeList = new Material[size];
            Material m;
            for (Block b : blockList) {
                m = b.getType();
                if(m==null)continue;
                switch(m){
                    /*case TNT:
                        b.setType(Material.AIR);*/
                    case AIR:
                    case LONG_GRASS:
                    case DOUBLE_PLANT:
                        continue;
                }
                checkList[++loop] = b;
                typeList[loop] = m;
            }
            if(checkList[0]!=null)explosionHandler(checkList, typeList); /*Bukkit.getScheduler().runTaskLater(blockPhysics, () -> explosionHandler(checkList, typeList), 1);*/
        }
    }
    @EventHandler
    public void blockExplode(BlockExplodeEvent e){
        List<Block> blockList = e.blockList();
        int size = blockList.size(), loop=-1;
        if(size>0) {
            Block[] checkList = new Block[size];
            Material[] typeList = new Material[size];
            Material m;
            for (Block b : blockList) {
                m = b.getType();
                if(m==null)continue;
                switch(m){
                    case TNT:
                        b.setType(Material.AIR);
                    case AIR:
                    case LONG_GRASS:
                    case DOUBLE_PLANT:
                        continue;
                }
                checkList[++loop] = b;
                typeList[loop] = m;
            }
            if(checkList[0]!=null) explosionHandler(checkList, typeList); /*Bukkit.getScheduler().runTaskLater(blockPhysics, () -> explosionHandler(checkList, typeList), 1);*/
        }
    }

    private int x_xl = 1000000;
    private int x_zl = 1000000;
    private ChunkCoord x_chunkCoord = null;
    private HashMap<LocalCoord,byte[]> x_locValues= null;

    private void explosionHandler(Block[] blockList, Material[] typeList) {
        int g_time= WorldRules.G_TIME,x, z,xset,zset, y, loop, size = blockList.length, count = 0, damage = WorldRules.EXPLOSIVE_DAMAGE;
        Block b;
        Material m;
        World w = WorldRules.GAME_WORLD;
        LocalCoord lc;
        for (loop = -1; ++loop < size; ) {
            b = blockList[loop];
            if (b == null) break;
            m = typeList[loop];
            y = b.getY();
            x = b.getX();
            z = b.getZ();
            lc = HyperKeys.localCoord[(y << 8) | (x << 28 >>> 28 << 4) | (z << 28 >>> 28)];
            xset = x >> 4;
            zset = z >> 4;
            if (x_xl != xset || x_zl != zset) {
                x_xl = xset;
                x_zl = zset;
                x_chunkCoord = new ChunkCoord(xset, zset);
//                if(chunkValues.isEmpty())return;
                ChunkValues cv = chunkValues.get(x_chunkCoord);
                if (cv == null) return;
                x_locValues = cv.blockVals;
            }
            byte[] blockData = x_locValues.getOrDefault(lc, null);

            if (blockData != null) {
                if ((blockData[2] -= damage) > 0) {
                    w.getBlockAt(x, y, z).setType(m,false);
                    blockList[loop] = null;
                }else if(blockData[3]<1){
                    blockList[loop]=null;
                }else {
                    ++count;
                }
                if (DataUtil.olderThanAndStamp(blockData,g_time-1,g_time)&&blockData[4] > 0) {
                    wd.terraForm_Entry.relaxedOffer(b);
                }
            }else{
                Bukkit.broadcastMessage("No block data at: x " + x + " y " + y + " z " + z + ", probably grass, or water");
            }
        }
        if (count > 1) {
            if (WorldRules.ASYNC) HyperScheduler.worldDataUpdater.runTask(() -> wd.explosionUpdate(blockList,x_chunkCoord,x_locValues));
            else wd.explosionUpdate(blockList,x_chunkCoord,x_locValues);
        } else if (count>0){
            if (WorldRules.ASYNC) HyperScheduler.worldDataUpdater.runTask(() -> wd.blockBreak(Coords.COORD(blockList[0]),x_chunkCoord,x_locValues));
            else wd.blockBreak(Coords.COORD(blockList[0]),x_chunkCoord,x_locValues);
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
        if(WorldRules.ASYNC) HyperScheduler.worldDataUpdater.runTask(()->wd.cacheFallingBlocks(fallingBlocks,blockLocations));
        else wd.cacheFallingBlocks(fallingBlocks,blockLocations);

    }


    @EventHandler
    public void blockFall(EntityChangeBlockEvent e){
        Block b = e.getBlock();
        /*int x = b.getX()>>4,z=b.getZ()>>4;
        if (xl != x || zl != z) {
            xl = x;
            zl = z;
            chunkCoord = Coords.CHUNK(x,z);
            locValues = chunkValues.get(chunkCoord).blockVals;
        }*/
        Entity ent = e.getEntity();
        switch (ent.getType()) {
            case FALLING_BLOCK:
                if (WorldRules.ASYNC) HyperScheduler.worldDataUpdater.runTask(() -> wd.setFallenBlocks((FallingBlock) ent, b));
                else wd.setFallenBlocks((FallingBlock) ent, b);
        }

    }
}
