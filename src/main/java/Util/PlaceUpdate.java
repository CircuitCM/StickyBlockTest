package Util;

import PositionalKeys.ChunkCoord;
import PositionalKeys.HyperKeys;
import PositionalKeys.LocalCoord;
import Settings.HSettings;
import Storage.ChunkValues;
import Storage.FastUpdateHandler;
import Storage.ValueStorage;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.jctools.maps.NonBlockingHashMap;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;


public class PlaceUpdate {

    private NonBlockingHashMap<ChunkCoord, ChunkValues> chunkData;

    public PlaceUpdate(ValueStorage vs, FastUpdateHandler updateHandler){

        checked = updateHandler.checkedCoords;
        chunkVals = updateHandler.chunkValueHolder;
        chunkMark = updateHandler.chunkValueMarker;
        fallQuery = updateHandler.blockFallQuery;
        rcr = updateHandler.relativeChunkReference;
        updtPtrs = updateHandler.blockUpdate;
        chunkData = vs.chunkValues;
    }

    private final boolean[] checked;
    private final HashMap<LocalCoord,byte[]>[] chunkVals;
    private final Queue<byte[]> chunkMark;
    private final HashSet<LocalCoord>[] fallQuery;
    private final byte[][] rcr;

    //next coord to check
    private Queue<LocalCoord> coordQ = new ArrayDeque<>(2048);
    //which chunk it's in
    private Queue<byte[]> chunkRef = new ArrayDeque<>(2048);
    //coord key
    private LocalCoord[] ls = new LocalCoord[6];
    // relative tensile values
    private final byte[] tenseVals = new byte[6];
    // relative chunk coord holder
    private final byte[] lrs = new byte[12];
    // will continue updating relatives
    private final boolean[] ms = new boolean[6];

