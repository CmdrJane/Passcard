package ru.aiefu.passcard;

import org.jetbrains.annotations.Nullable;

public class PlayerDB {
    public String playerName;
    private String password;
    private String lastIp;
    private long session;

    public PlayerDB(String playerName, String password, String ip, long session) {
        this.playerName = playerName;
        this.password = password;
        this.lastIp = ip;
        this.session = session;
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
    public long getSession(){
        return this.session;
    }
    public void setSession(long session){
        this.session = session;
    }
}
