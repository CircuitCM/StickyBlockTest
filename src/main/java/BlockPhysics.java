import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

//todo: less code somehow, split into classes for easier implementation of new/custom features, async calculations?

/* notes, runs on spigot 1.8.8 but will probably work in 1.7 to 1.13? untested.
The logic and checks are run ontop of the minecraft server runtime,
it does not store any block data besides its location,
and calls the locations type and data before making the block there fall.
Additionally it all happens within a single mc tick, maybe split it up into 1 or 2 ticks for a smoother server?
(probably unnecessary)
 */
public class BlockPhysics extends JavaPlugin implements Listener {

    private Map<Location, Integer> sV; //block or rather location values
    //private Material[] banned_blocks; for later

    private ArrayList<Location>[] al; //blocks that should be reupdated ordered by priority, after a block break event
    private List<Location> queryFall; //blocks added to the query during the the reupdate of values, called to fall after reupdate completion
    private YLocComparator yc = new YLocComparator();
    private final int range_value = 10; //current range, increasing doesn't break the world, decreasing might

    String world = "world";
    World thisworld = Bukkit.getWorld(world);
    private int spawn_min = -15;
    private int spawn_max = 15;


    //used to sort which blocks should be made to fall first, needed to avoid entities breaking other blocks queried to fall
    class YLocComparator implements Comparator<Location> {
        @Override
        public int compare(Location loc1, Location loc2) {
            return Double.compare(loc1.getY(), loc2.getY());
        }
    }