    public void placeChecks(LocalCoord lc, ChunkCoord cc) {

        byte loop;
        byte valref;
        int parsedCoord;
        byte[] r;
        byte[] t;
        byte xc;
        byte zc;
        LocalCoord l;
        final byte range = HSettings.TENSILE_RANGE;
        int relChunk = 9*4+4;
        //make local check if helps
        Queue<LocalCoord> coordQ = this.coordQ;
        Queue<byte[]> chunkRef = this.chunkRef;
        HashMap<LocalCoord,byte[]>[] chunkVals = this.chunkVals;

        coordQ.add(lc);
        chunkRef.add(rcr[relChunk]);
        checked[(relChunk<<16)+(lc.parsedCoord & 0xffff)]=true;
        //move outside, don't need to now
        byte[] data = chunkData.get(cc).blockVals.get(lc);
        data[2]=1;
//        chunkVals[relChunk] = cv.blockVals;
//        chunkVals[relChunk].get(lc)[2]=1;
        chunkMark.add(rcr[relChunk]);

        while(!coordQ.isEmpty()) {

            l = coordQ.poll();
            r = chunkRef.poll();

            parsedCoord = l.parsedCoord;
            xc = r[0];
            zc = r[1];

            //z pos get
            if(parsedCoord<<28>>>28==15){
                ls[0]= HyperKeys.localCoord[(parsedCoord<<16>>>24<<8)|(parsedCoord<<24>>>28<<4)];
                lrs[0]=xc;
                lrs[1]=(byte)(zc+1);
            }else{
                ls[0]= HyperKeys.localCoord[parsedCoord+1];
                lrs[0]=xc;
                lrs[1]=zc;
            }
            //z neg
            if(parsedCoord<<28>>>28==0){
                ls[1]= HyperKeys.localCoord[(parsedCoord<<16>>>24<<8)|(parsedCoord<<24>>>28<<4)|0x0f];
                lrs[2]=xc;
                lrs[3]=(byte)(zc-1);
            }else{
                ls[1]= HyperKeys.localCoord[parsedCoord-1];
                lrs[2]=xc;
                lrs[3]=zc;
            }
            //x pos
            if(parsedCoord<<24>>>28==15){
                ls[2]= HyperKeys.localCoord[(parsedCoord<<16>>>24<<8)|(parsedCoord<<28>>>28)];
                lrs[4]=(byte)(xc+1);
                lrs[5]=zc;
            }else{
                ls[2]= HyperKeys.localCoord[parsedCoord+16];
                lrs[4]=xc;
                lrs[5]=zc;
            }
            //x neg
            if(parsedCoord<<24>>>28==0){
                ls[3]= HyperKeys.localCoord[(parsedCoord<<16>>>24<<8)|0x0f0|(parsedCoord<<28>>>28)];
                lrs[6]=(byte)(xc-1);
                lrs[7]=zc;
            }else{
                ls[3]= HyperKeys.localCoord[parsedCoord-16];
                lrs[6]=xc;
                lrs[7]=zc;
            }
            //up
            ls[4]= HyperKeys.localCoord[parsedCoord+256];
            lrs[8]=xc;
            lrs[9]=zc;

            //down
            ls[5]= HyperKeys.localCoord[parsedCoord-256];
            lrs[10]=xc;
            lrs[11]=zc;

            for (loop=-1; ++loop<6;) {

                relChunk =9*lrs[loop<<1]+lrs[(loop<<1)+1];

                if(chunkVals[relChunk]==null){
                    chunkVals[relChunk]=chunkData.get(new ChunkCoord(((cc.parsedCoord>>16)+lrs[loop<<1])-4,((cc.parsedCoord<<16>>16)+lrs[(loop<<1)+1])-4)).blockVals;
                    chunkMark.add(rcr[relChunk]);
                }

                t = chunkVals[relChunk].computeIfAbsent(ls[loop], n -> new byte[]{126, 0, 0, 0, 0, 0});

                ms[loop] =!(checked[(relChunk<<16)|(ls[loop].parsedCoord & 0xffff)]||t[2]==0);
                tenseVals[loop]=t[0];
            }

            valref = Mathz.lowestTensile(tenseVals);
            relChunk = 9*r[0]+r[1];
            chunkVals[relChunk].get(l)[0]=valref;

            if (valref < range) {
                fallQuery[relChunk].remove(l);
            }
            if (valref >= range) {
                fallQuery[relChunk].add(l);
            }
            for (loop = -1; ++loop<6;) {
                if (ms[loop] && valref < tenseVals[loop]) {
                    relChunk =9*lrs[loop<<1]+lrs[(loop<<1)+1];
                    checked[(relChunk<<16)|(ls[loop].parsedCoord & 0xffff)]=true;
                    coordQ.add(ls[loop]);
                    chunkRef.add(rcr[relChunk]);
                }
            }
        }
    }

    //next coord to check
    private Queue<LocalCoord> ccoordQ = new ArrayDeque<>(1024);

    private final boolean[] cchecked = new boolean[65536];
    //coord key
    private LocalCoord[] cls = new LocalCoord[6];
    // relative tensile values
    private final byte[] ctenseVals = new byte[6];
    // will continue updating relatives
    private final boolean[] cms = new boolean[6];

