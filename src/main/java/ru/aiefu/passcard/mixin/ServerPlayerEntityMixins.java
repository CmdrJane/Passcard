package ru.aiefu.passcard.mixin;

import com.google.crypto.tink.KeysetHandle;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.aiefu.passcard.IPlayerPass;
import ru.aiefu.passcard.Passcard;
import ru.aiefu.passcard.PlayerDB;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixins extends PlayerEntity implements IPlayerPass {
    public ServerPlayerEntityMixins(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Shadow public ServerPlayNetworkHandler networkHandler;
    KeysetHandle privateKey = null;
    private PlayerDB playerDB;
    boolean authState = false;
    private int retries = 0;
    private int timeoutTimer = 0;

    @Inject(method = "playerTick", at =@At("HEAD"), cancellable = true)
    public void timeOutTimer(CallbackInfo ci){
        if(!this.authState){
            ++timeoutTimer;
            if(timeoutTimer > Passcard.config_instance.getLoginTimeout()){
                timeoutTimer = 0;
                this.privateKey = null;
                this.getServer().execute(() -> this.networkHandler.disconnect(new LiteralText("Authorization Timeout")));
            }
            ci.cancel();
        }
        else timeoutTimer = 0;
    }

    public void setPrivateKey(KeysetHandle handler){
     this.privateKey = handler;
    }
    public KeysetHandle getPrivateKey(){
        return this.privateKey;
    }

    @Override
    public PlayerDB getPlayerDB() {
        return this.playerDB;
    }

    @Override
    public void setPlayerDB(PlayerDB db) {
        this.playerDB = db;
    }


    @Override
    public boolean getAuthState() {
        return this.authState;
    }

    @Override
    public void setAuthState(boolean bl) {
        this.authState = bl;
    }

    @Override
    public void setRetries(int r) {
        this.retries = r;
    }

    @Override
    public int getRetries() {
        return this.retries;
    }

    @Override
    public void resetTimeoutTimer() {
        this.timeoutTimer = 0;
    }
}
