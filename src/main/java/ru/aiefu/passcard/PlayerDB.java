package ru.aiefu.passcard;

import org.jetbrains.annotations.Nullable;

public class PlayerDB {
    public String playerName;
    private String password;
    private String lastIp;

    public PlayerDB(String playerName, String password, String ip) {
        this.playerName = playerName;
        this.password = password;
        this.lastIp = ip;
    }
    @Nullable
    public String getPassword(){
        return this.password;
    }
    public void setPassword(String password){
        this.password = password;
    }
    public String getLastIp(){
        return this.lastIp;
    }
    public void setLastIp(String lastIp){
        this.lastIp = lastIp;
    }
}