    public void setNewChunk(ChunkSnapshot c) {

        int loop;
        byte valref;
        final int parsedChunk=(c.getX()<<16)|c.getZ();
        final ChunkCoord cc = new ChunkCoord(parsedChunk);
        int parsedCoord;
        byte[] t;
        LocalCoord l;
        chunkData.put(cc, new ChunkValues());
        HashMap<LocalCoord,byte[]> chunkVals = chunkData.get(cc).blockVals;

        for (loop=-1; ++loop<256;) {
            l = HyperKeys.localCoord[loop];
            chunkVals.put(l, new byte[]{0, 127, 0, 0, 0, 0});
            ccoordQ.add(HyperKeys.localCoord[l.parsedCoord]);
        }

        while(!ccoordQ.isEmpty()) {

            l = ccoordQ.poll();

            parsedCoord = l.parsedCoord;
            //Bukkit.getServer().broadcastMessage("Checking Local Coordinate, X: "+(parsedCoord<<24>>>28)+" Z: "+(parsedCoord<<28>>>28));

            //z pos get
            if(parsedCoord<<28>>>28==15){
                cls[1]= null;
            }else{
                cls[1]= HyperKeys.localCoord[parsedCoord+1];
                //Bukkit.getServer().broadcastMessage("It's relative z-pos, X: "+((parsedCoord+1)<<24>>>28)+" Z: "+((parsedCoord+1)<<28>>>28));
            }
            //z neg
            if(parsedCoord<<28>>>28==0){
                cls[0]= null;
            }else{
                cls[0]= HyperKeys.localCoord[parsedCoord-1];
            }
            //x pos
            if(parsedCoord<<24>>>28==15){
                cls[2]= null;
            }else{
                cls[2]= HyperKeys.localCoord[parsedCoord+16];
                //Bukkit.getServer().broadcastMessage("It's relative x-pos, X: "+((parsedCoord+16)<<24>>>28)+" Z: "+((parsedCoord+16)<<28>>>28));
            }
            //x neg
            if(parsedCoord<<24>>>28==0){
                cls[3]= null;
            }else{
                cls[3]= HyperKeys.localCoord[parsedCoord-16];
                //Bukkit.getServer().broadcastMessage("It's relative x-neg, X: "+((parsedCoord-16)<<24>>>28)+" Z: "+((parsedCoord-16)<<28>>>28));
            }
            //up
            if(parsedCoord<<16>>>24==255) {
                cls[4] = null;
            }else{
                cls[4] = HyperKeys.localCoord[parsedCoord + 256];
            }

            //down
            if(parsedCoord<<16>>>24==0) {
                cls[5] = null;
            }else {
                cls[5] = HyperKeys.localCoord[parsedCoord - 256];
            }

            for (loop=-1; ++loop<6;) {
                if(cls[loop]==null){
                    cms[loop]=false;
                    ctenseVals[loop]=126;
                }else {
                    parsedCoord = cls[loop].parsedCoord;
                    Material thisBlock = Material.getMaterial(c.getBlockTypeId(parsedCoord << 24 >>> 28, parsedCoord >>> 8, parsedCoord << 28 >>> 28));
                    if(thisBlock==null) thisBlock=Material.AIR;
                    t = chunkVals.computeIfAbsent(cls[loop], n -> new byte[]{126, 0, 1, 0, 0, 0});
                    switch (thisBlock) {
                        case STATIONARY_LAVA:
                        case STATIONARY_WATER:
                        case AIR:
                            t[2] = 0;
                            break;
                        case BEDROCK:
                            t[0] = 0;
                            t[1] = 127;
                            t[2] = 0;
                            break;
                    }

                    cms[loop] = !(cchecked[cls[loop].parsedCoord & 0xffff] || t[2] == 0);
                    ctenseVals[loop] = t[0];
                }
            }

            valref = Mathz.lowestTensile(ctenseVals);
            chunkVals.get(l)[0]=valref;

            for (loop=-1; ++loop<6;) {
                if (cms[loop] && valref<ctenseVals[loop]) {
                    cchecked[cls[loop].parsedCoord & 0xffff]=true;
                    ccoordQ.add(cls[loop]);
                }
            }
        }

        for(LocalCoord lc : chunkVals.keySet()){
            cchecked[lc.parsedCoord & 0xffff]=false;
        }
    }

    private final ArrayDeque<LocalCoord>[] updtPtrs;

