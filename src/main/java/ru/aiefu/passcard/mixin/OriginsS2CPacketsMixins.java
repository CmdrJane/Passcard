package ru.aiefu.passcard.mixin;

import io.github.apace100.origins.networking.ModPacketsS2C;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(ModPacketsS2C.class)
public class OriginsS2CPacketsMixins {
    @Inject(method = "openOriginScreen", at =@At("HEAD"), remap = false, cancellable = true)
    private static void preventOpeningIfNotLogged(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender, CallbackInfo ci){
        ci.cancel();
        return;
    }
}
