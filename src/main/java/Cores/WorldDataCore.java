package Cores;

import Events.FallingBlockSpawnEvent;
import PositionalKeys.ChunkCoord;
import PositionalKeys.HyperKeys;
import PositionalKeys.LocalCoord;
import Settings.HSettings;
import Storage.ChunkValues;
import Storage.FastUpdateHandler;
import Storage.ValueStorage;
import Util.BreakUpdate;
import Util.Mathz;
import Util.PlaceUpdate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.plugin.java.JavaPlugin;
import org.jctools.maps.NonBlockingHashMap;

import java.util.*;

import static org.bukkit.Bukkit.*;

enum ResetType{
    PLACE_BREAK,FALL
}

public class WorldDataCore {

    //Todo: Make everything expect the block place and break events a normal new object runnable

    private JavaPlugin i;
    public PlaceUpdate pu;
    private BreakUpdate bu;

    ////
    public ValueStorage vs;
    private final NonBlockingHashMap<ChunkCoord, ChunkValues> chunkValues;
    /*Value Transfer*/
    private final IdentityHashMap<FallingBlock, byte[]> fallingBlockTransfer = new IdentityHashMap<>(8192);

    ////
    private FastUpdateHandler up;
    /*Fast Update Handler Stuff*/
    private final HashSet<LocalCoord>[] FallQuery;
    private final ArrayDeque<byte[]> chunkMark;
    private final HashMap<LocalCoord, byte[]>[] relValues;

    /*for bukkit injector/Falling Block stuff*/
    private final Mathz.YBlockComparator sortBlocksY = new Mathz.YBlockComparator();

    public WorldDataCore(JavaPlugin i) {
        vs = new ValueStorage();
        chunkValues = vs.chunkValues;
        up = new FastUpdateHandler(9, 9);
        FallQuery = up.blockFallQuery;
        chunkMark = up.chunkValueMarker;
        relValues = up.chunkValueHolder;
        pu = new PlaceUpdate(vs, up);
        bu = new BreakUpdate(vs, up);
        this.i = i;
    }

    private final void resetDataStructures(ResetType resetType) {
        int relChunk, reset;
        byte[] rl = chunkMark.poll();
        while (rl != null) {
            relChunk = 9 * rl[0] + rl[1];
            reset=relChunk<<16;
            if(relValues[relChunk].size()>8192) {
                for (int i = -1; ++i < 65536;) {
                    up.checkedCoords[reset | i] = false;
                }
            }else{
                for(LocalCoord lc : relValues[relChunk].keySet()){
                    up.checkedCoords[reset|(lc.parsedCoord&0xffff)]=false;
                }
            }
            if(resetType==ResetType.PLACE_BREAK) {
                FallQuery[relChunk].clear();
            }
            relValues[relChunk] = null;
            rl = chunkMark.poll();
        }
    }

    private int xl = 1000000;
    private int zl = 1000000;
    private HashMap<LocalCoord, byte[]> locValues = null;
    private ChunkCoord chunkCoord = null;

    public void blockBreak(Block b) {
        int rx,rz, x=b.getX(),z=b.getZ(),relchunk,plc /*local coord*/; World w = getWorld(HSettings.GAME_WORLD);
        LocalCoord lc = HyperKeys.localCoord[(b.getY()<<8)|(x<<28>>>28<<4)|(z<<28>>>28)];
        x>>=4; z>>=4;
        if (xl != x || zl != z) {
            xl = x;
            zl = z;
            chunkCoord = new ChunkCoord(x, z);
            locValues = chunkValues.get(chunkCoord).blockVals;
        }
        bu.breakChecks(lc, chunkCoord,locValues);
        pu.reUpdate(chunkCoord);

        boolean hasFall=false;
        ArrayList<Block> blocksToFall = new ArrayList<>(FallQuery[40].size()+6);

        for(byte[] cmark : chunkMark) {
            rx=cmark[0];
            rz=cmark[1];
            relchunk = (9 * rx) + rz;
            rx=(rx+x-4)<<4; rz=(rz+z-4)<<4;
            if (!FallQuery[relchunk].isEmpty()) {
                for (LocalCoord localCoord : FallQuery[relchunk]) {
                    plc = localCoord.parsedCoord;
                    b = w.getBlockAt(rx|(plc<<24>>>28),plc>>>8,rz|(plc<<28>>>28));
                    if(b.getType()==Material.AIR) airError(b,relchunk,localCoord);
                    else blocksToFall.add(b);
//                        broadcastMessage("Data at: x "+b.getX()+" y "+b.getY()+" z "+b.getZ()+" relchunk: "+ (rx)+" "+(rz));
                }
                hasFall = true;
            }
        }
        if(!hasFall){ resetDataStructures(ResetType.FALL); return; }
        if(HSettings.ASYNC) {
            getScheduler().runTask(i, () -> injectFallingBlocks(blocksToFall));
        }else{
            injectFallingBlocks(blocksToFall);
        }
        resetDataStructures(ResetType.PLACE_BREAK);
    }

