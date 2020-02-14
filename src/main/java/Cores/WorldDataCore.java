package Cores;

import Factories.HyperScheduler;
import Factories.Runnables.DegenBlocks;
import Factories.Runnables.FormBlocks;
import PositionalKeys.ChunkCoord;
import PositionalKeys.HyperKeys;
import PositionalKeys.LocalCoord;
import Settings.WorldRules;
import Storage.*;
import Util.*;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.Int2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.plugin.java.JavaPlugin;
import org.jctools.maps.NonBlockingHashMap;
import org.jctools.queues.SpscLinkedQueue;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.bukkit.Bukkit.broadcastMessage;
import static org.bukkit.Bukkit.getScheduler;

public class WorldDataCore {

    private final DataUtil.YBlockComparator sortBlocksY = new DataUtil.YBlockComparator();
    public final KryoIO kryoIO;

    //method help make static?
    public JavaPlugin i;
    private PlaceUpdate pu;
    private BreakUpdate bu;
    ////
    public ValueStorage vs = new ValueStorage();
    /*Value Transfer*/
    public final NonBlockingHashMap<ChunkCoord, ChunkValues> chunkValues = vs.chunkValues;
    private final IdentityHashMap<FallingBlock, byte[]> fallingBlockTransfer = new IdentityHashMap<>(8192);
    ////
    private FastUpdateHandler up;
    /*Fast Update Handler Stuff*/
    private final HashSet<LocalCoord>[] FallQuery;
    private final ArrayDeque<byte[]> chunkMark;
    private final HashMap<LocalCoord, byte[]>[] relValues;

    public WorldDataCore(KryoIO kryoIO) {
        random.nextLong();
        up = new FastUpdateHandler(9, 9);
        FallQuery = up.blockFallQuery;
        chunkMark = up.chunkValueMarker;
        relValues = up.chunkValueHolder;
        pu = new PlaceUpdate(vs, up);
        bu = new BreakUpdate(vs, up);
        kryoIO.setTerraKryoIO(cache_Count,terra_IOCache,yRegionTracker);
        kryoIO.setWorldKryoIO(chunkValues);
        this.kryoIO=kryoIO;
        HyperScheduler.scheduledExecutor.scheduleAtFixedRate(this::startTerraGuardian,45,20, TimeUnit.SECONDS);
    }

    private final void resetDataStructures(Operator resetType) {
        int relChunk, reset;
        byte[] rl;
        while ((rl = chunkMark.poll()) != null) {
            relChunk = 9 * rl[0] + rl[1];
            reset=relChunk<<16;
            for (int i = 65536; --i > -1;) {
                up.checkedCoords[reset | i] = false;
            }
            if(resetType== Operator.PLACE_BREAK) {
                FallQuery[relChunk].clear();
            }
            relValues[relChunk] = null;
        }
    }

    private int stxl = 1000000;
    private int stzl = 1000000;
    private HashMap<LocalCoord, byte[]> stlocValues = null;
    private ChunkCoord stchunkCoord = null;

    public void blockAtrophy(Block b){
        int x=b.getX(),z=b.getZ(),y=b.getY();
        LocalCoord lc = HyperKeys.localCoord[(y<<8)|(x<<28>>>28<<4)|(z<<28>>>28)];
        x>>=4; z>>=4;
        if (stxl != x || stzl != z) {
            stxl = x;
            stzl = z;
            stchunkCoord = Coords.CHUNK(x, z);
            stlocValues = chunkValues.get(stchunkCoord).blockVals;
        }
        byte[] bD = stlocValues.get(lc);
        if(b.getType()!=Material.AIR){
            if ((bD[2]-=WorldRules.ATROPHY_DAMAGE) <= 0) {
                b.setType(Material.AIR,false);
                blockBreak(lc);
            }else{
                DataUtil.stampTime(bD,WorldRules.G_TIME);
                terraDegen_Entry.relaxedOffer(b);
            }
        }
    }

