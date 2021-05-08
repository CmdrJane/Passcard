package ru.aiefu.passcard.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.aiefu.passcard.IPlayerPass;

@Mixin(Slot.class)
public class SlotMixins {
    @Inject(method = "canTakeItems(Lnet/minecraft/entity/player/PlayerEntity;)Z", at =@At(value = "HEAD"), cancellable = true)
    private void onSlotActionCheck(PlayerEntity playerEntity, CallbackInfoReturnable<Boolean> cir){
        if(playerEntity instanceof ServerPlayerEntity && !((IPlayerPass)playerEntity).getAuthState()){
            cir.setReturnValue(false);
            return;
        }
    }
}