    public void explosionUpdate(Block[] brokeBlocks) {
        Block b = brokeBlocks[0];
        if(b==null)return;
        int rx,rz, x=b.getX(),z=b.getZ(),relchunk,plc /*local coord*/; World w = getWorld(HSettings.GAME_WORLD);
        x>>=4; z>>=4;
        if (xl != x || zl != z) {
            xl = x;
            zl = z;
            chunkCoord = new ChunkCoord(x, z);
            locValues = chunkValues.get(chunkCoord).blockVals;
        }
        bu.multiBreak(chunkCoord,locValues,brokeBlocks);
        pu.reUpdate(chunkCoord);

        boolean hasFall=false;
        ArrayList<Block> blocksToFall = new ArrayList<>(FallQuery[40].size()+6);

        for(byte[] cmark : chunkMark) {
            rx=cmark[0];
            rz=cmark[1];
            relchunk = (9 * rx) + rz;
            rx=(rx+x-4)<<4; rz=(rz+z-4)<<4;
            if (!FallQuery[relchunk].isEmpty()) {
                for (LocalCoord localCoord : FallQuery[relchunk]) {
                    plc = localCoord.parsedCoord;
                    b = w.getBlockAt(rx|(plc<<24>>>28),plc>>>8,rz|(plc<<28>>>28));
                    if(b.getType()==Material.AIR) airError(b,relchunk,localCoord);
                    else blocksToFall.add(b);
//                        broadcastMessage("Data at: x "+b.getX()+" y "+b.getY()+" z "+b.getZ()+" relchunk: "+ (rx)+" "+(rz));
                }
                hasFall = true;
            }
        }
        if(!hasFall){ resetDataStructures(ResetType.FALL); return; }
        if(HSettings.ASYNC) {
            getScheduler().runTask(i, () -> injectFallingBlocks(blocksToFall));
        }else{
            injectFallingBlocks(blocksToFall);
        }
        resetDataStructures(ResetType.PLACE_BREAK);
    }

    public void blockPlace(Block b) {
        int rx,rz, x=b.getX(),z=b.getZ(),relchunk, plc /*local coord*/;
        LocalCoord lc = HyperKeys.localCoord[(b.getY()<<8)|(x<<28>>>28<<4)|(z<<28>>>28)];
        x>>=4; z>>=4;
        if (xl != x || zl != z) {
            xl = x;
            zl = z;
            chunkCoord = new ChunkCoord((x<<16)|z);
            locValues = chunkValues.get(chunkCoord).blockVals;
        }
        byte[] t = locValues.computeIfAbsent(lc, nc-> new byte[]{126, 1, 1, 0, 0, 0});
        t[1]=1;t[2]=1;
        /*switch (b.getType()){
            case COBBLESTONE:
                t[1]=5;
                break;
        }*/
        pu.placeChecks(lc, chunkCoord,locValues);

        boolean hasFall=false;
        ArrayList<Block> blocksToFall = new ArrayList<>(FallQuery[40].size());

        if (FallQuery[40].contains(lc)) {
            FallQuery[40].remove(lc);
            blocksToFall.add(b);
            hasFall=true;
        }

        for(byte[] cmark : chunkMark) {
            rx=cmark[0];
            rz=cmark[1];
            relchunk = (9 * rx) + rz;
            rx=(rx+x-4)<<4; rz=(rz+z-4)<<4;
            if (!FallQuery[relchunk].isEmpty()) {
                for (LocalCoord localCoord : FallQuery[relchunk]) {
                    plc = localCoord.parsedCoord;
                    b = getWorld(HSettings.GAME_WORLD).getBlockAt(rx|(plc<<24>>>28),plc>>>8,rz|(plc<<28>>>28));
                    if(b.getType()==Material.AIR) airError(b,relchunk,localCoord);
                    else blocksToFall.add(b);
//                        broadcastMessage("Data at: x "+b.getX()+" y "+b.getY()+" z "+b.getZ()+" relchunk: "+ (rx)+" "+(rz));
                }
                hasFall = true;
            }
        }
        if(!hasFall){ resetDataStructures(ResetType.FALL); return; }

        if(HSettings.ASYNC) {
            getScheduler().runTask(i, () -> injectFallingBlocks(blocksToFall));
        }else{
            injectFallingBlocks(blocksToFall);
        }
        resetDataStructures(ResetType.PLACE_BREAK);
    }