    public void blockForm(Block b){
        int x=b.getX(),z=b.getZ(),y=b.getY();
        LocalCoord lc = HyperKeys.localCoord[(y<<8)|(x<<28>>>28<<4)|(z<<28>>>28)];
        x>>=4; z>>=4;
        if (stxl != x || stzl != z) {
            stxl = x;
            stzl = z;
            stchunkCoord = Coords.CHUNK(x,z);
            stlocValues = chunkValues.get(stchunkCoord).blockVals;
        }
        byte[] bD = stlocValues.get(lc);
        if(bD==null){
            //probably from chunk deload
            terraForm_Entry.relaxedOffer(b);
            return;
        }
        if(b.getType()!=WorldRules.TERRAIN_TYPE[bD[4]]) {
            if(stlocValues.get(HyperKeys.localCoord[(lc.parsedCoord&0xffff)-256])[3]==0){
                Bukkit.broadcastMessage("Block below == air");
                terraForm_Entry.relaxedOffer(b);
                byte[] bt;
                while((bt=stlocValues.get(lc=HyperKeys.localCoord[(lc.parsedCoord&0xffff)-256]))[4]>0){
                    if(bt[3]==0)terraForm_Entry.relaxedOffer(b.getRelative(0,(lc.parsedCoord>>>8)-y,0));
                }
            }else if ((bD[2] -= WorldRules.ATROPHY_DAMAGE) <= 0) {
                bD[2]=1;bD[3] = 1;
                b.setType(WorldRules.TERRAIN_TYPE[bD[4]]);
                blockPlace(lc, b);
            } else {
                DataUtil.stampTime(bD, WorldRules.G_TIME);
                terraForm_Entry.relaxedOffer(b);
            }
        }//else ignore

    }

