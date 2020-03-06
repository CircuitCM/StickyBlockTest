package Util;

import PositionalKeys.ChunkCoord;
import PositionalKeys.LocalCoord;
import Settings.WorldRules;
import Storage.ChunkValues;
import Storage.FastUpdateHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.jctools.maps.NonBlockingHashMap;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;

import static PositionalKeys.HyperKeys.localCoord;


public class PlaceUpdate {

    private final NonBlockingHashMap<ChunkCoord, ChunkValues> chunkValues;
    private final int tensilerange = WorldRules.TENSILE_RANGE;

    public PlaceUpdate(NonBlockingHashMap<ChunkCoord,ChunkValues> cv, FastUpdateHandler updateHandler){

        checked = updateHandler.checkedCoords;
        chunkVals = updateHandler.chunkValueHolder;
        chunkMark = updateHandler.chunkValueMarker;
        fallQuery = updateHandler.blockFallQuery;
        rcr = updateHandler.relativeChunkReference;
        updtPtrs = updateHandler.blockUpdate;
        chunkValues=cv;
    }

    private final boolean[] checked;
    private final HashMap<LocalCoord,byte[]>[] chunkVals;
    private final ArrayDeque<byte[]> chunkMark;
    private final HashSet<LocalCoord>[] fallQuery;
    private final byte[][] rcr;

    //next coord to check
    private ArrayDeque<LocalCoord> coordQ = new ArrayDeque<>(2048);
    //which chunk it's in
    private ArrayDeque<byte[]> chunkRef = new ArrayDeque<>(2048);
    //coord key
    private LocalCoord[] ls = new LocalCoord[6];
    // relative tensile values
    private final byte[] tenseVals = new byte[6];
    // relative chunk coord holder
    private final byte[] lrs = new byte[12];
    // will continue updating relatives
    private final boolean[] ms = new boolean[6];