    /* used to call bukkit where it registers code that modifies/manipulates the server*/
    @Override
    public void onEnable() {
        //these need to be called here as instances to work properly
        this.al = new ArrayList[range_value];
        this.queryFall = new ArrayList<>();
        this.sV = new HashMap<>();
        saveDefaultConfig();


        setAl();
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "\nBlockPhysicsTest Initialized");
        stuffSchedule10min();
        aSyncLoad();

    }

    //to set arraylists before use
    public void setAl() {
        for (int i = 0; i < range_value; i++) {
            al[i] = new ArrayList<>();
        }
    }

    /*

    Saving and loading location key, int value to config

    */

    @Override
    public void onDisable() {
        saveBlocks();
    }

    public void stuffSchedule10min() {

        //final Configuration g = this.getConfig();
        new BukkitRunnable() {

            @Override
            public void run() {
                saveBlocks();
            }
        }.runTaskTimerAsynchronously(this, 400, 12000);
    }

    public void aSyncLoad() {

        new BukkitRunnable() {

            @Override
            public void run() {
                loadSV();
            }
        }.runTaskAsynchronously(this);
    }

    public void loadSV(){
        ConfigurationSection f = getConfig().getConfigurationSection("blocks");
        if(f==null) {
            Bukkit.broadcastMessage(ChatColor.GRAY+"\n[No block values to load]");
            return;
        }
        for(String s: f.getKeys(false)){
            String[] st = s.split("I");
            int i = f.getInt(s);
            sV.put(new Location(getServer().getWorld("world"),Double.parseDouble(st[0]),Double.parseDouble(st[1]),Double.parseDouble(st[2])), i);
            Bukkit.broadcastMessage("Loading"+s);
        }

    }

    public void saveBlocks(){

        ConfigurationSection f = getConfig().getConfigurationSection("blocks");
        if(f!=null) {
            f.getKeys(false).clear();
        }
        if(!sV.isEmpty()) {
            for (Location l : sV.keySet()) {
                int i = sV.get(l);
                getConfig().set("blocks."+l.getBlockX()+"I"+l.getBlockY()+"I"+l.getBlockZ(), i);
                Bukkit.broadcastMessage("Saving"+l.getX()+l.getY()+l.getZ());
            }
        }else{
            Bukkit.broadcastMessage(ChatColor.GRAY+"[No block values to save]");
        }
        this.saveConfig();
        Bukkit.broadcastMessage(ChatColor.GRAY+"[Saved new block values]");

    }

    /*

    Event interface used to get events from the bukkit server

    */

    @EventHandler
    public void blockPlaceUpdate(BlockPlaceEvent e) {

        Block b = e.getBlock();
        Location l = b.getLocation();
        Bukkit.broadcastMessage("heh " + l);
        if(sV.containsKey(l)){
            Bukkit.broadcastMessage("yes");
        }

        blockPlacePhysics(l);

    }



    @EventHandler
    public void blockFallUpdate(EntityChangeBlockEvent e) {
        Location l = e.getBlock().getLocation();

        if(!e.isCancelled()) {
            if ((e.getEntityType() == EntityType.FALLING_BLOCK)) {
                Block b = e.getBlock();
                Material m = b.getType();
                if (m.equals(Material.COBBLESTONE) || m.equals(Material.ENDER_STONE)) {
                    b.getDrops().clear();
                }
                bUC(l);
            }
        }
    }

    @EventHandler
    public void blockBreakUpdate(BlockBreakEvent e) {
        Block b = e.getBlock();
//        sV.remove(b.getLocation());
        blockBreakPhysics(b.getLocation());


    }

    @EventHandler
    public void explodeCancel(EntityExplodeEvent e){
        e.setCancelled(true);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        Action a = e.getAction();
        if(e.getPlayer().getItemInHand().getType()==Material.STICK) {
            if (a.equals(Action.RIGHT_CLICK_BLOCK)) {
                Location l = e.getClickedBlock().getLocation();
                e.getPlayer().sendMessage(ChatColor.GREEN + "Sticky value: " + sV.get(l));
            }
        }
    }

    /*

    Maths

    */

    public int getMin(int a, int b, int c, int d, int f, int g) {
        int min = Integer.MAX_VALUE;
        int[] ints = {a, b, c, d, f, g};

        for (int e : ints) {
            if (e < min) min = e;
        }
        return min;
    }

    @Deprecated
    public int getMax(int a, int b, int c, int d, int f, int g) {
        int max = Integer.MIN_VALUE-1;
        int[] ints = {a, b, c, d, f, g};

        for (int e : ints) {
            if (e > max) max = e;
        }
        return max;
    }

    public int getsV(Location l) {
        if (sV.containsKey(l)) {
            return sV.get(l);
        } else {
            return Integer.MAX_VALUE-1;
        }
    }

    public void blockBreakPhysics(Location l){
        breakChecks(l);
        structureUpdate();
        Collections.sort(queryFall, yc);
        queryFall.remove(l);
        collapse();
    }

    public void blockPlacePhysics(Location l){
        bUC(l);
        Collections.sort(queryFall, yc);
        collapse();
    }

    /*

    The meat, pretty simple tbh maybe someone can make it simpler without breaking it?

    */

    @SuppressWarnings("deprecation")
    public void collapse() {
        //sV.remove(l);

        for(Location l : queryFall){
            sV.remove(l);
            Block b = l.getBlock();
            Material m = b.getType();
            byte d = b.getData();
            getServer().getWorld(world).getBlockAt(l).setType(Material.AIR);
            getServer().getWorld(world).spawnFallingBlock(l.subtract(0,1,0), m, d);
        }
        queryFall.clear();
    }

    public void structureUpdate(){
        for (int i = 0; i <range_value; i++) {
            Bukkit.broadcastMessage("Updating... " +i );
            for(Location o :al[i]){
                Bukkit.broadcastMessage("hstuff" + o);
                bUC(o);
            }
            al[i].clear();
        }
    }


    public void breakChecks(Location l) {

        Location ypos = l.clone().add(0, 1, 0);
        Location yneg = l.clone().add(0, -1, 0);
        Location xpos = l.clone().add(1, 0, 0);
        Location xneg = l.clone().add(-1, 0, 0);
        Location zpos = l.clone().add(0, 0, 1);
        Location zneg = l.clone().add(0, 0, -1);

        int xpi = getsV(xpos);
        int xni = getsV(xneg);
        int zpi = getsV(zpos);
        int zni = getsV(zneg);

        Bukkit.broadcastMessage("i " +sV.get(l));
        if(sV.get(l)==null) return;
        int bi = getsV(l);
        sV.remove(l);
        queryFall.add(l);

        if(sV.containsKey(yneg) && bi>0) {
            breakChecks(yneg);
        }
        if(sV.containsKey(ypos)){
            breakChecks(ypos);
        }
        if(sV.containsKey(xpos) && bi<xpi) {
            breakChecks(xpos);
        }
        if(sV.containsKey(xpos) && bi>=xpi){
            al[sV.get(xpos)].add(xpos);
        }
        if(sV.containsKey(xneg) && bi<xni) {
            breakChecks(xneg);
        }
        if(sV.containsKey(xneg) && bi>=xni){
            al[sV.get(xneg)].add(xneg);
        }
        if(sV.containsKey(zpos) && bi<zpi) {
            breakChecks(zpos);
        }
        if(sV.containsKey(zpos) && bi>=zpi){
            al[sV.get(zpos)].add(zpos);
        }
        if(sV.containsKey(zneg) && bi<zni) {
            breakChecks(zneg);
        }
        if(sV.containsKey(zneg) && bi>=zni){
            al[sV.get(zneg)].add(zneg);
        }
    }

    @SuppressWarnings("deprecation")
    public void bUC(Location l) {
        Material ar = Material.AIR;

        if(getServer().getWorld(world).getBlockAt(l).getType()==ar){
            Bukkit.broadcastMessage("this is air I shouldn't be buccing this");
//            return;
        }

        Location ypos = l.clone().add(0, 1, 0);
        Location yneg = l.clone().add(0, -1, 0);


        Material ympos = getServer().getWorld(world).getBlockAt(ypos).getType();
        Material ymneg = getServer().getWorld(world).getBlockAt(yneg).getType();


        if(!sV.containsKey(yneg)&&!ymneg.equals(ar)&&!ymneg.equals(Material.WATER)&&!ymneg.equals(Material.LAVA)){
            for (int yPoint = -4; yPoint <= 0; yPoint++) {
                if (getServer().getWorld(world).getBlockAt(l.clone().add(0, yPoint, 0)).getType() == Material.BEDROCK) {
                    sV.put(l, 0);
                    if(ympos!=ar) {
                        bUC(ypos);
                    }
                    Bukkit.broadcastMessage(ChatColor.GREEN+"Bedrock within 4 blocks below\n" +
                        "setting base value");
                    return;
                }
            }

        }

        Location xpos = l.clone().add(1,0,0);
        Location xneg = l.clone().add(-1,0,0);
        Location zpos = l.clone().add(0,0,1);
        Location zneg = l.clone().add(0,0,-1);

        int ypi = getsV(ypos);
        int yni = getsV(yneg);
        int xpi = getsV(xpos);
        int xni = getsV(xneg);
        int zpi = getsV(zpos);
        int zni = getsV(zneg);
        int smallestRel = getMin(xpi+1,xni+1,zpi+1,zni+1,ypi,yni);

        sV.put(l,smallestRel);
        int bi = sV.get(l);

        Material xmpos = getServer().getWorld(world).getBlockAt(xpos).getType();
        Material xmneg = getServer().getWorld(world).getBlockAt(xneg).getType();
        Material zmpos = getServer().getWorld(world).getBlockAt(zpos).getType();
        Material zmneg = getServer().getWorld(world).getBlockAt(zneg).getType();

        if (xmpos!=ar && bi < xpi) {
            bUC(xpos);
        }
        if (xmneg!=ar && bi < xni) {
            bUC(xneg);
        }
        if (zmpos!=ar && bi < zpi) {
            bUC(zpos);
        }
        if (zmneg!=ar && bi < zni) {
            bUC(zneg);
        }
        if(ympos!=ar && bi<ypi){
            bUC(ypos);
        }
        if(ymneg!=ar && bi<yni){
            bUC(yneg);
        }
        if(bi<=range_value){
            queryFall.remove(l);
        }
        if (bi>=range_value && !queryFall.contains(l)) {
            queryFall.add(l);
        }

    }
}


