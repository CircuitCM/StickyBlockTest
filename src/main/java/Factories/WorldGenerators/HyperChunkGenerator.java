package Factories.WorldGenerators;

import Events.ChunkEvents;
import Events.PostChunkGenEvent;
import PositionalKeys.ChunkCoord;
import Settings.WorldRules;
import Storage.ChunkValues;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import org.jctools.maps.NonBlockingHashMap;

import java.util.Random;

public class HyperChunkGenerator extends ChunkGenerator {

    private final NonBlockingHashMap<ChunkCoord, ChunkValues> chunkData;
    private final ChunkEvents chunkEvents;
    private SimplexOctaveGenerator generator;
    private Random rand;


    public HyperChunkGenerator(ChunkEvents chunkEvents, Random rand){
        chunkData=chunkEvents.chunkData;
        this.chunkEvents = chunkEvents;
        rand.nextLong();
        this.rand = rand;
        generator = new SimplexOctaveGenerator(rand, 7);
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData chunk = createChunkData(world);
        byte[] yNoise = new byte[256];
        int currentHeight,X,Z,Xshift,Ys,Ymin,Xset,Zset;
        Double r; Biome b;
        chunkZ<<=4;chunkX<<=4;
        for (X = -1; ++X < 16;) {
            Xset=chunkX|X;
            Xshift=X<<4;
            for (Z = -1; ++Z < 16;){
                b = biome.getBiome(X,Z);
                biome.setBiome(X,Z,WorldRules.biomeLimiter.getOrDefault(b,b));
                Zset=chunkZ|Z;
                Ys=((Xset*Xset)+(Zset*Zset));
                if(Ys>16820000)continue;
                r =1+(Ys/6291456D);

                generator.setScale(0.004D+(r*0.003D));
                currentHeight = (int)(generator.noise(Xset, Zset, 1.119D, 1D)*r*1.45D+r*r*r*r+25D);
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
                chunk.setBlock(X, --currentHeight, Z, Material.GRASS);
                while(--currentHeight>Ymin) {
                    chunk.setBlock(X, currentHeight, Z, Material.DIRT);
                }
                ++currentHeight;
                Ymin-=5;
                while(--currentHeight>Ymin){
                    chunk.setBlock(X, currentHeight, Z, Material.BEDROCK);
                }
                yNoise[Xshift|Z]=(byte)Ymin;
            }
        }
        chunkEvents.submitPostGenEvent(new PostChunkGenEvent((chunkX >> 4 << 16) | (chunkZ >> 4), yNoise));
        return chunk;
//        HyperKeys.CCOORDS_512x512[(((chunkX>>4)+256)<<9)|((chunkZ>>4)+256)]
    }

    /*@Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return Arrays.asList((BlockPopulator)new LakePopulator(rand));
    }*/
}