    public void blockBreak(LocalCoord lc) {
        int rx,rz,relchunk,plc; Block b; World w = WorldRules.GAME_WORLD;

        bu.breakChecks(lc, stchunkCoord,stlocValues);
        pu.reUpdate(stchunkCoord);

        boolean hasFall=false;
        ArrayList<Block> blocksToFall = new ArrayList<>(FallQuery[40].size()+6);

        for(byte[] cmark : chunkMark) {
            rx=cmark[0];
            rz=cmark[1];
            relchunk = (9 * rx) + rz;
            rx=(rx+stxl-4)<<4; rz=(rz+stzl-4)<<4;
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
        if(!hasFall){ resetDataStructures(Operator.FALL); return; }

        if(WorldRules.ASYNC) {
            getScheduler().runTask(i, () -> injectFallingBlocks(blocksToFall));
        }else{
            injectFallingBlocks(blocksToFall);
        }
        resetDataStructures(Operator.PLACE_BREAK);
    }

    private void blockPlace(LocalCoord lc,Block b) {
        int rx,rz,relchunk,plc; World w = WorldRules.GAME_WORLD;
        byte[] t = stlocValues.computeIfAbsent(lc, nc-> new byte[]{126,126,1,1,1,0,0,0,0,0,0,0,0});
        t[2]=1;t[3]=1;
        pu.placeChecks(lc, stchunkCoord,stlocValues);

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
            rx=(rx+stxl-4)<<4; rz=(rz+stzl-4)<<4;
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
        if(!hasFall){ resetDataStructures(Operator.FALL); return; }
//        else resetDataStructures(Operator.PLACE_BREAK);
        if(WorldRules.ASYNC) {
            getScheduler().runTask(i, () -> injectFallingBlocks(blocksToFall));
        }else{
            injectFallingBlocks(blocksToFall);
        }
        resetDataStructures(Operator.PLACE_BREAK);
    }

    public void blockBreak(LocalCoord lc,ChunkCoord cc,HashMap<LocalCoord,byte[]> locValues) {
        int rx,rz, x=cc.parsedCoord>>16,z=cc.parsedCoord<<16>>16,relchunk,plc /*local coord*/; World w = WorldRules.GAME_WORLD;
        bu.breakChecks(lc, cc,locValues);
        pu.reUpdate(cc);

        boolean hasFall=false;
        ArrayList<Block> blocksToFall = new ArrayList<>(FallQuery[40].size()+6);

        Block b;
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
        if(!hasFall){ resetDataStructures(Operator.FALL); return; }
        //else resetDataStructures(Operator.PLACE_BREAK);
        if(WorldRules.ASYNC) {
            getScheduler().runTask(i, () -> injectFallingBlocks(blocksToFall));
        }else{
            injectFallingBlocks(blocksToFall);
        }
        resetDataStructures(Operator.PLACE_BREAK);
    }

    public void explosionUpdate(Block[] brokeBlocks,ChunkCoord cc, HashMap<LocalCoord,byte[]> localValues) {
        int rx,rz, x=cc.parsedCoord>>16,z=cc.parsedCoord<<16>>16,relchunk,plc /*local coord*/; World w = Bukkit.getWorld("world");

        bu.multiBreak(cc,localValues,brokeBlocks);
        pu.reUpdate(cc);

        boolean hasFall=false;
        ArrayList<Block> blocksToFall = new ArrayList<>(FallQuery[40].size()+4);

        Block b;
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
        if(!hasFall){ resetDataStructures(Operator.FALL); return; }
        if(WorldRules.ASYNC) {
            getScheduler().runTask(i, () -> injectFallingBlocks(blocksToFall));
        }else{
            injectFallingBlocks(blocksToFall);
        }
        resetDataStructures(Operator.PLACE_BREAK);
    }

    public void blockPlace(Block b,LocalCoord lc,ChunkCoord cc, HashMap<LocalCoord,byte[]> locValues) {
        int rx,rz, x=b.getX(),z=b.getZ(),relchunk, plc /*local coord*/,current=WorldRules.G_TIME;
        /*byte[] type = WorldRules.BLOCK_DEFAULTS.get(b.getType());
        if(type==null)type=WorldRules.DEFAULT_BLOCK;*/
        byte[] t = locValues.computeIfAbsent(lc, nc-> new byte[]{126,126,1,1,0,0,0,0,0,0,0,0,0});
        t[2]=1;t[3]=1;
        pu.placeChecks(lc, cc,locValues);
        boolean hasFall=false;
        ArrayList<Block> blocksToFall = new ArrayList<>(FallQuery[40].size());

        if (FallQuery[40].contains(lc)) {
            FallQuery[40].remove(lc);
            blocksToFall.add(b);
            hasFall=true;
        }else if(DataUtil.olderThanAndStamp(t,current-5,current)){
            terraDegen_Entry.offer(b);
        }

        for(byte[] cmark : chunkMark) {
            rx=cmark[0];
            rz=cmark[1];
            relchunk = (9 * rx) + rz;
            rx=(rx+x-4)<<4; rz=(rz+z-4)<<4;
            if (!FallQuery[relchunk].isEmpty()) {
                for (LocalCoord localCoord : FallQuery[relchunk]) {
                    plc = localCoord.parsedCoord;
                    b = WorldRules.GAME_WORLD.getBlockAt(rx|(plc<<24>>>28),plc>>>8,rz|(plc<<28>>>28));
                    if(b.getType()==Material.AIR) airError(b,relchunk,localCoord);
                    else blocksToFall.add(b);
//                        broadcastMessage("Data at: x "+b.getX()+" y "+b.getY()+" z "+b.getZ()+" relchunk: "+ (rx)+" "+(rz));
                }
                hasFall = true;
            }
        }
        if(!hasFall){ resetDataStructures(Operator.FALL); return; }
        if(WorldRules.ASYNC) {
            getScheduler().runTask(i, () -> injectFallingBlocks(blocksToFall));
        }else{
            injectFallingBlocks(blocksToFall);
        }
        resetDataStructures(Operator.PLACE_BREAK);
    }

    private void airError(Block b, int relchunk, LocalCoord localCoord){
        broadcastMessage("Error: ExplosionUpdate: Falling Air Block Queried");
        broadcastMessage("Data lost at: x "+b.getX()+" y "+b.getY()+" z "+b.getZ()+" relativeChunk: "+ (relchunk));
        broadcastMessage("Synchronous calculations suggested for better stability");
        byte[] data = relValues[relchunk].get(localCoord);
        data[0]=126;data[1]=126;data[2]=0;data[3]=0;
    }

    private void injectFallingBlocks(ArrayList<Block> blocks){
        int size=blocks.size(), loop; Block block; Location l; Material m; byte d; World w= Bukkit.getWorld(WorldRules.WORLD_ID);

        if(size>1) blocks.sort(sortBlocksY);
        final FallingBlock[] fallingBlocks = new FallingBlock[size];

        for(loop=-1;++loop<size;) {
            block = blocks.get(loop);
            l = block.getLocation();
            m = block.getType();
            d= block.getData();
            if(!block.getChunk().isLoaded())block.getChunk().load();
            block.setType(Material.AIR);
//            w.getBlockAt(l).setType(Material.AIR);
            fallingBlocks[loop] = w.spawnFallingBlock(l.subtract(0, 1, 0), m, d);
            l.add(0, 1, 0);
        }
//        getPluginManager().callEvent(new FallingBlockSpawnEvent(fallingBlocks, blocks));
        cacheFallingBlocks(fallingBlocks,blocks);
    }

    private int f_xl = 1000000;
    private int f_zl = 1000000;
    private HashMap<LocalCoord, byte[]> f_locValues = null;
    private ChunkCoord f_chunkCoord = null;

    public final void cacheFallingBlocks (FallingBlock[] fallingBlocks, ArrayList<Block> fallingBlockLocations){
        int current=WorldRules.G_TIME, oldestDate= current-5, fbsLength = fallingBlocks.length, x, z; FallingBlock fallingBlock; Block block; byte[] bdata; LocalCoord lc;

        for(int i = -1; ++i<fbsLength;){
            fallingBlock = fallingBlocks[i];
            block = fallingBlockLocations.get(i);

            x = block.getX(); z = block.getZ();
            lc = HyperKeys.localCoord[(block.getY()<<8)|(x<<28>>>28<<4)|(z<<28>>>28)];
            x>>=4; z>>=4;
            if(f_xl!=x || f_zl!=z){
                f_xl=x;
                f_zl=z;
                f_chunkCoord = Coords.CHUNK(x,z);
                f_locValues = chunkValues.get(f_chunkCoord).blockVals;
            }
            bdata = f_locValues.get(lc);
            if(DataUtil.olderThanAndStamp(bdata,oldestDate,current)&&bdata[4]>0){
                terraForm_Entry.relaxedOffer(block);
            }
            bdata[0]=126; bdata[1]=126;
            /*0=2-health,1=3-type*/
            byte[] bclone = {1,1,0};
            bclone[0]=bdata[2];
            bclone[1]=bdata[3];
            bdata[2]=0;
            bdata[3]=0;
            fallingBlockTransfer.put(fallingBlock,bclone);
        }
    }

    public void setFallenBlocks(FallingBlock fb, Block b){
        int x = b.getX(), z = b.getZ();
        LocalCoord lc = HyperKeys.localCoord[(b.getY()<<8)|(x<<28>>>28<<4)|(z<<28>>>28)];
        x>>=4; z>>=4;
        if(f_xl!=x || f_zl!=z){
            f_xl=x;
            f_zl=z;
            f_chunkCoord = Coords.CHUNK(x,z);
            f_locValues = chunkValues.get(f_chunkCoord).blockVals;
        }
        byte[] data = fallingBlockTransfer.get(fb);
        byte[] bdata;
        if(f_locValues.containsKey(lc)&&data != null) {
                bdata = f_locValues.get(lc);
                bdata[2]=data[0]; bdata[3]=data[1];
                fallingBlockTransfer.remove(fb);
        }else {
            bdata =new byte[]{126,126,1,1,0,0,0,0,0,0,0,0,0};
            f_locValues.put(lc,bdata);
            //switch case get type
        }
        int current = WorldRules.G_TIME;
        if(DataUtil.olderThanAndStamp(bdata,current-5,current)&&bdata[4]<1){
            terraDegen_Entry.relaxedOffer(b);
        }
        pu.blockLandCheck(lc,f_chunkCoord,f_locValues);
        resetDataStructures(Operator.FALL);
    }

    private final ObjectArrayFIFOQueue<ChunkCoord> terra_fileLoad = new ObjectArrayFIFOQueue<>(64);
    private final void queryTerraIO(){

        LCtoByteQ terra_Chunk; ChunkCoord cc; ChunkValues cv;
        for(Map.Entry<ChunkCoord, ChunkValues> entrySet: chunkValues.entrySet()){
//            Bukkit.broadcastMessage("Checking IO cache");
            cc=entrySet.getKey();
            cv=entrySet.getValue();
            if(cv.isLoaded) {
                Bukkit.broadcastMessage("chunk was loaded");
                terra_Chunk = terra_IOCache.getOrDefault(cc, null);
                if (terra_Chunk != null) {
                    int size =terra_Chunk.q1.size();
                    while (--size >= 0) {
                        if (terra_Chunk.q2.popByte() > 0) {
                            terraForm_IOEntry.enqueue(Coords.BLOCK_AT(terra_Chunk.q1.pop(), cc));
                            Bukkit.broadcastMessage("Adding to terraForm IO " +cache_Count);
                        } else {
                            terraDegen_IOEntry.enqueue(Coords.BLOCK_AT(terra_Chunk.q1.pop(), cc));
                        }
                        --cache_Count;
                    }
                    Bukkit.broadcastMessage("Checking cached files");
                }
                if (kryoIO.chunkFileInStorage(Operator.TERRA, cc.parsedCoord >> 16, cc.parsedCoord << 16 >> 16)) {
                    terra_fileLoad.enqueue(cc);
                }
            }else{
                Bukkit.broadcastMessage("Chunk marked as unloaded at: "+Coords.CHUNK_STRING(cc));
            }
        }
        if(!terra_fileLoad.isEmpty()){
            kryoIO.terraFileLoad(terra_fileLoad,terraForm_IOEntry,terraDegen_IOEntry);
        }else{
            Bukkit.broadcastMessage("No Files Cached");
        }
    }

    //Async Scheduled stuff
    private XRSR128pRand random = new XRSR128pRand(ThreadLocalRandom.current().nextLong()*System.currentTimeMillis(),System.nanoTime()*System.currentTimeMillis());
    public final SpscLinkedQueue<Block> terraForm_Entry = new SpscLinkedQueue<>();
    public final ObjectArrayFIFOQueue<Block> terraForm_IOEntry = new ObjectArrayFIFOQueue<>(256);

    public final SpscLinkedQueue<Block> terraDegen_Entry = new SpscLinkedQueue<>();
    public final ObjectArrayFIFOQueue<Block> terraDegen_IOEntry = new ObjectArrayFIFOQueue<>(256);
    //max val per cache 65536
    //for both time tracks, if on scheduler update the time, not on chunk load, could represent non used chunk to cache
    public int cache_Count = 0;
    public final Object2ObjectLinkedOpenHashMap<ChunkCoord, LCtoByteQ> terra_IOCache = new Object2ObjectLinkedOpenHashMap<>(256);

    //scheduled editing collections, should have limited size by their offload factors
    public volatile boolean degenTaskActive=false;
    public volatile boolean formTaskActive=false;
    public final Short2ObjectOpenHashMap<YGenTracker> yRegionTracker = new Short2ObjectOpenHashMap<YGenTracker>(4,0.8F);
    private final DataUtil.YBlockTerraSort yTSort = new DataUtil.YBlockTerraSort(chunkValues);
    private final LinkedList<Block> terraDegen = new LinkedList<>();
    private final ObjectArrayList<Block> terraForm = new ObjectArrayList<>(256);


    private int txl = 1000000;
    private int tzl = 1000000;
    private ChunkValues tlocValues = null;
    private ChunkCoord tchunkCoord = null;

    private final void assertLocalChunk_t(int x4, int z4){
        if (txl != x4 || tzl != z4) {
            txl = x4;
            tzl = z4;
            tchunkCoord = Coords.CHUNK(x4,z4);
            tlocValues = chunkValues.get(tchunkCoord);
        }
    }

    private final void startTerraGuardian(){

        Bukkit.broadcastMessage("Started Terra Guardian");
        queryTerraIO();
        Block b;
        int size, loop, g_time = WorldRules.G_TIME;
        ObjectArrayList<Block> toDegen;
        ObjectArrayList<Block> toForm;
        Int2ByteOpenHashMap toOverride = new Int2ByteOpenHashMap(16, Hash.FAST_LOAD_FACTOR);
        if(!degenTaskActive) {
            Bukkit.broadcastMessage("Pt1");
            while ((b = terraDegen_Entry.relaxedPoll()) != null) {
                if (updateCachePriority(b, toOverride, g_time, Operator.DEGEN)) {
                    terraDegen.add(b);
                }
            }
            Bukkit.broadcastMessage("Pt2");
            for (size = terraDegen_IOEntry.size(); --size >= 0; ) {
                if (updateCachePriority((b = terraDegen_IOEntry.dequeue()), toOverride, g_time, Operator.DEGEN)) {
                    terraDegen.add(b);
                }
            }
            Bukkit.broadcastMessage("Cache count after degen entries: "+cache_Count);
            Bukkit.broadcastMessage("Pt3");
            if (!terraDegen.isEmpty()) {
                toDegen = new ObjectArrayList<>(terraDegen.size() >>>8);
                ListIterator<Block> itr = terraDegen.listIterator();
                while (itr.hasNext()) {
                    b = itr.next();
                    if (random.nextInt(10) < 2) {
                        toDegen.add(b);
                        itr.remove();
                    }
                }
                if (!toDegen.isEmpty()) {
                    Bukkit.broadcastMessage("Starting Terra degen");
                    degenTaskActive = true;
                    new DegenBlocks(toDegen, toOverride,this).runTaskTimer(i, 20, 2);
                }
                itr = terraDegen.listIterator();
                while (itr.hasNext()) {
                    terraDegen_IOEntry.enqueue(itr.next());
                    itr.remove();
                }
            } else {
                Bukkit.broadcastMessage("Terra degen empty");
            }
        }
        if(!formTaskActive) {
            while ((b = terraForm_Entry.relaxedPoll()) != null) {
                if (updateCachePriority(b, toOverride, g_time, Operator.FORM)) {
                    terraForm.add(b);
                }
            }
            for (size = terraForm_IOEntry.size(); --size > -1; ) {
                Bukkit.broadcastMessage("Adding to terra form from IO");
                if (updateCachePriority((b = terraForm_IOEntry.dequeue()), toOverride, g_time, Operator.FORM)) {
                    terraForm.add(b);
                }
            }
            Bukkit.broadcastMessage("Cache count after form entries: "+cache_Count);
            if (!terraForm.isEmpty()) {
                Bukkit.broadcastMessage("Sorting Terra Form");
                ObjectLists.shuffle(terraForm, random);
                terraForm.unstableSort(yTSort);
                loop = (int) ((size = terraForm.size()) * 0.9D) - 1;
                toForm = new ObjectArrayList<>(size + 1);
                Bukkit.broadcastMessage("Checking Terra Form");
                while (--size >= loop) {
//                b =terraForm.remove(loop);
                    toForm.add(terraForm.pop());
                }
                if (!toForm.isEmpty()) {
                    Bukkit.broadcastMessage("Starting Terra Form");
                    formTaskActive = true;
                    new FormBlocks(toForm, toOverride, this, (size >> 11) + 2).runTaskTimer(i, 22, 3);
                }
                while(--size>-1) {
                    terraForm_IOEntry.enqueueFirst(terraForm.pop());
                }
            } else {
                if (formTaskActive) {
                    Bukkit.broadcastMessage("Terra form Active");
                } else {
                    Bukkit.broadcastMessage("Terra form empty");
                }
            }

        }/*else{
                kryoIO.saveTerraCache(cache_Count-(int)(cache_Count*0.05F));
         }*/
        if (cache_Count >= 50000) {
            kryoIO.saveTerraCache((int) (cache_Count * 0.6666D));
        }
        Bukkit.broadcastMessage("Completing Cycle");
    }

    private final boolean updateCachePriority(Block b, Int2ByteOpenHashMap toOverride, int g_time, Operator operator) {
        int x=b.getX(),z=b.getZ();
        LocalCoord lc = HyperKeys.localCoord[(b.getY() << 8) | (x << 28 >>> 28 << 4) | (z << 28 >>> 28)];
        LCtoByteQ bq;
        assertLocalChunk_t(x>>4,z>>4);
        terra_IOCache.putIfAbsent(tchunkCoord, new LCtoByteQ(64));
//        Bukkit.broadcastMessage("Updating cache");
        if (tlocValues==null||!tlocValues.isLoaded) {
            bq = terra_IOCache.getAndMoveToFirst(tchunkCoord);
            bq.q1.add(lc);
            bq.q2.add(operator == Operator.FORM?(byte)1:(byte)0);
            Bukkit.broadcastMessage("Block Cached in unloaded chunk");
//            Bukkit.broadcastMessage("this shouldn't be called right now");
            ++cache_Count;
            return false;
        } else{
            byte[] ts = tlocValues.blockVals.get(lc);
            if(DataUtil.youngerThan(ts,g_time)){
                if (Operator.FORM == operator) {
                    Bukkit.broadcastMessage("Form Block Too Young");
                    terraForm_IOEntry.enqueue(b);
                } else {
                    Bukkit.broadcastMessage("Degen Block Too Young");
                    terraDegen_IOEntry.enqueue(b);
                }
                return false;
            }else{
                tlocValues.overrideUnload=true;

                int i =toOverride.get(tchunkCoord.parsedCoord)&0xff;
                if(i!=255){
                    if(operator==Operator.FORM&&i!=15)toOverride.put(tchunkCoord.parsedCoord,(byte)(i|15));
                    else if(i!=240)toOverride.put(tchunkCoord.parsedCoord,(byte)(240|i));

                }
            }
        }
        Bukkit.broadcastMessage("pt5");
        return true;
    }
}
