package Methods;

import Enums.Coords;
import Factories.MemoryFactory;
import Storage.ChunkLocation;
import Storage.ValueStorage;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;

import java.util.*;

import static Methods.Mathz.yLocComp;
import static Runnables.BukkitInjector.injectFallingBlocks;


public class MethodInitializer {

    private ValueStorage vs;
    private PlaceUpdate pu;
    private BreakUpdate bu;
    private BedrockCheck bc;

    public MethodInitializer(ValueStorage vs){
        this.vs = vs;
        pu= new PlaceUpdate(vs);
        bu= new BreakUpdate(vs);
        bc= new BedrockCheck();

    }

    private void postBreakUpdate(Queue<Location>[] placeUpdate, Set<Location> fallQuery){
        for (int i = 0; i <placeUpdate.length; i++) {
            while(!placeUpdate[i].isEmpty()){
                pu.placeChecks(placeUpdate[i].poll(),fallQuery);
            }
        }
    }

    @Deprecated
    public void dropBlocks(Location[] fallQuery){
        if(fallQuery==null||fallQuery.length==0) return;
        Map<Location, Integer> healthTransfer = new HashMap<>();
        for (Location l : fallQuery) {
            if (vs.containsHealth(l)) {
                int i = vs.getHealth(l);
                healthTransfer.put(l,i);
                vs.delHealth(l);
            }
        }
    }

    public void setFallingBlockData(FallingBlock[] fbs, ArrayList<Location> ls){

        for(int n=0;n<ls.size();n++) {
            if(vs.containsHealth(ls.get(n))) {
                int i = vs.getHealth(ls.get(n));
                vs.putHTransfer(fbs[n], i);
                vs.delHealth(ls.get(n));
            }
        }

    }

    public boolean hasHealth(Block b){
        return vs.containsHealth(b.getLocation());
    }

    public void addHealth(Block b, int i){
        Location l = b.getLocation();
        if(vs.getHealth(l)<=2){
            vs.delHealth(l);
            return;
        }
        vs.addHealth(l,i);
    }

    public void breakPhysics(Block b){

        Location l = b.getLocation();

        if(vs.getOrMax(l)<Integer.MAX_VALUE-1) {
            Queue<Location>[] placeUpdate = MemoryFactory.newUpdateQuery();
            HashSet<Location> fallQuery = (HashSet<Location>) MemoryFactory.newFallQuery();
            bu.breakChecks(l,placeUpdate, fallQuery);
            postBreakUpdate(placeUpdate, fallQuery);
            fallQuery.remove(l);
            if(fallQuery.size()>0) {
                ArrayList<Location> sortedLowestYQuery = new ArrayList<>(fallQuery);
                sortedLowestYQuery.sort(yLocComp);
                injectFallingBlocks(sortedLowestYQuery);
            }
        }
    }

    public void placePhysics(Block b){
        Location l = b.getLocation();
        vs.putHealth(l,b.getType());
        HashSet<Location> fallQuery = (HashSet<Location>) MemoryFactory.newFallQuery();
        pu.placeChecks(l,fallQuery);
        if(fallQuery.size()>0){
            ArrayList<Location> sortedLowestYQuery = new ArrayList<>(fallQuery);
            sortedLowestYQuery.sort(yLocComp);
            injectFallingBlocks(sortedLowestYQuery);
        }
    }

    public void placePhysicsFall(FallingBlock fb, Block b){
        Location l = b.getLocation();
        if(vs.containsHTransfer(fb)) {
            vs.putHealth(l, vs.getHTransfer(fb));
            vs.delHTransfer(fb);
        }
        pu.placeChecksFall(l);
    }

    public void placePhysicsChunk(Chunk c){
        newChunkData(c);
        for(int y = 1; y<=3; y++) {
            Block b = c.getBlock(0,y,0);
            if(b.getType()!=Material.BEDROCK&&b.getType()!=Material.AIR) {
                Location l = b.getLocation();
                pu.placeChecksChunkRestricted(l, Coords.CHUNK(c));
            }
        }
    }

    public void newChunkData(Chunk c){

        vs.newChunkData(c);
        bc.checkBedrock(c).forEach(l -> {vs.put(l,0); vs.putHealth(l,Integer.MAX_VALUE-1);});
//        pu.placeChecks(c.getBlock(0,2,0).getLocation());

    }

    public void deleteChunkData(ChunkLocation cl){
        vs.clearChunkData(cl);
    }

    public boolean containsChunkData(Chunk c){
        return vs.containsChunkData(c);
    }

    public boolean containsChunkData(ChunkLocation cl){
        return vs.containsChunkData(cl);
    }


    public int getTensileValue(Block b){
        return vs.getOrMax(b.getLocation());
    }

    public int getHealthValue(Block b){
        Location l = b.getLocation();
        if(vs.containsHealth(l)) {
            return vs.getHealth(l);
        }
        return 1;
    }



}
