package Storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

enum YamlType {
    CHUNK_DATA("ChunkData.yml"), FACTIONS_DATA("FactionsData.yml"), CORE_SETTINGS("CoreSettings.yml");

    public String filePreset;

    YamlType(String fileName){
        filePreset=fileName;
    }

   public String CHUNK_DATA(){
        return filePreset;
   }

}


public class YamlConstructor {

    private final JavaPlugin plugin;

    private static Path chunkDataPath;

    //The bukkit yaml configuration that gets put in the config file

    /*todo: on chunk unload, new chunkdata file save chunk to chunkdata folder overwrite old file,
     todo: on chunk load, if chunk data file exists get chunkdata file, deserealize and put into hashmap
     */



    public YamlConstructor(JavaPlugin plugin) {

        this.plugin = plugin;
        chunkDataPath = plugin.getDataFolder().toPath().resolve("ChunkData");

        if (!Files.exists(chunkDataPath)) {
            try {
                Files.createDirectory(chunkDataPath);
            } catch (IOException e) {
                //fail to create directory
                e.printStackTrace();
            }
        }
    }

    public void saveYamlFile( FileConfiguration fc, ChunkLocation cl){
        try {
            fc.save(getYamlFile(cl));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean yamlFileExists(ChunkLocation cl){
        return Files.exists(chunkDataPath.resolve(cl.getX()+","+cl.getZ()+".yml"));
    }

    public FileConfiguration getYamlConfig(ChunkLocation cl){

        if(!yamlFileExists(cl)) return YamlConfiguration.loadConfiguration(plugin.getResource(YamlType.CHUNK_DATA.CHUNK_DATA()));

        return YamlConfiguration.loadConfiguration(getYamlFile(cl));

    }

    public File getYamlFile(ChunkLocation cl){
        File f = new File(chunkDataPath.resolve(cl.getX()+","+cl.getZ()+".yml").toString());
        if(!f.exists()){
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return f;

    }

    @Deprecated
    public FileConfiguration getNewYamlConfig(YamlType type){

        switch (type){
            case CHUNK_DATA:
                return YamlConfiguration.loadConfiguration(plugin.getResource(type.filePreset));

            case FACTIONS_DATA:
                return null;

            default:
                return null;
        }
    }



}