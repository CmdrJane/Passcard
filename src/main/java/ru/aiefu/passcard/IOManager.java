package ru.aiefu.passcard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class IOManager {
    public static void writePlayerData(ServerPlayerEntity player){
        PlayerDB db = ((IPlayerPass)player).getPlayerDB();
        String gson = new GsonBuilder().setPrettyPrinting().create().toJson(db);
        File file = new File("./config/Passcard/player-data/"+ player.getUuidAsString()+ ".json");
        fileWriter(file, gson);
    }

    public static PlayerDB readPlayerData(ServerPlayerEntity player){
        PlayerDB playerDB;
        try {
            playerDB = new Gson().fromJson(new FileReader("./config/Passcard/player-data/"+ player.getUuidAsString()+ ".json"), PlayerDB.class);
        } catch (Exception e){
            e.printStackTrace();
            playerDB = new PlayerDB(player.getName().getString(), null, player.getIp());
        }
        return playerDB;
    }

    public static void genCfg(){
        String gson = new GsonBuilder().setPrettyPrinting().create().toJson(new ConfigInstance());
        File file = new File("./config/Passcard/config.json");
        fileWriter(file, gson);
    }

    public static void readCfg(){
        ConfigInstance configInstance;
        try {
            configInstance = new Gson().fromJson(new FileReader("./config/Passcard/config.json"), ConfigInstance.class);
        } catch (Exception e){
            e.printStackTrace();
            configInstance = new ConfigInstance();
        }
        Passcard.config_instance = configInstance;
    }

    public static void fileWriter(File file, String gson){
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try(FileWriter writer = new FileWriter(file)) {
            writer.write(gson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