    public void placeChecks(LocalCoord lc, ChunkCoord cc, HashMap<LocalCoord,byte[]> localValues) {

        byte loop;
        byte valref;
        byte[] r;
        byte[] t;
        byte xc;
        byte zc;
        byte range = tensilerange;
        int parsedCoord, relChunk = 9*4+4;
        LocalCoord l;
        //make local check if helps
        ArrayDeque<LocalCoord> coordQ = this.coordQ;
        ArrayDeque<byte[]> chunkRef = this.chunkRef;
        HashMap<LocalCoord,byte[]>[] chunkVals = this.chunkVals;
        LocalCoord[] ls = this.ls;
        byte[] tenseVals = this.tenseVals;
        byte[] lrs =this.lrs;
        boolean[] ms = this.ms;
        boolean[] checked = this.checked;

        coordQ.add(lc);
        chunkRef.add(rcr[relChunk]);
        chunkVals[relChunk] = localValues;
        chunkMark.add(rcr[relChunk]);
        checked[(relChunk<<16)|(lc.parsedCoord & 0xffff)]=true;

        while(!coordQ.isEmpty()) {

            l = coordQ.poll();
            r = chunkRef.poll();

            parsedCoord = l.parsedCoord;
            xc = r[0];
            zc = r[1];

            //z pos get
            if(parsedCoord<<28>>>28==15){
                ls[0]= localCoord[(parsedCoord<<16>>>24<<8)|(parsedCoord<<24>>>28<<4)];
                lrs[0]=xc; lrs[1]=(byte)(zc+1);
            }else{
                ls[0]= localCoord[parsedCoord+1];
                lrs[0]=xc; lrs[1]=zc;
            }
            //z neg
            if(parsedCoord<<28>>>28==0){
                ls[1]= localCoord[(parsedCoord<<16>>>24<<8)|(parsedCoord<<24>>>28<<4)|0x0f];
                lrs[2]=xc; lrs[3]=(byte)(zc-1);
            }else{
                ls[1]= localCoord[parsedCoord-1];
                lrs[2]=xc; lrs[3]=zc;
            }
            //x pos
            if(parsedCoord<<24>>>28==15){
                ls[2]= localCoord[(parsedCoord<<16>>>24<<8)|(parsedCoord<<28>>>28)];
                lrs[4]=(byte)(xc+1); lrs[5]=zc;
            }else{
                ls[2]= localCoord[parsedCoord+16];
                lrs[4]=xc; lrs[5]=zc;
            }
            //x neg
            if(parsedCoord<<24>>>28==0){
                ls[3]= localCoord[(parsedCoord<<16>>>24<<8)|0x0f0|(parsedCoord<<28>>>28)];
                lrs[6]=(byte)(xc-1); lrs[7]=zc;
            }else{
                ls[3]= localCoord[parsedCoord-16];
                lrs[6]=xc; lrs[7]=zc;
            }
            //up
            ls[4]= localCoord[parsedCoord+256];
            lrs[8]=xc; lrs[9]=zc;

            //down
            ls[5]= localCoord[parsedCoord-256];
            lrs[10]=xc; lrs[11]=zc;

            for (loop=-1; ++loop<6;) {

                relChunk =9*lrs[loop<<1]+lrs[(loop<<1)+1];

                if(chunkVals[relChunk]==null){
                    chunkVals[relChunk]=chunkValues.get(Coords.CHUNK(((cc.parsedCoord>>16)+lrs[loop<<1])-4,((cc.parsedCoord<<16>>16)+lrs[(loop<<1)+1])-4)).blockVals;
                    chunkMark.add(rcr[relChunk]);
                }

                t = chunkVals[relChunk].computeIfAbsent(ls[loop], n -> new byte[]{126,126,0,0,0,0,0,0,0,0,0,0,0});

                ms[loop] =!(checked[(relChunk<<16)|(ls[loop].parsedCoord & 0xffff)]||t[3]==0);
                tenseVals[loop]=t[0];
            }

            valref = DataUtil.lowestTensile(tenseVals);
            relChunk = 9*r[0]+r[1];
            chunkVals[relChunk].get(l)[0]=valref;

            if (valref >= range) {
                fallQuery[relChunk].add(l);
            }
            if (valref < range) {
                fallQuery[relChunk].remove(l);
            }

            for (loop = -1; ++loop<6;) {
                if (ms[loop] && valref < tenseVals[loop]) {
                    relChunk =9*lrs[loop<<1]+lrs[(loop<<1)+1];
                    coordQ.add(ls[loop]);
                    chunkRef.add(rcr[relChunk]);
                    checked[(relChunk<<16)|(ls[loop].parsedCoord & 0xffff)]=true;
                }
            }
        }
    }

