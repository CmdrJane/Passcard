package ru.aiefu.passcard;

import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.proto.Keyset;
import com.google.protobuf.InvalidProtocolBufferException;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.LiteralText;
import ru.aiefu.passcard.compat.OriginsCompat;
import ru.aiefu.passcard.compat.OriginsCompatClient;
import ru.aiefu.passcard.gui.LoginScreen;

import java.security.GeneralSecurityException;

public class PasscardClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(PacketsIDs.OPEN_AUTH_SCREEN, (client, handler, buf, responseSender) -> {
            byte[] bytes = buf.readByteArray();
            boolean bl = buf.readBoolean();
            int timeout = buf.readInt();
            boolean wrongPass = false;
            if(buf.readableBytes() > 0){
                wrongPass = buf.readBoolean();
            }
            try {
                KeysetHandle publicKey = CleartextKeysetHandle.fromKeyset(Keyset.parseFrom(bytes));
                boolean finalWrongPass = wrongPass;
                client.execute(() -> client.openScreen(new LoginScreen(new LiteralText(""), publicKey, bl, timeout, finalWrongPass)));
            } catch (GeneralSecurityException | InvalidProtocolBufferException e) {
                e.printStackTrace();
                client.world.disconnect();
                client.disconnect();
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(PacketsIDs.CLOSE_ALL_SCREENS, (client, handler, buf, responseSender) -> {
           client.execute(() -> client.openScreen(null));
        });
        ClientPlayNetworking.registerGlobalReceiver(PacketsIDs.SYNC_PLAYER_POS, (client, handler, buf, responseSender) -> {
            try {
                client.player.updatePosition(buf.readDouble(), buf.readDouble(), buf.readDouble());
            } catch (Exception e){
                e.printStackTrace();
            }
        });
        if(FabricLoader.getInstance().isModLoaded("origins")){
            ClientPlayConnectionEvents.INIT.register((handler, client) -> {
                OriginsCompatClient.registerOriginsReceiver();
            });
        }
    }
}
