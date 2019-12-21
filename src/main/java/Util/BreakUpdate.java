package Util;

import PositionalKeys.ChunkCoord;
import PositionalKeys.HyperKeys;
import PositionalKeys.LocalCoord;
import Storage.FastUpdateHandler;
import Storage.ValueStorage;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;

public class BreakUpdate {

    private ValueStorage vs;

    public BreakUpdate(ValueStorage valueStore, FastUpdateHandler updateHandler){
        vs= valueStore;
        checked = updateHandler.checkedCoords;
        chunkVals = updateHandler.chunkValueHolder;
        chunkMark = updateHandler.chunkValueMarker;
        fallQuery = updateHandler.blockFallQuery;
        updtPtrs = updateHandler.blockUpdate;
        rcr = updateHandler.relativeChunkReference;
    }

    private final boolean[] checked;
    private final HashMap<LocalCoord,byte[]>[] chunkVals;
    private final Queue<byte[]> chunkMark;
    private final HashSet<LocalCoord>[] fallQuery;
    private final ArrayDeque<LocalCoord>[] updtPtrs;
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

    public void breakChecks(ChunkCoord cc, LocalCoord lc) {

        byte loop;
        byte valref;
        int parsedCoord;
        byte[] r;
        byte[] t;
        byte xc;
        byte zc;
        byte badsort=127;
        LocalCoord l;
        int relChunk = 9*4+4;

        coordQ.add(lc);
        chunkRef.add(rcr[relChunk]);
        checked[(relChunk<<16)+(lc.parsedCoord & 0xffff)]=true;
        //move outside
        chunkVals[relChunk] = vs.chunkValues.get(cc).blockVals;
        chunkVals[relChunk].get(lc)[2]=1;

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
                    chunkVals[relChunk]=vs.chunkValues.get(new ChunkCoord(cc.parsedCoord>>>16+lrs[loop<<1]-4,cc.parsedCoord<<16>>>16+lrs[(loop<<1)+1]-4)).blockVals;
                    chunkMark.add(rcr[relChunk]);
                }

                t = chunkVals[relChunk].computeIfAbsent(ls[loop], n -> new byte[]{126, 0, 0, 0, 0, 0});

                ms[loop] =!(checked[(relChunk<<16)|(ls[loop].parsedCoord & 0xffff)]||t[2]==0);
                tenseVals[loop]=t[0];
            }

            relChunk = 9*r[0]+r[1];
            valref = chunkVals[relChunk].get(l)[0];
            chunkVals[relChunk].get(l)[0]=126;
            fallQuery[relChunk].add(l);

            if (ms[4]) {
                relChunk=9*lrs[8]+lrs[9];
                checked[(relChunk<<16)|(ls[4].parsedCoord & 0xffff)]=true;
                coordQ.add(ls[4]);
                chunkRef.add(rcr[relChunk]);
            }
            if (ms[5] && valref > 0) {
                relChunk=9*lrs[10]+lrs[11];
                checked[(relChunk<<16)|(ls[5].parsedCoord & 0xffff)]=true;
                coordQ.add(ls[5]);
                chunkRef.add(rcr[relChunk]);
            }

            for (loop = -1; ++loop<4;) {
                relChunk =9*lrs[loop<<1]+lrs[(loop<<1)+1];
                if (ms[loop] && valref < tenseVals[loop]) {
                    checked[(relChunk<<16)|(ls[loop].parsedCoord & 0xffff)]=true;
                    coordQ.add(ls[loop]);
                    chunkRef.add(rcr[relChunk]);
                    continue;
                }
                if (ms[loop] && valref >= tenseVals[loop]) {
                    if(badsort>valref){
                        updtPtrs[relChunk].addFirst(ls[loop]);
                        badsort=valref;
                    }else {
                        updtPtrs[relChunk].addLast(ls[loop]);
                    }
                }
            }
        }
        for(byte[] rl:chunkMark){
            relChunk=9*rl[0]+rl[1];
            for(LocalCoord ll: fallQuery[relChunk]){
                checked[(relChunk<<16)|(ll.parsedCoord & 0xffff)]=false;
            }
        }
    }
}
