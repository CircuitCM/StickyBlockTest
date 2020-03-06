package Util;

import PositionalKeys.ChunkCoord;
import PositionalKeys.LocalCoord;
import Storage.ChunkValues;
import Storage.FastUpdateHandler;
import org.bukkit.block.Block;
import org.jctools.maps.NonBlockingHashMap;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;

import static PositionalKeys.HyperKeys.localCoord;

public class BreakUpdate {

    private final NonBlockingHashMap<ChunkCoord, ChunkValues> chunkValues;

    public BreakUpdate(NonBlockingHashMap<ChunkCoord, ChunkValues> cv, FastUpdateHandler updateHandler){
        chunkValues=cv;
        checked = updateHandler.checkedCoords;
        chunkVals = updateHandler.chunkValueHolder;
        chunkMark = updateHandler.chunkValueMarker;
        fallQuery = updateHandler.blockFallQuery;
        updtPtrs = updateHandler.blockUpdate;
        rcr = updateHandler.relativeChunkReference;
    }

    private final boolean[] checked;
    private final HashMap<LocalCoord,byte[]>[] chunkVals;
    private final ArrayDeque<byte[]> chunkMark;
    private final HashSet<LocalCoord>[] fallQuery;
    private final ArrayDeque<LocalCoord>[] updtPtrs;
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

    public void breakChecks(LocalCoord lc, ChunkCoord cc, HashMap<LocalCoord,byte[]> localValues) {

        byte loop;
        byte valref;
        byte[] r;
        byte[] t;
        byte xc;
        byte zc;
        int parsedCoord, relChunk = 9*4+4;
        LocalCoord l;

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
        t = localValues.computeIfAbsent(lc, c -> new byte[]{126,126,0,0,0,0,0,0,0,0,0,0,0});
        t[2] = 0; t[3]=0;
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
                lrs[0]=xc;
                lrs[1]=(byte)(zc+1);
            }else{
                ls[0]= localCoord[parsedCoord+1];
                lrs[0]=xc;
                lrs[1]=zc;
            }
            //z neg
            if(parsedCoord<<28>>>28==0){
                ls[1]= localCoord[(parsedCoord<<16>>>24<<8)|(parsedCoord<<24>>>28<<4)|0x0f];
                lrs[2]=xc;
                lrs[3]=(byte)(zc-1);
            }else{
                ls[1]= localCoord[parsedCoord-1];
                lrs[2]=xc;
                lrs[3]=zc;
            }
            //x pos
            if(parsedCoord<<24>>>28==15){
                ls[2]= localCoord[(parsedCoord<<16>>>24<<8)|(parsedCoord<<28>>>28)];
                lrs[4]=(byte)(xc+1);
                lrs[5]=zc;
            }else{
                ls[2]= localCoord[parsedCoord+16];
                lrs[4]=xc;
                lrs[5]=zc;
            }
            //x neg
            if(parsedCoord<<24>>>28==0){
                ls[3]= localCoord[(parsedCoord<<16>>>24<<8)|0x0f0|(parsedCoord<<28>>>28)];
                lrs[6]=(byte)(xc-1);
                lrs[7]=zc;
            }else{
                ls[3]= localCoord[parsedCoord-16];
                lrs[6]=xc;
                lrs[7]=zc;
            }
            //up
            ls[4]= localCoord[parsedCoord+256];
            lrs[8]=xc;
            lrs[9]=zc;

            //down
            ls[5]= localCoord[parsedCoord-256];
            lrs[10]=xc;
            lrs[11]=zc;

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

            relChunk = 9*r[0]+r[1];
            valref = chunkVals[relChunk].get(l)[0];
            chunkVals[relChunk].get(l)[0]=126;