    private void airError(Block b, int relchunk, LocalCoord localCoord){
        broadcastMessage("Error: ExplosionUpdate: Falling Air Block Queried");
        broadcastMessage("Data lost at: x "+b.getX()+" y "+b.getY()+" z "+b.getZ()+" relativeChunk: "+ (relchunk));
        broadcastMessage("Synchronous calculations suggested for better stability");
        byte[] data = relValues[relchunk].get(localCoord);
        data[0]=126;data[1]=0;data[2]=0;data[3]=0;data[4]=0;data[5]=0;
    }

    private void injectFallingBlocks(ArrayList<Block> blocks){
        int size=blocks.size(), loop; Block block; Location l; Material m; byte d; World w= getWorld(HSettings.GAME_WORLD);

        if(size>1) blocks.sort(sortBlocksY);
        final FallingBlock[] fallingBlocks = new FallingBlock[size];

        for(loop=-1;++loop<size;) {
            block = blocks.get(loop);
            l = block.getLocation();
            m = block.getType();
            d= block.getData();
            w.getBlockAt(l).setType(Material.AIR);
            fallingBlocks[loop] = w.spawnFallingBlock(l.subtract(0, 1, 0), m, d);
            l.add(0, 1, 0);
        }
        getPluginManager().callEvent(new FallingBlockSpawnEvent(fallingBlocks, blocks));
    }

    public final void cacheFallingBlocks (FallingBlock[] fallingBlocks, ArrayList<Block> fallingBlockLocations){
        int loop, fbsLength = fallingBlocks.length, x, z; FallingBlock fallingBlock; Block block; byte[] bdata; LocalCoord lc;

        for(int i = -1; ++i<fbsLength;){
            fallingBlock = fallingBlocks[i];
            block = fallingBlockLocations.get(i);

            x = block.getX(); z = block.getZ();
            lc = HyperKeys.localCoord[(block.getY()<<8)|(x<<28>>>28<<4)|(z<<28>>>28)];
            x>>=4; z>>=4;
            if(xl!=x || zl!=z){
                xl=x;
                zl=z;
                chunkCoord = new ChunkCoord((x<<16)|z);
                locValues = chunkValues.get(chunkCoord).blockVals;
            }
            bdata = locValues.get(lc);
            bdata[0]=126;
            byte[] bclone = {126, 1, 1, 0, 0, 0};
            for(loop=0;++loop<6;){
                bclone[loop]=bdata[loop];
                bdata[loop]=0;
            }

            fallingBlockTransfer.put(fallingBlock,bclone);
        }
    }

    public void setFallenBlocks(FallingBlock fb, Block b){
        int x = b.getX(), z = b.getZ();
        LocalCoord lc = HyperKeys.localCoord[(b.getY()<<8)|(x<<28>>>28<<4)|(z<<28>>>28)];
        x>>=4; z>>=4;
        if(xl!=x || zl!=z){
            xl=x;
            zl=z;
            chunkCoord = new ChunkCoord((x<<16)|z);
            locValues = chunkValues.get(chunkCoord).blockVals;
        }
        byte[] data = fallingBlockTransfer.get(fb);
        if(data!=null) {
            locValues.put(lc, data);
            fallingBlockTransfer.remove(fb);
        }else{
            locValues.put(lc, new byte[]{126,1,1,0,0,0});
            //switch case get type
        }
        pu.blockLandCheck(lc,chunkCoord,locValues);
        resetDataStructures(ResetType.FALL);

    }
}