    public void blockLandCheck(LocalCoord lc, ChunkCoord cc, HashMap<LocalCoord,byte[]> localValues) {

        byte loop;
        byte valref;
        byte[] r;
        byte[] t;
        byte xc;
        byte zc;
        int parsedCoord, relChunk = 9*4+4;
        LocalCoord l;
        //make local check if helps
        ArrayDeque<LocalCoord> coordQ = this.coordQ;
        ArrayDeque<byte[]> chunkRef = this.chunkRef;
        HashMap<LocalCoord,byte[]>[] chunkVals = this.chunkVals;
        LocalCoord[] ls = this.ls;
        byte[] tenseVals = this.tenseVals;
        byte[] lrs =this.lrs;
        boolean[] ms = this.ms;
        boolean[] checked = this.checked;

        coordQ.add(lc);
        chunkRef.add(rcr[relChunk]);
        checked[(relChunk<<16)|(lc.parsedCoord & 0xffff)]=true;
        chunkVals[relChunk] = localValues;
        chunkMark.add(rcr[relChunk]);

        while(!coordQ.isEmpty()) {

            l = coordQ.poll();
            r = chunkRef.poll();

            parsedCoord = l.parsedCoord;
            xc = r[0];
            zc = r[1];
            //z pos get
            if(parsedCoord<<28>>>28==15){
                ls[0]= localCoord[(parsedCoord<<16>>>24<<8)|(parsedCoord<<24>>>28<<4)];
                lrs[0]=xc; lrs[1]=(byte)(zc+1);
            }else{
                ls[0]= localCoord[parsedCoord+1];
                lrs[0]=xc; lrs[1]=zc;
            }
            //z neg
            if(parsedCoord<<28>>>28==0){
                ls[1]= localCoord[(parsedCoord<<16>>>24<<8)|(parsedCoord<<24>>>28<<4)|0x0f];
                lrs[2]=xc; lrs[3]=(byte)(zc-1);
            }else{
                ls[1]= localCoord[parsedCoord-1];
                lrs[2]=xc; lrs[3]=zc;
            }
            //x pos
            if(parsedCoord<<24>>>28==15){
                ls[2]= localCoord[(parsedCoord<<16>>>24<<8)|(parsedCoord<<28>>>28)];
                lrs[4]=(byte)(xc+1); lrs[5]=zc;
            }else{
                ls[2]= localCoord[parsedCoord+16];
                lrs[4]=xc; lrs[5]=zc;
            }
            //x neg
            if(parsedCoord<<24>>>28==0){
                ls[3]= localCoord[(parsedCoord<<16>>>24<<8)|0x0f0|(parsedCoord<<28>>>28)];
                lrs[6]=(byte)(xc-1); lrs[7]=zc;
            }else{
                ls[3]= localCoord[parsedCoord-16];
                lrs[6]=xc; lrs[7]=zc;
            }
            //up
            ls[4]= localCoord[parsedCoord+256];
            lrs[8]=xc; lrs[9]=zc;

            //down
            ls[5]= localCoord[parsedCoord-256];
            lrs[10]=xc; lrs[11]=zc;

            for (loop=-1; ++loop<6;) {

                relChunk =9*lrs[loop<<1]+lrs[(loop<<1)+1];

                if(chunkVals[relChunk]==null){
                    chunkVals[relChunk]=chunkValues.get(Coords.CHUNK(((cc.parsedCoord>>16)+lrs[loop<<1])-4,((cc.parsedCoord<<16>>16)+lrs[(loop<<1)+1])-4)).blockVals;
                    chunkMark.add(rcr[relChunk]);
                }

                t = chunkVals[relChunk].computeIfAbsent(ls[loop], n -> new byte[]{126,126,0,0,0,0,0,0,0,0,0,0,0});

                ms[loop] =!(checked[(relChunk<<16)|(ls[loop].parsedCoord & 0xffff)]||t[3]==0);
                tenseVals[loop]=t[0];
            }

            valref = DataUtil.lowestTensile(tenseVals);
            relChunk = 9*r[0]+r[1];
            chunkVals[relChunk].get(l)[0]=valref;

            for (loop = -1; ++loop<6;) {
                if (ms[loop] && valref < tenseVals[loop]) {
                    relChunk =9*lrs[loop<<1]+lrs[(loop<<1)+1];
                    coordQ.add(ls[loop]);
                    chunkRef.add(rcr[relChunk]);
                    checked[(relChunk<<16)|(ls[loop].parsedCoord & 0xffff)]=true;
                }
            }
        }
    }

    //next coord to check
    private ArrayDeque<LocalCoord> ccoordQ = new ArrayDeque<>(1024);

    private final boolean[] cchecked = new boolean[65536];
    //coord key
    private LocalCoord[] cls = new LocalCoord[6];
    // relative tensile values
    private final byte[] ctenseVals = new byte[6];
    // will continue updating relatives
    private final boolean[] cms = new boolean[6];

