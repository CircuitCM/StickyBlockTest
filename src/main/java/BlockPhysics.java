import org.bukkit.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;


//todo: less code somehow (done), split into classes for easier implementation of new/custom features (donish), async calculations? (yeeaaah...)
//todo: pt2,

/* notes, runs on spigot 1.8.8 but will probably work in 1.7 to 1.13? untested.
The logic and checks are run ontop of the minecraft server runtime,
it does not store any block data besides its location,
and calls the locations type and data before making the block there fall.
Additionally it all happens within a single mc tick, maybe split it up into 1 or 2 ticks for a smoother server?
(probably unnecessary)
 */
public class BlockPhysics extends JavaPlugin implements Listener {

    private final CoreConstructor cC;

    public BlockPhysics() {
        cC = new CoreConstructor(this);
    }

    @Override
    public void onEnable() {

        this.saveDefaultConfig();

//        cC.ltv.runTaskAsynchronously(this);

        getServer().getPluginManager().registerEvents(cC.bb, this);
        getServer().getPluginManager().registerEvents(cC.bp, this);
        getServer().getPluginManager().registerEvents(cC.bf, this);
        getServer().getPluginManager().registerEvents(cC.bp, this);
        getServer().getPluginManager().registerEvents(cC.pi, this);

//        cC.stv.runTaskTimerAsynchronously(this,300,1200);

        getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "\nBlockPhysicsTest Initialized");
        stuffSchedule10min();
        aSyncLoad();

    }

    @Override
    public void onDisable(){

//        cC.saveForDisableLol(this.getConfig());

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

        blockPlacePhysics(l);
    }


    @EventHandler
    public void blockFallUpdate(EntityChangeBlockEvent e) {
        Location l = e.getBlock().getLocation();

        if ((e.getEntityType() == EntityType.FALLING_BLOCK)) {
            Block b = e.getBlock();
            Material m = b.getType();
            if (m.equals(Material.COBBLESTONE) || m.equals(Material.ENDER_STONE)) {
                b.getDrops().clear();
            }
            bUC(l);
        }
    }

    @EventHandler
    public void blockBreakUpdate(BlockBreakEvent e) {
        Location l = e.getBlock().getLocation();

        Block b = e.getBlock();
        Collection<ItemStack> d = b.getDrops();
        b.setType(Material.AIR);
        for(ItemStack ds : d) Bukkit.getServer().getWorld(world).dropItemNaturally(l,ds);
        blockBreakPhysics(b.getLocation());

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
//        queryFall.remove(l);
        Collections.sort(queryFall, yc);
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
            for(Location o :al[i]){
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

        if(sV.get(l)==null) return;
        int bi = sV.get(l);
        sV.remove(l);
        queryFall.add(l);

        if(sV.containsKey(yneg) && bi>0) {
            Bukkit.broadcastMessage("Negative breakcheck activated");
            breakChecks(yneg);
        }
        if(sV.containsKey(ypos)){
            breakChecks(ypos);
        }
        if(sV.containsKey(xpos) && bi<xpi) {
            Bukkit.broadcastMessage("xpos more");
            breakChecks(xpos);
        }
        if(sV.containsKey(xpos) && bi>=xpi){
            Bukkit.broadcastMessage("xpos less");
            al[sV.get(xpos)].add(xpos);
        }
        if(sV.containsKey(xneg) && bi<xni) {
            Bukkit.broadcastMessage("xneg more");
            breakChecks(xneg);
        }
        if(sV.containsKey(xneg) && bi>=xni){
            Bukkit.broadcastMessage("xneg less");
            al[sV.get(xneg)].add(xneg);
        }
        if(sV.containsKey(zpos) && bi<zpi) {
            Bukkit.broadcastMessage("zpos more");
            breakChecks(zpos);
        }
        if(sV.containsKey(zpos) && bi>=zpi){
            Bukkit.broadcastMessage("zpos less");
            al[sV.get(zpos)].add(zpos);
        }
        if(sV.containsKey(zneg) && bi<zni) {
            Bukkit.broadcastMessage("zneg more");
            breakChecks(zneg);
        }
        if(sV.containsKey(zneg) && bi>=zni){
            Bukkit.broadcastMessage("zneg less");
            al[sV.get(zneg)].add(zneg);
        }
    }

    @SuppressWarnings("deprecation")
    public void bUC(Location l) {

        Location ypos = l.clone().add(0, 1, 0);
        Location yneg = l.clone().add(0, -1, 0);
        Material ar = Material.AIR;

        Material ympos = getServer().getWorld(world).getBlockAt(ypos).getType();
        Material ymneg = getServer().getWorld(world).getBlockAt(yneg).getType();

        if(!sV.containsKey(yneg)&&!ymneg.equals(ar)&&!ymneg.equals(Material.WATER)&&!ymneg.equals(Material.LAVA)){
            for (int yPoint = -4; yPoint <= 0; yPoint++) {
                if (getServer().getWorld(world).getBlockAt(l.clone().add(0, yPoint, 0)).getType() == Material.BEDROCK) {
                    sV.put(l, 0);
                    if(ympos!=ar) {
                        bUC(ypos);
                    }
                    //Bukkit.broadcastMessage(ChatColor.GREEN+"Bedrock within 4 blocks below\n" +
                        //"setting base value");
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


