package Methods;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Collection;
import java.util.LinkedList;

public class BedrockCheck {

    public Collection<Location> checkBedrock(Chunk c){

        Collection<Location> br = new LinkedList<>();

        for(int x=0; x<=15; x++){
            for(int z=0; z<=15; z++){
                for(int y=0; y<=255; y++){
                    Block b = c.getBlock(x,y,z);
                    if(b.getType()== Material.BEDROCK){
                        extraCheck(b, br);
                        break;
                    }
                }
            }
        }
        return br;
    }


    private void extraCheck(Block b, Collection<Location> br){
        br.add(b.getLocation());
        for(int y=1;y<=6;y++){
            Block b1 = b.getRelative(0,y,0);
            if(b1.getType()==Material.BEDROCK){
                br.add(b1.getLocation());
            }
        }
    }

}