    public void setNewChunk(ChunkSnapshot c) {

        int loop, parsedCoord;
        byte valref;
        byte[] t;
        LocalCoord l;
        ChunkCoord cc = Coords.CHUNK(c.getX(),c.getZ());
        HashMap<LocalCoord,byte[]> chunkVals = chunkValues.get(cc).blockVals;

        for (loop=-1; ++loop<256;) {
            l = localCoord[loop];
            chunkVals.put(l, new byte[]{0,0,127,0,0,0,0,0,0,0,0,0,0});
            ccoordQ.add(l);
        }

        while(!ccoordQ.isEmpty()) {

            l = ccoordQ.poll();
            parsedCoord = l.parsedCoord;

            //z pos get
            if(parsedCoord<<28>>>28==15)cls[1]= null;
            else cls[1]= localCoord[parsedCoord+1];
            //z neg
            if(parsedCoord<<28>>>28==0) cls[0]= null;
            else cls[0]= localCoord[parsedCoord-1];
            //x pos
            if(parsedCoord<<24>>>28==15)cls[2]= null;
            else cls[2]= localCoord[parsedCoord+16];
            //x neg
            if(parsedCoord<<24>>>28==0)cls[3]= null;
            else cls[3]= localCoord[parsedCoord-16];
            //up
            if(parsedCoord<<16>>>24==255) cls[4] = null;
            else cls[4] = localCoord[parsedCoord + 256];
            //down
            if(parsedCoord<<16>>>24==0)cls[5] = null;
            else cls[5] = localCoord[parsedCoord - 256];

            for (loop=-1; ++loop<6;) {
                if(cls[loop]==null){
                    cms[loop]=false;
                    ctenseVals[loop]=126;
                }else {
                    parsedCoord = cls[loop].parsedCoord;
                    Material thisBlock = Material.getMaterial(c.getBlockTypeId(parsedCoord << 24 >>> 28, parsedCoord >>> 8, parsedCoord << 28 >>> 28));
                    if(thisBlock==null) thisBlock=Material.AIR;
                    t = chunkVals.computeIfAbsent(cls[loop], n -> new byte[]{126,126,1,1,0,0,0,0,0,0,0,0,0});
                    switch (thisBlock) {
                        case STATIONARY_LAVA:
                        case STATIONARY_WATER:
                        case WATER:
                        case LAVA:
                        case SAND:
                        case GRAVEL:
                        case TNT:
                        case FIRE:
                        case AIR:
                            t[1] = 0;
                            t[2] = 0;
                            break;
                        case BEDROCK:
                            t[0] = 0;
                            t[1] = 127;
                            t[2] = 0;
                            break;
                    }
                    cms[loop] = !(cchecked[cls[loop].parsedCoord & 0xffff] || t[3] == 0);
                    ctenseVals[loop] = t[0];
                }
            }

            valref = DataUtil.lowestTensile(ctenseVals);
            chunkVals.get(l)[0]=valref;

            for (loop=-1; ++loop<6;) {
                if (cms[loop] && valref<ctenseVals[loop]) {
                    cchecked[cls[loop].parsedCoord & 0xffff]=true;
                    ccoordQ.add(cls[loop]);
                }
            }
        }
        for(LocalCoord lc : chunkVals.keySet()) cchecked[lc.parsedCoord & 0xffff]=false;
    }

    private final ArrayDeque<LocalCoord>[] updtPtrs;

