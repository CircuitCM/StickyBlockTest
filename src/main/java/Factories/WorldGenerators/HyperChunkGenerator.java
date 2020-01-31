package Factories.WorldGenerators;

import Cores.WorldDataCore;
import PositionalKeys.ChunkCoord;
import PositionalKeys.HyperKeys;
import PositionalKeys.LocalCoord;
import Settings.WorldRules;
import Storage.ChunkValues;
import Util.Coords;
import Util.DataUtil;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import org.jctools.maps.NonBlockingHashMap;

import java.util.HashMap;
import java.util.Random;

public class HyperChunkGenerator extends ChunkGenerator {

    private final NonBlockingHashMap<ChunkCoord, ChunkValues> chunkData;
    private SimplexOctaveGenerator generator;
    private Random rand;


    public HyperChunkGenerator(WorldDataCore wd,Random rand){
        chunkData=wd.vs.chunkValues;
        rand.nextLong();
        this.rand = rand;
        generator = new SimplexOctaveGenerator(rand, 7);
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData chunk = createChunkData(world);
        ChunkValues cv = new ChunkValues();
        HashMap<LocalCoord, byte[]> blockData =cv.blockVals;
        int currentHeight,X,Z,Xshift,XZshift,Ys,Ymin,Xset,Zset;
        Double r; Biome b;
        chunkZ<<=4;chunkX<<=4;
        for (X = -1; ++X < 16;) {
            Xset=chunkX|X;
            Xshift=X<<4;
            for (Z = -1; ++Z < 16;){
                b = biome.getBiome(X,Z);
                if(WorldRules.biomeLimiter.containsKey(b)){
                    biome.setBiome(X,Z,WorldRules.biomeLimiter.get(b));
                }
                Zset=chunkZ|Z;
                XZshift=Xshift|Z;
                Ys=((Xset*Xset)+(Zset*Zset));
                if(Ys>16820000)continue;
                r =1+(Ys/6291456D);

                generator.setScale(0.004D+(r*0.003D));
                currentHeight = (int)(generator.noise(Xset, Zset, 1.119D, 1.1D)*r*1.45D+r*r*r*r+28D);

                Ymin = currentHeight - 10;

                Ys = rand.nextInt(10000);
                if(Ys<3){
                    chunk.setBlock(X, currentHeight, Z, Material.RED_MUSHROOM);
                }else if(Ys<20){
                    chunk.setBlock(X, currentHeight, Z, Material.DOUBLE_PLANT.getId(),(byte)3);
                    chunk.setBlock(X, 1+currentHeight, Z, Material.DOUBLE_PLANT.getId(),(byte)11);
                }else if(Ys<1500){
                    chunk.setBlock(X, currentHeight, Z, Material.LONG_GRASS.getId(),(byte)1);
                }else if(Ys<2000){
                    chunk.setBlock(X, currentHeight, Z, Material.DOUBLE_PLANT.getId(),(byte)2);
                    chunk.setBlock(X, 1+currentHeight, Z, Material.DOUBLE_PLANT.getId(),(byte)10);
                }

                blockData.put(HyperKeys.localCoord[(currentHeight<<8)|XZshift],new byte[]{126,126,0,0,0,0,0,0,0,0,0,0,0});
                chunk.setBlock(X, --currentHeight, Z, Material.GRASS);
                blockData.put(HyperKeys.localCoord[(currentHeight<<8)|XZshift],new byte[]{0,0,1,1,2,0,0,0,0,0,0,0,0});
                while(--currentHeight>Ymin) {
                    chunk.setBlock(X, currentHeight, Z, Material.DIRT);
                    blockData.put(HyperKeys.localCoord[(currentHeight<<8)|XZshift],new byte[]{0,0,1,1,1,0,0,0,0,0,0,0,0});
                }
                ++currentHeight;
                Ymin-=5;
                while(--currentHeight>Ymin){
                    chunk.setBlock(X, currentHeight, Z, Material.BEDROCK);
                    blockData.put(HyperKeys.localCoord[(currentHeight<<8)|XZshift],new byte[]{0,0,127,0,0,0,0,0,0,0,0,0,0});
                }
            }
        }
        DataUtil.initYNoiseMarker(cv);
        chunkData.put(Coords.CHUNK(chunkX >> 4, chunkZ >> 4),cv);
        return chunk;
//        HyperKeys.CCOORDS_512x512[(((chunkX>>4)+256)<<9)|((chunkZ>>4)+256)]
    }

    /*@Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return Arrays.asList((BlockPopulator)new LakePopulator(rand));
    }*/
}
