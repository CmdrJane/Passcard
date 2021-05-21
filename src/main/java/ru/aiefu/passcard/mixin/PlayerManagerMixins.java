package ru.aiefu.passcard.mixin;

import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.hybrid.EciesAeadHkdfPrivateKeyManager;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.aiefu.passcard.*;
import ru.aiefu.passcard.compat.OriginsCompat;

import java.net.SocketAddress;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixins {
    @Shadow @Nullable public abstract ServerPlayerEntity getPlayer(String name);

    @Inject(method = "onPlayerConnect", at =@At("TAIL"))
    private void openAuthScreen(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) throws GeneralSecurityException {
        IPlayerPass playerPass = (IPlayerPass)player;
        playerPass.setJoinInvTicks(100);
        PlayerDB playerDB = IOManager.readPlayerData(player);
        if(playerDB == null){
            playerDB = IOManager.readPlayerReserveData(player);
        }
        if(playerDB == null){
            connection.disconnect(new LiteralText("Your profile data is corrupted, contact admin for help"));
            return;
        }
        playerPass.setPlayerDB(playerDB);
        IOManager.writePlayerReserveDataIfNotExist(player);
        PlayerDB db = playerPass.getPlayerDB();
        if(System.currentTimeMillis() < db.getSession() && db.getLastIp() != null && db.getLastIp().equals(player.getIp())){
            playerPass.setAuthState(true);
            if(FabricLoader.getInstance().isModLoaded("origins")){
                OriginsCompat.openOriginsScreen(player);
            }
        }
        else {
            PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
            KeysetHandle privateKeysetHandle = KeysetHandle.generateNew(EciesAeadHkdfPrivateKeyManager.eciesP256HkdfHmacSha256Aes128CtrHmacSha256Template());
            playerPass.setPrivateKey(privateKeysetHandle);
            KeysetHandle publicKeysetHandle = privateKeysetHandle.getPublicKeysetHandle();
            byte[] byteArray = CleartextKeysetHandle.getKeyset(publicKeysetHandle).toByteArray();
            boolean bl = db.getPassword() == null;
            buffer.writeByteArray(byteArray);
            buffer.writeBoolean(bl);
            buffer.writeInt(Passcard.config_instance.getLoginTimeout());
            ServerPlayNetworking.send(player, PacketsIDs.OPEN_AUTH_SCREEN, buffer);
        }
    }
    @Inject(method = "savePlayerData",  at = @At("TAIL"))
    private void onPlayerDataSaveToStorage(ServerPlayerEntity player, CallbackInfo ci){
        IOManager.writePlayerData(player);
    }
    @Inject(method = "checkCanJoin", at =@At("HEAD"), cancellable = true)
    private void checkIfPlayerAlreadyOnline(SocketAddress address, GameProfile profile, CallbackInfoReturnable<Text> cir){
        String newPlayer = profile.getName();
        PlayerEntity player = getPlayer(newPlayer);
        if(player != null){
            cir.setReturnValue(new LiteralText("Another player with that username are already online"));
            return;
        }
    }
    @Inject(method = "remove", at =@At("HEAD"))
    private void setPasscardSessionTime(ServerPlayerEntity player, CallbackInfo ci){
        if(((IPlayerPass)player).getAuthState()) {
            ((IPlayerPass) player).getPlayerDB().setSession(LocalDateTime.now().plusMinutes(15).toEpochSecond(OffsetDateTime.now().getOffset()) * 1000);
        }
    }
}
