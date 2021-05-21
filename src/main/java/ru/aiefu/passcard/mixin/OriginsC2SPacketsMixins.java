package ru.aiefu.passcard.mixin;

import io.github.apace100.origins.networking.ModPacketsC2S;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.aiefu.passcard.IPlayerPass;

@Pseudo
@Mixin(ModPacketsC2S.class)
public class OriginsC2SPacketsMixins {
    @Inject(method = "useActivePowers", at =@At("HEAD"), remap = false, cancellable = true)
    private static void preventActiveUseIfNotLoggedIn(MinecraftServer minecraftServer, ServerPlayerEntity playerEntity, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender, CallbackInfo ci){
        if( !((IPlayerPass)playerEntity).getAuthState() ){
            ci.cancel();
            return;
        }
    }
}
