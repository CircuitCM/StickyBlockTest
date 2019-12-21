package Cores;

import Events.FallingBlockSpawnEvent;
import Factories.BlockEventExecutor;
import PositionalKeys.ChunkCoord;
import PositionalKeys.LocalCoord;
import Settings.HSettings;
import Storage.ChunkValues;
import Storage.FastUpdateHandler;
import Storage.ValueStorage;
import Util.BreakUpdate;
import Util.Coords;
import Util.Mathz;
import Util.PlaceUpdate;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.plugin.java.JavaPlugin;
import org.jctools.maps.NonBlockingHashMap;
import org.jctools.queues.SpscArrayQueue;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

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

    ////
    private final BlockEventExecutor bee = new BlockEventExecutor(120, 10, "BlockWorldUpdater");
    /* Block Event Executor Reference*/
    private final ThreadPoolExecutor bThread = bee.tpool;
    private final AtomicInteger tLevel = bee.consumerThreads;
    private final SpscArrayQueue<Block> placeQueue = bee.placeQueuePriority;
    private final SpscArrayQueue<Block> breakQueue = bee.breakQueuePriority;
    private final SpscArrayQueue<Runnable> generalQueue = bee.generalRun;
    private final Runnable placePriority;
    private final Runnable breakPriority;
    private final Runnable generalRun;

    /*for bukkit injector/Falling Block stuff*/
    private final Runnable injectFallingBlocks;
    private final SpscArrayQueue<Object[]> bukkitInjectorQueue = new SpscArrayQueue<>(32);
    private final TreeSet<LocalCoord> sortedCoords = new TreeSet<>(new Mathz.YCoordComparator());

    public WorldDataCore(JavaPlugin i) {
        vs = new ValueStorage();
        chunkValues = vs.chunkValues;
        up = new FastUpdateHandler(9, 9);
        FallQuery = up.blockFallQuery;
        chunkMark = up.chunkValueMarker;
        pu = new PlaceUpdate(vs, up);
        bu = new BreakUpdate(vs, up);
        this.i = i;

        injectFallingBlocks = () -> {
            final SpscArrayQueue<Object[]> fbi = bukkitInjectorQueue;
            Object[] fbs = fbi.poll();
            if (fbs == null) return;

            final TreeSet<LocalCoord> sortC = sortedCoords;
            final ArrayList<FallingBlock> fbt = new ArrayList<>(8);
            final ArrayList<int[]> fbl = new ArrayList<>(8);
            HashSet<LocalCoord>[] chunkedBlocks;
            byte[][] chunkOffset;
            ChunkCoord relativeChunk;
            int mapRef;
            int[] chunkAt;
            int[] blockAt;
            Block b;
            LocalCoord lc;
            FallingBlock fb;
            while (fbs != null) {
                chunkedBlocks = (HashSet<LocalCoord>[]) fbs[0];
                chunkOffset = (byte[][]) fbs[1];
                relativeChunk = (ChunkCoord) fbs[2];
                for (byte[] offset : chunkOffset) {
                    chunkAt = Coords.CHUNK_AT(relativeChunk, offset);
                    mapRef = 9 * offset[0] + offset[1];
                    if (chunkedBlocks[mapRef].isEmpty()) continue;

                    sortC.addAll(chunkedBlocks[mapRef]);
                    lc = sortC.pollFirst();
                    while (lc != null) {
                        //x y z
                        blockAt = Coords.BLOCK_AT(lc, chunkAt);
                        b = Bukkit.getServer().getWorld(HSettings.GAME_WORLD).getBlockAt(blockAt[0], blockAt[1], blockAt[2]);
                        fbl.add(blockAt);
                        fb = Bukkit.getServer().getWorld(HSettings.GAME_WORLD).spawnFallingBlock(b.getLocation().clone().subtract(0, 1, 0), b.getType(), b.getData());
                        fbt.add(fb);
                        lc = sortC.pollFirst();
                    }
                }
                fbs = fbi.poll();
            }
            Bukkit.getPluginManager().callEvent(new FallingBlockSpawnEvent(fbl, fbt));
        };

        placePriority = () -> {
            final SpscArrayQueue<Block> pQ = placeQueue;
            Block placedBlock = pQ.poll();
            ChunkCoord cc;
            while (placedBlock != null) {
                cc = Coords.CHUNK(placedBlock);
                pu.placeChecks(Coords.COORD(placedBlock), cc);

                final HashSet<LocalCoord>[] copyFall = new HashSet[81];
                System.arraycopy(FallQuery, 0, copyFall, 0, 81);

                byte[][] cmarks = new byte[chunkMark.size()][];
                byte pos=-1;
                for(byte[] cmark : chunkMark){
                    cmarks[++pos]=cmark;
                }

                Object[] a = {copyFall, cmarks, cc};

                bukkitInjectorQueue.offer(a);
                Bukkit.getScheduler().runTask(i, injectFallingBlocks);
                resetDataStructures();
                placedBlock = pQ.poll();
            }
            final SpscArrayQueue<Block> bQ = breakQueue;
            final SpscArrayQueue<Runnable> gQ = generalQueue;
            Block brokeBlock = bQ.poll();
            Runnable run = gQ.poll();
            placedBlock = pQ.poll();
            while (brokeBlock != null || run != null || placedBlock != null) {

                if (brokeBlock != null) blockBreak(brokeBlock);
                if (run != null) run.run();
                if (placedBlock != null) blockPlace(placedBlock);

                brokeBlock = bQ.poll();
                run = gQ.poll();
                placedBlock = pQ.poll();
            }
            tLevel.decrementAndGet();
        };

        breakPriority = () -> {
            final SpscArrayQueue<Block> bQ = breakQueue;
            Block brokeBlock = bQ.poll();
            ChunkCoord cc;
            while (brokeBlock != null) {
                cc = Coords.CHUNK(brokeBlock);
                bu.breakChecks(cc, Coords.COORD(brokeBlock));
                pu.reUpdate();

                final HashSet<LocalCoord>[] copyFall = new HashSet[81];
                System.arraycopy(FallQuery, 0, copyFall, 0, 81);

                final byte[][] cmark = (byte[][]) chunkMark.toArray();

                Object[] a = {copyFall, cmark, cc};

                bukkitInjectorQueue.offer(a);
                Bukkit.getScheduler().runTask(i, injectFallingBlocks);
                resetDataStructures();
                brokeBlock = bQ.poll();
            }
            final SpscArrayQueue<Block> pQ = placeQueue;
            final SpscArrayQueue<Runnable> gQ = generalQueue;
            Runnable run = gQ.poll();
            Block placedBlock = pQ.poll();
            brokeBlock = bQ.poll();
            while (run != null || brokeBlock != null || placedBlock != null) {
                //event stuff
                run = gQ.poll();
                brokeBlock = bQ.poll();
                placedBlock = pQ.poll();
            }
            tLevel.decrementAndGet();
        };

        generalRun = () -> {
            final SpscArrayQueue<Block> bQ = breakQueue;
            final SpscArrayQueue<Block> pQ = placeQueue;
            final SpscArrayQueue<Runnable> gQ = generalQueue;
            Runnable run = gQ.poll();
            Block brokeBlock = bQ.poll();
            Block placedBlock = pQ.poll();
            while (run != null || brokeBlock != null || placedBlock != null) {

                if (run != null) run.run();
                if (brokeBlock != null) blockBreak(brokeBlock);
                if (placedBlock != null) blockPlace(placedBlock);

                brokeBlock = bQ.poll();
                run = gQ.poll();
                placedBlock = pQ.poll();
            }
            if (tLevel.decrementAndGet() >= 1)
                Bukkit.getServer().broadcastMessage("Warning more than one thread active in WorldDataUpdater");
        };

    }

    public final void blockBreakUpdate(Block b) {
        breakQueue.offer(b);
        if (tLevel.incrementAndGet() <= 1) {
            bThread.execute(breakPriority);
        } else {
            tLevel.decrementAndGet();
        }
    }

    public final void blockPlaceUpdate(Block b) {
        placeQueue.offer(b);
        if (tLevel.incrementAndGet() <= 1) {
            bThread.execute(placePriority);
            return;
        }
        tLevel.decrementAndGet();
    }

    public final void generalRun(Runnable r) {
        generalQueue.offer(r);
        if (tLevel.incrementAndGet() <= 1) {
            bThread.execute(generalRun);
            return;
        }
        tLevel.decrementAndGet();
    }

    public final void resetDataStructures() {
        int relChunk;
        byte[] rl = chunkMark.poll();
        while(rl!=null){
            relChunk=9*rl[0]+rl[1];
            for(int i=-1;++i<65536;){
                up.checkedCoords[(relChunk<<16)|i]=false;
            }
            up.blockFallQuery[relChunk].clear();
            up.chunkValueHolder[relChunk].clear();
            rl = chunkMark.poll();
        }
    }

    private final void blockBreak(Block b) {

        ChunkCoord cc = Coords.CHUNK(b);
        bu.breakChecks(cc, Coords.COORD(b));
        pu.reUpdate();

        final HashSet<LocalCoord>[] copyFall = new HashSet[81];
        System.arraycopy(FallQuery, 0, copyFall, 0, 81);

        final byte[][] cmark = (byte[][]) chunkMark.toArray();
        Object[] a = {copyFall, cmark, cc};

        bukkitInjectorQueue.offer(a);
        Bukkit.getScheduler().runTask(i, injectFallingBlocks);
        resetDataStructures();
    }

    private void blockPlace(Block b){
        ChunkCoord cc = Coords.CHUNK(b);
            pu.placeChecks(Coords.COORD(b), cc);

            final HashSet<LocalCoord>[] copyFall = new HashSet[81];
            System.arraycopy(FallQuery, 0, copyFall, 0, 81);

            final byte[][] cmark = (byte[][]) chunkMark.toArray();

            Object[] a = {copyFall, cmark, cc};

            bukkitInjectorQueue.offer(a);
            Bukkit.getScheduler().runTask(i, injectFallingBlocks);
            resetDataStructures();
    }

    public void generateNewChunk(Chunk c){
        pu.setNewChunk(c.getChunkSnapshot());
    }

    public final void cacheFallingBlocks(final ArrayList<FallingBlock> fbs, final ArrayList<int[]> fbl){

        final int lgth = fbl.size();
        int xl = 1000000;
        int zl = 1000000;
        int x;
        int z;

        HashMap<LocalCoord,byte[]>localValues=null;
        FallingBlock fb;
        int[] xyz;

        for(int i = 0; ++i<=lgth;){
            fb = fbs.get(i);
            xyz = fbl.get(i);
            x = xyz[0];
            z = xyz[2];
            if(xl!=x>>4 || zl!=z>>4){
                xl=x>>4;
                zl=z>>4;
                localValues = chunkValues.get(new ChunkCoord(xl,zl)).blockVals;
            }
            byte[] bdata = localValues.get(Coords.COORD(xyz));
            bdata[0]=126;
            byte[] bclone = {126,0,0,0,0,0};
            System.arraycopy(bdata,0,bclone,0,6);
            fallingBlockTransfer.put(fb,bclone);
        }
    }

    private int xl = 1000000;
    private int zl = 1000000;
    private HashMap<LocalCoord,byte[]>locValues = null;

    public void setFallenBlocks(FallingBlock fb,Block b){

        final int x = b.getX();
        final int z = b.getZ();
        if(xl!=x>>4 || zl!=z>>4){
            xl=x>>4;
            zl=z>>4;
            locValues = chunkValues.get(new ChunkCoord(xl,zl)).blockVals;
        }
        locValues.put(Coords.COORD(b),fallingBlockTransfer.get(fb));
        fallingBlockTransfer.remove(fb);
    }
}