    public void reUpdate(ChunkCoord cc) {

        byte loop;
        byte valref;
        byte[] r;
        byte[] t;
        byte xc;
        byte zc;
        byte range = WorldRules.TENSILE_RANGE;
        int parsedCoord, relChunk;
        LocalCoord l;

        ArrayDeque<LocalCoord> coordQ = this.coordQ;
        ArrayDeque<byte[]> chunkRef = this.chunkRef;
        HashMap<LocalCoord,byte[]>[] chunkVals = this.chunkVals;
        LocalCoord[] ls = this.ls;
        byte[] tenseVals = this.tenseVals;
        byte[] lrs =this.lrs;
        boolean[] ms = this.ms;
        boolean[] checked = this.checked;

        for(byte[] rl:chunkMark){
            relChunk=9*rl[0]+rl[1];
            l = updtPtrs[relChunk].pollLast();
            while (l!=null){
                coordQ.add(l);
                chunkRef.add(rl);
                l=updtPtrs[relChunk].pollLast();
            }
        }

        while(!coordQ.isEmpty()) {

            l = coordQ.poll(); r = chunkRef.poll(); parsedCoord = l.parsedCoord; xc = r[0]; zc = r[1];
            if(xc>6||xc<2) Bukkit.broadcastMessage("xc out of bounds! reupdate "+ xc);
            if(zc>6||zc<2)Bukkit.broadcastMessage("zc out of bounds! reupdate "+ zc);
            //z pos get
            if(parsedCoord<<28>>>28==15){
                ls[0]= localCoord[(parsedCoord<<16>>>24<<8)|(parsedCoord<<24>>>28<<4)];
                lrs[0]=xc; lrs[1]=(byte)(zc+1);
            }else{
                ls[0]= localCoord[parsedCoord+1];
                lrs[0]=xc; lrs[1]=zc;
            }
            //z neg
            if(parsedCoord<<28>>>28==0){
                ls[1]= localCoord[(parsedCoord<<16>>>24<<8)|(parsedCoord<<24>>>28<<4)|0x0f];
                lrs[2]=xc; lrs[3]=(byte)(zc-1);
            }else{
                ls[1]= localCoord[parsedCoord-1];
                lrs[2]=xc; lrs[3]=zc;
            }
            //x pos
            if(parsedCoord<<24>>>28==15){
                ls[2]= localCoord[(parsedCoord<<16>>>24<<8)|(parsedCoord<<28>>>28)];
                lrs[4]=(byte)(xc+1); lrs[5]=zc;
            }else{
                ls[2]= localCoord[parsedCoord+16];
                lrs[4]=xc; lrs[5]=zc;
            }
            //x neg
            if(parsedCoord<<24>>>28==0){
                ls[3]= localCoord[(parsedCoord<<16>>>24<<8)|0x0f0|(parsedCoord<<28>>>28)];
                lrs[6]=(byte)(xc-1); lrs[7]=zc;
            }else{
                ls[3]= localCoord[parsedCoord-16];
                lrs[6]=xc; lrs[7]=zc;
            }
            //up
            ls[4]= localCoord[parsedCoord+256];
            lrs[8]=xc; lrs[9]=zc;

            //down
            ls[5]= localCoord[parsedCoord-256];
            lrs[10]=xc; lrs[11]=zc;

            for (loop=-1; ++loop<6;) {
                relChunk =9*lrs[loop<<1]+lrs[(loop<<1)+1];
                if(chunkVals[relChunk]==null){
                    chunkVals[relChunk]=chunkValues.get(Coords.CHUNK(((cc.parsedCoord>>16)+lrs[loop<<1])-4,((cc.parsedCoord<<16>>16)+lrs[(loop<<1)+1])-4)).blockVals;
                    chunkMark.add(rcr[relChunk]);
                }
                t = chunkVals[relChunk].computeIfAbsent(ls[loop], n -> new byte[]{126,126,0,0,0,0,0,0,0,0,0,0,0});
                ms[loop] =!(checked[(relChunk<<16)|(ls[loop].parsedCoord & 0xffff)]||t[3]==0);
                tenseVals[loop]=t[0];
            }
            valref = DataUtil.lowestTensile(tenseVals);
            relChunk = 9*r[0]+r[1];
            chunkVals[relChunk].get(l)[0]=valref;
            if (valref >= range) {
                fallQuery[relChunk].add(l);
            }
            if (valref < range) {
                fallQuery[relChunk].remove(l);
            }
            for (loop = -1; ++loop<6;) {
                if (ms[loop] && valref < tenseVals[loop]) {
                    relChunk =9*lrs[loop<<1]+lrs[(loop<<1)+1];
                    coordQ.add(ls[loop]);
                    chunkRef.add(rcr[relChunk]);
                    checked[(relChunk<<16)|(ls[loop].parsedCoord & 0xffff)]=true;
                }
            }
        }
    }
}