            for (loop = -1; ++loop<4;){
                relChunk =9*lrs[loop<<1]+lrs[(loop<<1)+1];
                if (ms[loop] && valref < tenseVals[loop]) {
                    coordQ.add(ls[loop]);
                    chunkRef.add(rcr[relChunk]);
                    checked[(relChunk<<16)|(ls[loop].parsedCoord & 0xffff)]=true;
                    fallQuery[relChunk].add(ls[loop]);
                    continue;
                }
                if (ms[loop] && valref >= tenseVals[loop]) {
                    updtPtrs[relChunk].add(ls[loop]);
                }
            }
            if (ms[4]) {
                relChunk=9*lrs[8]+lrs[9];
                coordQ.add(ls[4]);
                chunkRef.add(rcr[relChunk]);
                checked[(relChunk<<16)|(ls[4].parsedCoord & 0xffff)]=true;
                fallQuery[relChunk].add(ls[4]);
            }
            if (ms[5] && valref > 0) {
                relChunk=9*lrs[10]+lrs[11];
                coordQ.add(ls[5]);
                chunkRef.add(rcr[relChunk]);
                checked[(relChunk<<16)|(ls[5].parsedCoord & 0xffff)]=true;
                fallQuery[relChunk].add(ls[5]);
            }
        }
        for(byte[] rl:chunkMark){
            relChunk=9*rl[0]+rl[1];
            for(LocalCoord ll: fallQuery[relChunk]){
                checked[(relChunk<<16)|(ll.parsedCoord & 0xffff)]=false;
            }
        }
    }

    public void multiBreak(ChunkCoord cc, HashMap<LocalCoord,byte[]> localValues, Block[] blocks) {

        byte valref;
        byte[] t;
        byte[] r;
        byte xc;
        byte zc;
        int x,z, loop,relChunk=9*4+4,parsedCoord,size = blocks.length,rx=cc.parsedCoord>>16,rz=cc.parsedCoord<<16>>16;
        Block b; LocalCoord l;

        ArrayDeque<LocalCoord> coordQ = this.coordQ;
        ArrayDeque<byte[]> chunkRef = this.chunkRef;
        HashMap<LocalCoord,byte[]>[] chunkVals = this.chunkVals;
        LocalCoord[] ls = this.ls;
        byte[] tenseVals = this.tenseVals;
        byte[] lrs =this.lrs;
        boolean[] ms = this.ms;
        boolean[] checked = this.checked;

        chunkVals[relChunk] = localValues;
        chunkMark.add(rcr[relChunk]);

        for(loop=-1;++loop<size;) {
            b=blocks[loop];
            if(b!=null) {
                x = b.getX();
                z = b.getZ();
                l = localCoord[(b.getY() << 8) | (x << 28 >>> 28 << 4) | (z << 28 >>> 28)];
                x>>=4;z>>=4;

                relChunk = (9 * (x - rx +4)) + (z - rz +4);
                if(chunkVals[relChunk]==null){
                    chunkVals[relChunk]=chunkValues.get(Coords.CHUNK(x,z)).blockVals;
                    chunkMark.add(rcr[relChunk]);
                }
                coordQ.add(l);
                chunkRef.add(rcr[relChunk]);
                t = chunkVals[relChunk].computeIfAbsent(l, n -> new byte[]{126,126,0,0,0,0,0,0,0,0,0,0,0});
                t[2] = 0; t[3]=0;
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
                ls[0]= localCoord[(parsedCoord<<16>>>24<<8)|(parsedCoord<<24>>>28<<4)];
                lrs[0]=xc;
                lrs[1]=(byte)(zc+1);
            }else{
                ls[0]= localCoord[parsedCoord+1];
                lrs[0]=xc;
                lrs[1]=zc;
            }
            //z neg
            if(parsedCoord<<28>>>28==0){
                ls[1]= localCoord[(parsedCoord<<16>>>24<<8)|(parsedCoord<<24>>>28<<4)|0x0f];
                lrs[2]=xc;
                lrs[3]=(byte)(zc-1);
            }else{
                ls[1]= localCoord[parsedCoord-1];
                lrs[2]=xc;
                lrs[3]=zc;
            }
            //x pos
            if(parsedCoord<<24>>>28==15){
                ls[2]= localCoord[(parsedCoord<<16>>>24<<8)|(parsedCoord<<28>>>28)];
                lrs[4]=(byte)(xc+1);
                lrs[5]=zc;
            }else{
                ls[2]= localCoord[parsedCoord+16];
                lrs[4]=xc;
                lrs[5]=zc;
            }
            //x neg
            if(parsedCoord<<24>>>28==0){
                ls[3]= localCoord[(parsedCoord<<16>>>24<<8)|0x0f0|(parsedCoord<<28>>>28)];
                lrs[6]=(byte)(xc-1);
                lrs[7]=zc;
            }else{
                ls[3]= localCoord[parsedCoord-16];
                lrs[6]=xc;
                lrs[7]=zc;
            }
            //up
            ls[4]= localCoord[parsedCoord+256];
            lrs[8]=xc;
            lrs[9]=zc;

            //down
            ls[5]= localCoord[parsedCoord-256];
            lrs[10]=xc;
            lrs[11]=zc;

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

            relChunk = 9*r[0]+r[1];
            valref = chunkVals[relChunk].get(l)[0];
            chunkVals[relChunk].get(l)[0]=126;

            for (loop = -1; ++loop<4;) {
                relChunk =9*lrs[loop<<1]+lrs[(loop<<1)+1];
                if (ms[loop] && valref < tenseVals[loop]) {
                    coordQ.add(ls[loop]);
                    chunkRef.add(rcr[relChunk]);
                    checked[(relChunk<<16)|(ls[loop].parsedCoord & 0xffff)]=true;
                    fallQuery[relChunk].add(ls[loop]);
                    continue;
                }
                if (ms[loop] && valref >= tenseVals[loop]) {
//                    Coords.BLOCK_AT(ls[loop],Coords.CHUNK(((cc.parsedCoord>>16)+lrs[loop<<1])-4,((cc.parsedCoord<<16>>16)+lrs[(loop<<1)+1])-4)).setType(Material.STONE);
                    updtPtrs[relChunk].add(ls[loop]);
                }
            }
            if (ms[4]) {
                relChunk=9*lrs[8]+lrs[9];
                coordQ.add(ls[4]);
                chunkRef.add(rcr[relChunk]);
                checked[(relChunk<<16)|(ls[4].parsedCoord & 0xffff)]=true;
                fallQuery[relChunk].add(ls[4]);
            }
            if (ms[5] && valref > 0) {
                relChunk=9*lrs[10]+lrs[11];
                coordQ.add(ls[5]);
                chunkRef.add(rcr[relChunk]);
                checked[(relChunk<<16)|(ls[5].parsedCoord & 0xffff)]=true;
                fallQuery[relChunk].add(ls[5]);
            }
        }
        for(byte[] rl:chunkMark){
            relChunk=9*rl[0]+rl[1];
            for(LocalCoord ll: fallQuery[relChunk]) checked[(relChunk<<16)|(ll.parsedCoord & 0xffff)]=false;
        }
    }
}
