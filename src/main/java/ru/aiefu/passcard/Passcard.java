package ru.aiefu.passcard;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.HybridDecrypt;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.hybrid.EciesAeadHkdfPrivateKeyManager;
import com.google.crypto.tink.hybrid.HybridConfig;
import com.google.crypto.tink.hybrid.HybridDecryptWrapper;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import ru.aiefu.passcard.compat.OriginsCompat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Date;

public class Passcard implements ModInitializer {

	public static final String MOD_ID = "passcard";

	public static ConfigInstance config_instance;

	@Override
	public void onInitialize() {
		try {
			HybridConfig.register();
			HybridDecryptWrapper.register();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
		try {
			craftPaths();
		} catch (IOException e) {
			e.printStackTrace();
		}
		registerServerSidePackets();
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			IPlayerPass origPlayer = (IPlayerPass) oldPlayer;
			IPlayerPass player = (IPlayerPass) newPlayer;
			player.setPlayerDB(origPlayer.getPlayerDB());
			player.setAuthState(origPlayer.getAuthState());
			if(origPlayer.getPrivateKey() != null){
				player.setPrivateKey(origPlayer.getPrivateKey());
			}
		});
	}

	public void registerServerSidePackets(){
		ServerPlayNetworking.registerGlobalReceiver(PacketsIDs.SEND_AUTH_REQUEST, (server, player, handler, buf, responseSender) -> {
			byte[] bytes = buf.readByteArray();
			if (bytes != null){
				server.execute(() -> {
					try {
						IPlayerPass playerPass = (IPlayerPass) player;
						HybridDecrypt hybridDecrypt = playerPass.getPrivateKey().getPrimitive(HybridDecrypt.class);
						byte[] byteArr = hybridDecrypt.decrypt(bytes, null);
						String pass = new String(byteArr, StandardCharsets.UTF_8);
						pass = StringUtils.truncate(pass, 16);
						PlayerDB playerDB = playerPass.getPlayerDB();
						if(playerDB.getPassword() != null && BCrypt.verifyer().verify(pass.toCharArray(), playerDB.getPassword().toCharArray()).verified){
							playerPass.setAuthState(true);
							playerDB.setLastIp(player.getIp());
							playerPass.setPrivateKey(null);
							if(FabricLoader.getInstance().isModLoaded("origins")){
								OriginsCompat.openOriginsScreen(player);
							} else ServerPlayNetworking.send(player, PacketsIDs.CLOSE_ALL_SCREENS, new PacketByteBuf(Unpooled.buffer()));
						} else if(playerDB.getPassword() == null) {
							playerDB.setPassword(BCrypt.withDefaults().hashToString(4, pass.toCharArray()));
							playerDB.setLastIp(player.getIp());
							playerPass.setAuthState(true);
							IOManager.writePlayerData(player);
							playerPass.setPrivateKey(null);
							if(FabricLoader.getInstance().isModLoaded("origins")){
								OriginsCompat.openOriginsScreen(player);
							} else ServerPlayNetworking.send(player, PacketsIDs.CLOSE_ALL_SCREENS, new PacketByteBuf(Unpooled.buffer()));
						} else {
							playerPass.resetTimeoutTimer();
							playerPass.setRetries(playerPass.getRetries() + 1);
							if(playerPass.getRetries() > 5){
								playerPass.setRetries(0);
								player.networkHandler.disconnect(new LiteralText("Too many tries"));
							} else {
								PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
								KeysetHandle privateKeysetHandle = KeysetHandle.generateNew(EciesAeadHkdfPrivateKeyManager.eciesP256HkdfHmacSha256Aes128CtrHmacSha256Template());
								playerPass.setPrivateKey(privateKeysetHandle);
								KeysetHandle publicKeysetHandle = privateKeysetHandle.getPublicKeysetHandle();
								byte[] byteArray = CleartextKeysetHandle.getKeyset(publicKeysetHandle).toByteArray();
								boolean bl = playerPass.getPlayerDB().getPassword() == null;
								buffer.writeByteArray(byteArray);
								buffer.writeBoolean(bl);
								buffer.writeInt(Passcard.config_instance.getLoginTimeout());
								buffer.writeBoolean(true);
								ServerPlayNetworking.send(player, PacketsIDs.OPEN_AUTH_SCREEN, buffer);
							}
						}

					} catch (GeneralSecurityException e) {
						e.printStackTrace();
						player.networkHandler.disconnect(new LiteralText("Security Exception, try re-login"));
					}
				});
			} else player.networkHandler.disconnect(new LiteralText("Something went wrong, try re-login"));
		});
	}
	public void craftPaths() throws IOException {
		if(!Files.isDirectory(Paths.get("./config"))){
			Files.createDirectory(Paths.get("./config"));
		}
		if(!Files.isDirectory(Paths.get("./config/Passcard"))){
			Files.createDirectory(Paths.get("./config/Passcard"));
		}
		if(!Files.isDirectory(Paths.get("./config/Passcard/player-data"))){
			Files.createDirectory(Paths.get("./config/Passcard/player-data"));
		}
		if(!Files.exists(Paths.get("./config/Passcard/config.json"))){
			IOManager.genCfg();
		}
		IOManager.readCfg();
	}
	public static String getUUIDIgnoreCase(ServerPlayerEntity player){
		return PlayerEntity.getOfflinePlayerUuid(player.getGameProfile().getName().toLowerCase()).toString();
	}
}