    public void reUpdate() {

        byte loop;
        byte valref;
        int parsedCoord;
        byte[] r;
        byte[] t;
        byte xc;
        byte zc;
        LocalCoord l;
        int relChunk;
        final byte range = HSettings.TENSILE_RANGE;

        for(byte[] rl:chunkMark){
            relChunk=9*rl[0]+rl[1];
            if(updtPtrs[relChunk].isEmpty())continue;
            l = updtPtrs[relChunk].poll();
            while (l!=null){
                coordQ.add(l);
                chunkRef.add(rcr[relChunk]);
                l=updtPtrs[relChunk].poll();
            }
        }

        while(!coordQ.isEmpty()) {

            l = coordQ.poll();
            r = chunkRef.poll();

            parsedCoord = l.parsedCoord;
            xc = r[0];
            zc = r[1];

            //z pos get
            if(parsedCoord<<28>>>28==15){
                ls[0]= HyperKeys.localCoord[(parsedCoord<<16>>>24<<8)|(parsedCoord<<24>>>28<<4)];
                lrs[0]=xc;
                lrs[1]=(byte)(zc+1);
            }else{
                ls[0]= HyperKeys.localCoord[parsedCoord+1];
                lrs[0]=xc;
                lrs[1]=zc;
            }
            //z neg
            if(parsedCoord<<28>>>28==0){
                ls[1]= HyperKeys.localCoord[(parsedCoord<<16>>>24<<8)|(parsedCoord<<24>>>28<<4)|0x0f];
                lrs[2]=xc;
                lrs[3]=(byte)(zc-1);
            }else{
                ls[1]= HyperKeys.localCoord[parsedCoord-1];
                lrs[2]=xc;
                lrs[3]=zc;
            }
            //x pos
            if(parsedCoord<<24>>>28==15){
                ls[2]= HyperKeys.localCoord[(parsedCoord<<16>>>24<<8)|(parsedCoord<<28>>>28)];
                lrs[4]=(byte)(xc+1);
                lrs[5]=zc;
            }else{
                ls[2]= HyperKeys.localCoord[parsedCoord+16];
                lrs[4]=xc;
                lrs[5]=zc;
            }
            //x neg
            if(parsedCoord<<24>>>28==0){
                ls[3]= HyperKeys.localCoord[(parsedCoord<<16>>>24<<8)|0x0f0|(parsedCoord<<28>>>28)];
                lrs[6]=(byte)(xc-1);
                lrs[7]=zc;
            }else{
                ls[3]= HyperKeys.localCoord[parsedCoord-16];
                lrs[6]=xc;
                lrs[7]=zc;
            }
            //up
            ls[4]= HyperKeys.localCoord[parsedCoord+256];
            lrs[8]=xc;
            lrs[9]=zc;

            //down
            ls[5]= HyperKeys.localCoord[parsedCoord-256];
            lrs[10]=xc;
            lrs[11]=zc;

            for (loop=-1; ++loop<6;) {
                relChunk =9*lrs[loop<<1]+lrs[(loop<<1)+1];
                t = chunkVals[relChunk].computeIfAbsent(ls[loop], n -> new byte[]{126, 0, 0, 0, 0, 0});
                ms[loop] =!(checked[(relChunk<<16)|(ls[loop].parsedCoord & 0xffff)]||t[2]==0);
                tenseVals[loop]=t[0];
            }

            valref = Mathz.lowestTensile(tenseVals);
            relChunk = 9*r[0]+r[1];
            chunkVals[relChunk].get(l)[0]=valref;

            if (valref < range) {
                fallQuery[relChunk].remove(l);
            }
            if (valref >= range) {
                fallQuery[relChunk].add(l);
            }
            for (loop = -1; ++loop<6;) {
                if (ms[loop] && valref < tenseVals[loop]) {
                    relChunk =9*lrs[loop<<1]+lrs[(loop<<1)+1];
                    checked[(relChunk<<16)|(ls[loop].parsedCoord & 0xffff)]=true;
                    coordQ.add(ls[loop]);
                    chunkRef.add(rcr[relChunk]);
                }
            }
        }
    }
}
