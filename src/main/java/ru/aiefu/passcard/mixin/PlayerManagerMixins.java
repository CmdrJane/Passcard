package ru.aiefu.passcard.mixin;

import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.hybrid.EciesAeadHkdfPrivateKeyManager;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.aiefu.passcard.IOManager;
import ru.aiefu.passcard.IPlayerPass;
import ru.aiefu.passcard.PacketsIDs;
import ru.aiefu.passcard.Passcard;

import java.security.GeneralSecurityException;

@Mixin(PlayerManager.class)
public class PlayerManagerMixins {
    @Inject(method = "onPlayerConnect", at =@At("TAIL"))
    private void openAuthScreen(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) throws GeneralSecurityException {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        IPlayerPass playerPass = (IPlayerPass)player;
        playerPass.setPlayerDB(IOManager.readPlayerData(player));
        KeysetHandle privateKeysetHandle = KeysetHandle.generateNew(EciesAeadHkdfPrivateKeyManager.eciesP256HkdfHmacSha256Aes128CtrHmacSha256Template());
        playerPass.setPrivateKey(privateKeysetHandle);
        KeysetHandle publicKeysetHandle = privateKeysetHandle.getPublicKeysetHandle();
        byte[] byteArray = CleartextKeysetHandle.getKeyset(publicKeysetHandle).toByteArray();
        boolean bl = playerPass.getPlayerDB().getPassword() == null;
        buffer.writeByteArray(byteArray);
        buffer.writeBoolean(bl);
        buffer.writeInt(Passcard.config_instance.getLoginTimeout());
        ServerPlayNetworking.send(player, PacketsIDs.OPEN_AUTH_SCREEN, buffer);
    }
    @Inject(method = "savePlayerData",  at = @At("TAIL"))
    private void onPlayerDataSaveToStorage(ServerPlayerEntity player, CallbackInfo ci){
        IOManager.writePlayerData(player);
    }
}
