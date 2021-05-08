package ru.aiefu.passcard;

public class ConfigInstance {
    private int loginTimeout = 90;
    public int getLoginTimeout(){
        return this.loginTimeout * 20;
    }
}
