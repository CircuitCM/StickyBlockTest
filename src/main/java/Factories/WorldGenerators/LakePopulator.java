package Factories.WorldGenerators;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

public class LakePopulator extends BlockPopulator {

    private Random random;

    public LakePopulator(Random rand){
        random=rand;
    }

    @Override
    public void populate(World world, Random rand, Chunk chunk) {
        if (random.nextInt(100)<8) {  // The chance of spawning a lake
            int chunkX = chunk.getX();
            int chunkZ = chunk.getZ();
            int X = (chunkX <<4) + random.nextInt(31)-16;
            int Z = (chunkZ <<4) + random.nextInt(31)-16;
            int Y;Material m;
            for (Y=255, m=Material.AIR;m==Material.AIR&&Y>5;) {
                m = chunk.getBlock(X, --Y, Z).getType();
            }
            if(Y<7)return;
            Y -= 3;
            double r = 1D+(((X*X)+(Z*Z))/3145728D);
            boolean[] aboolean = new boolean[16384];
            boolean[] sboolean = new boolean[16384];

            byte[] dbyte = new byte[16384];

            int i = random.nextInt(4)+3;

            int j, j1, k1,i2,n,track=0,track2=0;
            double d0, d1, d2, d3, d4, d5, d6, d7,d8,d9;

            for (j = 0; j < i; ++j) {
                d0 = random.nextDouble() *17.0D + 6D;
                d1 = random.nextDouble() *3D + 3.0D;
                d2 = random.nextDouble() *17.0D + 6D;
                d3 = random.nextDouble() * (22.0D - d0) + 4D + (d0 /= 2.0D);
                d4 = random.nextDouble() * (5.0D - d1) +1.5D-r+(d1 /= 2.0D);
                d5 = random.nextDouble() * (22.0D - d2) + 4D + (d2 /= 2.0D);

                for (int k = 0; k < 32; ++k) {
                    d6 = ((double) k - d3) / d0*r;
                    for (int l = 0; l < 32; ++l) {
                        d8 = ((double) l - d5) / d2*r;
                        for (int i1 = 0; i1 < 16; ++i1) {
                            d7 = ((double) i1 - d4) / d1;
                            d9 = d6 * d6 + d7 * d7 + d8 * d8;
                            i2 = ((k * 32 + l) * 16) + i1;
                            if (d9 < 1.0D) {
                                aboolean[i2] = true;
                            }else if(d9<1.75D){
                                sboolean[i2]=true;
                            }else if(d9<2.25D){
                                dbyte[i2]=1;
                               m = world.getBlockAt(X +k,  Y+l, Z + i1).getType();
                               if(m!=Material.AIR){
                                   ++track2;
                               }
                               ++track;
                            }else if(d9<3D){
                                dbyte[i2]=2;
                            }
                        }
                    }
                }
            }
            Block b;
            if(r>3.81D) {
                for (j = 0; j < 32; ++j) {
                    for (k1 = 0; k1 < 32; ++k1) {
                        for (j1 = 0; j1 < 16; ++j1) {
                            n = Y + j1;
                            if (aboolean[((j * 32 + k1) * 16) + j1]) {
                                b = world.getBlockAt(X + j, n, Z + k1);
                                if (b.getType() != Material.BEDROCK) {
                                    b = world.getBlockAt(X + j, --n, Z + k1);
                                    while (b.getType() == Material.AIR&&n>15) {
                                        b = world.getBlockAt(X + j, --n, Z + k1);
                                    }
                                    b.setType(Material.AIR);
                                    world.getBlockAt(X + j, --n, Z + k1).setType(Material.WATER);
                                }
                            }
                        }
                    }
                }
            }else{

                if(track2>track*0.85){
                    track=3;
                }else if(track2>track*0.7){
                    track=2;
                }else if(track2>track*0.6){
                    track=1;
                }else if(track2<track*0.4){
                    track = -1;
                }
                else if(track2<track*0.15){
                    track = -2;
                }else{
                    track=0;
                }
                for (j = 0; j <32; ++j) {
                    for (k1 = 0; k1 < 32; ++k1) {
                        for (j1 = 0; j1 < 16; ++j1) {
                            n = Y + j1 + track;
                            if (aboolean[((j * 32 + k1) * 16) + j1]) {
                                b = world.getBlockAt(X + j, n, Z + k1);
                                if (b.getType() != Material.BEDROCK) {
                                    b.setType(j1 > 2 ? Material.AIR : Material.WATER);
                                }

                            } else if (sboolean[((j * 32 + k1) * 16) + j1]) {
                                b = world.getBlockAt(X + j, n, Z + k1);
                                if (b.getType() != Material.BEDROCK) {
                                    if (j1 > 2) {
                                        b.setType(Material.AIR);

                                    } else {
                                        b.setType(Material.SAND);
                                        b = world.getBlockAt(X + j, --n, Z + k1);
                                        while (b.getType() == Material.AIR && n > 15) {
                                            b.setType(Material.SAND);
                                            b = world.getBlockAt(X + j, --n, Z + k1);
                                        }
                                    }
                                }
                            } else if (dbyte[((j * 32 + k1) * 16) + j1] == 1) {
                                b = world.getBlockAt(X + j, n, Z + k1);
                                if (b.getType() != Material.BEDROCK && b.getType() != Material.DIRT && b.getType() != Material.GRASS) {
                                    if (j1 > 3) {
                                        b.setType(Material.AIR);

                                    } else {
                                        b.setType(Material.GRASS);
                                        b = world.getBlockAt(X + j, --n, Z + k1);
                                        while (b.getType() == Material.AIR && n > 15) {
                                            b.setType(Material.GRASS);
                                            b = world.getBlockAt(X + j, --n, Z + k1);
                                        }
                                    }

                                }
                            } else if (dbyte[((j * 32 + k1) * 16) + j1] == 2) {
                                b = world.getBlockAt(X + j, n, Z + k1);
                                if (b.getType() != Material.BEDROCK && b.getType() != Material.DIRT && b.getType() != Material.GRASS) {
                                    b.setType(j1 > 2 ? Material.AIR : Material.GRASS);
                                }
                            }
                        }
                    }
                }
            }
           /* for (j = 0; j < 16; ++j) {
                for (k1 = 0; k1 < 16; ++k1) {
                    for (j1 = 4; j1 < 8; ++j1) {
                        if (aboolean[(j * 16 + k1) * 8 + j1]) {
                            int X1 = X+j;
                            int Y1 = Y+j1-1;
                            int Z1 = Z+k1;
                            if (world.getBlockAt(X1, Y1, Z1).getType() == Material.DIRT) {
                                world.getBlockAt(X1, Y1, Z1).setType(Material.GRASS);
                            }
                        }
                    }
                }
            }*/
        }
    }
}
