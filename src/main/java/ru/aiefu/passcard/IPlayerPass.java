package ru.aiefu.passcard;

import com.google.crypto.tink.KeysetHandle;

public interface IPlayerPass {
    void setPrivateKey(KeysetHandle handler);
    KeysetHandle getPrivateKey();
    PlayerDB getPlayerDB();
    void setPlayerDB(PlayerDB db);
    boolean getAuthState();
    void setAuthState(boolean bl);
    void setRetries(int r);
    int getRetries();
    void resetTimeoutTimer();
    void setJoinInvTicks(int ticks);
}
