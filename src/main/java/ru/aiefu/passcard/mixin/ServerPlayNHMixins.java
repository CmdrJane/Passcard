package ru.aiefu.passcard.mixin;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.aiefu.passcard.IPlayerPass;
import ru.aiefu.passcard.PacketsIDs;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNHMixins {
    @Shadow public ServerPlayerEntity player;

    @Inject(method="onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V", at =@At("HEAD"), cancellable = true)
    private void preventMovingIfNotAuth(PlayerMoveC2SPacket packet, CallbackInfo ci){
        if(!((IPlayerPass)this.player).getAuthState()){
            player.updatePosition(player.getX(), player.getY(), player.getZ());
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeDouble(player.getX());
            buf.writeDouble(player.getY());
            buf.writeDouble(player.getZ());
            ServerPlayNetworking.send(player, PacketsIDs.SYNC_PLAYER_POS, buf);
            ci.cancel();
        }
    }
    @Inject(method = "onCreativeInventoryAction(Lnet/minecraft/network/packet/c2s/play/CreativeInventoryActionC2SPacket;)V", at =@At("HEAD"), cancellable = true)
    private void creativeAuthCheck(CreativeInventoryActionC2SPacket packet, CallbackInfo ci){
        if(!((IPlayerPass)this.player).getAuthState()){
            ci.cancel();
        }
    }
    @Inject(method = "onPlayerAction(Lnet/minecraft/network/packet/c2s/play/PlayerActionC2SPacket;)V", at =@At("HEAD"), cancellable = true)
    private void checkAuthOnPlayerAct(PlayerActionC2SPacket packet, CallbackInfo ci){
        if(!((IPlayerPass)this.player).getAuthState()){
            ci.cancel();
        }
    }
    @Inject(method = "onPlayerInteractBlock(Lnet/minecraft/network/packet/c2s/play/PlayerInteractBlockC2SPacket;)V", at =@At("HEAD"), cancellable = true)
    private void checkAuthOnBlockInteract(PlayerInteractBlockC2SPacket packet, CallbackInfo ci){
        if(!((IPlayerPass)this.player).getAuthState()){
            ci.cancel();
        }
    }
    @Inject(method = "onPlayerInteractItem(Lnet/minecraft/network/packet/c2s/play/PlayerInteractItemC2SPacket;)V", at =@At("HEAD"), cancellable = true)
    private void checkAuthOnItemInteract(PlayerInteractItemC2SPacket packet, CallbackInfo ci){
        if(!((IPlayerPass)this.player).getAuthState()){
            ci.cancel();
        }
    }
    @Inject(method = "onPlayerInteractEntity(Lnet/minecraft/network/packet/c2s/play/PlayerInteractEntityC2SPacket;)V", at =@At("HEAD"), cancellable = true)
    private void checkAuthOnEntityInteract(PlayerInteractEntityC2SPacket packet, CallbackInfo ci){
        if(!((IPlayerPass)this.player).getAuthState()){
            ci.cancel();
        }
    }
    @Inject(method = "onClientCommand(Lnet/minecraft/network/packet/c2s/play/ClientCommandC2SPacket;)V", at =@At("HEAD"), cancellable = true)
    private void checkAuthOnClientCommand(ClientCommandC2SPacket packet, CallbackInfo ci){
        if(!((IPlayerPass)this.player).getAuthState()){
            ci.cancel();
        }
    }
    @Inject(method = "onGameMessage(Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;)V", at =@At("HEAD"), cancellable = true)
    private void checkAuthOnGameMessage(ChatMessageC2SPacket packet, CallbackInfo ci){
        if(!((IPlayerPass)this.player).getAuthState()){
            ci.cancel();
        }
    }
    @Inject(method = "disconnect", at =@At("HEAD"))
    private void resetPrivateSecKey(Text reason, CallbackInfo ci){
        ((IPlayerPass)this.player).setPrivateKey(null);
    }
}
