package ru.aiefu.passcard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class IOManager {
    public static void writePlayerData(ServerPlayerEntity player){
        PlayerDB db = ((IPlayerPass)player).getPlayerDB();
        String gson = new GsonBuilder().setPrettyPrinting().create().toJson(db);
        File file = new File("./config/Passcard/player-data/"+ Passcard.getUUIDIgnoreCase(player)+ ".json");
        fileWriter(file, gson);
    }

    public static void writePlayerReserveData(ServerPlayerEntity player){
        PlayerDB db = ((IPlayerPass)player).getPlayerDB();
        String gson = new GsonBuilder().setPrettyPrinting().create().toJson(db);
        File file = new File("./config/Passcard/player-data/"+ Passcard.getUUIDIgnoreCase(player)+ ".old.json");
        fileWriter(file, gson);
    }
    public static void writePlayerReserveDataIfNotExist(ServerPlayerEntity player){
        String path = "./config/Passcard/player-data/" + Passcard.getUUIDIgnoreCase(player) + ".old.json";
        if(!Files.exists(Paths.get(path))){
            PlayerDB db = ((IPlayerPass) player).getPlayerDB();
            String gson = new GsonBuilder().setPrettyPrinting().create().toJson(db);
            File file = new File(path);
            fileWriter(file, gson);
        }
    }

    public static PlayerDB readPlayerData(ServerPlayerEntity player){
        PlayerDB playerDB;
        try {
            playerDB = new Gson().fromJson(new FileReader("./config/Passcard/player-data/"+ Passcard.getUUIDIgnoreCase(player)+ ".json"), PlayerDB.class);
        } catch (Exception e){
            System.out.println("Cannot find player profile, generating new one");
            playerDB = new PlayerDB(player.getName().getString(), null, player.getIp(), 0);
        }
        return playerDB;
    }
    @Nullable
    public static PlayerDB readPlayerReserveData(ServerPlayerEntity player){
        PlayerDB playerDB = null;
        try {
            playerDB = new Gson().fromJson(new FileReader("./config/Passcard/player-data/"+ Passcard.getUUIDIgnoreCase(player)+ ".old.json"), PlayerDB.class);
        } catch (Exception e){
            System.out.println("Cannot read reserve profile");
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
